package com.autio.android_app.ui.stories.view_model

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autio.android_app.R
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.extensions.currentPlayBackPosition
import com.autio.android_app.extensions.isPlayEnabled
import com.autio.android_app.extensions.isPlaying
import com.autio.android_app.extensions.isPrepared
import com.autio.android_app.player.EMPTY_PLAYBACK_STATE
import com.autio.android_app.player.MediaItemData
import com.autio.android_app.player.PlayerServiceConnection
import com.autio.android_app.ui.di.coroutines.IoDispatcher
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.ui.stories.view_states.BottomNavigationViewState
import com.autio.android_app.util.POSITION_UPDATE_INTERVAL_MILLIS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * [ViewModel] that watches a [PlayerServiceConnection] to become connected
 * and provides the root/initial media ID of the underlying MediaBrowserCompat
 */


@HiltViewModel
class BottomNavigationViewModel @Inject constructor(
    private val app: Application,
    private val playerServiceConnection: PlayerServiceConnection,
    private val autioRepository: AutioRepository,
    private val prefRepository: PrefRepository,
    @IoDispatcher
    private val coroutineDispatcher: CoroutineDispatcher
) : AndroidViewModel(app) {

    val isPayWallVisible = ObservableBoolean(false)


    private val _bottomNavigationViewState = MutableLiveData<BottomNavigationViewState>()
    val bottomNavigationViewState: LiveData<BottomNavigationViewState> = _bottomNavigationViewState

    val initialRemainingStories = prefRepository.remainingStories
    val remainingStoriesLiveData = prefRepository.remainingStoriesLiveData

    private val storiesJob = SupervisorJob()
    fun setPayWallVisible(isVisible: Boolean) {
        isPayWallVisible.set(isVisible)
    }

    private fun setViewState(newState: BottomNavigationViewState) {
        _bottomNavigationViewState.postValue(newState)
    }

    fun onCreate() {
        playerServiceConnection.apply {
            playbackState.observeForever(playbackStateObserver)
            nowPlaying.observeForever(mediaMetadataObserver)
            checkPlaybackPosition()
        }
    }

    private var postedPlay = false
    private var playbackState = EMPTY_PLAYBACK_STATE

    val playingStory = MutableLiveData<Story?>()
    private val mediaPosition = MutableLiveData<Int>().apply {
        postValue(0)
    }
    val mediaButtonRes = MutableLiveData<Int>().apply {
        postValue(R.drawable.ic_album)
    }

    private var updatePosition = true
    private val handler = Handler(Looper.getMainLooper())

    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        playbackState = it ?: EMPTY_PLAYBACK_STATE
        val currentStory = playerServiceConnection.nowPlaying.value
        updateMediaState(playbackState, currentStory)
    }

    private val mediaMetadataObserver = Observer<Story?> {
        updateMediaState(playbackState, it)
    }


    private fun checkPlaybackPosition(): Boolean = handler.postDelayed(
        {
            val currPosition = playbackState.currentPlayBackPosition.toInt()
            if (mediaPosition.value != currPosition) {
                if (currPosition >= 30000 && !postedPlay) {
                    postPlay()
                }
            }
            mediaPosition.postValue(currPosition)
            if (updatePosition) checkPlaybackPosition()
        }, POSITION_UPDATE_INTERVAL_MILLIS
    )

    fun postPlay() {
        val storyToPost = playingStory.value
        postedPlay = true
        viewModelScope.launch(coroutineDispatcher) {
            if (storyToPost != null) {
                val downloadedStory =
                    autioRepository.getDownloadedStoryById(playingStory.value!!.id)
                val connectivityManager =
                    app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfo = connectivityManager.activeNetwork
                val network = getNetworkStatus(networkInfo, connectivityManager)
                autioRepository.postStoryPlayed(
                    prefRepository.userId,
                    prefRepository.userApiToken,
                    storyToPost,
                    wasPresent = true,
                    autoPlay = true,
                    downloadedStory != null,
                    network
                )
                setViewState(BottomNavigationViewState.FetchedStoryToPlay(storyToPost))
            } else setViewState(BottomNavigationViewState.FetchedStoryToPlayFailed)
        }
    }


    private fun getNetworkStatus(
        networkInfo: Network?,
        connectivityManager: ConnectivityManager
    ) = if (networkInfo == null) "disconnected" else {
        val actNw = connectivityManager.getNetworkCapabilities(
            networkInfo
        )
        when {
            actNw?.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            ) == true -> "wifi"
            actNw?.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            ) == true -> "cellular"
            else -> "disconnected"
        }
    }

    override fun onCleared() {
        super.onCleared()

        storiesJob.cancel()

        // Remove the permanent observers from the MusicServiceConnection.
        playerServiceConnection.playbackState.removeObserver(
            playbackStateObserver
        )
        playerServiceConnection.nowPlaying.removeObserver(
            mediaMetadataObserver
        )

        // Stop updating the position
        updatePosition = false
    }

    private fun updateMediaState(
        playbackState: PlaybackStateCompat, story: Story?
    ) {
        // Only update media item once we have duration available
        if (story?.duration != 0 && story?.id != null) {
            this.playingStory.postValue(
                story
            )
        }

        // Update the media button resource ID
        mediaButtonRes.postValue(
            when (playbackState.isPlaying) {
                true -> R.drawable.ic_player_pause
                else -> R.drawable.ic_player_play
            }
        )
    }

// Player code

    /**
     * This method takes a [MediaItemData] and does one of the following:
     * - If the item is *not* the active item, the play it directly
     * - If the item *is* the active item, check whether "pause" is a permitted command. If it is,
     *   then pause playback, otherwise send "play" to resume playback.
     */
    private fun playMedia(
        mediaItem: MediaItemData, pauseAllowed: Boolean = true
    ) {
        val nowPlaying = playerServiceConnection.nowPlaying.value
        val transportControls = playerServiceConnection.transportControls

        val isPrepared = playerServiceConnection.playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.mediaId == nowPlaying?.id) {
            playerServiceConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (pauseAllowed) transportControls.pause() else Unit
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Log.w(
                            "BottomNavigationViewModel",
                            "Playable item clicked but neither play nor pause" + "are enabled! (mediaId=${mediaItem.mediaId})"
                        )
                    }
                }
            }
        } else {
            postedPlay = false
            transportControls.playFromMediaId(mediaItem.mediaId.toString(), null)
            //TODO(Use ourRepo)
            // FirebaseStoryRepository.addStoryToUserHistory(
            //     prefRepository.firebaseKey, mediaItem.mediaId,
            //     onSuccessListener = { timestamp ->
            //         viewModelScope.launch(Dispatchers.IO) {
            //             autioRepository.addStoryToHistory(History(mediaItem.mediaId, timestamp))
        }
    }


    fun playMediaId(mediaId: Int) {
        val nowPlaying = playerServiceConnection.nowPlaying.value
        val transportControls = playerServiceConnection.transportControls

        val isPrepared = playerServiceConnection.playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaId == nowPlaying?.id) {
            playerServiceConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> transportControls.pause()
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Log.w(
                            "BottomNavigationViewModel",
                            "Playable item clicked but neither play nor pause are enabled! (mediaId=$mediaId)"
                        )
                    }
                }
            }
        } else {
            postedPlay = false
            transportControls.playFromMediaId(
                mediaId.toString(), null
            )
            //TODO(Use ourRepo)
            //FirebaseStoryRepository
            //    .addStoryToUserHistory(
            //        prefRepository.firebaseKey,
            //        mediaId,
            //        onSuccessListener = { timestamp ->
            //            viewModelScope.launch(coroutineDispatcher) {
            //                autioRepository.addStoryToHistory(History(mediaId, timestamp))
            //            }
            //        })
        }
    }

    fun rewindFifteenSeconds() {
        val transportControls = playerServiceConnection.transportControls
        mediaPosition.value?.let {
            transportControls.seekTo(
                0L.coerceAtLeast(
                    it.toLong() - 15000
                )
            )
        }
    }

    fun skipToNextStory() {
        val transportControls = playerServiceConnection.transportControls
        transportControls.skipToNext()
    }

    fun setPlaybackPosition(progress: Int) {
        val transportControls = playerServiceConnection.transportControls
        transportControls.seekTo(progress * 1000L)
    }

    // TODO: Add items to queue
    fun addMediaToQueue() {
        val transportControls = playerServiceConnection.transportControls
    }

// Backend calls

    private suspend fun getInitialData() {
        viewModelScope.launch {
            val storiesFetch = async(
                start = CoroutineStart.LAZY
            ) { getRemoteStories() }
            val favoritesFetch = async(
                start = CoroutineStart.LAZY
            ) { setLikesToStories() }
            val bookmarksFetch = async(
                start = CoroutineStart.LAZY
            ) { setBookmarksToStories() }
            val historyFetch = async(
                start = CoroutineStart.LAZY
            ) { setListenedAtToStories() }
            storiesFetch.await()
            favoritesFetch.await()
            bookmarksFetch.await()
            historyFetch.await()
            postPlay()
        }
    }


    private suspend fun getRemoteStories() {
        viewModelScope.launch(coroutineDispatcher) {
            kotlin.runCatching {
                autioRepository.getLastModifiedStory()
            }.onSuccess {
                val result = it.getOrNull()
                result?.let {
                    val date = result.modifiedDate ?: 63808881662
                    //TODO(double check this logic later, my quick fix for updating new stories(not in mappoint datatable))

                    val stories = autioRepository.getStoriesAfterModifiedDate(date.toInt())
                    //TODO(COULD THIS BE CAUSING ON MIGRATION ROOM ERROR)
                    autioRepository.addStories(stories)
                }
            }.onFailure { }
        }
    }

    private suspend fun setBookmarksToStories() {
        withContext(coroutineDispatcher) {

            val userBookmarkedStories = autioRepository.getUserBookmarks(
                prefRepository.userId
            )
            //TODO(CHECK THIS)
            autioRepository.setBookmarksDataToLocalStories(userBookmarkedStories.map { it })
        }
    }

    private suspend fun setLikesToStories() {
        withContext(coroutineDispatcher) {
            val userFavoriteStories = autioRepository.getUserFavoriteStories(
                prefRepository.userId
            )
            //TODO(Finish this)
            // autioRepository.setLikesDataToLocalStories(userFavoriteStories.filter { it.isGiven == true }
            //   .map { it.storyId }
            //)
        }
    }

    private suspend fun setListenedAtToStories() {
        withContext(coroutineDispatcher) {
            val userHistory = autioRepository.getUserStoriesHistory(
                prefRepository.userId
            )
            //TODO(Finish this)
            //  autioRepository.setListenedAtToLocalStories(userHistory.map{it.toEntity()})
        }
    }

    fun clearRoomCache() {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.deleteCachedData()
        }
    }

//    fun debugConsumePremium() {
//        applicationRepository.debugConsumeSingleTrip()
//    }

//    val messages: LiveData<Int>
//        get() = applicationRepository.messages.asLiveData()

//    val billingLifecycleObserver: LifecycleObserver
//        get() = applicationRepository.billingLifecycleObserver

}

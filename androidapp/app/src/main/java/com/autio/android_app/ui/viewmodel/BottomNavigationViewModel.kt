package com.autio.android_app.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.autio.android_app.R
import com.autio.android_app.data.database.repository.StoryRepository
import com.autio.android_app.data.model.history.History
import com.autio.android_app.data.model.plays.PlaysDto
import com.autio.android_app.data.model.story.Story
import com.autio.android_app.data.repository.ApiService
import com.autio.android_app.data.repository.legacy.FirebaseStoryRepository
import com.autio.android_app.data.repository.legacy.PrefRepository
import com.autio.android_app.extensions.*
import com.autio.android_app.player.EMPTY_PLAYBACK_STATE
import com.autio.android_app.player.MediaItemData
import com.autio.android_app.player.PlayerServiceConnection
import kotlinx.coroutines.*
import java.util.*

/**
 * [ViewModel] that watches a [PlayerServiceConnection] to become connected
 * and provides the root/initial media ID of the underlying MediaBrowserCompat
 */
class BottomNavigationViewModel(
    private val app: Application,
    playerServiceConnection: PlayerServiceConnection,
    private val storyRepository: StoryRepository,
//    private val applicationRepository: CoreApplicationRepository
) : AndroidViewModel(
    app
) {
    private val prefRepository by lazy {
        PrefRepository(
            app
        )
    }

    val initialRemainingStories =
        prefRepository.remainingStories
    val remainingStoriesLiveData =
        prefRepository.remainingStoriesLiveData

    private val storiesJob =
        SupervisorJob()

    fun onCreate() {
//        viewModelScope.launch {
//            getInitialData()
//        }
    }

    private var postedPlay =
        false
    private var playbackState =
        EMPTY_PLAYBACK_STATE
    val playingStory =
        MutableLiveData<Story?>()
    private val mediaPosition =
        MutableLiveData<Int>().apply {
            postValue(
                0
            )
        }
    val mediaButtonRes =
        MutableLiveData<Int>().apply {
            postValue(
                R.drawable.ic_album
            )
        }

    private var updatePosition =
        true
    private val handler =
        Handler(
            Looper.getMainLooper()
        )

    private val playbackStateObserver =
        Observer<PlaybackStateCompat> {
            playbackState =
                it
                    ?: EMPTY_PLAYBACK_STATE
            val currentStory =
                playerServiceConnection.nowPlaying.value
            updateMediaState(
                playbackState,
                currentStory
            )
        }

    private val mediaMetadataObserver =
        Observer<Story?> {
            updateMediaState(
                playbackState,
                it
            )
        }

    private val playerServiceConnection =
        playerServiceConnection.also {
            it.playbackState.observeForever(
                playbackStateObserver
            )
            it.nowPlaying.observeForever(
                mediaMetadataObserver
            )
            checkPlaybackPosition()
        }

    private fun checkPlaybackPosition(): Boolean =
        handler.postDelayed(
            {
                val currPosition =
                    playbackState.currentPlayBackPosition.toInt()
                if (mediaPosition.value != currPosition)
                    if (currPosition >= 30000 && !postedPlay) {
                        postPlay()
                    }
                mediaPosition.postValue(
                    currPosition
                )
                if (updatePosition)
                    checkPlaybackPosition()
            },
            POSITION_UPDATE_INTERVAL_MILLIS
        )

    private fun postPlay() {
        val storyToPost =
            playingStory.value
        postedPlay =
            true
        if (storyToPost != null) {
            viewModelScope.launch(
                Dispatchers.IO
            ) {
                val downloadedStory =
                    storyRepository.getDownloadedStoryById(
                        playingStory.value!!.id
                    )
                val connectivityManager =
                    app.getSystemService(
                        Context.CONNECTIVITY_SERVICE
                    ) as ConnectivityManager
                val networkInfo =
                    connectivityManager.activeNetwork
                val network =
                    if (networkInfo == null) "disconnected" else {
                        val actNw =
                            connectivityManager.getNetworkCapabilities(
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
                ApiService.postStoryPlayed(
                    prefRepository.userId,
                    prefRepository.userApiToken,
                    PlaysDto(
                        storyToPost.id,
                        wasPresent = true,
                        autoPlay = true,
                        downloadedStory != null,
                        network,
                        storyToPost.lat,
                        storyToPost.lon
                    )
                ) {
                    if (it != null) {
                        viewModelScope.launch(
                            Dispatchers.IO
                        ) {
                            storyRepository.markStoryAsListenedAtLeast30Secs(
                                storyToPost.id
                            )
                        }
                        prefRepository.remainingStories =
                            it.playsRemaining
                    }
                }
            }
        } else {
            ApiService.postStoryPlayed(
                prefRepository.userId,
                prefRepository.userApiToken,
                PlaysDto()
            ) {
                if (it != null) {
                    prefRepository.remainingStories =
                        it.playsRemaining
                }
            }
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
        updatePosition =
            false
    }

    private fun updateMediaState(
        playbackState: PlaybackStateCompat,
        story: Story?
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
        mediaItem: MediaItemData,
        pauseAllowed: Boolean = true
    ) {
        val nowPlaying =
            playerServiceConnection.nowPlaying.value
        val transportControls =
            playerServiceConnection.transportControls

        val isPrepared =
            playerServiceConnection.playbackState.value?.isPrepared
                ?: false
        if (isPrepared && mediaItem.mediaId == nowPlaying?.id) {
            playerServiceConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying ->
                        if (pauseAllowed) transportControls.pause() else Unit
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Log.w(
                            "BottomNavigationViewModel",
                            "Playable item clicked but neither play nor pause" +
                                    "are enabled! (mediaId=${mediaItem.mediaId})"
                        )
                    }
                }
            }
        } else {
            postedPlay =
                false
            transportControls.playFromMediaId(
                mediaItem.mediaId,
                null
            )
            FirebaseStoryRepository.addStoryToUserHistory(
                prefRepository.firebaseKey,
                mediaItem.mediaId,
                onSuccessListener = { timestamp ->
                    viewModelScope.launch(
                        Dispatchers.IO
                    ) {
                        storyRepository.addStoryToHistory(
                            History(
                                mediaItem.mediaId,
                                timestamp
                            )
                        )
                    }
                }
            )
        }
    }

    fun playMediaId(
        mediaId: String
    ) {
        val nowPlaying =
            playerServiceConnection.nowPlaying.value
        val transportControls =
            playerServiceConnection.transportControls

        val isPrepared =
            playerServiceConnection.playbackState.value?.isPrepared
                ?: false
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
            postedPlay =
                false
            transportControls.playFromMediaId(
                mediaId,
                null
            )
            FirebaseStoryRepository.addStoryToUserHistory(
                prefRepository.firebaseKey,
                mediaId,
                onSuccessListener = { timestamp ->
                    viewModelScope.launch(
                        Dispatchers.IO
                    ) {
                        storyRepository.addStoryToHistory(
                            History(
                                mediaId,
                                timestamp
                            )
                        )
                    }
                }
            )
        }
    }

    fun rewindFifteenSeconds() {
        val transportControls =
            playerServiceConnection.transportControls
        mediaPosition.value?.let {
            transportControls.seekTo(
                0L.coerceAtLeast(
                    it.toLong() - 15000
                )
            )
        }
    }

    fun skipToNextStory() {
        val transportControls =
            playerServiceConnection.transportControls
        transportControls.skipToNext()
    }

    fun setPlaybackPosition(
        progress: Int
    ) {
        val transportControls =
            playerServiceConnection.transportControls
        transportControls.seekTo(
            progress * 1000L
        )
    }

    // TODO: Add items to queue
    fun addMediaToQueue() {
        val transportControls =
            playerServiceConnection.transportControls
    }

    // Backend calls

    private suspend fun getInitialData() {
        viewModelScope.launch {
            val storiesFetch =
                async(
                    start = CoroutineStart.LAZY
                ) { getRemoteStories() }
            val favoritesFetch =
                async(
                    start = CoroutineStart.LAZY
                ) { setLikesToStories() }
            val bookmarksFetch =
                async(
                    start = CoroutineStart.LAZY
                ) { setBookmarksToStories() }
            val historyFetch =
                async(
                    start = CoroutineStart.LAZY
                ) { setListenedAtToStories() }
            storiesFetch.await()
            favoritesFetch.await()
            bookmarksFetch.await()
            historyFetch.await()
            postPlay()
        }
    }

    /**
     * Get remote stories stored in Firebase to save them into
     * room database
     * It starts by fetching inside room the last modified story date
     * so it uses this data to request to Firebase the stories updated
     * after it
     */
    private suspend fun getRemoteStories() {
        val lastFetchedStory =
            storyRepository.getLastModifiedStory()
        val date =
            lastFetchedStory?.modifiedDate
                ?: 63808881662
        withContext(
            Dispatchers.IO
        ) {
            // TODO: change Firebase code with commented code once endpoint is stable
            val stories =
                FirebaseStoryRepository.getStoriesAfterModifiedDate(
                    date.toInt()
                )
            storyRepository.addStories(
                stories
            )
//            val dateFormat =
//                SimpleDateFormat(
//                    "yyyy/MM/dd'T'HH:mm:ss",
//                    Locale.getDefault()
//                )
//            val formattedDate =
//                dateFormat.format(
//                    Date(
//                        (date.toLong() * 1000)
//                    )
//                )
//            apiService.getStoriesAfterDate(
//                prefRepository.userId,
//                prefRepository.userApiToken,
//                formattedDate
//            ) {
//                if (it != null) {
//                    storyRepository.addStories(
//                        it.toTypedArray()
//                    )
//                }
//            }
        }
    }

    private suspend fun setBookmarksToStories() {
        withContext(
            Dispatchers.IO
        ) {
            // TODO: change Firebase code with commented code once stable
            val userBookmarkedStories =
                FirebaseStoryRepository.getUserBookmarks(
                    prefRepository.firebaseKey
                )
            storyRepository.setBookmarksDataToLocalStories(
                userBookmarkedStories.map { it.storyId }
            )
//            apiService.getStoriesFromUserBookmarks(
//                prefRepository.userId,
//                prefRepository.userApiToken
//            ) { stories ->
//                if (stories != null) {
//                    storyRepository.setBookmarksDataToLocalStories(
//                        stories.map { it.id }
//                    )
//                }
//            }
        }
    }

    private suspend fun setLikesToStories() {
        withContext(
            Dispatchers.IO
        ) {
            val userFavoriteStories =
                FirebaseStoryRepository.getUserFavoriteStories(
                    prefRepository.firebaseKey
                )
            storyRepository.setLikesDataToLocalStories(
                userFavoriteStories.filter { it.isGiven == true }
                    .map { it.storyId }
            )
//            ApiService.likedStoriesByUser(
//                prefRepository.userId,
//                prefRepository.userApiToken
//            ) { stories ->
//                if (stories != null) {
//                    storyRepository.setLikesDataToLocalStories(
//                        stories.map { it.id }
//                    )
//                }
//            }
        }
    }

    private suspend fun setListenedAtToStories() {
        withContext(
            Dispatchers.IO
        ) {
            val userHistory =
                FirebaseStoryRepository.getUserStoriesHistory(
                    prefRepository.firebaseKey
                )
            storyRepository.setListenedAtToLocalStories(
                userHistory
            )
        }
    }

    fun clearRoomCache() {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            storyRepository.deleteCachedData()
        }
    }

//    fun debugConsumePremium() {
//        applicationRepository.debugConsumeSingleTrip()
//    }

//    val messages: LiveData<Int>
//        get() = applicationRepository.messages.asLiveData()

//    val billingLifecycleObserver: LifecycleObserver
//        get() = applicationRepository.billingLifecycleObserver

    class Factory(
        private val app: Application,
        private val playerServiceConnection: PlayerServiceConnection,
        private val storyRepository: StoryRepository,
//        private val applicationRepository: CoreApplicationRepository
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress(
            "unchecked_cast"
        )
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            return BottomNavigationViewModel(
                app,
                playerServiceConnection,
                storyRepository,
//                applicationRepository
            ) as T
        }
    }
}

private const val POSITION_UPDATE_INTERVAL_MILLIS =
    100L
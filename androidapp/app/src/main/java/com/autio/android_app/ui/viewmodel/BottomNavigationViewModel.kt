package com.autio.android_app.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.autio.android_app.R
import com.autio.android_app.data.model.story.Story
import com.autio.android_app.extensions.*
import com.autio.android_app.player.EMPTY_PLAYBACK_STATE
import com.autio.android_app.player.MediaItemData
import com.autio.android_app.player.PlayerServiceConnection
import com.autio.android_app.util.Event

/**
 * [ViewModel] that watches a [PlayerServiceConnection] to become connected
 * and provides the root/initial media ID of the underlying [MediaBrowserCompat]
 */
class BottomNavigationViewModel(
    playerServiceConnection: PlayerServiceConnection
) : ViewModel() {
    private val _storiesInScreen =
        MutableLiveData<Map<String, Story>>(
            emptyMap()
        )
    val storiesInScreen: LiveData<Map<String, Story>> =
        _storiesInScreen

    fun setStoryInView(
        story: Story
    ) {
        val previousStories =
            _storiesInScreen.value?.toMutableMap()
                ?: mutableMapOf()
        if (!previousStories.contains(
                story.id
            )
        ) {
            previousStories[story.id] =
                story
            _storiesInScreen.postValue(
                previousStories
            )
        }
    }

    fun removeStoryFromView(
        story: Story
    ) {
        val previousStories =
            _storiesInScreen.value?.toMutableMap()
                ?: mutableMapOf()
        if (previousStories.contains(
                story.id
            )
        ) {
            previousStories.remove(
                story.id
            )
            _storiesInScreen.postValue(
                previousStories
            )
        }
    }

    val rootMediaId: LiveData<String> =
        Transformations.map(
            playerServiceConnection.isConnected
        ) { isConnected ->
            if (isConnected) {
                playerServiceConnection.rootMediaId
            } else {
                null
            }
        }
    private var playbackState =
        EMPTY_PLAYBACK_STATE
    val currentStory =
        MutableLiveData<Story?>()
    val mediaPosition =
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
            updateState(
                playbackState,
                currentStory
            )
        }

    private val mediaMetadataObserver =
        Observer<Story?> {
            updateState(
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
                    mediaPosition.postValue(
                        currPosition
                    )
                if (updatePosition)
                    checkPlaybackPosition()
            },
            POSITION_UPDATE_INTERVAL_MILLIS
        )

    override fun onCleared() {
        super.onCleared()

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

    private fun updateState(
        playbackState: PlaybackStateCompat,
        story: Story?
    ) {
        // Only update media item once we have duration available
        if (story?.duration != 0 && story?.id != null) {
            this.currentStory.postValue(
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

    /**
     * [navigateToMediaItem] acts as an event, rather than state. [Observer]
     * are notified of the change as usual with [LiveData], but only one
     * will actually read data
     */
    val navigateToMediaItem: LiveData<Event<String>> get() = _navigateToMediaItem
    private val _navigateToMediaItem =
        MutableLiveData<Event<String>>()

    /**
     * This [LiveData] object is used to notify the BottomNavigationActivity that
     * the main content fragment needs to be swapped
     */
    val navigateToFragment: LiveData<Event<FragmentNavigationRequest>> get() = _navigateToFragment
    private val _navigateToFragment =
        MutableLiveData<Event<FragmentNavigationRequest>>()

    /**
     * This method takes a [MediaItemData] and routes it depending on
     * whether it's browsable or not
     *
     * If item is browsable. handle it by sending an event to the Activity to
     * browse to it, otherwise play it
     */
    fun mediaItemClicked(
        clickedItem: MediaItemData
    ) {
        if (clickedItem.browsable) {
            browseToItem(
                clickedItem
            )
        } else {
            playMedia(
                clickedItem
            )
//            showFragment(NowPlayingFragment.newInstance())
        }
    }

    fun storyClicked(
        clickedStory: Story
    ) {
        val storyMetadata =
            MediaItemData(
                mediaId = clickedStory.id,
                clickedStory.title,
                clickedStory.narrator,
                Uri.parse(
                    clickedStory.imageUrl
                ),
                false,
                0
            )
        playMedia(
            storyMetadata
        )
    }

    /**
     * Convenience method used to swap the fragment shown in the BottomNavigation activity
     */
    fun showFragment(
        fragment: Fragment,
        backStack: Boolean = true,
        tag: String? = null
    ) {
        _navigateToFragment.value =
            Event(
                FragmentNavigationRequest(
                    fragment,
                    backStack,
                    tag
                )
            )
    }

    /**
     * This posts a browse [Event] that will be handled by the
     * observer in [BottomNavigation]
     */
    private fun browseToItem(
        mediaItem: MediaItemData
    ) {
        _navigateToMediaItem.value =
            Event(
                mediaItem.mediaId
            )
    }

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
                            TAG,
                            "Playable item clicked but neither play nor pause" +
                                    "are enabled! (mediaId=${mediaItem.mediaId})"
                        )
                    }
                }
            }
        } else {
            transportControls.playFromMediaId(
                mediaItem.mediaId,
                null
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
                            TAG,
                            "Playable item clicked but neither play nor pause are enabled! (mediaId=$mediaId)"
                        )
                    }
                }
            }
        } else {
            Log.d(
                TAG,
                "Still not prepared!!"
            )
            transportControls.playFromMediaId(
                mediaId,
                null
            )
        }
    }

    fun changePlaybackSpeed() {
        val transportControls =
            playerServiceConnection.transportControls
        playerServiceConnection.playbackState.value?.let { playbackState ->
            if (playbackState.playbackSpeed != 0F) {
                val newSpeed =
                    when (playbackState.playbackSpeed) {
                        0.5F -> 1F
                        1F -> 1.1F
                        1.1F -> 1.25F
                        1.25F -> 1.5F
                        1.5F -> 1.75F
                        1.75F -> 2F
                        2F -> 0.5f
                        else -> playbackState.playbackSpeed
                    }
                transportControls.setPlaybackSpeed(
                    newSpeed
                )
            }
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
        val transportControls = playerServiceConnection.transportControls
    }

    class Factory(
        private val playerServiceConnection: PlayerServiceConnection
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress(
            "unchecked_cast"
        )
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            return BottomNavigationViewModel(
                playerServiceConnection
            ) as T
        }
    }

    companion object {
        private val TAG =
            BottomNavigationViewModel::class.simpleName
        private const val POSITION_UPDATE_INTERVAL_MILLIS =
            100L
    }
}

/**
 * Helper class used to pass fragment navigation requests between BottomNavigation
 * and its corresponding ViewModel
 */
data class FragmentNavigationRequest(
    val fragment: Fragment,
    val backStack: Boolean = false,
    val tag: String? = null
)
package com.autio.android_app.ui.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.*
import com.autio.android_app.R
import com.autio.android_app.data.model.story.Story
import com.autio.android_app.data.repository.FirebaseStoryRepository
import com.autio.android_app.extensions.*
import com.autio.android_app.player.EMPTY_PLAYBACK_STATE
import com.autio.android_app.player.PlayerServiceConnection
import kotlinx.coroutines.launch

class PlayerFragmentViewModel(
    app: Application,
    playerServiceConnection: PlayerServiceConnection
) : AndroidViewModel(
    app
) {
    private var playbackState: PlaybackStateCompat =
        EMPTY_PLAYBACK_STATE
    val currentStory =
        MutableLiveData<Story?>()
    val mediaPosition =
        MutableLiveData<Long>().apply {
            postValue(
                0L
            )
        }
    val speedButtonRes = MutableLiveData<Int>().apply {
        postValue(R.drawable.ic_speed_audio_1x)
    }
    val mediaButtonRes =
        MutableLiveData<Int>().apply {
            postValue(
                R.drawable.ic_album
            )
        }

    private val _storyLikes = MutableLiveData<Map<String, Boolean>>()
    val storyLikes: LiveData<Map<String, Boolean>> = _storyLikes

    private var updatePosition =
        true
    private val handler =
        Handler(
            Looper.getMainLooper()
        )

    /**
     * When the session's [PlaybackStateCompat] changes, the [mediaItems] need to be updated
     * so the correct [MediaItemData.playbackRes] is displayed on the active item.
     * (i.e.: play/pause button or blank)
     */
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

    /**
     * When the session's [MediaMetadataCompat] changes, the [mediaItems] need to be updated
     * as it means the currently active item has changed. As a result, the new, and potentially
     * old item (if there was one), both need to have their [MediaItemData.playbackRes]
     * changed. (i.e.: play/pause button or blank)
     */
    private val mediaMetadataObserver =
        Observer<Story?> {
            updateState(
                playbackState,
                it
            )
        }

    /**
     * Because there's a complex dance between this [ViewModel] and the [PlayerServiceConnection]
     * (which is wrapping a [MediaBrowserCompat] object), the usual guidance of using
     * [Transformations] doesn't quite work.
     *
     * Specifically there's three things that are watched that will cause the single piece of
     * [LiveData] exposed from this class to be updated.
     *
     * [PlayerServiceConnection.playbackState] changes state based on the playback state of
     * the player, which can change the [MediaItemData.playbackRes]s in the list.
     *
     * [PlayerServiceConnection.nowPlaying] changes based on the item that's being played,
     * which can also change the [MediaItemData.playbackRes]s in the list.
     */
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

    /**
     * Internal function that recursively calls itself every [POSITION_UPDATE_INTERVAL_MILLIS] ms
     * to check the current playback position and updates the corresponding LiveData object when it
     * has changed.
     */
    private fun checkPlaybackPosition(): Boolean =
        handler.postDelayed(
            {
                val currPosition =
                    playbackState.currentPlayBackPosition
                if (mediaPosition.value != currPosition)
                    mediaPosition.postValue(
                        currPosition
                    )
                if (updatePosition)
                    checkPlaybackPosition()
            },
            POSITION_UPDATE_INTERVAL_MILLIS
        )

    /**
     * Since we use [LiveData.observeForever] above (in [playerServiceConnection]), we want
     * to call [LiveData.removeObserver] here to prevent leaking resources when the [ViewModel]
     * is not longer in use.
     */
    override fun onCleared() {
        super.onCleared()

        // Remove the permanent observers from the PlayerServiceConnection.
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
        // Update the playback speed button resource ID
        speedButtonRes.postValue(
            when (playbackState.playbackSpeed) {
                0.5F -> R.drawable.ic_speed_audio_halfx
                1F -> R.drawable.ic_speed_audio_1x
                1.1F -> R.drawable.ic_speed_audio_1point1x
                1.25F -> R.drawable.ic_speed_audio_1point25x
                1.5F -> R.drawable.ic_speed_audio_1point5x
                1.75F -> R.drawable.ic_speed_audio_1point75x
                2F -> R.drawable.ic_speed_audio_2x
                else -> R.drawable.ic_speed_audio_1x
            }
        )

        // Update the media button resource ID
        mediaButtonRes.postValue(
            when (playbackState.isPlaying) {
                true -> R.drawable.ic_player_pause
                else -> R.drawable.ic_player_play
            }
        )

        story?.id?.let { storyId ->
            // Call the service calling the Firebase likes' collection
            viewModelScope.launch {
                val response = FirebaseStoryRepository.getLikesByStoryId(storyId)
                response.likes?.let {
                    _storyLikes.value = it
                }
            }

            // Only update media item once we have duration available
            if (story.duration != 0) {
//                val nowPlayingMetadata =
//                    NowPlayingMetadata(
//                        story.id,
//                        story.description.iconUri
//                            ?: Uri.EMPTY,
//                        story.title?.trim(),
//                        story.displaySubtitle?.trim(),
//                        story.narrator,
//                        story.author,
//                        story.description.description?.toString(),
//                        story.duration,
//                        NowPlayingMetadata.timestampToMSS(
//                            app,
//                            story.duration
//                        ),
//                        story.category
//                            ?: ""
//                    )
                this.currentStory.postValue(
                    story
                )
            }
        }
    }

    class Factory(
        private val app: Application,
        private val playerServiceConnection: PlayerServiceConnection
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress(
            "unchecked_cast"
        )
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            return PlayerFragmentViewModel(
                app,
                playerServiceConnection
            ) as T
        }
    }
}

private const val POSITION_UPDATE_INTERVAL_MILLIS =
    100L
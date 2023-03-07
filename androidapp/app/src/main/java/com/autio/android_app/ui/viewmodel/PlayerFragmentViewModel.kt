package com.autio.android_app.ui.viewmodel

import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.*
import com.autio.android_app.R
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.extensions.*
import com.autio.android_app.player.EMPTY_PLAYBACK_STATE
import com.autio.android_app.player.PlayerServiceConnection
import com.autio.android_app.ui.stories.models.Story
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

const val POSITION_UPDATE_INTERVAL_MILLIS = 17L

@HiltViewModel
class PlayerFragmentViewModel @Inject constructor(
    private val prefRepository: PrefRepository,
    private val autioRepository: AutioRepository,
    playerServiceConnection: PlayerServiceConnection
) : ViewModel() {


    private var playbackState: PlaybackStateCompat = EMPTY_PLAYBACK_STATE
    val currentStory = MutableLiveData<Story?>()
    val mediaPosition = MutableLiveData<Long>().apply {
        postValue(
            0L
        )
    }

    private val _speed = MutableLiveData(
        1F
    )
    private val _speedButtonRes = MutableLiveData(
        R.drawable.ic_speed_audio_1x
    )
    val speedButtonRes: LiveData<Int> = _speedButtonRes

    val mediaButtonRes = MutableLiveData<Int>().apply {
        postValue(
            R.drawable.ic_album
        )
    }

    private val _storyLikes = MutableLiveData<Map<Int, Boolean>>()
    val storyLikes: LiveData<Map<Int, Boolean>> = _storyLikes

    private val _isStoryBookmarked = MutableLiveData<Boolean>()
    val isStoryBookmarked: LiveData<Boolean> = _isStoryBookmarked

    private var updatePosition = true
    private val handler = Handler(
        Looper.getMainLooper()
    )

    /**
     * When the session's [PlaybackStateCompat] changes, the [mediaItems] need to be updated
     * so the correct [MediaItemData.playbackRes] is displayed on the active item.
     * (i.e.: play/pause button or blank)
     */
    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        playbackState = it ?: EMPTY_PLAYBACK_STATE
        val currentStory = playerServiceConnection.nowPlaying.value
        updateState(
            playbackState, currentStory
        )
    }

    /**
     * When the session's [MediaMetadataCompat] changes, the [mediaItems] need to be updated
     * as it means the currently active item has changed. As a result, the new, and potentially
     * old item (if there was one), both need to have their [MediaItemData.playbackRes]
     * changed. (i.e.: play/pause button or blank)
     */
    private val mediaMetadataObserver = Observer<Story?> {
        updateState(
            playbackState, it
        )
    }

    /**
     * Because there's a complex dance between this [ViewModel] and the [PlayerServiceConnection],
     * the usual guidance of using [Transformations] doesn't quite work.
     *
     * Specifically there's three things that are watched that will cause the single piece of
     * [LiveData] exposed from this class to be updated.
     *
     * [PlayerServiceConnection.playbackState] changes state based on the playback state of
     *
     * [PlayerServiceConnection.nowPlaying] changes based on the item that's being played
     */
    private val playerServiceConnection = playerServiceConnection.also {
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
    private fun checkPlaybackPosition(): Boolean = handler.postDelayed(
        {
            val currPosition = playbackState.currentPlayBackPosition
            if (mediaPosition.value != currPosition) mediaPosition.postValue(
                currPosition
            )
            if (updatePosition) checkPlaybackPosition()
        }, POSITION_UPDATE_INTERVAL_MILLIS
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
        updatePosition = false
    }

    fun changePlaybackSpeed() {
        val transportControls = playerServiceConnection.transportControls

        val newSpeed = when (_speed.value) {
            0.5F -> 1F
            1F -> 1.1F
            1.1F -> 1.25F
            1.25F -> 1.5F
            1.5F -> 1.75F
            1.75F -> 2F
            2F -> 0.5f
            else -> 1F
        }

        transportControls.setPlaybackSpeed(
            newSpeed
        )
        _speed.postValue(
            newSpeed
        )
    }

    private fun updateState(
        playbackState: PlaybackStateCompat, story: Story?
    ) {
        // Update the playback speed button resource ID

        _speedButtonRes.postValue(
            when (_speed.value) {
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

        if (story?.id != null) {
            viewModelScope.launch {//TODO(Handle viewState and result where this goes up)
                autioRepository.likesByStory(
                    prefRepository.userId, prefRepository.userApiToken, story.id
                )
                val storiesResult = autioRepository.getStoriesFromUserBookmarks(
                    prefRepository.userId, prefRepository.userApiToken
                )
                val stories = storiesResult.getOrNull()

                //TODO(Make this logic work, supposively this checks if the story is bookmarked and updates live data with true or false)

                // _isStoryBookmarked.value =
                //  stories.map {
                //      it.firstOrNull() { it.id == story.id } != null
                //  }

            }

            // Only update media item once we have duration available
            if (story != null) {
                if (story.duration != 0) {
                    this.currentStory.postValue(
                        story
                    )
                }
            }
        }
    }
}




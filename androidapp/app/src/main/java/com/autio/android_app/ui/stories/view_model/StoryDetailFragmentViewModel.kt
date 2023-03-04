package com.autio.android_app.ui.stories.view_model

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autio.android_app.R
import com.autio.android_app.extensions.currentPlayBackPosition
import com.autio.android_app.extensions.isPlaying
import com.autio.android_app.player.EMPTY_PLAYBACK_STATE
import com.autio.android_app.player.PlayerServiceConnection
import com.autio.android_app.ui.stories.models.Story
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val POSITION_UPDATE_INTERVAL_MILLIS = 100L

@HiltViewModel
class StoryDetailFragmentViewModel @Inject constructor(
    private val playerServiceConnection: PlayerServiceConnection
) : ViewModel() {

    private var playbackState: PlaybackStateCompat = EMPTY_PLAYBACK_STATE
    private val currentStory = MutableLiveData<Story?>()
    private val mediaPosition = MutableLiveData<Long>().apply {
        postValue(0L)
    }
    private val mediaButtonRes = MutableLiveData<Int>().apply {
        postValue(R.drawable.ic_album)
    }

    private var updatePosition = true
    private val handler = Handler(
        Looper.getMainLooper()
    )

    /**
     * When the session's [PlaybackStateCompat] changes, [mediaItems] need to be updated
     * so correct [MediaItemData.playbackRes] is displayed on the active item.
     */
    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        playbackState = it ?: EMPTY_PLAYBACK_STATE
        val metadata = playerServiceConnection.nowPlaying.value
        updateState(
            playbackState, metadata
        )
    }

    /**
     * When session's [MediaMetadataCompat] changes, [mediaItems] need to be updated
     * as currently active item has changed. As a result, the new, and potentially the old
     * element (if there was one), both need to have their [MediaItemData.playbackRes]
     * changed
     */
    private val storyObserver = Observer<Story?> {
        updateState(
            playbackState, it
        )
    }

    /**
     * There's three things that are observed that cause the [LiveData] exposed from this
     * class to be updated
     *
     * [PlayerServiceConnection.playbackState] changes state based on playback state of the
     * player, which can change the [MediaItemData.playbackRes] in the list
     *
     * [PlayerServiceConnection.nowPlaying] changes based on what is being played, which
     * can algo change the [MediaItemData.playbackRes] in map's marker
     */
    private val playerServiceConnection = playerServiceConnection.also {
        it.playbackState.observeForever(
            playbackStateObserver
        )
        it.nowPlaying.observeForever(
            storyObserver
        )
        checkPlaybackPosition()
    }

    /**
     * Internal function that recursively calls itself every [POSITION_UPDATE_INTERVAL_MILLIS]
     * ms to check current playback position, and updates corresponding LiveData object when
     * it has changed
     */
    private fun checkPlaybackPosition(): Boolean = handler.postDelayed(
        {
            val currentPos = playbackState.currentPlayBackPosition
            if (mediaPosition.value != currentPos) mediaPosition.postValue(
                currentPos
            )
            if (updatePosition) checkPlaybackPosition()
        }, com.autio.android_app.ui.viewmodel.POSITION_UPDATE_INTERVAL_MILLIS
    )

    /**
     * Since [LiveData.observeForever] is used, it is necessary to remove the observers
     * here to prevent leaking resources when [ViewModel] is not longer in use
     */
    override fun onCleared() {
        super.onCleared()

        // Remove permanent observers from PlayerServiceConnection
        playerServiceConnection.playbackState.removeObserver(
            playbackStateObserver
        )
        playerServiceConnection.nowPlaying.removeObserver(
            storyObserver
        )

        // Stop updating position
        updatePosition = false
    }

    private fun updateState(
        playbackState: PlaybackStateCompat, story: Story?
    ) {
        // Only update media item once duration is available
        if (story?.duration != 0 && story?.id != null) {
//            val nowPlayingMetadata =
//                NowPlayingMetadata(
//                    story.id!!,
//                    story.artUri,
//                    story.title!!.trim(),
//                    story.displaySubtitle,
//                    story.narrator,
//                    story.author,
//                    story.description.description?.toString(),
//                    story.duration,
//                    NowPlayingMetadata.timestampToMSS(
//                        app,
//                        story.duration
//                    ),
//                    story.category ?: ""
//                )
            this.currentStory.postValue(
                story
            )
        }

        // Update media button resource ID
        mediaButtonRes.postValue(
            when (playbackState.isPlaying) {
                true -> R.drawable.ic_player_pause
                else -> R.drawable.ic_player_play
            }
        )
    }

    fun initView(storyParam: Story?) {
        TODO("Not yet implemented")
    }

    class Factory(
        private val app: Application, private val playerServiceConnection: PlayerServiceConnection
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress(
            "unchecked_cast"
        )
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            return StoryDetailFragmentViewModel(
                app, playerServiceConnection
            ) as T
        }
    }
}
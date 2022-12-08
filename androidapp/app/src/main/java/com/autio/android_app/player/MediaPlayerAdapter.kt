package com.autio.android_app.player

import android.content.Context
import android.media.MediaPlayer
import android.os.SystemClock
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log

class MediaPlayerAdapter(
    context: Context,
    private val listener: PlaybackInfoListener
) : PlayerAdapter(
    context
) {
    private var mediaPlayer: MediaPlayer? =
        null
    private var recordUrl: String? = null
    private lateinit var currentMedia: MediaMetadataCompat
    private var state = PlaybackStateCompat.STATE_PAUSED
    private var currentMediaPlayedToCompletion: Boolean? =
        null

    // Work-around for a MediaPlayer bug related to the behaviour of seekTo()
    // while not playing
    private var seekWhileNotPlaying =
        -1

    /**
     * Once the {@link MediaPlayer} is released, it can't be used again,
     * and another one has to be created. Inside onStop() of the activity instancing
     * this adapter, the {@link MediaPlayer} is released. Then in onStart() of the
     * same activity, a new {@link MediaPlayer} object has to be created. That's why this
     * method is private, and called by load(Int) and not the constructor.
     */
    private fun initializeMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer =
                MediaPlayer().apply {
                    setOnCompletionListener {
                        listener.onPlaybackCompleted()

                        // Set the state to "paused" because it most closely matches
                        // the state in MediaPlayer with regards to available state
                        // transitions compared to "stop"
                        // Paused allows: seekTo(), start(), pause(), stop()
                        // Stop allows: stop()
                        setNewState(
                            PlaybackStateCompat.STATE_PAUSED
                        )
                    }
                    setOnPreparedListener {
                        play()
                    }
                }
        }
    }

    override fun playFromMedia(
        metadata: MediaMetadataCompat
    ) {
        currentMedia =
            metadata
        val storyId =
            metadata.description.mediaId
        if (storyId != null) {
            StoryLibrary.getRecord(storyId)
                ?.let {
                    playStory(
                        it
                    )
                }
        }
    }

    override fun getCurrentMedia(): MediaMetadataCompat =
        currentMedia

    private fun playStory(
        storyRecord: String
    ) {
        var mediaChanged =
            recordUrl == null || storyRecord != recordUrl
        if (currentMediaPlayedToCompletion == true) {
            mediaChanged =
                true
            currentMediaPlayedToCompletion =
                false
        }
        if (!mediaChanged) {
            if (!isPlaying()) {
                play()
            }
            return
        } else {
            release()
        }

        recordUrl =
            storyRecord

        initializeMediaPlayer()

        try {
            mediaPlayer!!.setDataSource(
                recordUrl
            )
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to find resource: $recordUrl",
                e
            )
        }

        try {
            mediaPlayer!!.prepareAsync()
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to download resource: $recordUrl",
                e
            )
        }
    }

    override fun onStop() {
        // Regardless of whether or not the MediaPlayer has been created
        // or started, the state must be updated, so that MediaNotificationManager
        // can take down the notification
        setNewState(
            PlaybackStateCompat.STATE_STOPPED
        )
        release()
    }

    private fun release() {
        mediaPlayer?.release()
        mediaPlayer =
            null
    }

    override fun isPlaying(): Boolean =
        mediaPlayer != null && mediaPlayer!!.isPlaying

    override fun onPlay() {
        if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
            mediaPlayer!!.start()
            setNewState(
                PlaybackStateCompat.STATE_PLAYING
            )
        }
    }

    override fun onPause() {
        if (isPlaying()) {
            mediaPlayer!!.pause()
            setNewState(
                PlaybackStateCompat.STATE_PAUSED
            )
        }
    }

    // This is the main reducer for the player state machine
    private fun setNewState(
        newPlayerState: Int
    ) {
        state =
            newPlayerState

        // Whether playback goes to completion, or it is stopped,
        // the currentMediaPlayedToCompletion is set to true
        if (state == PlaybackStateCompat.STATE_STOPPED) {
            currentMediaPlayedToCompletion =
                true
        }

        // Work around for MediaPlayer.currentPosition when it changes
        // while not playing
        val reportPosition =
            if (seekWhileNotPlaying >= 0) {
                seekWhileNotPlaying
            } else {
                if (mediaPlayer == null) 0 else mediaPlayer!!.currentPosition
            }
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            seekWhileNotPlaying =
                -1
        }

        val stateBuilder =
            PlaybackStateCompat.Builder()
                .apply {
                    setActions(
                        getAvailableActions()
                    )
                    setState(
                        state!!,
                        reportPosition.toLong(),
                        1.0f,
                        SystemClock.elapsedRealtime()
                    )
                }
        listener.onPlaybackStateChange(
            stateBuilder.build()
        )
    }

    /**
     * Set the current capabilities available on this session. Note: If
     * a capability is not listed in the bitmask of capabilities, the
     * MediaSession will not handle it. For example, if you don't want
     * ACTION_STOP to be handled by the MediaSession, then don't include
     * it in the returned bitmask
     */
    private fun getAvailableActions() : Long {
        var actions =
            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        when (state) {
            PlaybackStateCompat.STATE_STOPPED -> (PlaybackStateCompat.ACTION_PLAY
                    or PlaybackStateCompat.ACTION_PAUSE).let {
                actions =
                    actions or it; actions
            }
            PlaybackStateCompat.STATE_PLAYING -> (PlaybackStateCompat.ACTION_STOP
                    or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_SEEK_TO).let {
                actions =
                    actions or it; actions
            }
            PlaybackStateCompat.STATE_PAUSED -> (PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_STOP).let {
                        actions = actions or it; actions
            }
            else -> (PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE
                    or PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_PAUSE).let {
                        actions = actions or it; actions
            }
        }
        return actions
    }

    override fun seekTo(
        position: Long
    ) {
        if (mediaPlayer?.isPlaying == false) {
            seekWhileNotPlaying = position.toInt()
        }
        mediaPlayer?.seekTo(
            position.toInt()
        )

        // Set the new state because the position changed
        // and should be reported to clients
        state?.let {
            setNewState(
                it
            )
        }
    }

    override fun setVolume(
        volume: Float
    ) {
        mediaPlayer?.setVolume(volume, volume)
    }

    companion object {
        private val TAG = MediaPlayerAdapter::class.simpleName
    }
}
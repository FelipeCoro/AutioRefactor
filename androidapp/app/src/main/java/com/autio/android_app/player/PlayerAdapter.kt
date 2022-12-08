package com.autio.android_app.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat

abstract class PlayerAdapter(
    val context: Context
) {

    private var audioNoisyReceiverRegistered =
        false
    private val audioNoisyReceiver =
        object :
            BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?
            ) {
                if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent?.action) {
                    if (isPlaying()) pause()
                }
            }
        }

    private val audioManager =
        context.getSystemService(
            Context.AUDIO_SERVICE
        ) as AudioManager

    /**
     * Helper class for managing audio focus related tasks
     */
    private val audioFocusHelper =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    playOnAudioFocus =
                        false
                    stop()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    if (isPlaying()) {
                        playOnAudioFocus =
                            true
                        pause()
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> setVolume(
                    MEDIA_VOLUME_DUCK
                )
                AudioManager.AUDIOFOCUS_GAIN -> {
                    if (playOnAudioFocus && !isPlaying()) {
                        play()
                    } else if (isPlaying()) {
                        setVolume(
                            MEDIA_VOLUME_DEFAULT
                        )
                    }
                    playOnAudioFocus =
                        false
                }
            }
        }

    private val focusRequest =
        AudioFocusRequest.Builder(
            AudioManager.AUDIOFOCUS_GAIN
        )
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(
                        AudioAttributes.USAGE_MEDIA
                    )
                    .setContentType(
                        AudioAttributes.CONTENT_TYPE_MUSIC
                    )
                    .build()
            )
            .setAcceptsDelayedFocusGain(
                true
            )
            .setOnAudioFocusChangeListener(audioFocusHelper, Handler(Looper.getMainLooper()))
            .build()

    private var playOnAudioFocus =
        false

    abstract fun playFromMedia(
        metadata: MediaMetadataCompat
    )

    abstract fun getCurrentMedia(): MediaMetadataCompat

    abstract fun isPlaying(): Boolean

    fun play() {
        if (audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            registerAudioNoisyReceiver()
            onPlay()
        }
    }

    /**
     * Called when media is ready to be played and indicates the app has audio focus
     */
    protected abstract fun onPlay()

    fun pause() {
        if (!playOnAudioFocus) {
            audioManager.abandonAudioFocusRequest(focusRequest)
        }

        unregisterAudioNoisyReceiver()
        onPause()
    }

    /**
     * Called when media must be paused
     */
    protected abstract fun onPause()

    fun stop() {
        audioManager.abandonAudioFocusRequest(focusRequest)
        unregisterAudioNoisyReceiver()
        onStop()
    }

    /**
     * Called when the media must be stopped. The player should clean up resources
     * at this point.
     */
    protected abstract fun onStop()

    abstract fun seekTo(
        position: Long
    )

    abstract fun setVolume(
        volume: Float
    )

    private fun registerAudioNoisyReceiver() {
        if (!audioNoisyReceiverRegistered) {
            context.registerReceiver(
                audioNoisyReceiver,
                AUDIO_NOISY_INTENT_FILTER
            )
            audioNoisyReceiverRegistered =
                true
        }
    }

    private fun unregisterAudioNoisyReceiver() {
        if (audioNoisyReceiverRegistered) {
            context.unregisterReceiver(
                audioNoisyReceiver
            )
            audioNoisyReceiverRegistered =
                false
        }
    }

    companion object {
        private const val MEDIA_VOLUME_DEFAULT =
            1.0f
        private const val MEDIA_VOLUME_DUCK =
            0.3f

        private val AUDIO_NOISY_INTENT_FILTER =
            IntentFilter(
                AudioManager.ACTION_AUDIO_BECOMING_NOISY
            )
    }
}
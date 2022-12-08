package com.autio.android_app.player

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatSeekBar


class MediaSeekBar(
    context: Context,
    attributes: AttributeSet,
    defStyleAttr: Int
) : AppCompatSeekBar(
    context
) {
    private var mMediaController: MediaControllerCompat? =
        null
    private var mControllerCallback: ControllerCallback? =
        null

    private var mIsTracking =
        false
    private val mOnSeekBarChangeListener: OnSeekBarChangeListener =
        object :
            OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
            }

            override fun onStartTrackingTouch(
                seekBar: SeekBar
            ) {
                mIsTracking =
                    true
            }

            override fun onStopTrackingTouch(
                seekBar: SeekBar
            ) {
                mMediaController!!.transportControls.seekTo(
                    progress.toLong()
                )
                mIsTracking =
                    false
            }
        }
    private var mProgressAnimator: ValueAnimator? =
        null

    override fun setOnSeekBarChangeListener(
        l: OnSeekBarChangeListener?
    ) {
        // Prohibit adding seek listeners to this subclass.
    }

    fun setMediaController(
        mediaController: MediaControllerCompat?
    ) {
        if (mediaController != null) {
            mControllerCallback =
                ControllerCallback()
            mediaController.registerCallback(
                mControllerCallback!!
            )
        } else if (mMediaController != null) {
            mMediaController!!.unregisterCallback(
                mControllerCallback!!
            )
            mControllerCallback =
                null
        }
        mMediaController =
            mediaController
    }

    fun disconnectController() {
        if (mMediaController != null) {
            mMediaController!!.unregisterCallback(
                mControllerCallback!!
            )
            mControllerCallback =
                null
            mMediaController =
                null
        }
    }

    private inner class ControllerCallback :
        MediaControllerCompat.Callback(),
        AnimatorUpdateListener {
        override fun onPlaybackStateChanged(
            state: PlaybackStateCompat?
        ) {
            super.onPlaybackStateChanged(
                state
            )

            // If there's an ongoing animation, stop it now.
            if (mProgressAnimator != null) {
                mProgressAnimator?.cancel()
                mProgressAnimator =
                    null
            }
            val progress =
                state?.position?.toInt()
                    ?: 0
            setProgress(
                progress
            )

            // If the media is playing then the seekbar should follow it, and the easiest
            // way to do that is to create a ValueAnimator to update it so the bar reaches
            // the end of the media the same time as playback gets there (or close enough).
            if (state != null && state.state == PlaybackStateCompat.STATE_PLAYING) {
                val timeToEnd: Int =
                    ((max - progress) / state.playbackSpeed).toInt()
                mProgressAnimator =
                    ValueAnimator.ofInt(
                        progress,
                        max
                    )
                        .setDuration(
                            timeToEnd.toLong()
                        )
                mProgressAnimator?.interpolator =
                    LinearInterpolator()
                mProgressAnimator?.addUpdateListener(
                    this
                )
                mProgressAnimator?.start()
            }
        }

        override fun onMetadataChanged(
            metadata: MediaMetadataCompat?
        ) {
            super.onMetadataChanged(
                metadata
            )
            val max =
                metadata?.getLong(
                    MediaMetadataCompat.METADATA_KEY_DURATION
                )
                    ?.toInt()
                    ?: 0
            progress =
                0
            setMax(
                max
            )
        }

        override fun onAnimationUpdate(
            valueAnimator: ValueAnimator
        ) {
            // If the user is changing the slider, cancel the animation.
            if (mIsTracking) {
                valueAnimator.cancel()
                return
            }
            val animatedIntValue =
                valueAnimator.animatedValue as Int
            progress =
                animatedIntValue
        }
    }
}
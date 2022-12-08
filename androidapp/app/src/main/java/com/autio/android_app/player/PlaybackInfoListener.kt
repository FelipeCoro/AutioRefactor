package com.autio.android_app.player

import android.support.v4.media.session.PlaybackStateCompat

/**
 * Listener to provide state update from {@link MediaPlayerAdapter}
 * to {@link PlayerService}
 */
abstract class PlaybackInfoListener {
    abstract fun onPlaybackStateChange(state: PlaybackStateCompat)

    fun onPlaybackCompleted() {}
}
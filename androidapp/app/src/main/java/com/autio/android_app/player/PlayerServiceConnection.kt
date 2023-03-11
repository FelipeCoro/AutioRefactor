package com.autio.android_app.player

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.media.MediaBrowserServiceCompat
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.domain.mappers.toModel
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.extensions.flag
import com.autio.android_app.extensions.id
import com.autio.android_app.player.PlayerService.Companion.NETWORK_FAILURE
import com.autio.android_app.player.PlayerServiceConnection.MediaBrowserConnectionCallback
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.ui.di.coroutines.MainDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * Class that manages a connection to a [MediaBrowserServiceCompat] instance, typically a
 * [PlayerService] or one of its subclasses.
 *
 * Typically it's best to construct/inject dependencies either using DI or
 * using InjectorUtils in the app module. There are a few difficulties for that here:
 * - [MediaBrowserCompat] is a final class, so mocking it directly is difficult.
 * - A [MediaBrowserConnectionCallback] is a parameter into the construction of
 *   a [MediaBrowserCompat], and provides callbacks to this class.
 * - [MediaBrowserCompat.ConnectionCallback.onConnected] is the best place to construct
 *   a [MediaControllerCompat] that will be used to control the [MediaSessionCompat].
 *
 *  Because of these reasons, rather than constructing additional classes, this is treated as
 *  a black box (which is why there's very little logic here).
 *
 *  This is also why the parameters to construct a [PlayerServiceConnection] are simple
 *  parameters, rather than private properties. They're only required to build the
 *  [MediaBrowserConnectionCallback] and [MediaBrowserCompat] objects.
 */
class PlayerServiceConnection @Inject constructor(
    @ApplicationContext context: Context,
    serviceComponent: ComponentName,
    private val autioRepository: AutioRepository,
    @MainDispatcher private val coroutineDispatcher: CoroutineDispatcher,
    private val prefRepository: PrefRepository
) {

    val isConnected = MutableLiveData<Boolean>().apply {
        postValue(false)
    }
    val networkFailure = MutableLiveData<Boolean>().apply {
        postValue(false)
    }

    val playbackState = MutableLiveData<PlaybackStateCompat>().apply {
        postValue(EMPTY_PLAYBACK_STATE)
    }
    val nowPlaying = MutableLiveData<Story?>().apply { postValue(null) }

    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(
        context
    )
    private val mediaBrowser = MediaBrowserCompat(
        context, serviceComponent, mediaBrowserConnectionCallback, null
    ).apply { connect() }
    private lateinit var mediaController: MediaControllerCompat

    fun subscribe(
        parentId: String, callback: MediaBrowserCompat.SubscriptionCallback
    ) {
        mediaBrowser.subscribe(
            parentId, callback
        )
    }

    fun unsubscribe(
        parentId: String, callback: MediaBrowserCompat.SubscriptionCallback
    ) {
        mediaBrowser.unsubscribe(
            parentId, callback
        )
    }

    private inner class MediaBrowserConnectionCallback(
        private val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {
        /**
         * Invoked after [MediaBrowserCompat.connect] when the request has successfully
         * completed.
         */
        override fun onConnected() {
            // Get a MediaController for the MediaSession.
            mediaController = MediaControllerCompat(
                context, mediaBrowser.sessionToken
            )
            mediaController.registerCallback(
                MediaControllerCallback()
            )
            isConnected.postValue(true)
        }

        /**
         * Invoked when the client is disconnected from the media browser.
         */
        override fun onConnectionSuspended() {
            isConnected.postValue(false)
        }

        /**
         * Invoked when the connection to the media browser failed.
         */
        override fun onConnectionFailed() {
            isConnected.postValue(false)
        }
    }

    //TODO(Cannot inject to inner classes, invetigate fix later for coroutine which was injected at MediaControllerCallback())
    private  inner  class MediaControllerCallback () : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(
            state: PlaybackStateCompat?
        ) {
            playbackState.postValue(state ?: EMPTY_PLAYBACK_STATE)
        }

        override fun onMetadataChanged(
            metadata: MediaMetadataCompat?
        ) {
            // When ExoPlayer stops we will receive a callback with "empty" metadata. This is a
            // metadata object which has been instantiated with default values. The default value
            // for media ID is null so we assume that if this value is null we are not playing
            // anything.
            if (metadata?.id == null) {
                nowPlaying.postValue(null)
            } else {
                CoroutineScope(coroutineDispatcher + SupervisorJob()).launch {

                    val currentStory = autioRepository.getStoryById(prefRepository.userId,prefRepository.userApiToken,
                        metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID).toInt())
                    currentStory.let {
                        nowPlaying.value = it.getOrNull()
                    }
                }
            }
        }

        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
            Log.d("PlayerConnection", "onQueueChanged: $queue")
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when (event) {
                NETWORK_FAILURE -> networkFailure.postValue(true)
            }
        }

        /**
         * Normally if a [MediaBrowserServiceCompat] drops its connection the callback comes via
         * [MediaControllerCompat.Callback] (here). But since other connection status events
         * are sent to [MediaBrowserCompat.ConnectionCallback], we catch the disconnect here and
         * send it on to the other callback.
         */
        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }

    /*
    companion object {
        // For Singleton instantiation.
        @Volatile
        private var instance: PlayerServiceConnection? = null

        fun getInstance(
            context: Context,
            serviceComponent: ComponentName,
            autioLocalDataSourceImpl: AutioLocalDataSourceImpl
        ) = instance ?: synchronized(
            this
        ) {
            instance ?: PlayerServiceConnection(
                context, serviceComponent, autioLocalDataSourceImpl
            ).also {
                instance = it
            }
        }
    }*/
}

val EMPTY_PLAYBACK_STATE: PlaybackStateCompat =
    PlaybackStateCompat.Builder()
        .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
        .build()

val NOTHING_PLAYING: MediaMetadataCompat =
    MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0).build()

package com.autio.android_app.player

import android.content.ComponentName
import android.content.Context
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.autio.android_app.data.model.story.Story

/**
 * Adapter for MediaBrowser that handles connection and basic browsing
 */
class MediaBrowserAdapter(
    val context: Context
) {
    private val state =
        InternalState()

    private val listeners =
        ArrayList<MediaBrowserChangeListener>()

    /**
     * Receives a callback from the MediaBrowser when it successfully connected
     * to the MediaBrowserService {@link PlayerService}
     */
    private val mediaBrowserConnectionCallback =
        object:
            MediaBrowserCompat.ConnectionCallback() {
            // Result of onStart()
            override fun onConnected() {
                try {
                    // Get MediaController for MediaSession
                    mediaController =
                        MediaControllerCompat(
                            context,
                            mediaBrowser?.sessionToken!!
                        )
                    mediaController!!.registerCallback(
                        mediaControllerCallback
                    )

                    // Sync existing MediaSession state to UI
                    mediaControllerCallback.onMetadataChanged(
                        mediaController!!.metadata
                    )
                    mediaControllerCallback.onPlaybackStateChanged(
                        mediaController!!.playbackState
                    )

                    performOnAllListeners(object: ListenerCommand {
                        override fun perform(
                            listener: MediaBrowserChangeListener
                        ) {
                            listener.onConnected(mediaController!!)
                        }
                    })
                } catch (e: RemoteException) {
                    Log.d(TAG, "onConnected: Problem $e")
                    throw RuntimeException(e)
                }

                mediaBrowser!!.subscribe(
                    mediaBrowser!!.root, mediaBrowserSubscriptionCallback)

                Log.d(TAG, "onConnected: Connected successfully...")
            }
        }

    /**
     * Receives callbacks from the MediaController and updates UI state
     */
    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(
            metadata: MediaMetadataCompat?
        ) {
            if (isMediaIdSame(metadata, state.mediaMetadata)) {
                Log.d(TAG, "onMetadataChanged: Filtering out needless update")
                return
            } else {
                state.mediaMetadata = metadata
            }
            performOnAllListeners(object: ListenerCommand {
                override fun perform(
                    listener: MediaBrowserChangeListener
                ) {
                    listener.onMetadataChanged(metadata)
                }
            })
        }

        override fun onPlaybackStateChanged(
            newState: PlaybackStateCompat?
        ) {
            Log.d(TAG, "onPlaybackStateChanged: $newState")
            state.playbackState = newState
            performOnAllListeners(object: ListenerCommand {
                override fun perform(
                    listener: MediaBrowserChangeListener
                ) {
                    listener.onPlaybackStateChanged(newState)
                }
            })
        }

        // Happens if PlayerService is destroyed while the Activity is in foreground
        // and onStart() has been called (but not onStop())
        override fun onSessionDestroyed() {
            resetState()
            onPlaybackStateChanged(null)
            Log.d(TAG, "onSessionDestroyed: PlayerService was destroyed...")
        }

        private fun isMediaIdSame(currentMedia: MediaMetadataCompat?, newMedia: MediaMetadataCompat?) : Boolean {
            if (currentMedia == null || newMedia == null) return false
            val currentMediaId = currentMedia.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
            val newMediaId = newMedia.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
            return currentMediaId == newMediaId
        }
    }

    /**
     * Receives callbacks from the MediaBrowser when the MediaBrowserService
     * has loaded new media ready for playback
     */
    private val mediaBrowserSubscriptionCallback =
        object: MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                assert(mediaController != null)

                // Queue up all media items
                for (item in children) {
                    mediaController!!.addQueueItem(item.description)
                }

                // Call "playFromMedia" so UI is updated
                mediaController!!.transportControls.prepare()
            }
        }

    private var mediaBrowser: MediaBrowserCompat? =
        null
    private var mediaController: MediaControllerCompat? =
        null

    fun onStart() {
        if (mediaBrowser == null) {
            mediaBrowser =
                MediaBrowserCompat(
                    context,
                    ComponentName(
                        context,
                        PlayerService::class.java
                    ),
                    mediaBrowserConnectionCallback,
                    null
                ).apply {
                    connect()
                }
        }
        Log.d(TAG, "onStart: Creating MediaBrowser and connecting...")
    }

    fun onStop() {
        if (mediaController != null) {
            mediaController!!.unregisterCallback(mediaControllerCallback)
            mediaController = null
        }
        if (mediaBrowser?.isConnected == true) {
            mediaBrowser!!.disconnect()
            mediaBrowser = null
        }
        resetState()
        Log.d(TAG, "onStop: Releasing MediaController, Disconnection from MediaBrowser")
    }

    fun addToQueue(story: Story) {
        if (mediaController != null) {
            mediaController!!.addQueueItem(
                MediaDescriptionCompat.Builder()
                    .setMediaId(story.id)
                    .setTitle(story.title)
                    .build()
            )
        }
    }

    /**
     * The internal state of the app needs to revert to what it looks like when it started
     * before any connections to the {@link PlayerService} happens via the {@link MediaSessionCompat}
     */
    private fun resetState() {
        state.reset()
        performOnAllListeners(object: ListenerCommand {
            override fun perform(
                listener: MediaBrowserChangeListener
            ) {
                listener.onPlaybackStateChanged(null)
            }
        })
        Log.d(TAG, "resetState: ")
    }

    fun getTransportControls(): MediaControllerCompat.TransportControls {
        if (mediaController == null) {
            Log.d(TAG, "getTransportControls: MediaController is null...")
            throw IllegalStateException()
        }
        return mediaController!!.transportControls
    }

    fun addListener(listener: MediaBrowserChangeListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: MediaBrowserChangeListener) {
        if (listeners.contains(listener)) listeners.remove(listener)
    }

    fun performOnAllListeners(command: ListenerCommand) {
        for (listener in listeners) {
            try {
                command.perform(listener)
            } catch (e: Exception) {
                removeListener(listener)
            }
        }
    }

    interface ListenerCommand {
        fun perform(listener: MediaBrowserChangeListener)
    }

    /**
     * Helper class for easily subscribing to changes in a MediaBrowserService connection
     */
    abstract class MediaBrowserChangeListener {
        abstract fun onConnected(
            mediaController: MediaControllerCompat
        )

        abstract fun onMetadataChanged(
            mediaMetadata: MediaMetadataCompat?
        )

        abstract fun onPlaybackStateChanged(
            playbackState: PlaybackStateCompat?
        )
    }

    /**
     * Holder class that contains internal state
     */
    inner class InternalState {
        var playbackState: PlaybackStateCompat? =
            null
        var mediaMetadata: MediaMetadataCompat? = null

        fun reset() {
            playbackState =
                null
            mediaMetadata =
                null
        }
    }

    companion object {
        private val TAG = MediaBrowserAdapter::class.simpleName
    }
}
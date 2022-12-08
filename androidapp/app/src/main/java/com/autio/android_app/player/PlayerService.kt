package com.autio.android_app.player

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.autio.android_app.util.PackageValidator

class PlayerService :
    MediaBrowserServiceCompat() {

    private lateinit var packageValidator : PackageValidator
    private var mediaSession: MediaSessionCompat? =
        null
    private var playback: PlayerAdapter? =
        null
    private var mediaNotificationManager: MediaNotificationManager? =
        null
    private var mediaSessionCallback =
        object :
            MediaSessionCompat.Callback() {
            private val playlist =
                ArrayList<MediaSessionCompat.QueueItem>()
            private var queueIndex =
                -1
            private var preparedMedia: MediaMetadataCompat? =
                null

            override fun onAddQueueItem(
                description: MediaDescriptionCompat?
            ) {
                playlist.add(
                    MediaSessionCompat.QueueItem(
                        description,
                        description.hashCode()
                            .toLong()
                    )
                )
                queueIndex =
                    if (queueIndex == -1) 0 else queueIndex

                onPrepare()
            }

            override fun onRemoveQueueItem(
                description: MediaDescriptionCompat?
            ) {
                super.onRemoveQueueItem(
                    description
                )
                playlist.remove(
                    MediaSessionCompat.QueueItem(
                        description,
                        description.hashCode()
                            .toLong()
                    )
                )
                queueIndex =
                    if (playlist.isEmpty()) -1 else queueIndex
            }

            override fun onPrepare() {
                if (queueIndex < 0 && playlist.isEmpty()) {
                    Log.d(
                        TAG,
                        "onPrepare: Nothing to play..."
                    )
                    return
                }

                val storyId =
                    playlist[queueIndex].description.mediaId
                preparedMedia =
                    storyId?.let {
                        StoryLibrary.getMetadata(
                            this@PlayerService,
                            it
                        )
                    }
                mediaSession?.setMetadata(
                    preparedMedia
                )

                if (mediaSession?.isActive == false) {
                    mediaSession?.isActive =
                        true
                }
                Log.d(
                    TAG,
                    "onPrepare: Service is prepared"
                )
            }

            override fun onPlay() {
                if (!isReadyToPlay()) {
                    Log.d(
                        TAG,
                        "onPlay: Nothing to play..."
                    )
                    return
                }
                Log.d(
                    TAG,
                    "onPlay: Playing $preparedMedia"
                )

                if (preparedMedia == null) {
                    onPrepare()
                }

                playback?.playFromMedia(
                    preparedMedia!!
                )
                Log.d(
                    TAG,
                    "onPlayFromMediaId: MediaSession active"
                )
            }

            override fun onPause() {
                playback?.pause()
            }

            override fun onStop() {
                playback?.stop()
                mediaSession?.isActive =
                    false
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                queueIndex =
                    (++queueIndex % playlist.size)
                preparedMedia =
                    null
                onPlay()
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                queueIndex =
                    if (queueIndex > 0) queueIndex - 1 else playlist.size - 1
                preparedMedia =
                    null
                onPlay()
            }

            override fun onSeekTo(
                pos: Long
            ) {
                playback?.seekTo(
                    pos
                )
            }

            private fun isReadyToPlay(): Boolean {
                return playlist.isNotEmpty()
            }
        }
    private var serviceInStartedState: Boolean? =
        null

    override fun onCreate() {
        super.onCreate()
        packageValidator = PackageValidator(
            this
        )
        initMediaSession()
    }

    override fun onTaskRemoved(
        rootIntent: Intent?
    ) {
        super.onTaskRemoved(
            rootIntent
        )
        stopSelf()
    }

    override fun onDestroy() {
        mediaNotificationManager?.onDestroy()
        playback?.stop()
        mediaSession?.release()
        Log.d(
            TAG,
            "onDestroy: MediaPlayerAdapter stopped and MediaSession released"
        )
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        if (!packageValidator.isCallerAllowed(
                this,
                clientPackageName,
                clientUid
            )
        ) {
            Log.i(
                TAG,
                "OnGetRoot: Browsing NOT ALLOWED for unknown caller. "
                        + "Returning empty browser root so all apps can use MediaController."
                        + clientPackageName
            )
            return BrowserRoot(
                MEDIA_EMPTY_ROOT,
                null
            )
        }

        return BrowserRoot(
            StoryLibrary.getRoot(),
            null
        )
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<List<MediaBrowserCompat.MediaItem>>
    ) {
        Log.d(TAG, "onLoadChildren: parentMediaId=$parentId")
        if (MEDIA_EMPTY_ROOT == parentId) {
            result.sendResult(
                arrayListOf()
            )
        } else {
            result.sendResult(
                StoryLibrary.getStoryItems()
            )
        }
    }

    private fun initMediaSession() {
        val mediaButtonReceiver =
            ComponentName(
                applicationContext,
                MediaButtonReceiver::class.java
            )
        val mediaButtonIntent =
            Intent(
                Intent.ACTION_MEDIA_BUTTON
            ).apply {
                setClass(
                    this@PlayerService,
                    MediaButtonReceiver::class.java
                )
            }
        val pendingIntent =
            PendingIntent.getBroadcast(
                this,
                1,
                mediaButtonIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        mediaSession =
            MediaSessionCompat(
                baseContext,
                "PlayerService",
                mediaButtonReceiver,
                null
            ).apply {
                setCallback(
                    mediaSessionCallback
                )
                setFlags(
                    MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS
                )
                setMediaButtonReceiver(
                    pendingIntent
                )
            }
        sessionToken =
            mediaSession?.sessionToken

        mediaNotificationManager =
            MediaNotificationManager(
                this
            )

        playback =
            MediaPlayerAdapter(
                this,
                object :
                    PlaybackInfoListener() {
                    private val serviceManager =
                        ServiceManager()

                    override fun onPlaybackStateChange(
                        state: PlaybackStateCompat
                    ) {
                        // Report the state to MediaSession
                        mediaSession?.setPlaybackState(
                            state
                        )

                        // Manage the started state of this service
                        when (state.state) {
                            PlaybackStateCompat.STATE_PLAYING -> serviceManager.moveServiceToStartedState(
                                state
                            )
                            PlaybackStateCompat.STATE_PAUSED -> serviceManager.updateNotificationForPause(
                                state
                            )
                            PlaybackStateCompat.STATE_STOPPED -> serviceManager.moveServiceOutOfStartedState()
                            else -> {}
                        }
                    }
                })
        Log.d(
            TAG,
            "onCreate: PlayerService creating MediaSession and MediaNotificationManager"
        )
    }

    inner class ServiceManager {
        fun moveServiceToStartedState(
            state: PlaybackStateCompat
        ) {
            val notification =
                playback?.getCurrentMedia()
                    ?.let { metadata ->
                        sessionToken?.let { token ->
                            mediaNotificationManager?.getNotification(
                                metadata,
                                state,
                                token
                            )
                        }
                    }

            if (serviceInStartedState == false) {
                ContextCompat.startForegroundService(
                    this@PlayerService,
                    Intent(
                        this@PlayerService,
                        this@PlayerService::class.java
                    )
                )
                serviceInStartedState =
                    true
            }

            startForeground(
                MediaNotificationManager.NOTIFICATION_ID,
                notification
            )
        }

        fun updateNotificationForPause(
            state: PlaybackStateCompat
        ) {
            stopForeground(
                STOP_FOREGROUND_DETACH
            )
            val notification =
                playback?.getCurrentMedia()
                    ?.let { metadata ->
                        sessionToken?.let { token ->
                            mediaNotificationManager?.getNotification(
                                metadata,
                                state,
                                token
                            )
                        }
                    }
            mediaNotificationManager?.notificationManager?.notify(
                MediaNotificationManager.NOTIFICATION_ID,
                notification
            )
        }

        fun moveServiceOutOfStartedState() {
            stopForeground(
                STOP_FOREGROUND_REMOVE
            )
            stopSelf()
            serviceInStartedState =
                false
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        MediaButtonReceiver.handleIntent(
            mediaSession,
            intent
        )
        return super.onStartCommand(
            intent,
            flags,
            startId
        )
    }

    companion object {
        private val TAG =
            PlayerService::class.simpleName

        private const val MEDIA_EMPTY_ROOT =
            "EMPTY_ROOT"
    }
}
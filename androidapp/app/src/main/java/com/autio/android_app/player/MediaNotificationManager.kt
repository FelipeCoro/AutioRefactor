package com.autio.android_app.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat.Token
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.autio.android_app.R
import com.autio.android_app.ui.view.usecases.home.BottomNavigation

/**
 * Keeps track of a notification and updates it for a given MediaSession. This is
 * required so the PlayerService don't get destroyed during playback.
 */
class MediaNotificationManager(
    val playerService: PlayerService
) {
    private var playAction =
        NotificationCompat.Action(
            com.google.android.exoplayer2.R.drawable.exo_icon_play,
            "Play",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                playerService,
                PlaybackStateCompat.ACTION_PLAY
            )
        )
    private var pauseAction =
        NotificationCompat.Action(
            com.google.android.exoplayer2.R.drawable.exo_icon_pause,
            "Pause",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                playerService,
                PlaybackStateCompat.ACTION_PAUSE
            )
        )
    private var nextAction =
        NotificationCompat.Action(
            com.google.android.exoplayer2.R.drawable.exo_icon_next,
            "Skip to next",
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                playerService,
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            )
        )
    var notificationManager: NotificationManager =
        playerService.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        private set

    init {
        notificationManager.cancelAll()
    }

    fun onDestroy() {
        Log.d(
            TAG,
            "onDestroy"
        )
    }

    fun getNotification(
        metadata: MediaMetadataCompat,
        state: PlaybackStateCompat,
        token: Token
    ): Notification {
        val isPlaying =
            state.state == PlaybackStateCompat.STATE_PLAYING
        val description =
            metadata.description
        val builder =
            buildNotification(
                state,
                token,
                isPlaying,
                description
            )
        return builder.build()
    }

    private fun buildNotification(
        state: PlaybackStateCompat,
        token: Token,
        isPlaying: Boolean,
        description: MediaDescriptionCompat
    ): NotificationCompat.Builder {
        // Required after Oreo version
        createChannel()

        val builder =
            object :
                NotificationCompat.Builder(
                    playerService,
                    CHANNEL_ID
                ) {

            }
        builder.setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(
                    token
                )
                .setShowActionsInCompactView(
                    0,
                    1,
                    2
                )
                .setShowCancelButton(
                    true
                )
                .setCancelButtonIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        playerService,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )
        )
            .setColor(
                ContextCompat.getColor(
                    playerService,
                    R.color.white
                )
            )
            .setSmallIcon(
                R.mipmap.ic_launcher
            )
            // Pending intent that is fired when user clicks on notification.
            .setContentIntent(
                createContentIntent()
            )
            // Title - Usually Song name.
            .setContentTitle(
                description.title
            )
            // Subtitle - Usually Artist name.
            .setContentText(
                description.subtitle
            )
//            .setLargeIcon(MusicLibrary.getAlbumBitmap(mService, description.getMediaId()))
            // When notification is deleted (when playback is paused and notification can be
            // deleted) fire MediaButtonPendingIntent with ACTION_STOP.
            .setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    playerService,
                    PlaybackStateCompat.ACTION_STOP
                )
            )
            // Show controls on lock screen even when user hides sensitive content.
            .setVisibility(
                NotificationCompat.VISIBILITY_PUBLIC
            )

//        if ((state.actions and PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0L) {
//            builder.addAction(prevAction)
//        }
        builder.addAction(
            if (isPlaying) pauseAction else playAction
        )

        // If skip to prev action is enabled
        if ((state.actions and PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0L) {
            builder.addAction(
                nextAction
            )
        }

        return builder
    }

    private fun createChannel() {
        if (notificationManager.getNotificationChannel(
                CHANNEL_ID
            ) == null
        ) {
            val name =
                "MediaSession"
            val description =
                "MediaSession and MediaPlayer"
            val importance =
                NotificationManager.IMPORTANCE_LOW
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    name,
                    importance
                ).apply {
                    setDescription(
                        description
                    )
                    enableLights(
                        true
                    )
                    // Sets the notification light color for notifications posted to this
                    // channel, if the device supports this feature
                    lightColor =
                        Color.BLUE
                    enableVibration(
                        true
                    )
                    vibrationPattern =
                        longArrayOf(
                            100,
                            200,
                            300,
                            400,
                            500,
                            400,
                            300,
                            200,
                            400
                        )
                }
            notificationManager.createNotificationChannel(
                channel
            )
            Log.d(
                TAG,
                "createChannel: New channel created"
            )
        } else {
            Log.d(
                TAG,
                "createChannel: Existing channel reused"
            )
        }
    }

    private fun createContentIntent(): PendingIntent {
        val openUI =
            Intent(
                playerService,
                BottomNavigation::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        return PendingIntent.getActivity(
            playerService,
            REQUEST_CODE,
            openUI,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )
    }

    companion object {
        const val NOTIFICATION_ID =
            412

        private val TAG =
            MediaNotificationManager::class.simpleName
        private const val CHANNEL_ID =
            "com.autio.android_app.storyplayer.channel"
        private const val REQUEST_CODE =
            501
    }
}
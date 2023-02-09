package com.autio.android_app.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.autio.android_app.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import kotlinx.coroutines.*

/**
 * Keeps track of a notification and updates it for a given MediaSession. This is
 * required so the PlayerService don't get destroyed during playback.
 */
class MediaNotificationManager(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener,
) {
    private val serviceJob =
        SupervisorJob()
    private val serviceScope =
        CoroutineScope(
            Dispatchers.Main + serviceJob
        )
    private val notificationManager: PlayerNotificationManager
    private val platformNotificationManager: NotificationManager =
        context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

    init {
        val mediaController =
            MediaControllerCompat(
                context,
                sessionToken
            )

        val builder =
            PlayerNotificationManager.Builder(
                context,
                NOTIFICATION_ID,
                CHANNEL_ID,
            )
        with(
            builder
        ) {
            setMediaDescriptionAdapter(
                DescriptionAdapter(
                    mediaController
                )
            )
            setNotificationListener(
                notificationListener
            )
            setChannelNameResourceId(
                R.string.notification_channel
            )
            setChannelDescriptionResourceId(
                R.string.notification_channel_description
            )
        }
        notificationManager =
            builder.build()
        notificationManager.setMediaSessionToken(
            sessionToken
        )
        notificationManager.setSmallIcon(
            R.drawable.ic_notification
        )
        notificationManager.setUseRewindAction(
            true
        )
        notificationManager.setUseFastForwardAction(
            false
        )
    }

    fun hideNotification() {
        notificationManager.setPlayer(
            null
        )
    }

    fun showNotificationForPlayer(
        player: Player
    ) {
        notificationManager.setPlayer(
            player
        )
    }

    private inner class DescriptionAdapter(
        private val controller: MediaControllerCompat
    ) : PlayerNotificationManager.MediaDescriptionAdapter {
        var currentIconUri: Uri? =
            null
        var currentBitmap: Bitmap? =
            null

        override fun createCurrentContentIntent(
            player: Player
        ): PendingIntent? =
            controller.sessionActivity

        override fun getCurrentContentText(
            player: Player
        ) =
            controller.metadata.description.subtitle.toString()

        override fun getCurrentContentTitle(
            player: Player
        ) =
            controller.metadata.description.title.toString()

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            val iconUri =
                controller.metadata.description.iconUri
            return if (currentIconUri != iconUri || currentBitmap == null) {
                currentIconUri =
                    iconUri
                serviceScope.launch {
                    currentBitmap =
                        iconUri?.let {
                            resolveUriAsBitmap(
                                it
                            )
                        }
                    currentBitmap?.let {
                        callback.onBitmap(
                            it
                        )
                    }
                }
                null
            } else {
                currentBitmap
            }
        }

        private suspend fun resolveUriAsBitmap(
            uri: Uri
        ): Bitmap? {
            return withContext(
                Dispatchers.IO
            ) {
                try {
                    Glide.with(
                        context
                    )
                        .applyDefaultRequestOptions(
                            glideOptions
                        )
                        .asBitmap()
                        .load(
                            uri
                        )
                        .listener(
                            object :
                                RequestListener<Bitmap> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Bitmap>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Bitmap?,
                                    model: Any?,
                                    target: Target<Bitmap>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    return false
                                }
                            })
                        .error(
                            R.drawable.ic_notification
                        )
                        .placeholder(
                            R.drawable.ic_notification
                        )
                        .submit(
                            NOTIFICATION_LARGE_ICON_SIZE,
                            NOTIFICATION_LARGE_ICON_SIZE
                        )
                        .get()
                } catch (e: Exception) {
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.ic_notification
                    )
                }
            }
        }
    }

    private val glideOptions =
        RequestOptions().fallback(
            R.drawable.ic_notification
        )
            .diskCacheStrategy(
                DiskCacheStrategy.DATA
            )
            .error(
                R.drawable.ic_notification
            )

    companion object {
        const val NOTIFICATION_ID =
            0xb339

        const val NOTIFICATION_LARGE_ICON_SIZE =
            144

        private const val CHANNEL_ID =
            "autio.audio.travel.guide.story-player.channel"
    }
}
package com.autio.android_app.player

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import com.autio.android_app.extensions.asAlbumArtContentUri
import com.autio.android_app.notifications.MediaNotificationManager.Companion.NOTIFICATION_LARGE_ICON_SIZE
import com.autio.android_app.player.PlayerService.Companion.MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


internal class PersistentStorage private constructor(
    val context: Context
) {
    private var preferences: SharedPreferences =
        context.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )

    suspend fun saveRecentStory(
        description: MediaDescriptionCompat,
        position: Long
    ) {
        withContext(
            Dispatchers.IO
        ) {
            val localIconUri =
                try {
                    Glide.with(
                        context
                    )
                        .asFile()
                        .load(
                            description.iconUri
                        )
                        .submit(
                            NOTIFICATION_LARGE_ICON_SIZE,
                            NOTIFICATION_LARGE_ICON_SIZE
                        )
                        .get()
                        .asAlbumArtContentUri()
                } catch (e: Exception) {
                    Uri.parse(
                        "android.resource://com.autio.android_app/drawable/ic_notification"
                    )
                }

            preferences.edit()
                .putString(
                    RECENT_STORY_MEDIA_ID_KEY,
                    description.mediaId
                )
                .putString(
                    RECENT_STORY_TITLE_KEY,
                    description.title.toString()
                )
                .putString(
                    RECENT_STORY_SUBTITLE_KEY,
                    description.subtitle.toString()
                )
                .putString(
                    RECENT_STORY_ICON_URI_KEY,
                    localIconUri.toString()
                )
                .putLong(
                    RECENT_STORY_POSITION_KEY,
                    position
                )
                .apply()
        }
    }

    fun loadRecentStory(): MediaBrowserCompat.MediaItem? {
        val mediaId =
            preferences.getString(
                RECENT_STORY_MEDIA_ID_KEY,
                null
            )
        return if (mediaId == null) {
            null
        } else {
            val extras =
                Bundle().also {
                    val position =
                        preferences.getLong(
                            RECENT_STORY_POSITION_KEY,
                            0L
                        )
                    it.putLong(
                        MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS,
                        position
                    )
                }
            MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                    .setMediaId(
                        mediaId
                    )
                    .setTitle(
                        preferences.getString(
                            RECENT_STORY_TITLE_KEY,
                            ""
                        )
                    )
                    .setSubtitle(
                        preferences.getString(
                            RECENT_STORY_SUBTITLE_KEY,
                            ""
                        )
                    )
                    .setIconUri(
                        Uri.parse(
                            preferences.getString(
                                RECENT_STORY_ICON_URI_KEY,
                                ""
                            )
                        )
                    )
                    .setExtras(
                        extras
                    )
                    .build(),
                FLAG_PLAYABLE
            )
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PersistentStorage? =
            null

        fun getInstance(
            context: Context
        ) =
            INSTANCE
                ?: synchronized(
                    this
                ) {
                    INSTANCE
                        ?: PersistentStorage(
                            context
                        ).also {
                            INSTANCE =
                                it
                        }
                }

        private const val PREFERENCES_NAME =
            "autio_player_pref"
        private const val RECENT_STORY_MEDIA_ID_KEY =
            "recent_story_media_id"
        private const val RECENT_STORY_TITLE_KEY =
            "recent_story_title"
        private const val RECENT_STORY_SUBTITLE_KEY =
            "recent_story_subtitle"
        private const val RECENT_STORY_ICON_URI_KEY =
            "recent_story_icon_uri"
        private const val RECENT_STORY_POSITION_KEY =
            "recent_story_position"
    }
}
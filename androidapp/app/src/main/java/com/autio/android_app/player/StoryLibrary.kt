package com.autio.android_app.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import com.autio.android_app.R
import com.autio.android_app.data.model.story.Story
import java.io.IOException
import java.net.URL
import java.util.*
import kotlin.concurrent.thread

class StoryLibrary {
    companion object {
        private val TAG =
            StoryLibrary::class.simpleName

        private val playlist =
            TreeMap<String, MediaMetadataCompat>()
        private val playlistRecordUrl =
            HashMap<String, String>()

        fun getRoot(): String =
            "root"

        fun addStoriesToLibrary(
            stories: List<Story>
        ) {
            for (story in stories) {
                createMediaMetadataCompat(
                    story
                )
            }
        }

        fun getRecord(
            storyId: String
        ): String? {
            return if (playlistRecordUrl.containsKey(
                    storyId
                )
            ) {
                playlistRecordUrl[storyId]
            } else {
                null
            }
        }

        fun getStoryItems(): List<MediaBrowserCompat.MediaItem> {
            val result =
                ArrayList<MediaBrowserCompat.MediaItem>()
            for (metadata in playlist.values) {
                result.add(
                    MediaBrowserCompat.MediaItem(
                        metadata.description,
                        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                    )
                )
            }
            return result
        }

        fun getStoryBitmap(
            context: Context,
            storyId: String,
            callback: (Bitmap) -> Unit
        ) {
            val story =
                playlist[storyId]!!
            thread {
                try {
                    val url =
                        URL(story.description.iconUri!!.toString())
                    val connection =
                        url.openConnection()
                            .apply {
                                doInput =
                                    true
                                connect()
                            }
                    val input =
                        connection.getInputStream()
                    callback(
                        BitmapFactory.decodeStream(
                            input
                        )
                    )
                } catch (e: IOException) {
                    callback(
                        BitmapFactory.decodeResource(
                            context.resources,
                            R.mipmap.ic_launcher
                        )
                    )
                }
            }
        }

        fun getMetadata(
            context: Context,
            storyId: String
        ): MediaMetadataCompat {
            val metadataWithoutBitmap =
                playlist[storyId]
            var imageArt: Bitmap? =
                null
            getStoryBitmap(
                context,
                storyId
            ) {
                imageArt =
                    it
            }

            // Since MediaMetadataCompat is immutable, we need to create
            // a copy to set the art
            // It is not initially set on all items so they don't take
            // unnecessary memory
            val builder =
                MediaMetadataCompat.Builder()
            for (key in arrayOf(
                MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
                MediaMetadataCompat.METADATA_KEY_TITLE,
                MediaMetadataCompat.METADATA_KEY_ARTIST,
                MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST,
                MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION,
                MediaMetadataCompat.METADATA_KEY_GENRE,
            )) {
                builder.putString(
                    key,
                    metadataWithoutBitmap!!.getString(
                        key
                    )
                )
            }
            builder.putLong(
                MediaMetadataCompat.METADATA_KEY_DURATION,
                metadataWithoutBitmap!!.getLong(
                    MediaMetadataCompat.METADATA_KEY_DURATION
                )
            )
            builder.putBitmap(
                MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                imageArt
            )
            return builder.build()
        }

        private fun getStoryArtUri(
            imageUrl: String?
        ): String? {
            if (imageUrl == null) return null
            return Uri.parse(
                imageUrl
            )
                .toString()
        }

        private fun createMediaMetadataCompat(
            story: Story
        ) {
            playlist[story.id] =
                MediaMetadataCompat.Builder()
                    .putString(
                        MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
                        story.id
                    )
                    .putString(
                        MediaMetadataCompat.METADATA_KEY_TITLE,
                        story.title
                    )
                    .putString(
                        MediaMetadataCompat.METADATA_KEY_ARTIST,
                        story.narrator
                    )
                    .putString(
                        MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST,
                        story.author
                    )
                    .putLong(
                        MediaMetadataCompat.METADATA_KEY_DURATION,
                        story.duration.toLong()
                    )
                    .putString(
                        MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION,
                        story.description
                    )
                    .putString(
                        MediaMetadataCompat.METADATA_KEY_GENRE,
                        story.category?.title
                            ?: ""
                    )
                    .putString(
                        MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                        getStoryArtUri(
                            story.imageUrl
                        )
                    )
                    .build()
            playlistRecordUrl[story.id] =
                story.recordUrl
        }
    }
}
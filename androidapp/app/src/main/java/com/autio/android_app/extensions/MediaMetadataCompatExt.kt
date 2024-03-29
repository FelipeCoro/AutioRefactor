package com.autio.android_app.extensions

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.autio.android_app.player.library.JsonSource
import com.autio.android_app.ui.stories.models.Story
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.util.MimeTypes
import java.util.concurrent.TimeUnit

/**
 * Useful extensions for [MediaMetadataCompat].
 */

inline val MediaMetadataCompat.id: String?
    get() = MediaMetadataCompat.METADATA_KEY_MEDIA_ID


inline val MediaMetadataCompat.title: String?
    get() = getString(
        MediaMetadataCompat.METADATA_KEY_TITLE
    )

inline val MediaMetadataCompat.artist: String?
    get() = getString(
        MediaMetadataCompat.METADATA_KEY_ARTIST
    )

inline val MediaMetadataCompat.duration
    get() = getLong(
        MediaMetadataCompat.METADATA_KEY_DURATION
    )

inline val MediaMetadataCompat.album: String?
    get() = getString(
        MediaMetadataCompat.METADATA_KEY_ALBUM
    )

inline val MediaMetadataCompat.author: String?
    get() = getString(
        MediaMetadataCompat.METADATA_KEY_AUTHOR
    )

inline val MediaMetadataCompat.narrator: String?
    get() = getString(
        MediaMetadataCompat.METADATA_KEY_ARTIST
    )

inline val MediaMetadataCompat.writer: String?
    get() = getString(
        MediaMetadataCompat.METADATA_KEY_WRITER
    )

inline val MediaMetadataCompat.composer: String?
    get() = getString(
        MediaMetadataCompat.METADATA_KEY_COMPOSER
    )

inline val MediaMetadataCompat.compilation: String?
    get() = getString(
        MediaMetadataCompat.METADATA_KEY_COMPILATION
    )

inline val MediaMetadataCompat.date: String?
    get() = getString(
        MediaMetadataCompat.METADATA_KEY_DATE
    )

inline val MediaMetadataCompat.year: String?
    get() = getString(
        MediaMetadataCompat.METADATA_KEY_YEAR
    )

inline val MediaMetadataCompat.category: String?
    get() = getString(
        MediaMetadataCompat.METADATA_KEY_GENRE
    )

inline val MediaMetadataCompat.trackNumber
    get() = getLong(
        MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER
    )

inline val MediaMetadataCompat.trackCount
    get() = getLong(
        MediaMetadataCompat.METADATA_KEY_NUM_TRACKS
    )

inline val MediaMetadataCompat.discNumber
    get() = getLong(
        MediaMetadataCompat.METADATA_KEY_DISC_NUMBER
    )

inline val MediaMetadataCompat.albumArtist: String?
    get() = getString(
        MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST
    )

inline val MediaMetadataCompat.art: Bitmap
    get() = getBitmap(
        MediaMetadataCompat.METADATA_KEY_ART
    )

inline val MediaMetadataCompat.artUri: Uri
    get() = this.getString(
        MediaMetadataCompat.METADATA_KEY_ART_URI
    ).toUri()

inline val MediaMetadataCompat.albumArt: Bitmap?
    get() = getBitmap(
        MediaMetadataCompat.METADATA_KEY_ALBUM_ART
    )

inline val MediaMetadataCompat.albumArtUri: Uri
    get() = this.getString(
        MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI
    ).toUri()

inline val MediaMetadataCompat.userRating
    get() = getLong(
        MediaMetadataCompat.METADATA_KEY_USER_RATING
    )

inline val MediaMetadataCompat.rating
    get() = getLong(
        MediaMetadataCompat.METADATA_KEY_RATING
    )

inline val MediaMetadataCompat.displayTitle: String?
    get() = getString(
        MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE
    )

inline val MediaMetadataCompat.displaySubtitle: String?
    get() = getString(
        MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE
    )

inline val MediaMetadataCompat.displayDescription: String?
    get() = getString(
        MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION
    )

inline val MediaMetadataCompat.displayIcon: Bitmap
    get() = getBitmap(
        MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON
    )

inline val MediaMetadataCompat.displayIconUri: Uri
    get() = this.getString(
        MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI
    ).toUri()

inline val MediaMetadataCompat.mediaUri: Uri
    get() = this.getString(
        MediaMetadataCompat.METADATA_KEY_MEDIA_URI
    ).toUri()

inline val MediaMetadataCompat.downloadStatus
    get() = getLong(
        MediaMetadataCompat.METADATA_KEY_DOWNLOAD_STATUS
    )

/**
 * Custom property for storing whether a [MediaMetadataCompat] item represents an
 * item that is [MediaItem.FLAG_BROWSABLE] or [MediaItem.FLAG_PLAYABLE].
 */
inline val MediaMetadataCompat.flag
    get() = this.getLong(
        METADATA_KEY_UAMP_FLAGS
    ).toInt()

/**
 * Useful extensions for [MediaMetadataCompat.Builder].
 */

// These do not have getters, so create a message for the error.
const val NO_GET = "Property does not have a 'get'"

/**
 * Extension method for [MediaMetadataCompat.Builder] to set the fields from
 * our JSON constructed object (to make the code a bit easier to see).
 */
fun MediaMetadataCompat.Builder.from(
    story: Story
): MediaMetadataCompat.Builder {
    // The duration from the JSON is given in seconds, but the rest of the code works in
    // milliseconds. Here's where we convert to the proper units.
    val durationMs = TimeUnit.SECONDS.toMillis(
        story.duration.toLong()
    )

    id = story.id.toString() //TODO(SHOULD BE ID BUT MEDIA DATA PLAYER_ID ONLY RECEIVES STRING)
    title = story.title
    narrator = story.narrator
    duration = durationMs
    category = story.category
    mediaUri = story.recordUrl
    albumArtUri = story.imageUrl
    flag = MediaBrowserCompat.MediaItem.FLAG_PLAYABLE

    // To make things easier for *displaying* these, set the display properties as well.
    displayTitle = story.title
    displaySubtitle = story.narrator
    displayDescription = story.description
    displayIconUri = story.imageUrl

    // Add downloadStatus to force the creation of an "extras" bundle in the resulting
    // MediaMetadataCompat object. This is needed to send accurate metadata to the
    // media session during updates.
    downloadStatus = MediaDescriptionCompat.STATUS_NOT_DOWNLOADED

    // Allow it to be used in the typical builder style.
    return this
}

inline var MediaMetadataCompat.Builder.id: String
    @Deprecated(
        NO_GET, level = DeprecationLevel.ERROR
    ) get() = throw IllegalAccessException(
        "Cannot get from MediaMetadataCompat.Builder"
    )
    set(value) {
        putString(
            MediaMetadataCompat.METADATA_KEY_MEDIA_ID, value
        )
    }

inline var MediaMetadataCompat.Builder.title: String?
    @Deprecated(
        NO_GET, level = DeprecationLevel.ERROR
    ) get() = throw IllegalAccessException(
        "Cannot get from MediaMetadataCompat.Builder"
    )
    set(value) {
        putString(
            MediaMetadataCompat.METADATA_KEY_TITLE, value
        )
    }

inline var MediaMetadataCompat.Builder.narrator: String?
    @Deprecated(
        NO_GET, level = DeprecationLevel.ERROR
    ) get() = throw IllegalAccessException(
        "Cannot get from MediaMetadataCompat.Builder"
    )
    set(value) {
        putString(
            MediaMetadataCompat.METADATA_KEY_ARTIST, value
        )
    }

inline var MediaMetadataCompat.Builder.album: String?
    @Deprecated(
        NO_GET, level = DeprecationLevel.ERROR
    ) get() = throw IllegalAccessException(
        "Cannot get from MediaMetadataCompat.Builder"
    )
    set(value) {
        putString(
            MediaMetadataCompat.METADATA_KEY_ALBUM, value
        )
    }

inline var MediaMetadataCompat.Builder.duration: Long
    @Deprecated(
        NO_GET, level = DeprecationLevel.ERROR
    ) get() = throw IllegalAccessException(
        "Cannot get from MediaMetadataCompat.Builder"
    )
    set(value) {
        putLong(
            MediaMetadataCompat.METADATA_KEY_DURATION, value
        )
    }

inline var MediaMetadataCompat.Builder.category: String?
    @Deprecated(
        NO_GET, level = DeprecationLevel.ERROR
    ) get() = throw IllegalAccessException(
        "Cannot get from MediaMetadataCompat.Builder"
    )
    set(value) {
        putString(
            MediaMetadataCompat.METADATA_KEY_GENRE, value
        )
    }

inline var MediaMetadataCompat.Builder.mediaUri: String?
    @Deprecated(
        NO_GET, level = DeprecationLevel.ERROR
    ) get() = throw IllegalAccessException(
        "Cannot get from MediaMetadataCompat.Builder"
    )
    set(value) {
        putString(
            MediaMetadataCompat.METADATA_KEY_MEDIA_URI, value
        )
    }

inline var MediaMetadataCompat.Builder.albumArtUri: String?
    @Deprecated(
        NO_GET, level = DeprecationLevel.ERROR
    ) get() = throw IllegalAccessException(
        "Cannot get from MediaMetadataCompat.Builder"
    )
    set(value) {
        putString(
            MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, value
        )
    }

inline var MediaMetadataCompat.Builder.albumArt: Bitmap?
    @Deprecated(
        NO_GET, level = DeprecationLevel.ERROR
    ) get() = throw IllegalAccessException(
        "Cannot get from MediaMetadataCompat.Builder"
    )
    set(value) {
        putBitmap(
            MediaMetadataCompat.METADATA_KEY_ALBUM_ART, value
        )
    }

inline var MediaMetadataCompat.Builder.trackNumber: Long
    @Deprecated(
        NO_GET, level = DeprecationLevel.ERROR
    ) get() = throw IllegalAccessException(
        "Cannot get from MediaMetadataCompat.Builder"
    )
    set(value) {
        putLong(
            MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, value
        )
    }

inline var MediaMetadataCompat.Builder.trackCount: Long
    @Deprecated(
        NO_GET, level = DeprecationLevel.ERROR
    ) get() = throw IllegalAccessException(
        "Cannot get from MediaMetadataCompat.Builder"
    )
    set(value) {
        putLong(
            MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, value
        )
    }

inline var MediaMetadataCompat.Builder.displayTitle: String?
    @Deprecated(
        NO_GET, level = DeprecationLevel.ERROR
    ) get() = throw IllegalAccessException(
        "Cannot get from MediaMetadataCompat.Builder"
    )
    set(value) {
        putString(
            MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, value
        )
    }

inline var MediaMetadataCompat.Builder.displaySubtitle: String?
    @Deprecated(
        NO_GET, level = DeprecationLevel.ERROR
    ) get() = throw IllegalAccessException(
        "Cannot get from MediaMetadataCompat.Builder"
    )
    set(value) {
        putString(
            MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, value
        )
    }

inline var MediaMetadataCompat.Builder.displayDescription: String?
    @Deprecated(
        NO_GET, level = DeprecationLevel.ERROR
    ) get() = throw IllegalAccessException(
        "Cannot get from MediaMetadataCompat.Builder"
    )
    set(value) {
        putString(
            MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, value
        )
    }

inline var MediaMetadataCompat.Builder.displayIconUri: String?
    @Deprecated(
        NO_GET, level = DeprecationLevel.ERROR
    ) get() = throw IllegalAccessException(
        "Cannot get from MediaMetadataCompat.Builder"
    )
    set(value) {
        putString(
            MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, value
        )
    }

inline var MediaMetadataCompat.Builder.downloadStatus: Long
    @Deprecated(
        NO_GET, level = DeprecationLevel.ERROR
    ) get() = throw IllegalAccessException(
        "Cannot get from MediaMetadataCompat.Builder"
    )
    set(value) {
        putLong(
            MediaMetadataCompat.METADATA_KEY_DOWNLOAD_STATUS, value
        )
    }

/**
 * Custom property for storing whether a [MediaMetadataCompat] item represents an
 * item that is [MediaItem.FLAG_BROWSABLE] or [MediaItem.FLAG_PLAYABLE].
 */
inline var MediaMetadataCompat.Builder.flag: Int
    @Deprecated(
        NO_GET, level = DeprecationLevel.ERROR
    ) get() = throw IllegalAccessException(
        "Cannot get from MediaMetadataCompat.Builder"
    )
    set(value) {
        putLong(
            METADATA_KEY_UAMP_FLAGS, value.toLong()
        )
    }

fun MediaMetadataCompat.toMediaItemMetadata(): MediaMetadata {
    return with(
        MediaMetadata.Builder()
    ) {
        setTitle(
            title
        )
        setDisplayTitle(
            displayTitle
        )
        setArtist(
            narrator
        )
        setTrackNumber(
            trackNumber.toInt()
        )
        setTotalTrackCount(
            trackCount.toInt()
        )
        setDiscNumber(
            discNumber.toInt()
        )
        setWriter(
            author
        )
        setArtworkUri(
            albumArtUri
        )
        setGenre(
            category
        )
        val extras = Bundle()
        getString(
            JsonSource.ORIGINAL_ARTWORK_URI_KEY
        )?.let {
            // album art is a content:// URI. Keep the original for Cast.
            extras.putString(
                JsonSource.ORIGINAL_ARTWORK_URI_KEY, getString(
                    JsonSource.ORIGINAL_ARTWORK_URI_KEY
                )
            )
        }
        extras.putLong(
            MediaMetadataCompat.METADATA_KEY_DURATION, duration
        )
        setExtras(
            extras
        )
    }.build()
}

fun MediaMetadataCompat.toMediaItem(): MediaItem {
    return with(
        MediaItem.Builder()
    ) {
        setMediaId(
            mediaUri.toString()
        )
        setUri(
            mediaUri
        )
        setMimeType(
            MimeTypes.AUDIO_MPEG
        )
        setMediaMetadata(
            toMediaItemMetadata()
        )
    }.build()
}

/**
 * Custom property that holds whether an item is [MediaItem.FLAG_BROWSABLE] or
 * [MediaItem.FLAG_PLAYABLE].
 */
const val METADATA_KEY_UAMP_FLAGS = "com.example.android.uamp.media.METADATA_KEY_UAMP_FLAGS"

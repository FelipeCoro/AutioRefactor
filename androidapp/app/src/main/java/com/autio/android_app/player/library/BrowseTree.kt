package com.autio.android_app.player.library

import android.content.Context
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaMetadataCompat
import com.autio.android_app.R
import com.autio.android_app.extensions.*

/**
 * Represents a tree of media that's used by [PlayerService.onLoadChildren].
 *
 * [BrowseTree] maps a media id (see: [MediaMetadataCompat.METADATA_KEY_MEDIA_ID]) to one (or
 * more) [MediaMetadataCompat] objects, which are children of that media id.
 *
 * For example, given the following conceptual tree:
 * root
 *  +-- Categories
 *  |    +-- Category_A
 *  |    |    +-- Story_1
 *  |    |    +-- Story_2
 *  ...
 *  +-- Bookmarks
 *  ...
 *
 *  Requesting `browseTree["root"]` would return a list that included "Bookmarks", "Favorites", and
 *  any other direct children. Taking the media ID of "Categories" ("Categories" in this example),
 *  `browseTree["Categories"]` would return a single item list "Category_A", and, finally,
 *  `browseTree["Category_A"]` would return "Story_1" and "Story_2". Since those are leaf nodes,
 *  requesting `browseTree["Story_1"]` would return null (there aren't any children of it).
 */
class BrowseTree(
    val context: Context,
    storiesSource: StorySource,
    val recentMediaId: String? = null
) {
    private val mediaIdToChildren =
        mutableMapOf<String, MutableList<MediaMetadataCompat>>()

    /**
     * Whether to allow unknown clients (not on allowed list)
     * to search on this [BrowseTree]
     */
    val searchableByUnknownCaller =
        true

    /**
     * The root's children are each category included in the
     * [BrowseTree] and the children of each category are the
     * stories with that category.
     */
    init {
        val rootList =
            mediaIdToChildren[BROWSABLE_ROOT]
                ?: mutableListOf()

        val allMetadata =
            MediaMetadataCompat.Builder()
                .apply {
                    id =
                        ALL_ROOT
                    title =
                        "All"
                    albumArtUri =
                        RESOURCE_ROOT_URI + context.resources.getResourceEntryName(
                            R.drawable.ic_notification
                        )
                    flag =
                        MediaItem.FLAG_BROWSABLE
                }
                .build()

        val historyMetadata =
            MediaMetadataCompat.Builder()
                .apply {
                    id =
                        HISTORY_ROOT
                    title =
                        "History"
                    albumArtUri =
                        RESOURCE_ROOT_URI + context.resources.getResourceEntryName(
                            R.drawable.ic_bordered_history
                        )
                    flag =
                        MediaItem.FLAG_BROWSABLE
                }
                .build()

        val bookmarksMetadata =
            MediaMetadataCompat.Builder()
                .apply {
                    id =
                        BOOKMARKS_ROOT
                    title =
                        "Bookmarks"
                    albumArtUri =
                        RESOURCE_ROOT_URI + context.resources.getResourceEntryName(
                            R.drawable.ic_bordered_bookmarks
                        )
                    flag =
                        MediaItem.FLAG_BROWSABLE
                }
                .build()

        rootList += allMetadata
        rootList += historyMetadata
        rootList += bookmarksMetadata
        mediaIdToChildren[BROWSABLE_ROOT] =
            rootList

        storiesSource.forEach { mediaItem ->
//            val bookmarkMediaId = mediaItem.album.urlEncoded
//            val bookmarkChildren = mediaIdToChildren[bookmarkMediaId] ?: buildBookmarkRoot(mediaItem)
//            bookmarkChildren += mediaItem

            // Add the first track of each album to the 'Recommended' category
//            if (mediaItem.trackNumber == 1L) {
//                val recommendedChildren = mediaIdToChildren[RECOMMENDED_ROOT]
//                    ?: mutableListOf()
//                recommendedChildren += mediaItem
//                mediaIdToChildren[RECOMMENDED_ROOT] = recommendedChildren
//            }

            // If this was recently played, add it to the recent root.
            if (mediaItem.id == recentMediaId) {
                mediaIdToChildren[RECENT_ROOT] =
                    mutableListOf(
                        mediaItem
                    )
            }
        }
    }

    /**
     * Provide access to the list of children with the `get` operator.
     * i.e.: `browseTree\[BROWSABLE_ROOT\]`
     */
    operator fun get(
        mediaId: String
    ) =
        mediaIdToChildren[mediaId]

    /**
     * Builds a node, under the root, that represents a collection, given
     * a [MediaMetadataCompat] object that's one of the songs on that album,
     * marking the item as [MediaItem.FLAG_BROWSABLE], since it will have child
     * node(s) AKA at least 1 song.
     */
    private fun buildBookmarkRoot(
        mediaItem: MediaMetadataCompat
    ): MutableList<MediaMetadataCompat> {
        val albumMetadata =
            MediaMetadataCompat.Builder()
                .apply {
                    id =
                        mediaItem.album.urlEncoded
                    title =
                        mediaItem.album
                    narrator =
                        mediaItem.artist
                    albumArt =
                        mediaItem.albumArt
                    albumArtUri =
                        mediaItem.albumArtUri.toString()
                    flag =
                        MediaItem.FLAG_BROWSABLE
                }
                .build()

        // Adds this album to the 'Bookmarks' category.
        val rootList =
            mediaIdToChildren[BOOKMARKS_ROOT]
                ?: mutableListOf()
        rootList += albumMetadata
        mediaIdToChildren[BOOKMARKS_ROOT] =
            rootList

        // Insert the album's root with an empty list for its children, and return the list.
        return mutableListOf<MediaMetadataCompat>().also {
            mediaIdToChildren[albumMetadata.id!!] =
                it
        }
    }

    companion object {
        const val BROWSABLE_ROOT =
            "/"
        const val EMPTY_ROOT =
            "@empty@"
        const val ALL_ROOT =
            "__ALL__"
        const val HISTORY_ROOT =
            "__RECOMMENDED__"
        const val BOOKMARKS_ROOT =
            "__CATEGORIES__"
        const val RECENT_ROOT =
            "__RECENT__"

        const val MEDIA_SEARCH_SUPPORTED =
            "android.media.browse.SEARCH_SUPPORTED"

        const val RESOURCE_ROOT_URI =
            "android.resource://com.autio.android_app.next/drawable"
    }
}
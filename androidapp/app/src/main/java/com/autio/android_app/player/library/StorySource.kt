package com.autio.android_app.player.library

import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import androidx.annotation.IntDef
import com.autio.android_app.extensions.*

/**
 * Interface used by [PlayerService] for looking up [MediaMetadataCompat] objects
 */
interface StorySource :
    Iterable<MediaMetadataCompat> {

    /**
     * Load data for this source
     */
    suspend fun load()

    /**
     * Performs a given action after this [StorySource] is ready for use
     *
     * @param performAction callback with boolean parameter triggered
     * when source is ready:
     * `true` -> source is prepared
     * `false` -> error occurred while preparing
     */
    fun whenReady(
        performAction: (Boolean) -> Unit
    ): Boolean

    fun search(
        query: String,
        extras: Bundle
    ): List<MediaMetadataCompat>
}

@IntDef(
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
)
@Retention(
    AnnotationRetention.SOURCE
)
annotation class State

/**
 * State indicating source was created, but not initialized yet
 */
const val STATE_CREATED =
    1

/**
 * State indicating initialization of source is in progress
 */
const val STATE_INITIALIZING =
    2

/**
 * State indicating source has been initialized and is ready for use
 */
const val STATE_INITIALIZED =
    3

/**
 * State indicating an error occurred
 */
const val STATE_ERROR =
    4

/**
 * Base class for stories sources
 */
abstract class AbstractStorySource :
    StorySource {
    @State
    var state =
        STATE_CREATED
        set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(
                    onReadyListeners
                ) {
                    field =
                        value
                    onReadyListeners.forEach { listener ->
                        listener(
                            state == STATE_INITIALIZED
                        )
                    }
                }
            } else {
                field =
                    value
            }
        }

    private val onReadyListeners =
        mutableListOf<(Boolean) -> Unit>()

    /**
     * Performs an action when this [StorySource] is ready
     */
    override fun whenReady(
        performAction: (Boolean) -> Unit
    ) =
        when (state) {
            STATE_CREATED, STATE_INITIALIZING -> {
                onReadyListeners += performAction
                false
            }
            else -> {
                performAction(
                    state != STATE_ERROR
                )
                true
            }
        }

    /**
     * Handles searching a [StorySource] from a focused voice search,
     * often from Google Assistant
     */
    override fun search(
        query: String,
        extras: Bundle
    ): List<MediaMetadataCompat> {
        val focusSearchResult =
            when (extras.getString(
                MediaStore.EXTRA_MEDIA_FOCUS
            )) {
                MediaStore.Audio.Genres.ENTRY_CONTENT_TYPE -> {
                    val category =
                        extras.getString(
                            EXTRA_MEDIA_GENRE
                        )
                    filter { story ->
                        story.title == category
                    }
                }
                MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE -> {
                    val artist =
                        extras.getString(
                            MediaStore.EXTRA_MEDIA_ARTIST
                        )
                    filter { story ->
                        (story.artist == artist || story.author == artist)
                    }
                }
                MediaStore.Audio.Media.ENTRY_CONTENT_TYPE -> {
                    val title =
                        extras.getString(
                            MediaStore.EXTRA_MEDIA_TITLE
                        )
                    val artist =
                        extras.getString(
                            MediaStore.EXTRA_MEDIA_ARTIST
                        )
                    filter { story ->
                        (story.artist == artist || story.author == artist) && story.title == title
                    }
                }
                else -> {
                    emptyList()
                }
            }

        // If there weren't any results for focused search, try to
        // find matches given the 'query' provided, searching against
        // a few of the fields
        if (focusSearchResult.isEmpty()) {
            return if (query.isNotBlank()) {
                filter { story ->
                    story.title.containsCaseInsensitive(
                        query
                    ) || story.title.containsCaseInsensitive(
                        query
                    )
                }
            } else {
                return shuffled()
            }
        } else {
            return focusSearchResult
        }
    }

    private val EXTRA_MEDIA_GENRE
        get() = MediaStore.EXTRA_MEDIA_GENRE
}

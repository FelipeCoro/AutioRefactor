package com.autio.android_app.player.library

import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import com.autio.android_app.data.database.DataBase
import com.autio.android_app.extensions.albumArtUri
import com.autio.android_app.extensions.displayIconUri
import com.autio.android_app.extensions.from
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.ui.di.coroutines.IoDispatcher
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

/**
 * Source of [MediaMetadataCompat] objects created from a basic
 * JSON stream
 */
class JsonSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: DataBase,
    @IoDispatcher private val coroutineDipatcher: CoroutineDispatcher
) : AbstractStorySource() {

    private var catalog: List<MediaMetadataCompat> = emptyList()

    init {
        state = STATE_INITIALIZING
    }

    override fun iterator() = catalog.iterator()

    override suspend fun load() {
        updateCatalog()?.let { updatedCatalog ->
            catalog = updatedCatalog
            state = STATE_INITIALIZED
        } ?: run {
            catalog = emptyList()
            state = STATE_ERROR
        }
    }

    private suspend fun updateCatalog(): List<MediaMetadataCompat>? {
        return withContext(coroutineDipatcher) {
            val storyCat = try {
                downloadJson()
            } catch (e: IOException) {
                return@withContext null
            }

            val mediaMetadataCompats = mapStories(storyCat)
            // Add description keys to be used by the ExoPlayer MediaSession extension when
            // announcing metadata changes.
            mediaMetadataCompats.forEach {
                it.description.extras?.putAll(it.bundle)
            }
            mediaMetadataCompats
        }
    }

    private fun mapStories(storyCat: JsonCatalog) = storyCat.stories.map { story ->
        val jsonImageUri = Uri.parse(
            story.imageUrl
        )
        val imageUri = AlbumArtContentProvider.mapUri(jsonImageUri)

        MediaMetadataCompat.Builder().from(story).apply {
            displayIconUri = imageUri.toString() // Used by ExoPlayer and Notification
            albumArtUri = imageUri.toString()
            // Keep the original artwork URI for being included in Cast metadata object.
            putString(ORIGINAL_ARTWORK_URI_KEY, jsonImageUri.toString())
        }.build()
    }.toList()

    /**
     * Attempts to download a catalog from a given Uri.
     *
     * @return The catalog downloaded, or an empty catalog if an error occurred.
     */
    private suspend fun downloadJson(): JsonCatalog {
        val stories = database.storyDao().readStories()
        val jsonArray = JSONArray(
            Gson().toJson(stories)
        )
        val jsonObject = JSONObject()
        jsonObject.put("stories", jsonArray)
        return Gson().fromJson(jsonObject.toString(), JsonCatalog::class.java)
    }

    companion object {
        private val TAG = JsonSource::class.simpleName
        const val ORIGINAL_ARTWORK_URI_KEY = "com.autio.android_app.JSON_ARTWORK_URI"
    }
}

/**
 * Wrapper object for the JSON in order to be process easily by GSON
 */
class JsonCatalog {
    var stories: List<Story> = ArrayList()
}

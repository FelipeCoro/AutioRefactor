package com.autio.android_app.data.database.entities

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.autio.android_app.data.api.model.story.StoryDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import java.net.URL

@Entity(tableName = "downloaded_stories")
data class DownloadedStoryEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val lat: Double,
    val lon: Double,
    val range: Int,
    val image: String? = null,
    val recordPath: String,
    val duration: Int,
    val publishedDate: Int,
    val modifiedDate: Int,
    val narrator: String,
    val author: String,
    val state: String?,
    @Embedded(
        prefix = "category_"
    )
    val category: CategoryEntity,
    val isLiked: Boolean?,
    val isBookmarked: Boolean?,
    val listenedAt: String?,
    val listenedAtLeast30Secs: Boolean? = null
) : Serializable {
    companion object {
        suspend fun fromStory(context: Context, storyDto: StoryDto): DownloadedStoryEntity? {
            val imagePathUri: Uri? =
                try {
                    //TODO (move this network call)
                    withContext(Dispatchers.IO) {
                        val url = URL(storyDto.imageUrl)
                        val connection = url.openConnection()
                        connection.connect()
                        val input = BufferedInputStream(url.openStream(), 8192)
                        val filename = storyDto.title.replace(" ", "_") + "art.jpg"
                        val imagesDir = File(context.filesDir, "images")
                        if (!imagesDir.exists()) {
                            imagesDir.mkdir()
                        }
                        val file = File(imagesDir, filename)
                        val output = FileOutputStream(file)
                        val data = ByteArray(1024)
                        var count = input.read(data)
                        while (count > 0) {
                            output.write(data, 0, count)
                            count = input.read(data)
                        }
                        output.flush()
                        output.close()
                        input.close()
                        Uri.parse(file.path)
                    }
                } catch (e: Exception) {
                    Log.e("DownloadStory", "errorOnObject: ", e)
                    return null
                }
            val recordPathUri =
                try {
                    //TODO (change this network call)
                    withContext(Dispatchers.IO) {
                        val url = URL(storyDto.recordUrl)
                        val connection = url.openConnection()
                        connection.connect()
                        val input = BufferedInputStream(url.openStream(), 8192)
                        val filename = storyDto.title.replace(" ", "_") + ".mp3"
                        val audioDir = File(context.filesDir, "audio")
                        if (!audioDir.exists()) {
                            audioDir.mkdir()
                        }
                        val file = File(audioDir, filename)
                        val output = FileOutputStream(file)
                        val data = ByteArray(1024)
                        var count = input.read(data)
                        while (count > 0) {
                            output.write(data, 0, count)
                            count = input.read(data)
                        }

                        output.flush()
                        output.close()
                        input.close()
                        Uri.parse(file.path)
                    }
                } catch (e: Exception) {
                    Log.e("DownloadStory", "errorOnObject: ", e)
                    return null
                }

            return DownloadedStoryEntity(
                id = storyDto.id,
                title = storyDto.title,
                description = storyDto.description,
                lat = storyDto.lat,
                lon = storyDto.lon,
                range = storyDto.range,
                image = imagePathUri.toString(),
                recordPath = recordPathUri.toString(),
                duration = storyDto.duration,
                publishedDate = storyDto.publishedDate,
                modifiedDate = storyDto.modifiedDate,
                narrator = storyDto.narrator,
                author = storyDto.author,
                state = storyDto.state,
                category = CategoryEntity(
                    storyDto.category!!.id,
                    storyDto.category!!.title,
                    storyDto.category!!.firebaseId,
                    storyDto.category!!.order
                ),
                isLiked = storyDto.isLiked,
                isBookmarked = storyDto.isBookmarked,
                listenedAt = storyDto.listenedAt,
                listenedAtLeast30Secs = storyDto.listenedAtLeast30Secs
            )
        }
    }
}

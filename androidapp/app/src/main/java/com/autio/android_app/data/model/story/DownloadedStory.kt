package com.autio.android_app.data.model.story

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import java.net.URL

@Entity(
    tableName = "downloaded_stories"
)
data class DownloadedStory(
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
    val category: Category,
    val isLiked: Boolean?,
    val isBookmarked: Boolean?,
    val listenedAt: String?,
    val listenedAtLeast30Secs: Boolean? = null
) : Serializable {
    override fun equals(
        other: Any?
    ): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DownloadedStory

        if (id != other.id) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (lat != other.lat) return false
        if (lon != other.lon) return false
        if (range != other.range) return false
        if (image != null) {
            if (other.image == null) return false
            if (image != other.image) return false
        } else if (other.image != null) return false
        if (recordPath != other.recordPath) return false
        if (duration != other.duration) return false
        if (publishedDate != other.publishedDate) return false
        if (modifiedDate != other.modifiedDate) return false
        if (narrator != other.narrator) return false
        if (author != other.author) return false
        if (state != other.state) return false
        if (category != other.category) return false

        return true
    }

    override fun hashCode(): Int {
        var result =
            id.hashCode()
        result =
            31 * result + title.hashCode()
        result =
            31 * result + description.hashCode()
        result =
            31 * result + lat.hashCode()
        result =
            31 * result + lon.hashCode()
        result =
            31 * result + range
        result =
            31 * result + (image?.hashCode()
                ?: 0)
        result =
            31 * result + recordPath.hashCode()
        result =
            31 * result + duration
        result =
            31 * result + publishedDate
        result =
            31 * result + modifiedDate
        result =
            31 * result + narrator.hashCode()
        result =
            31 * result + author.hashCode()
        result =
            31 * result + (state?.hashCode()
                ?: 0)
        result =
            31 * result + category.hashCode()
        return result
    }

    companion object {
        suspend fun fromStory(
            context: Context,
            story: Story
        ): DownloadedStory? {
            val imagePathUri: Uri? =
                try {
                    withContext(
                        Dispatchers.IO
                    ) {
                        val url =
                            URL(story.imageUrl)
                        val connection =
                            url.openConnection()
                        connection.connect()
                        val input =
                            BufferedInputStream(
                                url.openStream(),
                                8192
                            )
                        val filename =
                            story.title.replace(
                                " ",
                                "_"
                            ) + "art.jpg"
                        val imagesDir =
                            File(
                                context.filesDir,
                                "images"
                            )
                        if (!imagesDir.exists()) {
                            imagesDir.mkdir()
                        }
                        val file =
                            File(
                                imagesDir,
                                filename
                            )
                        val output =
                            FileOutputStream(
                                file
                            )
                        val data =
                            ByteArray(
                                1024
                            )
                        var count =
                            input.read(
                                data
                            )
                        while (count > 0) {
                            output.write(
                                data,
                                0,
                                count
                            )
                            count =
                                input.read(
                                    data
                                )
                        }

                        output.flush()
                        output.close()
                        input.close()
                        Uri.parse(
                            file.path
                        )
                    }
                } catch (e: Exception) {
                    Log.e(
                        "DownloadStory",
                        "errorOnObject: ",
                        e
                    )
                    return null
                }
            val recordPathUri =
                try {
                    withContext(
                        Dispatchers.IO
                    ) {
                        val url =
                            URL(story.recordUrl)
                        val connection =
                            url.openConnection()
                        connection.connect()
                        val input =
                            BufferedInputStream(
                                url.openStream(),
                                8192
                            )
                        val filename =
                            story.title.replace(
                                " ",
                                "_"
                            ) + ".mp3"
                        val audioDir =
                            File(
                                context.filesDir,
                                "audio"
                            )
                        if (!audioDir.exists()) {
                            audioDir.mkdir()
                        }
                        val file =
                            File(
                                audioDir,
                                filename
                            )
                        val output =
                            FileOutputStream(
                                file
                            )
                        val data =
                            ByteArray(
                                1024
                            )
                        var count =
                            input.read(
                                data
                            )
                        while (count > 0) {
                            output.write(
                                data,
                                0,
                                count
                            )
                            count =
                                input.read(
                                    data
                                )
                        }

                        output.flush()
                        output.close()
                        input.close()
                        Uri.parse(
                            file.path
                        )
                    }
                } catch (e: Exception) {
                    Log.e(
                        "DownloadStory",
                        "errorOnObject: ",
                        e
                    )
                    return null
                }

            return DownloadedStory(
                id = story.id,
                title = story.title,
                description = story.description,
                lat = story.lat,
                lon = story.lon,
                range = story.range,
                image = imagePathUri.toString(),
                recordPath = recordPathUri.toString(),
                duration = story.duration,
                publishedDate = story.publishedDate,
                modifiedDate = story.modifiedDate,
                narrator = story.narrator,
                author = story.author,
                state = story.state,
                category = Category(
                    story.category!!.id,
                    story.category!!.title,
                    story.category!!.firebaseId,
                    story.category!!.order
                ),
                isLiked = story.isLiked,
                isBookmarked = story.isBookmarked,
                listenedAt = story.listenedAt,
                listenedAtLeast30Secs = story.listenedAtLeast30Secs
            )
        }
    }
}
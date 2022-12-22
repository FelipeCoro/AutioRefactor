package com.autio.android_app.data.model.story

import androidx.room.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Entity(
    tableName = "stories",
    indices = [Index(
        value = ["id"],
        unique = true
    )]
)
data class Story(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    @SerializedName(
        "latitude"
    )
    val lat: Double,
    @SerializedName(
        "longitude"
    )
    val lon: Double,
    val range: Int,
    @SerializedName(
        "imageURL"
    )
    val imageUrl: String?,
    @SerializedName(
        "recordURL"
    )
    val recordUrl: String?,
    val duration: Int,
    @SerializedName(
        "dateAdded"
    )
    val publishedDate: Int,
    @SerializedName(
        "dateModified"
    )
    val modifiedDate: Int,
    @SerializedName(
        "narratorName"
    ) val narrator: String,
    @SerializedName(
        "authorName"
    ) val author: String,
    val state: String?,
    @Embedded(
        prefix = "category_"
    )
    val category: CategoryResponse,
) : Serializable {
    data class CategoryResponse(
        @PrimaryKey
        val id: String,
        val title: String?,
        val order: Int?,
    )

    companion object {
        fun DocumentSnapshot.toStory(): Story? {
            return try {
                val title =
                    getString(
                        "title"
                    )
                        ?: ""
                val description =
                    getString(
                        "description"
                    )
                        ?: ""
                val lat =
                    getDouble(
                        "latitude"
                    )
                        ?: 0.0
                val lon =
                    getDouble(
                        "longitude"
                    )
                        ?: 0.0
                val range =
                    getLong(
                        "rangeInMeters"
                    )
                        ?: Long.MIN_VALUE
                val imageUrl =
                    getString(
                        "imageUrl"
                    )
                val recordUrl =
                    getString(
                        "recordUrl"
                    )
                val duration = getLong("durationInSeconds") ?: 0
                val publishedDate = getString("dateAdded")?.let {
                    val l = LocalDate.parse(it, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                    l.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond.toInt()
                } ?: 0
                val modifiedDate = getLong("dateModifiedTimestamp") ?: 0
                val narratorName = getString("narratorName") ?: ""
                val authorName = getString("authorName") ?: ""
                val categoryId = getString("categoryId") ?: ""
                Story(
                    this.id,
                    title,
                    description,
                    lat,
                    lon,
                    range.toInt(),
                    imageUrl,
                    recordUrl,
                    duration.toInt(),
                    publishedDate,
                    modifiedDate.toInt(),
                    narratorName,
                    authorName,
                    "",
                    CategoryResponse(
                        categoryId,
                        null,
                        null,
                    )
                )
            } catch (e: Exception) {
                // TODO: Implement crashlytics
                null
            }
        }
    }
}

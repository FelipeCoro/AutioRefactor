package com.autio.android_app.data.model.story

import androidx.room.*
import com.google.firebase.database.PropertyName
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Base class which the whole app works around
 *
 * Stories are fetched from an API endpoint and then stored in
 * a room database for caching purposes
 * Another database table was created for downloaded stories in
 * device
 */
@Entity(
    tableName = "stories",
    indices = [Index(
        value = ["id"],
        unique = true
    )]
)
data class Story(
    @PrimaryKey
    var id: String = "",
    @get:PropertyName(
        "hhId"
    )
    @set:PropertyName(
        "hhId"
    )
    @SerializedName(
        "hhId"
    )
    var originalId: Int = 0,
    val title: String = "",
    val description: String = "",
    @get:PropertyName(
        "latitude"
    )
    @set:PropertyName(
        "latitude"
    )
    @SerializedName(
        "latitude"
    )
    var lat: Double = 0.0,
    @get:PropertyName(
        "longitude"
    )
    @set:PropertyName(
        "longitude"
    )
    @SerializedName(
        "longitude"
    )
    var lon: Double = 0.0,
    @get:PropertyName(
        "rangeInMeters"
    )
    @set:PropertyName(
        "rangeInMeters"
    )
    var range: Int = 0,
    @PropertyName(
        "imageURL"
    )
    @SerializedName(
        "imageURL"
    )
    val imageUrl: String = "",
    @PropertyName(
        "recordURL"
    )
    @SerializedName(
        "recordURL"
    )
    val recordUrl: String = "",
    @get:PropertyName(
        "durationInSeconds"
    )
    @set:PropertyName(
        "durationInSeconds"
    )
    var duration: Int = 0,
//    @get:PropertyName(
//        "dateAdded"
//    ) @set:PropertyName(
//        "dateAdded"
//    )
    @SerializedName(
        "dateAdded"
    )
    var publishedDate: Int = 0,
    @get:PropertyName(
        "dateModifiedTimestamp"
    ) @set:PropertyName(
        "dateModifiedTimestamp"
    )
    @SerializedName(
        "dateModified"
    )
    var modifiedDate: Int = 0,
    @get:PropertyName(
        "narratorName"
    ) @set:PropertyName(
        "narratorName"
    )
    @SerializedName(
        "narratorName"
    ) var narrator: String = "",
    @get:PropertyName(
        "authorName"
    ) @set:PropertyName(
        "authorName"
    )
    @SerializedName(
        "authorName"
    ) var author: String = "",
    val state: String = "",
    @Embedded(
        prefix = "category_"
    )
    var category: CategoryResponse? = null,
    val isLiked: Boolean? = null,
    val isBookmarked: Boolean? = null,
    val isDownloaded: Boolean? = null,
    val listenedAt: String? = null
) : Serializable

data class CategoryResponse(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val order: Int = 0
) : Serializable

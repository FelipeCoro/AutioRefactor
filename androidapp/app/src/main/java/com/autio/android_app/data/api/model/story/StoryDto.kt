package com.autio.android_app.data.api.model.story

import androidx.room.*
import com.autio.android_app.ui.stories.models.Category
import kotlinx.serialization.SerialName
import java.io.Serializable

/**
 * Base class which the whole app works around
 *
 * Stories are fetched from an API endpoint and then stored in
 * a room database for caching purposes
 * Another database table was created for downloaded stories in
 * device
 */
@kotlinx.serialization.Serializable
data class StoryDto(

    var id: Int = 0,
    val title: String = "",
    val description: String = "",
    @SerialName("latitude")
    var lat: Double = 0.0,
    @SerialName("longitude")
    var lon: Double = 0.0,
    var range: Int = 0,
    @SerialName("imageURL")
    val imageUrl: String = "",
    @SerialName("recordURL")
    val recordUrl: String = "",
    val authorId:Int = 0,
    var duration: Int = 0,
    @Embedded()
    var category: Category? = null,
    @SerialName("dateAdded")
    var publishedDate: Int = 0,
    @SerialName("dateModified")
    var modifiedDate: Int = 0,
    var imageAttribution:String? ="",
    @SerialName("narratorName")
    var narrator: String = "",
    @SerialName("authorName")
    var author: String = "",
    val state: String = "",
    val countryCode: String = "",
    @SerialName("private_id")
    val privateId:String ="",
    val isLiked: Boolean? = false,
    val isBookmarked: Boolean? = false,
    val isDownloaded: Boolean? = false,
    val listenedAt: String? = null,
    val listenedAtLeast30Secs: Boolean? = false,

) : Serializable

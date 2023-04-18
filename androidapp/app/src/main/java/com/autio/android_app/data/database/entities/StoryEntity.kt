package com.autio.android_app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.autio.android_app.ui.stories.models.Category

@Entity(tableName = "story")
data class StoryEntity(
    @PrimaryKey
    var id: Int = 0,
    val title: String = "",
    val description: String = "",
    var lat: Double = 0.0,
    var lon: Double = 0.0,
    var range: Int = 0,
    val imageUrl: String = "",
    val recordUrl: String = "",
    val authorId: Int = 0,
    var duration: Int = 0,
    var category: String = "",
    var publishedDate: Int = 0,
    var modifiedDate: Int = 0,
    var imageAttribution: String = "",
    var narrator: String = "",
    var author: String = "",
    val state: String = "",
    val countryCode: String = "",
    val privateId: String = "",
    val isLiked: Boolean? = false,
    var isBookmarked: Boolean? = false,
    var isDownloaded: Boolean? = false,
    val listenedAt: String? = null,
    val listenedAtLeast30Secs: Boolean? = false,
)

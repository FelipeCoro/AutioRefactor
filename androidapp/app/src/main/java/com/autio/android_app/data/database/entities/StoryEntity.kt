package com.autio.android_app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.autio.android_app.ui.stories.models.Category

@Entity(tableName = "story")
data class StoryEntity(
    @PrimaryKey
    val id: Int = 0,
    val fbid: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val range: Int = 0,
    val state: String = "",
    val countryCode: String = "",
    val title: String = "",
    val description: String = "",
    val narratorName: String = "",
    val authorName: String = "",
    val category: String = "",

    val imageUrl: String = "",
    val recordUrl: String = "",
    var duration: Int = 0,
    val isLiked: Boolean? = false,
    val listenedAt: String? = null, //TODO CHECK THIS Type
    val modifiedDate: Int = 0, //TODO CHECK THIS Type
    val isBookmarked: Boolean? = false,
    val listenedAtLeast30Secs: Boolean? = false,
    val isDownloaded: Boolean? = false,
    var publishedDate: Int = 0,
    )

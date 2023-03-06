package com.autio.android_app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class MapPointEntity(
    @PrimaryKey
    val id: String = "",
    val firebaseId: Int = 0,
    var publishedAt: Int = 0,
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val range: Int = 0,
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val recordUrl: String = "",
    var duration: Int = 0,
    val state: String = "",
    val countryCode: String = "",
    val originalId: Int = 0,
    val isLiked: Int = 0,
    val listenedAt: String = "", //TODO CHECK THIS Type
    val modifiedDate: Int = 0, //TODO CHECK THIS Type
    val isBookmarked: Boolean? = false,
    val listenedAtLeast30Secs: Boolean? = false,
    val  isDownloaded: Boolean? = false,
)



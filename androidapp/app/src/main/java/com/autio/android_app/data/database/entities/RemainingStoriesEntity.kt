package com.autio.android_app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remainingStories")
data class RemainingStoriesEntity(
    @PrimaryKey
    val id: Int = 0,
    val remainingStories: Int = 5
)

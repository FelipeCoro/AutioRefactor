package com.autio.android_app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HistoryEntity(
    @PrimaryKey
    val storyId: Int,
    val playedAt: String,
    val isBookmarked: Boolean = false //TODO(Check initialization)
)

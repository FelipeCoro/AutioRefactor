package com.autio.android_app.data.database.entities

import androidx.room.Entity

@Entity
data class HistoryEntity(
    val storyId: String,
    val playedAt: String
)

package com.autio.android_app.data.model.category

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index(
        value = ["id"],
        unique = true
    )]
)
data class StoryCategory constructor(
    @PrimaryKey
    val id: Int,
    val order: Int,
    val title: String
)
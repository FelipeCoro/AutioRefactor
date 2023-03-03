package com.autio.android_app.data.api.model.story

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
data class StoryCategoryDto constructor(
    @PrimaryKey
    val id: Int,
    val order: Int,
    val title: String
)

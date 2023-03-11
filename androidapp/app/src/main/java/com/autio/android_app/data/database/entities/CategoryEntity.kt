package com.autio.android_app.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@kotlinx.serialization.Serializable
@Entity(tableName = "user_categories")
data class CategoryEntity(
    @PrimaryKey
    var id: Int = 0,
    var title: String = "",
    var order: Int = 0
) : Serializable

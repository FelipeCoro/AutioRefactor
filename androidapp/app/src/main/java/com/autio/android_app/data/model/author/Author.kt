package com.autio.android_app.data.model.author

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "authors",
    indices = [Index(
        value = ["id"],
        unique = true
    )]
)
data class Author constructor(
    @PrimaryKey val id: Int,
    val name: String,
    @SerializedName(
        "bio"
    ) val biography: String,
    @SerializedName(
        "website"
    ) val url: String,
    @SerializedName(
        "profile_image_url"
    ) val imageUrl: String
)
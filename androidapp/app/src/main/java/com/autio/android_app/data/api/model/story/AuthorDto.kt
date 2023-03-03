package com.autio.android_app.data.api.model.story

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
data class AuthorDto constructor(
    @PrimaryKey val id: Int,
    val name: String,
    @SerializedName(
        "bio"
    ) val biography: String,
    @SerializedName(
        "website"
    ) val url: String?,
    @SerializedName(
        "profile_image_url"
    ) val imageUrl: String?
)

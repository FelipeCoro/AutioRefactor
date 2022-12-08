package com.autio.android_app.data.model.narrator

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "narrators",
    indices = [Index(
        value = ["id"],
        unique = true
    )]
)
data class Narrator constructor(
    @PrimaryKey
    val id: Int,
    val name: String,
    @SerializedName(
        "bio"
    )
    val biography: String,
    @SerializedName(
        "website"
    )
    val url: String,
    @SerializedName(
        "profile_image_url"
    )
    val imageUrl: String
)
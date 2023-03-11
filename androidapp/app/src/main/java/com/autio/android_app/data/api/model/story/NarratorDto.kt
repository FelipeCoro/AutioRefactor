package com.autio.android_app.data.api.model.story

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class NarratorDto constructor(
    @PrimaryKey
    val id: Int,
    val name: String,
    @SerialName("bio")
    val biography: String,
    @SerialName("website")
    val url: String?,
    @SerialName("profile_image_url")
    val imageUrl: String?
)

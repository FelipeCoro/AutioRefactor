package com.autio.android_app.data.api.model.story

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaysDto(
    @SerialName("firebase_story_identifier")
    val firebaseId: Int? = null,
    @SerialName("was_present")
    val wasPresent: Boolean? = null,
    @SerialName("autoplay_enabled")
    val autoPlay: Boolean? = null,
    @SerialName("on_disk")
    val isDownloaded: Boolean? = null,
    val connection: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

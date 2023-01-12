package com.autio.android_app.data.model.plays

import com.google.gson.annotations.SerializedName

data class PlaysDto(
    @SerializedName(
        "firebase_story_identifier"
    )
    val firebaseId: String? = null,
    @SerializedName("was_present")
    val wasPresent: Boolean? = null,
    @SerializedName("autoplay_enabled")
    val autoPlay : Boolean? = null,
    @SerializedName("on_disk")
    val isDownloaded : Boolean? = null,
    val connection: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
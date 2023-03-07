package com.autio.android_app.data.api.model.story

import com.google.gson.annotations.SerializedName

data class PlaysDto(
    @SerializedName(
        "firebase_story_identifier"
    )
    val firebaseId: Int? = null, //TODO(Check how this changes)
    @SerializedName(
        "was_present"
    )
    val wasPresent: Boolean? = null,
    @SerializedName(
        "autoplay_enabled"
    )
    val autoPlay: Boolean? = null,
    @SerializedName(
        "on_disk"
    )
    val isDownloaded: Boolean? = null,
    val connection: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

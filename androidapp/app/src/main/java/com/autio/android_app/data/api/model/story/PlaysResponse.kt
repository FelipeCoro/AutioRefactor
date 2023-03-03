package com.autio.android_app.data.api.model.story

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PlaysResponse(
    val play:Play,
    @SerializedName(
        "plays_remaining"
    )
    val playsRemaining: Int
) : Serializable

data class Play(
    val id: Int,
    @SerializedName(
        "firebase_story_identifier"
    )
    val firebaseId: String
)

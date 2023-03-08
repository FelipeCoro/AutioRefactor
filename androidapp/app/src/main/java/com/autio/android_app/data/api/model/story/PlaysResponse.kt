package com.autio.android_app.data.api.model.story

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import java.io.Serializable
@kotlinx.serialization.Serializable
data class PlaysResponse(
    val play:Play,
    @SerialName(
        "plays_remaining"
    )
    val playsRemaining: Int
) : Serializable

@kotlinx.serialization.Serializable
data class Play(
    val id: Int,
    @SerialName(
        "firebase_story_identifier"
    )
    val firebaseId: String
)

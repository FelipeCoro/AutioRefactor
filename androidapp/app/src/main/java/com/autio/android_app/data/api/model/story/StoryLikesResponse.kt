package com.autio.android_app.data.api.model.story

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StoryLikesResponse(
    @SerialName(
        "likes_count"
    )
    val likes: Int
)

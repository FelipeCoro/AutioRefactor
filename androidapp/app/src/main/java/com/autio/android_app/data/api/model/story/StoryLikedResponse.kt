package com.autio.android_app.data.api.model.story

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StoryLikedResponse(
    @SerialName(
        "is_liked"
    )
    val isLiked: Boolean
)

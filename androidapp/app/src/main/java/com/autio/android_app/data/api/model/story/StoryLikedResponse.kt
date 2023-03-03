package com.autio.android_app.data.api.model.story

import com.google.gson.annotations.SerializedName

data class StoryLikedResponse(
    @SerializedName(
        "is_liked"
    )
    val isLiked: Boolean
)

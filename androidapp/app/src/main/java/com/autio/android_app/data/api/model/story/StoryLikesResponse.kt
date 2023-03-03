package com.autio.android_app.data.api.model.story

import com.google.gson.annotations.SerializedName

data class StoryLikesResponse(
    @SerializedName(
        "likes_count"
    )
    val likes: Int
)

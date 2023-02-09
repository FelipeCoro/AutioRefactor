package com.autio.android_app.data.model.api_response

import com.google.gson.annotations.SerializedName

data class StoryLikesResponse(
    @SerializedName(
        "likes_count"
    )
    val likes: Int
)
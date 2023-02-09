package com.autio.android_app.data.model.api_response

import com.google.gson.annotations.SerializedName

data class StoryLikedResponse(
    @SerializedName(
        "is_liked"
    )
    val isLiked: Boolean
)
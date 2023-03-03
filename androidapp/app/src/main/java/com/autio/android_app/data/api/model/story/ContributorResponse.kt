package com.autio.android_app.data.api.model.story

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ContributorResponse(
    @SerializedName(
        "current_page"
    )
    val currentPage: Int,
    val data: List<ContributorStoryData>,
    @SerializedName(
        "total"
    )
    val totalPages: Int
) : Serializable

data class ContributorStoryData(
    val id: Int,
    @SerializedName(
        "firebase_identifier"
    )
    val firebaseId: String,
    val title: String,
    @SerializedName(
        "narration_url"
    )
    val narrationUrl: String?
) : Serializable

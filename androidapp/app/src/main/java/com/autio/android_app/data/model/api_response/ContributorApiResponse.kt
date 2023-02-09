package com.autio.android_app.data.model.api_response

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ContributorApiResponse(
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
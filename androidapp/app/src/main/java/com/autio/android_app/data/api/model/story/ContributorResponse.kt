package com.autio.android_app.data.api.model.story

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import java.io.Serializable
@kotlinx.serialization.Serializable
data class ContributorResponse(
    @SerialName(
        "current_page"
    )
    val currentPage: Int,
    val data: List<ContributorStoryData>,
    @SerialName(
        "total"
    )
    val totalPages: Int
) : Serializable

@kotlinx.serialization.Serializable
data class ContributorStoryData(
    val id: Int,
    @SerialName(
        "firebase_identifier"
    )
    val firebaseId: String?,
    val title: String,
    @SerialName(
        "narration_url"
    )
    val narrationUrl: String?
) : Serializable

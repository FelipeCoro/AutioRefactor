package com.autio.android_app.ui.stories.models

import android.os.Parcelable
import com.autio.android_app.data.api.model.story.ContributorStoryData
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contributor(
    val currentPage: Int,
    val data: List<ContributorStoryData>,
    val totalPages: Int
):Parcelable

@Parcelize
data class ContributorStoryData(
    val id: Int,
    val firebaseId: String?,
    val title: String,
    val narrationUrl: String?
):Parcelable

package com.autio.android_app.ui.stories.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Story(
    val id: Int = 0,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val range: Int = 0,
    val state: String = "",
    val countryCode: String = "",
    val title: String = "",
    val description: String = "",
    val narrator: String = "",
    val author: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val recordUrl: String = "",
    var duration: Int = 0,
    var isLiked: Boolean? = false,
    val listenedAt: String? = "", //TODO CHECK THIS Type
    val modifiedDate: Int = 0, //TODO CHECK THIS Type
    val isBookmarked: Boolean? = false,
    val listenedAtLeast30Secs: Boolean? = false,
    val isDownloaded: Boolean? = false,
    var publishedDate: Int = 0, //TODO (Dates should be Long not Int)


) : Parcelable

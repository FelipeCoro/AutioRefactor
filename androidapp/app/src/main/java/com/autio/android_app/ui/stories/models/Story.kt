package com.autio.android_app.ui.stories.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Story(
    var id: String = "",
    val originalId: Int = 0,
    val title: String = "",
    val description: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val range: Int = 0,
    val imageUrl: String = "",
    val recordUrl: String = "",
    var duration: Int = 0,
    //TODO (Dates should be Long not Int)
    var publishedDate: Int = 0,
    var modifiedDate: Int = 0,
    var narrator: String = "",
    var author: String = "",
    val state: String = "",
    val countryCode: String = "",
    var category: Category? = null,
    val isLiked: Boolean? = null,
    val isBookmarked: Boolean? = null,
    val isDownloaded: Boolean? = null,
    val listenedAt: String? = null,
    val listenedAtLeast30Secs: Boolean? = null
) : Parcelable

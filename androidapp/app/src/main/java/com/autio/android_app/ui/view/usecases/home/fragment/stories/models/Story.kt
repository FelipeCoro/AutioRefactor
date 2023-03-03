package com.autio.android_app.ui.view.usecases.home.fragment.stories.models

@kotlinx.serialization.Serializable
data class Story(
    val id: String = "",
    val originalId: Int = 0,
    val title: String = "",
    val description: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val range: Int = 0,
    val imageUrl: String = "",
    val recordUrl: String = "",
    var duration: Int = 0,
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
)

package com.autio.android_app.data.database.entities

import com.autio.android_app.ui.stories.models.Category

data class MapPoint(
    val id: String = "",
    val firebaseId: Int = 0,
    var publishedAt: Int = 0,
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val range: Int = 0,
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val recordUrl: String = "",
    var duration: Int = 0,
    val state: String = "",
    val countryCode: String = "",
)



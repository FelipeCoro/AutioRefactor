package com.autio.android_app.domain.mappers

import com.autio.android_app.data.database.entities.CategoryEntity
import com.autio.android_app.data.database.entities.HistoryEntity
import com.autio.android_app.data.database.entities.MapPoint
import com.autio.android_app.ui.stories.models.Category
import com.autio.android_app.ui.stories.models.History
import com.autio.android_app.ui.stories.models.Story

fun CategoryEntity.toModel(): Category {
    return Category(id, firebaseId, title, order)
}

fun MapPoint.toModel(): Story {
    return Story(
        id,
        lat = lat,
        lon = lon,
        range = range,
        publishedDate = publishedAt,
        state = state,
        countryCode = countryCode
    )
}

fun History.toEntity(): HistoryEntity {
    return HistoryEntity(
        storyId, playedAt
    )
}
package com.autio.android_app.data.api.model.history

import com.autio.android_app.data.api.model.story.StoryDto

@kotlinx.serialization.Serializable
data class HistoryDto(
    val data: List<StoryDto>,
    val info: Info
)
@kotlinx.serialization.Serializable
data class Info(
    val current_page: Int,
    val records_per_page: Int,
    val total_pages: Int,
    val total_records: Int
)

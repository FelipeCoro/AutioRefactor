package com.autio.android_app.data.api.model.bookmarks

import kotlinx.serialization.Serializable

@Serializable
data class RemoveBookmarkResponse(
    val removed: Boolean?
)

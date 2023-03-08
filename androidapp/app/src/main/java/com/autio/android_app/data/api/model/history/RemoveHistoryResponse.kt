package com.autio.android_app.data.api.model.history

import kotlinx.serialization.Serializable

@Serializable
data class RemoveHistoryResponse(
    val removed: Boolean
)

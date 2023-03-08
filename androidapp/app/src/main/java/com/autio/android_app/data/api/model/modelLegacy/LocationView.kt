package com.autio.android_app.data.api.model.modelLegacy

import androidx.annotation.DrawableRes
import kotlinx.serialization.Serializable

@Serializable
data class LocationView(
    @DrawableRes val resourceId: Int
)

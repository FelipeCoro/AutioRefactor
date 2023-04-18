package com.autio.android_app.data.api.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import java.io.Serializable

@kotlinx.serialization.Serializable
data class State(
    val initials: String,
    @SerialName(
        "lat"
    )
    val latitude: Double,
    @SerialName(
        "long"
    )
    val longitude: Double
) : Serializable

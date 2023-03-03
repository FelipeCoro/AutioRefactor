package com.autio.android_app.data.api.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class State(
    val initials: String,
    @SerializedName(
        "lat"
    )
    val latitude: Double,
    @SerializedName(
        "long"
    )
    val longitude: Double
) : Serializable

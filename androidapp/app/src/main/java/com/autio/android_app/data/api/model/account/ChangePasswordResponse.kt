package com.autio.android_app.data.api.model.account

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordResponse(
    @SerialName(
        "success"
    ) val success: String
)

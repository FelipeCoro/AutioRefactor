package com.autio.android_app.data.api.model.account

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordDto(
    @SerialName(
        "current_password"
    ) val currentPassword: String,
    @SerialName(
        "new_password"
    ) val newPassword: String,
    @SerialName(
        "new_password_confirmation"
    ) val passwordConfirmation: String
)

package com.autio.android_app.data.api.model.account

import com.google.gson.annotations.SerializedName

data class ChangePasswordDto(
    @SerializedName(
        "current_password"
    ) val currentPassword: String,
    @SerializedName(
        "new_password"
    ) val newPassword: String,
    @SerializedName(
        "new_password_confirmation"
    ) val passwordConfirmation: String
)

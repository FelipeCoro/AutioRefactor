package com.autio.android_app.data.api.model.account

import com.google.gson.annotations.SerializedName

data class CreateAccountDto(
    @SerializedName(
        "email"
    ) val email: String,
    @SerializedName(
        "email_confirmation"
    ) val emailConfirmation: String,
    @SerializedName(
        "password"
    ) val password: String,
    @SerializedName(
        "password_confirmation"
    ) val passwordConfirmation: String,
    @SerializedName(
        "name"
    ) val name: String
)

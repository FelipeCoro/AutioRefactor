package com.autio.android_app.data.api.model.account

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName


@kotlinx.serialization.Serializable
data class CreateAccountDto(
    @SerialName("email")
    val email: String,
    @SerialName("email_confirmation")
    val emailConfirmation: String,
    @SerialName("password")
    val password: String,
    @SerialName("password_confirmation")
    val passwordConfirmation: String,
    @SerialName("name")
    val name: String
)

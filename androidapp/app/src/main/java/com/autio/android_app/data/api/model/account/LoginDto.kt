package com.autio.android_app.data.api.model.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginDto(
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String
)

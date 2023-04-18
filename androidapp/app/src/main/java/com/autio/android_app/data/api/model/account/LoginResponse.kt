package com.autio.android_app.data.api.model.account

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class LoginResponse(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("email")
    val email: String,
    @SerialName("api_token")
    val apiToken: String,
    @SerialName("is_guest")
    val isGuest: Boolean
)

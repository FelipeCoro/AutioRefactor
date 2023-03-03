package com.autio.android_app.data.api.model.account

import com.google.gson.annotations.SerializedName


data class LoginResponse(
    @SerializedName(
        "id"
    ) val id: Int?,
    @SerializedName(
        "name"
    ) val name: String?,
    @SerializedName(
        "email"
    ) val email: String?,
    @SerializedName(
        "api_token"
    ) val apiToken: String?,
    @SerializedName(
        "is_guest"
    ) val isGuest: Boolean?,
    @SerializedName(
        "firebase_key"
    ) val firebaseKey: String?
)

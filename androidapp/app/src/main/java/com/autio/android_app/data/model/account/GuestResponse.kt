package com.autio.android_app.data.model.account

import com.google.gson.annotations.SerializedName

data class GuestResponse(
    @SerializedName(
        "id"
    ) val id: Int,
    @SerializedName(
        "api_token"
    ) val apiToken: String,
    @SerializedName(
        "is_guest"
    ) val isGuest: Boolean,
    @SerializedName(
        "firebase_key"
    ) val firebaseKey: String
)

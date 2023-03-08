package com.autio.android_app.data.api.model.account

import kotlinx.serialization.SerialName


@kotlinx.serialization.Serializable
data class GuestResponse(
    @SerialName("id")
    val id: Int,
    @SerialName("api_token")
    val apiToken: String,
    @SerialName("is_guest")
    val isGuest: Boolean,
    @SerialName("firebase_key")
    val firebaseKey:String,
    @SerialName("country_code")
    val countryCode:String
)

package com.autio.android_app.data.api.model.account

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class CheckSubscriptionResponse(
    @SerializedName("title")
    val title: String,
    @SerializedName("expired")
    val expired: Boolean,
    @SerializedName("cancel_at_period_end")
    val cancel_at_period_end: Boolean,
    @SerializedName("manage_url")
    val manage_url: String? = null,
    @SerializedName("expiry_description")
    val expiry_description: String? = null
)


package com.autio.android_app.data.model.account

import com.google.gson.annotations.SerializedName

data class ChangePasswordResponse(
    @SerializedName(
        "success"
    ) val success: String
)

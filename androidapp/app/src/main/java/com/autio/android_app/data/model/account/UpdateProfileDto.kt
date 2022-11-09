package com.autio.android_app.data.model.account

import com.google.gson.annotations.SerializedName

data class UpdateProfileDto(
    @SerializedName("email")val email: String,
    @SerializedName("name")val name:String
)

package com.autio.android_app.data.api.model.account

import com.autio.android_app.data.entities.story.Category
import com.google.gson.annotations.SerializedName

data class ProfileDto(
    @SerializedName(
        "email"
    ) val email: String,
    @SerializedName(
        "name"
    ) val name: String,
    @SerializedName(
        "category_order"
    ) val categories: List<Category>
)
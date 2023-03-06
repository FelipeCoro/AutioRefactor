package com.autio.android_app.data.api.model.account

import com.autio.android_app.data.database.entities.CategoryEntity
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
    ) val categories: List<CategoryEntity> //TODO(Not sure about this Type, quick fix from Category to CategoryEntity for runnability)
)

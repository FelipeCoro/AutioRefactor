package com.autio.android_app.data.api.model.account

import com.autio.android_app.data.database.entities.CategoryEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    @SerialName("email") val email: String,
    @SerialName("name") val name: String,
    @SerialName("category_order") val categories: List<CategoryEntity> //TODO(Not sure about this Type, quick fix from Category to CategoryEntity for runnability)
)

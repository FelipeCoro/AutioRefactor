package com.autio.android_app.data.model.story

import androidx.room.*
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(
    tableName = "stories",
    indices = [Index(
        value = ["id"],
        unique = true
    )]
)
data class Story constructor(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    @SerializedName(
        "latitude"
    )
    val lat: Double,
    @SerializedName(
        "longitude"
    )
    val lon: Double,
    val range: Int,
    @SerializedName("imageURL")
    val imageUrl: String?,
    @SerializedName("recordURL")
    val recordUrl: String?,
    val duration: Int,
    @SerializedName(
        "dateAdded"
    )
    val publishedDate: Int,
    @SerializedName(
        "dateModified"
    )
    val modifiedDate: Int,
    @SerializedName(
        "narratorName"
    ) val narrator: String,
    @SerializedName(
        "authorName"
    ) val author: String,
    val state: String?,
    @Embedded(
        prefix = "category_"
    )
    val category: CategoryResponse,
) : Serializable

data class CategoryResponse(
    @PrimaryKey
    val id: Int,
    val title: String,
    val link: String,
)
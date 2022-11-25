package com.autio.android_app.data.model.story

import androidx.room.*
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(
    tableName = "map_points",
    indices = [Index(
        value = ["id"],
        unique = true
    )]
)
data class StoryResponse constructor(
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
    val imageUrl: String,
    val recordUrl: String,
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
    @ColumnInfo(
        name = "state"
    )
    val state: String,
    @Embedded(
        prefix = "category_"
    )
    val category: CategoryResponse,
) : Serializable

data class CategoryResponse(
    @PrimaryKey
    @ColumnInfo(
        name = "id"
    )
    val id: Int,
    @ColumnInfo(
        name = "title"
    )
    val title: String,
    @ColumnInfo(
        name = "url"
    )
    val link: String,
)
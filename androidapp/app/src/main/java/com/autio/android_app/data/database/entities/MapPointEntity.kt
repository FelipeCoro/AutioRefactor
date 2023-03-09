package com.autio.android_app.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "map_points")
data class MapPointEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "published_at")
    var publishedAt: Int = 0,
    @ColumnInfo(name = "fbid")
    val fbid: String = "",
    @ColumnInfo(name = "lat")
    val lat: Double = 0.0,
    @ColumnInfo(name = "lng")
    val lng: Double = 0.0,
    @ColumnInfo(name = "range_in_meters")
    val range: Int = 0,
    @ColumnInfo(name = "state")
    val state: String? = "",
    @ColumnInfo(name = "country_code")
    val countryCode: String?,
    @ColumnInfo(name = "title")
    val title: String?,
    @ColumnInfo(name = "description")
    val description: String?,
    @ColumnInfo(name = "narrator_name")
    val narratorName: String?,
    @ColumnInfo(name = "author_name")
    val authorName: String?,
    @ColumnInfo(name = "category")
    val category: String? = "",
)



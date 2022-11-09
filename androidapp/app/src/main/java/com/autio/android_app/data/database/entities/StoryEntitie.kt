package com.autio.android_app.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "map_points")
data class StoryEntitie(
    @PrimaryKey val idStory:Int,
    @ColumnInfo(name = "id") var id:Int,
    @ColumnInfo(name = "published_at") var publishedAt:Int,
    @ColumnInfo(name = "fbid") var firebaseId: String,
    @ColumnInfo(name = "lat") var latitude: Double,
    @ColumnInfo(name = "lng") var longitude: Double,
    @ColumnInfo(name = "range_in_meters") var range: Int,
    @ColumnInfo(name = "state") var state: String,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "narrator_name") var narratorName: String,
    @ColumnInfo(name = "author_name") var authorName: String,
    @ColumnInfo(name = "category") var category: String,
)

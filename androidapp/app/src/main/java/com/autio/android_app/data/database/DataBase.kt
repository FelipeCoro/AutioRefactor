package com.autio.android_app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.autio.android_app.data.database.dao.CategoryDao
import com.autio.android_app.data.database.dao.DownloadedStoryDao
import com.autio.android_app.data.database.dao.StoryDao
import com.autio.android_app.data.database.entities.CategoryEntity
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.database.entities.MapPoint

@Database(
    entities = [MapPoint::class, DownloadedStoryEntity::class, CategoryEntity::class],
    version = 24
)
abstract class DataBase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun downloadedStoryDao(): DownloadedStoryDao
    abstract fun categoryDao(): CategoryDao
}

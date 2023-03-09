package com.autio.android_app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.autio.android_app.data.database.dao.CategoryDao
import com.autio.android_app.data.database.dao.DownloadedStoryDao
import com.autio.android_app.data.database.dao.MapPointDao
import com.autio.android_app.data.database.dao.StoryDao
import com.autio.android_app.data.database.entities.*

@Database(
    entities = [MapPointEntity::class, StoryEntity::class, DownloadedStoryEntity::class, CategoryEntity::class, HistoryEntity::class],
    version = 24
)
abstract class DataBase : RoomDatabase() {
    abstract fun mapPointDao(): MapPointDao
    abstract fun downloadedStoryDao(): DownloadedStoryDao
    abstract fun categoryDao(): CategoryDao
    abstract fun storyDao(): StoryDao
}

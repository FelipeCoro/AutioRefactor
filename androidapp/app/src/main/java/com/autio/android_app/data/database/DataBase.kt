package com.autio.android_app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.autio.android_app.data.database.dao.CategoryDao
import com.autio.android_app.data.database.dao.DownloadedStoryDao
import com.autio.android_app.data.database.dao.MapPointDao
import com.autio.android_app.data.database.entities.CategoryEntity
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.database.entities.HistoryEntity
import com.autio.android_app.data.database.entities.MapPointEntity

@Database(
    entities = [MapPointEntity::class, DownloadedStoryEntity::class, CategoryEntity::class, HistoryEntity::class],
    version = 24
)
abstract class DataBase : RoomDatabase() {
    abstract fun mapPointDao(): MapPointDao
    abstract fun downloadedStoryDao(): DownloadedStoryDao
    abstract fun categoryDao(): CategoryDao
}

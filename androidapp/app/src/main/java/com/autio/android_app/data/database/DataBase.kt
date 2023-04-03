package com.autio.android_app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.autio.android_app.data.database.dao.*
import com.autio.android_app.data.database.entities.*

@Database(
    entities = [MapPointEntity::class, StoryEntity::class, CategoryEntity::class, HistoryEntity::class, UserEntity::class, RemainingStoriesEntity::class],
    version = 26
)
abstract class DataBase : RoomDatabase() {
    abstract fun mapPointDao(): MapPointDao
    abstract fun categoryDao(): CategoryDao
    abstract fun storyDao(): StoryDao
    abstract fun userDao(): UserDao
    abstract fun remainingStoriesDao():RemainingStoriesDao
}

package com.autio.android_app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.autio.android_app.data.database.dao.CategoryDao
import com.autio.android_app.data.database.dao.DownloadedStoryDao
import com.autio.android_app.data.database.dao.StoryDao
import com.autio.android_app.data.database.entities.CategoryEntity
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.database.entities.StoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [StoryEntity::class, DownloadedStoryEntity::class, CategoryEntity::class],
    version = 24
)
abstract class DataBase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun downloadedStoryDao(): DownloadedStoryDao
    abstract fun categoryDao(): CategoryDao


    companion object {
        @Volatile
        private var INSTANCE: DataBase? = null

        fun getInstance(
            context: Context, scope: CoroutineScope
        ): DataBase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context, scope).also {
                INSTANCE = it
            }
        }
/*
        private fun buildDatabase(context: Context, scope: CoroutineScope): DataBase {
            return Room.databaseBuilder(
                context.applicationContext, DataBase::class.java,
            ).fallbackToDestructiveMigration().addCallback(
                StoryDatabaseCallback(context, scope)
            ).enableMultiInstanceInvalidation().build()
        }
        */

        class StoryDatabaseCallback(
            private val context: Context,
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                scope.launch {
                    INSTANCE?.let { database ->
                        val storyDao = database.storyDao()
                        storyDao.deleteAllStories()
                    }
                }
            }
        }
    }

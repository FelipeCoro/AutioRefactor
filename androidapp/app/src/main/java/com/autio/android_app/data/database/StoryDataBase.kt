package com.autio.android_app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.autio.android_app.data.database.dao.DownloadedStoryDao
import com.autio.android_app.data.database.dao.StoryDao
import com.autio.android_app.data.model.story.DownloadedStory
import com.autio.android_app.data.model.story.Story
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        Story::class,
        DownloadedStory::class
    ],
    version = 16
)
abstract class StoryDataBase :
    RoomDatabase() {

    abstract fun storyDao(): StoryDao
    abstract fun downloadedStoryDao(): DownloadedStoryDao

    private class StoryDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(
            db: SupportSQLiteDatabase
        ) {
            super.onCreate(
                db
            )
            INSTANCE?.let { database ->
                scope.launch {
                    val storyDao =
                        database.storyDao()
                    storyDao
                        .deleteAllStories()
                    // Fill database with own data
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: StoryDataBase? =
            null

        fun getInstance(
            context: Context,
            scope: CoroutineScope
        ): StoryDataBase =
            INSTANCE
                ?: synchronized(
                    this
                ) {
                    INSTANCE
                        ?: buildDatabase(
                            context,
                            scope
                        ).also {
                            INSTANCE =
                                it
                        }
                }

        private fun buildDatabase(
            context: Context,
            scope: CoroutineScope
        ) =
            Room.databaseBuilder(
                context.applicationContext,
                StoryDataBase::class.java,
                "STORIES"
            )
                .fallbackToDestructiveMigration()
                .addCallback(
                    StoryDatabaseCallback(
                        scope
                    )
                )
                .enableMultiInstanceInvalidation()
                .build()
    }
}
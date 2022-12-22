package com.autio.android_app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.autio.android_app.data.database.dao.StoryDao
import com.autio.android_app.data.model.story.Story
import java.util.concurrent.Executors

@Database(
    entities = [Story::class],
    version = 5
)
abstract class StoryDataBase :
    RoomDatabase() {

    abstract fun storyDao(): StoryDao

    companion object {
        private val executor = Executors.newSingleThreadExecutor()

        @Volatile
        private var INSTANCE: StoryDataBase? =
            null

        fun getInstance(
            context: Context
        ): StoryDataBase =
            INSTANCE
                ?: synchronized(
                    this
                ) {
                    INSTANCE
                        ?: buildDatabase(
                            context
                        ).also {
                            INSTANCE =
                                it
                        }
                }

        private fun buildDatabase(
            context: Context
        ) =
            Room.databaseBuilder(
                context.applicationContext,
                StoryDataBase::class.java,
                "STORIES"
            )
                .fallbackToDestructiveMigration()
                .addCallback(
                    callback
                )
                .enableMultiInstanceInvalidation()
                .build()

        private var callback: Callback =
            object :
                Callback() {
                override fun onOpen(
                    db: SupportSQLiteDatabase
                ) {
                    super.onOpen(
                        db
                    )
                    executor.execute {
                        INSTANCE?.storyDao()?.deleteAllStories()
                    }
                }
            }
    }
}
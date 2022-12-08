package com.autio.android_app.data.database

import android.content.Context
import android.os.AsyncTask
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.autio.android_app.data.database.dao.StoryDao
import com.autio.android_app.data.model.story.Story

@Database(
    entities = [Story::class],
    version = 3
)
abstract class StoryDataBase :
    RoomDatabase() {

    abstract fun storyDao(): StoryDao

    companion object {
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
                    PopulateDbAsync(
                        INSTANCE!!
                    )
                }
            }

        internal class PopulateDbAsync(
            storyDatabase: StoryDataBase
        ) :
            AsyncTask<Unit, Unit, Unit>() {
            private val storyDao: StoryDao

            init {
                storyDao =
                    storyDatabase.storyDao()
            }

            override fun doInBackground(
                vararg p0: Unit
            ) {
                storyDao.deleteAllStories()
            }
        }
    }
}
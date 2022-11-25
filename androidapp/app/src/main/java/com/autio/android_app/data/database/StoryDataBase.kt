package com.autio.android_app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.autio.android_app.data.database.dao.StoryDao
import com.autio.android_app.data.model.story.StoryResponse

@Database(
    entities = [StoryResponse::class],
    version = 1
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
                "map_points"
            )
                .build()
    }
}
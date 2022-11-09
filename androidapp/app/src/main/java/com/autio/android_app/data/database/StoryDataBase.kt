package com.autio.android_app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.autio.android_app.data.database.dao.StoryDao
import com.autio.android_app.data.database.entities.StoryEntitie

@Database(entities = [StoryEntitie::class], version = 1)
abstract class StoryDataBase: RoomDatabase() {

    abstract fun storyDao(): StoryDao

    companion object{
        @Volatile
        private var INSTANCE: StoryDataBase? = null

        fun getDatabase(context: Context): StoryDataBase{
            val tempInstance  = INSTANCE
            if (tempInstance != null){
                return tempInstance
            }
            synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StoryDataBase::class.java,
                    "map_points"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
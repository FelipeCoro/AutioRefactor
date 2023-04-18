package com.autio.android_app.data.di

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.autio.android_app.data.database.DataBase
import com.autio.android_app.data.database.dao.*
import com.autio.android_app.data.database.entities.RemainingStoriesEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {
    private const val CONST_DATABASE_NAME = "STORIES"


    @Volatile
    private var INSTANCE: DataBase? = null

    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext context: Context): DataBase {
        return INSTANCE ?: synchronized(this) {
            val scope = CoroutineScope(Dispatchers.IO)

            val instance = Room.databaseBuilder(context, DataBase::class.java, CONST_DATABASE_NAME)
                .createFromAsset("database/published_map_points.db")
                //.fallbackToDestructiveMigration()
                .addCallback(StoriesDatabaseCallback(scope))
                //.enableMultiInstanceInvalidation()
                .build()
                .also { INSTANCE = it }
            instance
        }
    }

    private class StoriesDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let {database ->
                scope.launch {
                    database.remainingStoriesDao().insertStories(RemainingStoriesEntity(0,5))
                }
            }
        }
    }

    @Singleton
    @Provides
    fun providesMapPointDao(database: DataBase): MapPointDao = database.mapPointDao()

    @Singleton
    @Provides
    fun providesCategoryDao(database: DataBase): CategoryDao = database.categoryDao()

    @Singleton
    @Provides
    fun providesStoryDao(database: DataBase): StoryDao = database.storyDao()

    @Singleton
    @Provides
    fun providesUserDao(database: DataBase): UserDao = database.userDao()

    @Singleton
    @Provides
    fun providesRemainingStoriesDao(database: DataBase): RemainingStoriesDao =
        database.remainingStoriesDao()

}

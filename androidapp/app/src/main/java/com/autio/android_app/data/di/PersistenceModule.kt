package com.autio.android_app.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.autio.android_app.data.database.DataBase
import com.autio.android_app.data.database.dao.CategoryDao
import com.autio.android_app.data.database.dao.DownloadedStoryDao
import com.autio.android_app.data.database.dao.StoryDao
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.data.repository.prefs.PrefRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {
    private const val CONST_DATABASE_NAME = "STORIES"

    @Provides
    @Singleton
    fun providesDatabase(
        @ApplicationContext context: Context,
        coroutineScope: CoroutineScope,
        callback: RoomDatabase.Callback
    ): DataBase {
        return Room.databaseBuilder(context, DataBase::class.java, CONST_DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .enableMultiInstanceInvalidation()
            .build()
    }

    @Provides
    @Singleton
    fun provideStoryDatabaseCallBack(
        @ApplicationContext context: Context,
        coroutineScope: CoroutineScope,
    ): RoomDatabase.Callback {
        return object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

            }
        }
    }

    @Singleton
    @Provides
    fun providesStoryDao(database: DataBase): StoryDao = database.storyDao()

    @Singleton
    @Provides
    fun providesCategoryDao(database: DataBase): CategoryDao = database.categoryDao()

    @Singleton
    @Provides
    fun providesDownloadedStoryDao(database: DataBase): DownloadedStoryDao =
        database.downloadedStoryDao()
}
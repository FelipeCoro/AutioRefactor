package com.autio.android_app.data.di

import android.content.ComponentCallbacks
import android.content.Context
import androidx.room.Room
import com.autio.android_app.data.database.DataBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Scope
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
        callback: DataBase.StoryDatabaseCallback
    ): DataBase {
        return Room.databaseBuilder(context, DataBase::class.java, CONST_DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .enableMultiInstanceInvalidation()
            .build()
    }

    @Provides
    fun provideStoryDatabaseCallBack(
        @ApplicationContext context: Context,
        coroutineScope: CoroutineScope,
    ): DataBase.StoryDatabaseCallback {
        return DataBase.StoryDatabaseCallback(context, coroutineScope)
    }
}
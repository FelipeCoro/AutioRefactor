package com.autio.android_app.util

import android.content.Context
import com.autio.android_app.data.database.StoryDataBase


class DatabaseCopier {
    private val TAG =
        DatabaseCopier::class.java.simpleName
    private val DATABASE_NAME =
        "published_map_points_with_states.sqlite"

    private var mAppDataBase: StoryDataBase? =
        null
    private var appContext: Context? =
        null

    private object Holder {
        val INSTANCE =
            DatabaseCopier()
    }

    fun getInstance(
        context: Context
    ): DatabaseCopier? {
        appContext =
            context
        return Holder.INSTANCE
    }

    private fun DatabaseCopier() {

    }

    fun getRoomDatabase(): StoryDataBase? {
        return mAppDataBase
    }

}
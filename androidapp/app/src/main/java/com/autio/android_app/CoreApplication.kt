package com.autio.android_app

import android.app.Application
import com.autio.android_app.data.database.StoryDataBase
import com.autio.android_app.data.database.repository.StoryRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class CoreApplication :
    Application() {
    // No need to cancel this scope as it'll be torn down with the process
    private val applicationScope =
        CoroutineScope(
            SupervisorJob()
        )

    val database by lazy {
        StoryDataBase.getInstance(
            this,
            applicationScope
        )
    }
    val storyRepository by lazy {
        StoryRepository(
            database.storyDao(),
            database.downloadedStoryDao()
        )
    }
}
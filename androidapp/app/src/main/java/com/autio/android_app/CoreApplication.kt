package com.autio.android_app

import android.app.Application
import com.autio.android_app.data.database.repository.StoryRepository
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CoreApplication : Application()
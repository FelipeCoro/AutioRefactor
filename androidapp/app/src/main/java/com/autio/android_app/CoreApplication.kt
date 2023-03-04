package com.autio.android_app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CoreApplication @Inject constructor() : Application() {}

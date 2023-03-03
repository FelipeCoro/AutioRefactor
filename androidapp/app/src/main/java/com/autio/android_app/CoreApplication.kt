package com.autio.android_app

import android.app.Application
import com.autio.android_app.billing.RevenueCatRepository
import com.autio.android_app.data.database.DataBase
import com.autio.android_app.data.repository.datasource.local.AutioLocalDataSourceImpl
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class CoreApplication :
    Application() {
    lateinit var appContainer: AppContainer

    /**
     * Container for objects shared across the app
     */
    inner class AppContainer {
        // No need to cancel this scope as it'll be torn down with the process
        private val applicationScope =
            CoroutineScope(
                SupervisorJob()
            )

        val database by lazy {
            DataBase.getInstance(
                this@CoreApplication,
                applicationScope
            )
        }
        val autioLocalDataSourceImpl by lazy {
            AutioLocalDataSourceImpl(
                database.storyDao(),
                database.downloadedStoryDao(),
                database.categoryDao()
            )
        }

        //        private val billingDataSource =
//            BillingDataSource.getInstance(
//                this@CoreApplication,
//                applicationScope,
//                IN_APP_SKUS,
//                SUBS_SKUS,
//                arrayOf()
//            )
//        val applicationRepository =
//            CoreApplicationRepository(
//                billingDataSource,
//                applicationScope
//            )

        val revenueCatRepository =
            RevenueCatRepository.getInstance(
                this@CoreApplication
            )
    }

    override fun onCreate() {
        super.onCreate()
        appContainer =
            AppContainer()
    }
}

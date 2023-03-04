package com.autio.android_app.util

import android.content.Context
import com.autio.android_app.ui.viewmodel.NetworkStatusViewModel

/**
 * Static methods used to inject classes needed for various Activities and Fragments
 */
object InjectorUtils {
    fun provideNetworkStatusViewModel(
        context: Context
    ): NetworkStatusViewModel.Factory {
        val networkStatusTracker =
            NetworkStatusTracker(
                context
            )
        return NetworkStatusViewModel.Factory(
            networkStatusTracker
        )
    }
/*
    private fun providePlayerServiceConnection(
        context: Context
    ): PlayerServiceConnection {
        val applicationContext =
            context.applicationContext as Application
        return PlayerServiceConnection.getInstance(
            context,
            ComponentName(
                context,
                PlayerService::class.java
            ),
            (applicationContext as CoreApplication).appContainer.autioLocalDataSourceImpl
        )
    }

    fun provideStoryViewModel(
        context: Context
    ): StoryViewModel.Factory {
        val applicationContext =
            context.applicationContext as Application
        return StoryViewModel.Factory(
            (applicationContext as CoreApplication).appContainer.autioLocalDataSourceImpl
        )
    }

    fun providePurchaseViewModel(
        context: Context
    ): PurchaseViewModel.Factory {
        val applicationContext =
            context.applicationContext as CoreApplication

        return PurchaseViewModel.Factory(
            applicationContext.appContainer.revenueCatRepository
        )
    }

    fun provideBottomNavigationViewModel(
        context: Context
    ): BottomNavigationViewModel.Factory {
        val applicationContext =
            context.applicationContext as CoreApplication
        val playerServiceConnection =
            providePlayerServiceConnection(
                applicationContext
            )
        return BottomNavigationViewModel.Factory(
            applicationContext,
            playerServiceConnection,
            applicationContext.appContainer.autioLocalDataSourceImpl,
//            applicationContext.appContainer.applicationRepository
        )
    }

    fun provideAccountFragmentViewModel(
        context: Context
    ): AccountFragmentViewModel.Factory {
        val applicationContext =
            context.applicationContext as Application
        return AccountFragmentViewModel.Factory(
            applicationContext,
            (applicationContext as CoreApplication).appContainer.autioLocalDataSourceImpl
        )
    }

    fun provideMapFragmentViewModel(
        context: Context,
        mediaId: String
    ): MapFragmentViewModel.Factory {
        val applicationContext =
            context.applicationContext
        val playerServiceConnection =
            providePlayerServiceConnection(
                applicationContext
            )
        return MapFragmentViewModel.Factory(
            applicationContext as Application,
            mediaId,
            playerServiceConnection,
            (applicationContext as CoreApplication).appContainer.autioLocalDataSourceImpl
        )
    }

    fun providePlayerFragmentViewModel(
        context: Context
    )
            : PlayerFragmentViewModel.Factory {
        val applicationContext =
            context.applicationContext
        val playerServiceConnection =
            providePlayerServiceConnection(
                applicationContext
            )
        return PlayerFragmentViewModel.Factory(
            applicationContext as Application,
            playerServiceConnection
        )
    }*/
}

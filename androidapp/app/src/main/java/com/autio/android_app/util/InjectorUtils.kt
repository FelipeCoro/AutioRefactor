package com.autio.android_app.util

import android.app.Application
import android.content.ComponentName
import android.content.Context
import com.autio.android_app.CoreApplication
import com.autio.android_app.player.PlayerService
import com.autio.android_app.player.PlayerServiceConnection
import com.autio.android_app.ui.viewmodel.*

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
            (applicationContext as CoreApplication).storyRepository
        )
    }

    fun provideStoryViewModel(
        context: Context
    ): StoryViewModel.Factory {
        val applicationContext =
            context.applicationContext as Application
        return StoryViewModel.Factory(
            (applicationContext as CoreApplication).storyRepository
        )
    }

    fun provideBottomNavigationViewModel(
        context: Context
    ): BottomNavigationViewModel.Factory {
        val applicationContext =
            context.applicationContext as Application
        val playerServiceConnection =
            providePlayerServiceConnection(
                applicationContext
            )
        return BottomNavigationViewModel.Factory(
            applicationContext,
            playerServiceConnection,
            (applicationContext as CoreApplication).storyRepository
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
            (applicationContext as CoreApplication).storyRepository
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
    }
}
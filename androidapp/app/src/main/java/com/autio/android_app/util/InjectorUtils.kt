package com.autio.android_app.util

import android.app.Application
import android.content.ComponentName
import android.content.Context
import com.autio.android_app.player.PlayerService
import com.autio.android_app.player.PlayerServiceConnection
import com.autio.android_app.ui.viewmodel.BottomNavigationViewModel
import com.autio.android_app.ui.viewmodel.MapFragmentViewModel
import com.autio.android_app.ui.viewmodel.PlayerFragmentViewModel

/**
 * Static methods used to inject classes needed for various Activities and Fragments
 */
object InjectorUtils {
    private fun providePlayerServiceConnection(
        context: Context
    ): PlayerServiceConnection {
        return PlayerServiceConnection.getInstance(
            context,
            ComponentName(
                context,
                PlayerService::class.java
            )
        )
    }

    fun provideBottomNavigationViewModel(
        context: Context
    ): BottomNavigationViewModel.Factory {
        val applicationContext =
            context.applicationContext
        val playerServiceConnection =
            providePlayerServiceConnection(
                applicationContext
            )
        return BottomNavigationViewModel.Factory(
            playerServiceConnection
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
            mediaId,
            playerServiceConnection
        )
    }

    fun providePlayerFragmentViewModel(context: Context)
            : PlayerFragmentViewModel.Factory {
        val applicationContext = context.applicationContext
        val playerServiceConnection = providePlayerServiceConnection(applicationContext)
        return PlayerFragmentViewModel.Factory(
            applicationContext as Application, playerServiceConnection
        )
    }
}
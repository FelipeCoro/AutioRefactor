package com.autio.android_app.ui.di

import android.content.ComponentName
import android.content.Context
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.player.PlayerService
import com.autio.android_app.player.PlayerServiceConnection
import com.autio.android_app.ui.di.coroutines.MainDispatcher
import com.autio.android_app.ui.network_monitor.NetworkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UiInjectorModule {

    @Provides
    @Singleton
    fun providePlayerServiceConnection(
        @ApplicationContext
        context: Context,
        autioRepository: AutioRepository,
        @MainDispatcher
        coroutineDispatcher: CoroutineDispatcher,
        prefRepository: PrefRepository
    ): PlayerServiceConnection {
        return PlayerServiceConnection(
            context,
            ComponentName(context, PlayerService::class.java),
            autioRepository,
            coroutineDispatcher,
            prefRepository
        )
    }

    @Provides
    @Singleton
    fun provideNetworkManager(@ApplicationContext context: Context): NetworkManager {
        return NetworkManager(context)
    }
}

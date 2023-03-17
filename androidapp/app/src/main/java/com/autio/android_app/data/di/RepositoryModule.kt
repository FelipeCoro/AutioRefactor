package com.autio.android_app.data.di

import com.autio.android_app.data.repository.datasource.local.AutioLocalDataSource
import com.autio.android_app.data.repository.datasource.local.AutioLocalDataSourceImpl
import com.autio.android_app.data.repository.datasource.remote.AutioRemoteDataSource
import com.autio.android_app.data.repository.datasource.remote.AutioRemoteDataSourceImpl
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.data.repository.prefs.PrefRepositoryImpl
import com.autio.android_app.data.repository.revenue.RevenueCatRepository
import com.autio.android_app.data.repository.revenue.RevenueCatRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindsAutioRemoteDataSource(
        remoteDataSourceImpl: AutioRemoteDataSourceImpl
    ): AutioRemoteDataSource

    @Binds
    abstract fun bindsAutioLocalDataSource(
        localDataSourceImpl: AutioLocalDataSourceImpl
    ): AutioLocalDataSource

    @Binds
    abstract fun bindsRevenueCatRepository(
        revenueCatRepository: RevenueCatRepositoryImpl
    ): RevenueCatRepository

    @Binds
    @Singleton
    abstract fun bindsPrefRepository(
        prefRepository: PrefRepositoryImpl
    ): PrefRepository

}

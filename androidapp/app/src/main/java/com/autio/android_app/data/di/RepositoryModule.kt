package com.autio.android_app.data.di

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

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindsAutioRemoteDataSource(
        remoteDataSourceImpl: AutioRemoteDataSourceImpl
    ): AutioRemoteDataSource

    @Binds
    abstract fun bindsRevenueCatRepository(
        revenueCatRepository: RevenueCatRepositoryImpl
    ): RevenueCatRepository

    @Binds
    abstract fun bindsPrefRepository(
        prefRepository: PrefRepositoryImpl
    ): PrefRepository
}
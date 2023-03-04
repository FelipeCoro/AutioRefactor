package com.autio.android_app.data.di

import com.autio.android_app.data.api.ApiClient
import com.autio.android_app.data.repository.datasource.remote.AutioRemoteDataSource
import com.autio.android_app.data.repository.datasource.remote.AutioRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
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

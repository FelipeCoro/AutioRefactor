package com.autio.android_app.core.di

import com.autio.android_app.data.api.ApiClient
import com.autio.android_app.data.repository.datasource.remote.AutioRemoteDataSource
import com.autio.android_app.data.repository.datasource.remote.AutioRemoteDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {

    @Provides
    fun provideAutioRemoteDataSource(apiClient: ApiClient): AutioRemoteDataSource {
        return AutioRemoteDataSourceImpl(apiClient)
    }

}

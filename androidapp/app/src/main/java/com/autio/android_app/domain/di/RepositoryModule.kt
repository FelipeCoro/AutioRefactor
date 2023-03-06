package com.autio.android_app.domain.di

import com.autio.android_app.data.repository.AutioRepositoryImpl
import com.autio.android_app.domain.repository.AutioRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindsAutioRepository(
        autioRepository: AutioRepositoryImpl
    ): AutioRepository

}

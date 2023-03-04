package com.autio.android_app.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.autio.android_app.BuildConfig
import com.autio.android_app.core.RequestInterceptor
import com.autio.android_app.data.api.ApiClient
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    fun provideBaseUrl(): String = BuildConfig.base_url

    @Provides
    fun provideRetrofit(baseUrl: String, loggingInterceptor: RequestInterceptor): ApiClient {

        val client = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

        val retrofit =
            Retrofit.Builder().baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client).build()

        return retrofit.create(ApiClient::class.java)
    }
}

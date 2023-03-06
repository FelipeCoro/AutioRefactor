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
import com.autio.android_app.data.repository.prefs.PrefRepository
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
class ApiModule { //TODO(Had to change this to class from object in order to inject Interceptor)
    @Inject
    lateinit var loggingInterceptor: RequestInterceptor

    @Provides
    fun provideBaseUrl(): String = BuildConfig.base_url

    @Provides
    fun provideRetrofit(baseUrl: String): ApiClient {

        val client = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

        val retrofit =
            Retrofit.Builder().baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client).build()

        return retrofit.create(ApiClient::class.java)
    }
}

package com.autio.android_app.core.di

import com.autio.android_app.BuildConfig
import com.autio.android_app.data.api.ApiClient
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit


@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    private const val APPLICATION_JSON_MEDIA_TYPE = "application/json"

    @Provides
    fun providesAutioService(
        retrofitBuilder: Retrofit.Builder,
    ): ApiClient {
        val retrofit = retrofitBuilder
            .baseUrl(BuildConfig.base_url).build()
        return retrofit.create(ApiClient::class.java)
    }


    private val jsonProperties = Json {
        isLenient = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    @ExperimentalSerializationApi
    @Provides
    fun providesKotlinxConverterFactory(): Converter.Factory {
        val contentType = APPLICATION_JSON_MEDIA_TYPE.toMediaType()
        return jsonProperties.asConverterFactory(contentType)
    }

    @Provides
    fun providesRetrofitClient(
        okHttpclient: OkHttpClient, converterFactory: Converter.Factory
    ): Retrofit.Builder {
        return Retrofit.Builder()
            //.addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(converterFactory)
            .client(okHttpclient)
    }


    @Provides
    fun provideOkHttpClient(interceptor: Interceptor): OkHttpClient {
        val builder = OkHttpClient.Builder()
       // builder.addInterceptor(interceptor)
        return builder.build()
    }

    @Provides
    fun provideInterceptor(): Interceptor {
        return LoggingInterceptor()
    }
}

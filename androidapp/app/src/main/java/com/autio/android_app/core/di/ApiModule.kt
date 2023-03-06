package com.autio.android_app.core.di

import com.autio.android_app.BuildConfig
import com.autio.android_app.data.api.ApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Provides
    fun providesAutioService(
        retrofitBuilder: Retrofit.Builder,
    ): ApiClient {
        val retrofit = retrofitBuilder
            .baseUrl(BuildConfig.base_url).build()
        return retrofit.create(ApiClient::class.java)
    }

    @Provides
    fun providesRetrofitClient(
        okHttpclient: OkHttpClient//, converterFactory: Converter.Factory
    ): Retrofit.Builder {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            //.addConverterFactory(converterFactory)
            .client(okHttpclient)
    }

    @Provides
    fun provideOkHttpClient(interceptor: Interceptor): OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(interceptor)
        return builder.build()
    }

    @Provides
    fun provideInterceptor(): Interceptor {
        return LoggingInterceptor()
    }
}

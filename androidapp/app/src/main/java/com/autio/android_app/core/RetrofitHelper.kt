package com.autio.android_app.core

import com.autio.android_app.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitHelper {

    private val okHttpClient: OkHttpClient =
        OkHttpClient()
            .newBuilder()
            .addInterceptor(
                RequestInterceptor
            )
            .callTimeout(
                120,
                TimeUnit.SECONDS
            )
            .writeTimeout(
                120,
                TimeUnit.SECONDS
            )
            .readTimeout(
                120,
                TimeUnit.SECONDS
            )
            .connectTimeout(
                120,
                TimeUnit.SECONDS
            )
            .retryOnConnectionFailure(
                true
            )
            .build()

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(
                BuildConfig.base_url
            )
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .client(
                okHttpClient
            )
            .build()
    }

    fun <T> buildService(
        service: Class<T>
    ): T {
        return getRetrofit().create(
            service
        )
    }
}

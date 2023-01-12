package com.autio.android_app.core

import com.autio.android_app.data.model.account.RequestInterceptor
import com.autio.android_app.util.Constants.baseUrl
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
            .callTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .connectTimeout(1, TimeUnit.MINUTES)
            .retryOnConnectionFailure(true)
            .build()

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(
                baseUrl
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
package com.autio.android_app.data.model.account

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

object RequestInterceptor :
    Interceptor {

    override fun intercept(
        chain: Interceptor.Chain
    ): Response {
        val request =
            chain.request()
        println(
            "Outgoing request to ${request.url()}"
        )

        var response = chain.proceed(request)
        Log.d("RequestInterceptor", "${response.code()}: ${response.headers()}")

        if (response.code() == 429) {
            try {
                Log.d("RequestInterceptor", "wait for request to retry")
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                Log.e("RequestInterceptor", "exception:", e)
            }

            response.body()?.close()
            response = chain.proceed(request)
        }

        return response
    }
}
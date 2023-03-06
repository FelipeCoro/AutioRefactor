package com.autio.android_app.core

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer


object RequestInterceptor : Interceptor {

    override fun intercept(
        chain: Interceptor.Chain
    ): Response {
        val request = chain.request().newBuilder().addHeader(
                "Accept", "application/json"
            ).addHeader(
                "Content-Type", "application/json"
            ).build()
        println(
            "Outgoing request to ${request.url()}"
        )
        println(
            "Headers: ${request.headers()}"
        )
        println(
            "Body: ${request.getBodyAsString()}"
        )

        var response = chain.proceed(
            request
        )

        if (response.code() == 429) {
            try {
                Log.d(
                    "RequestInterceptor", "wait for request to retry"
                )
                Thread.sleep(
                    1000
                )
            } catch (e: InterruptedException) {
                Log.e(
                    "RequestInterceptor", "exception:", e
                )
            }

            response.body()?.close()
            response = chain.proceed(
                request
            )
        }

        return response
    }

    /**
     * Prints body as a string message instead of the default encoded
     * message from an API request
     */
    private fun Request.getBodyAsString(): String {
        val requestCopy = this.newBuilder().build()
        val buffer = Buffer()
        requestCopy.body()?.writeTo(
                buffer
            )
        return buffer.readUtf8()
    }
}

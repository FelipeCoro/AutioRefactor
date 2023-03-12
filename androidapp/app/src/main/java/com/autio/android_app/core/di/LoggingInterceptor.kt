package com.autio.android_app.core.di

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.http2.Http2Reader.Companion.logger

class LoggingInterceptor : Interceptor {

    companion object {
        const val MAX_RETRY_TRIES = 3
        const val RETRY_WAITING_TIME = 1000L
        const val INITIAL_DELIMITER =
            "■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■"
        const val DELIMITER =
            "═════════════════════════════════════════════════════════════════════════════════════════════════════════════════"
    }

    override fun intercept(chain: Chain): Response {
        val request: Request = chain.request()
        val t1 = System.nanoTime()

        logRequest(request, chain)
        val response: Response = doRequest(chain, request)

        val t2 = System.nanoTime()

        val rawJson = logResponse(response, t2, t1)
        return response.newBuilder()
            .body(rawJson.toResponseBody(response.body?.contentType())).build()
    }

    private fun doRequest(
        chain: Chain,
        request: Request
    ): Response {
        //var response: Response = chain.proceed(request)
       /* if (!response.isSuccessful) {
            response = retryRequestToServer(response, chain, request)
        }*/
        return chain.proceed(request)
    }


    private fun retryRequestToServer(
        response: Response,
        chain: Chain,
        request: Request
    ): Response {
        response.let {
            var response1 = response
            try {
                var retryConnection = 0
                while (!response1.isSuccessful && retryConnection < MAX_RETRY_TRIES) {
                    Thread.sleep(RETRY_WAITING_TIME)
                    response1.close().also {
                        Log.w(
                            LoggingInterceptor::class.simpleName,
                            buildString { appendLine("Retry request => ${request.url}") }
                        )
                    }
                    response1 = chain.proceed(request)
                    retryConnection += 1
                }
            } catch (ex: Exception) {
                val st = "Error on request, connecting to ${request.url}"
                logger.severe(
                    buildString {
                        appendLine(" ")
                        appendLine(INITIAL_DELIMITER)
                        appendLine(st)
                        appendLine(DELIMITER)
                    }
                )
                throw ex
            }
            return response1
        }
    }
}

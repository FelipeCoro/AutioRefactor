package com.autio.android_app.core.di


import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.http2.Http2Reader

fun logResponse(
    response: Response,
    t2: Long,
    t1: Long
): String {
    val st = String.format(
        "RECEIVED RESPONSE : for %s in [ %.1fms ]%n",
        response.request.url, (t2 - t1) / 1e6
    )

    val rawJson = response.body?.string() ?: ""

    Http2Reader.logger.info(
        buildString {
            appendLine(" ")
            appendLine(LoggingInterceptor.INITIAL_DELIMITER)
            append(st)
            appendLine("STATUS CODE:  ${response.code}")
            append("HEADERS : \n${response.headers}")
            appendLine("BODY : \n${rawJson}")
            append(LoggingInterceptor.DELIMITER)
            appendLine()
        }
    )
    return rawJson
}

fun logRequest(request: Request, chain: Interceptor.Chain) {

    val url = if (request.url.toString().length > 200) {
        request.url.toString().substring(200) + "..."
    } else request.url.toString()

    Http2Reader.logger.info(
        buildString {
            appendLine(" ")
            appendLine(LoggingInterceptor.INITIAL_DELIMITER)
            appendLine(
                "SENDING REQUEST: [${request.method.uppercase()}] to $url"
            )
            appendLine("HTTPS : ${request.isHttps}")
            if (request.headers.size > 0) {
                append("HEADERS : \n${request.headers}")
            } else {
                append("HEADERS : NONE")
            }
            append(LoggingInterceptor.DELIMITER)
            if (request.method in listOf("POST", "PUT", "PATCH")) {
                appendLine("BODY : ${request.body}")
                appendLine(LoggingInterceptor.DELIMITER)
            }
            appendLine()
        }
    )
}

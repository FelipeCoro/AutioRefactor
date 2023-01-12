package com.autio.android_app.data

sealed class DataRequestState<T> {
    class Start<T> : DataRequestState<T>()
    class Success<T>(var data: T) : DataRequestState<T>()
    class Error<T>(val error: Throwable) : DataRequestState<T>()
}
package com.autio.android_app.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*

class Timer {
    private val job =
        SupervisorJob()
    private val scope =
        CoroutineScope(
            Dispatchers.Default + job
        )

    private val _isActive = MutableLiveData<Boolean?>(null)
    val isActive : LiveData<Boolean?> = _isActive

    private val timer =
        scope.launch(
            Dispatchers.IO,
            CoroutineStart.LAZY
        ) {
            _isActive.postValue(true)
            delay(
                5000
            )
            _isActive.postValue(false)
        }

    fun startTimer() {
        timer.start()
    }

    fun cancelTimer() {
        _isActive.postValue(null)
        timer.cancel()
    }
}
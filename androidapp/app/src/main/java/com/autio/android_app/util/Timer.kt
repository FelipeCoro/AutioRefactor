package com.autio.android_app.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*

class Timer(
    private val timeMillis: Long = 5000
) {
    private val job =
        SupervisorJob()
    private val scope =
        CoroutineScope(
            Dispatchers.Default + job
        )

    private var isPaused =
        false

    private val _isActive =
        MutableLiveData<Boolean?>(
            null
        )
    val isActive: LiveData<Boolean?> =
        _isActive

    private val timer =
        scope.launch(
            Dispatchers.IO,
            CoroutineStart.LAZY
        ) {
            _isActive.postValue(
                true
            )
            delay(
                timeMillis
            )
            if (!isPaused) {
                _isActive.postValue(
                    false
                )
            }
        }

    fun startTimer() {
        timer.start()
    }

    fun pauseTimer() {
        isPaused =
            true
    }

    fun finishTimer() {
        isPaused =
            false
        _isActive.postValue(
            false
        )
    }

    fun cancelTimer() {
        _isActive.postValue(
            null
        )
        isPaused =
            false
        timer.cancel()
    }
}
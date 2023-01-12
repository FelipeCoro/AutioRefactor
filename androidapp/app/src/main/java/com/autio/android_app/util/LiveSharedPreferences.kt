package com.autio.android_app.util

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.lifecycle.LiveData

abstract class LiveSharedPreferences<T>(
    private val preferences: SharedPreferences,
    val key: String,
    val defValue: T
) : LiveData<T>() {
    private val preferenceChangeListener =
        OnSharedPreferenceChangeListener { _, key ->
            if (key == this.key) {
                value =
                    getValueFromPreferences(
                        key,
                        defValue
                    )
            }
        }

    abstract fun getValueFromPreferences(
        key: String,
        defValue: T
    ): T

    override fun onActive() {
        super.onActive()
        preferences.registerOnSharedPreferenceChangeListener(
            preferenceChangeListener
        )
    }

    override fun onInactive() {
        super.onInactive()
        preferences.unregisterOnSharedPreferenceChangeListener(
            preferenceChangeListener
        )
    }
}

class SharedPreferenceIntLiveData(
    private val sharedPrefs: SharedPreferences,
    key: String,
    defValue: Int
) :
    LiveSharedPreferences<Int>(
        sharedPrefs,
        key,
        defValue
    ) {
    override fun getValueFromPreferences(
        key: String,
        defValue: Int
    ): Int =
        sharedPrefs.getInt(
            key,
            defValue
        )
}
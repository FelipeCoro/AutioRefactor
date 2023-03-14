package com.autio.android_app.data.repository.prefs

import com.autio.android_app.util.SharedPreferenceIntLiveData

interface PrefRepository {
    var userId: Int
    var userName: String
    var userEmail: String
    var userApiToken: String
    var isUserGuest: Boolean
    var remainingStories: Int
    val remainingStoriesLiveData: SharedPreferenceIntLiveData
    var userSubIsActive:Boolean
    fun String.put(int: Int)
    fun String.put(string: String)
    fun String.put(boolean: Boolean)
    fun String.getInt(): Int
    fun String.getString(): String
    fun String.getBoolean(): Boolean
    fun clearData()
}

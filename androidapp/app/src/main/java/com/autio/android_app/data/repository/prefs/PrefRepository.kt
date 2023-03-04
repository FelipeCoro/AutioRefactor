package com.autio.android_app.data.repository.prefs

import android.content.Context
import android.content.SharedPreferences
import com.autio.android_app.util.Constants.REMAINING_STORIES
import com.autio.android_app.util.Constants.USER_API_TOKEN
import com.autio.android_app.util.Constants.USER_EMAIL
import com.autio.android_app.util.Constants.USER_FIREBASE_KEY
import com.autio.android_app.util.Constants.USER_ID
import com.autio.android_app.util.Constants.USER_IS_GUEST
import com.autio.android_app.util.Constants.USER_NAME
import com.autio.android_app.util.Constants.USER_PREFERENCES
import com.autio.android_app.util.SharedPreferenceIntLiveData

interface PrefRepository {

    var firebaseKey: String
    var userId: Int
    var userName: String
    var userEmail: String
    var userApiToken: String

    fun String.put(
        int: Int
    )

    fun String.put(
        string: String
    )

    fun String.put(
        boolean: Boolean
    )

    fun String.getInt(): Int
    fun String.getString(): String
    fun String.getBoolean(): Boolean
    fun clearData()
}

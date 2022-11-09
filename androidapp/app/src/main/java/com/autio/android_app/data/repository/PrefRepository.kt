package com.autio.android_app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.autio.android_app.util.Constants.ON_BOARDING_FINISHED
import com.autio.android_app.util.Constants.USER_API_TOKEN
import com.autio.android_app.util.Constants.USER_EMAIL
import com.autio.android_app.util.Constants.USER_ID
import com.autio.android_app.util.Constants.USER_IS_GUEST
import com.autio.android_app.util.Constants.USER_NAME
import com.autio.android_app.util.Constants.USER_PREFERENCES
import com.google.gson.Gson

class PrefRepository(
    val context: Context
) {

    private val pref: SharedPreferences =
        context.getSharedPreferences(
            USER_PREFERENCES,
            Context.MODE_PRIVATE
        )
    private val editor =
        pref.edit()
    private val gson =
        Gson()

    private fun String.put(
        long: Long
    ) {
        editor.putLong(
            this,
            long
        )
        editor.commit()
    }

    private fun String.put(
        int: Int
    ) {
        editor.putInt(
            this,
            int
        )
        editor.commit()
    }

    private fun String.put(
        string: String
    ) {
        editor.putString(
            this,
            string
        )
        editor.commit()
    }

    private fun String.put(
        boolean: Boolean
    ) {
        editor.putBoolean(
            this,
            boolean
        )
        editor.commit()
    }

    private fun String.getLong() =
        pref.getLong(
            this,
            0
        )

    private fun String.getInt() =
        pref.getInt(
            this,
            0
        )

    private fun String.getString() =
        pref.getString(
            this,
            ""
        )!!

    private fun String.getBoolean() =
        pref.getBoolean(
            this,
            false
        )

    fun setOnBoardingFinished(
        onBoardingFinished: Boolean
    ) =
        ON_BOARDING_FINISHED.put(
            onBoardingFinished
        )

    fun getOnBoardingFinished() =
        ON_BOARDING_FINISHED.getBoolean()

    fun setUserId(
        id: Int
    ) =
        USER_ID.put(
            id
        )

    fun getUserId() =
        USER_ID.getInt()

    fun setUserName(
        name: String
    ) =
        USER_NAME.put(
            name
        )

    fun getUserName() =
        USER_NAME.getString()

    fun setUserEmail(
        email: String
    ) =
        USER_EMAIL.put(
            email
        )

    fun getUserEmail() =
        USER_EMAIL.getString()

    fun setUserApiToken(
        apiToken: String
    ) =
        USER_API_TOKEN.put(
            apiToken
        )

    fun getUserApiToken() =
        USER_API_TOKEN.getString()

    fun setIsUserGuest(
        isGuest: Boolean
    ) =
        USER_IS_GUEST.put(
            isGuest
        )

    fun getIsUserGuest() =
        USER_IS_GUEST.getBoolean()

    fun clearData() {
        editor.clear()
        editor.commit()
    }

}
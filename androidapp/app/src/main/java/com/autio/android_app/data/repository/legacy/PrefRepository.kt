package com.autio.android_app.data.repository.legacy

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

    var userId: Int =
        USER_ID.getInt()
        set(id) = USER_ID.put(
            id
        )

    var userName: String =
        USER_NAME.getString()
        set(
            name
        ) =
            USER_NAME.put(
                name
            )

    var userEmail: String =
        USER_EMAIL.getString()
        set(
            email
        ) = USER_EMAIL.put(
            email
        )

    var userApiToken: String =
        USER_API_TOKEN.getString()
        set(apiToken) = USER_API_TOKEN.put(
            apiToken
        )

    var firebaseKey: String =
        USER_FIREBASE_KEY.getString()
        set(firebaseKey) = USER_FIREBASE_KEY.put(
            firebaseKey
        )

    var isUserGuest: Boolean =
        USER_IS_GUEST.getBoolean()
        set(isGuest) = USER_IS_GUEST.put(
            isGuest
        )

    var remainingStories: Int =
        REMAINING_STORIES.getInt()
        set(remainingStories) = REMAINING_STORIES.put(
            remainingStories
        )

    fun clearData() {
        editor.clear()
        editor.commit()
    }

    val remainingStoriesLiveData =
        SharedPreferenceIntLiveData(
            pref,
            REMAINING_STORIES,
            -1
        )
}
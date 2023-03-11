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
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PrefRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PrefRepository {
    private val pref: SharedPreferences = context.getSharedPreferences(
        USER_PREFERENCES, Context.MODE_PRIVATE
    )
    private val editor = pref.edit()

    override fun String.put(int: Int) {
        editor.putInt(this, int)
        editor.commit()
    }

    override fun String.put(string: String) {
        editor.putString(this, string)
        editor.commit()
    }

    override fun String.put(boolean: Boolean) {
        editor.putBoolean(this, boolean)
        editor.commit()
    }

    override fun String.getInt() = pref.getInt(this, 0)

    override fun String.getString() = pref.getString(this, "")!!

    override fun String.getBoolean() = pref.getBoolean(this, false)

    override var userId: Int = USER_ID.getInt()
        set(id) = USER_ID.put(id)

    override var userName: String = USER_NAME.getString()
        set(name) = USER_NAME.put(name)

    override var userEmail: String = USER_EMAIL.getString()
        set(email) = USER_EMAIL.put(email)

    override var userApiToken: String = "Bearer tok_Q22sw7X2iN2jcVQgRfRRo8tm4anlVwX2AVgvZH7amzs0HqNRtDpBxoZtCK7h"// USER_API_TOKEN.getString()TODO(Change this back after fix)
        set(apiToken) = USER_API_TOKEN.put(apiToken)

    override var isUserGuest: Boolean = USER_IS_GUEST.getBoolean()
        set(isGuest) = USER_IS_GUEST.put(isGuest)

    override var remainingStories: Int = REMAINING_STORIES.getInt()
        set(remainingStories) = REMAINING_STORIES.put(remainingStories)

    override fun clearData() {
        editor.clear()
        editor.commit()
    }

    override val remainingStoriesLiveData = SharedPreferenceIntLiveData(
        pref, REMAINING_STORIES, -1
    )
}

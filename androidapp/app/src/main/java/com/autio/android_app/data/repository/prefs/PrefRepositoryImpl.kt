package com.autio.android_app.data.repository.prefs

import android.content.Context
import android.content.SharedPreferences
import com.autio.android_app.util.Constants.USER_PREFERENCES
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PrefRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PrefRepository {
    private val pref: SharedPreferences = context.getSharedPreferences(
        USER_PREFERENCES, Context.MODE_PRIVATE
    )
    private val editor = pref.edit()

    fun String.put(int: Int) {
        editor.putInt(this, int)
        editor.commit()
    }

    fun String.put(string: String) {
        editor.putString(this, string)
        editor.commit()
    }

    fun String.put(boolean: Boolean) {
        editor.putBoolean(this, boolean)
        editor.commit()
    }

    fun String.getInt() = pref.getInt(this, 0)

    fun String.getString() = pref.getString(this, "")!!

    fun String.getBoolean() = pref.getBoolean(this, false)

    override fun clearData() {
        editor.clear()
        editor.commit()
    }

}

package com.autio.android_app.util

import android.content.Context
import com.autio.android_app.R
import com.autio.android_app.data.api.model.State
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class FetchJSONFromModule {
    companion object {
        private fun readJSONFromAsset(
            context: Context
        ): String? {
            return try {
                val inputStream =
                    context.resources.openRawResource(
                        R.raw.states
                    )
                inputStream.bufferedReader()
                    .use { it.readText() }
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
            }
        }

        fun parseJSON(
            context: Context
        ): List<com.autio.android_app.data.api.model.State> {
            val typeOfMap =
                object :
                    TypeToken<List<com.autio.android_app.data.api.model.State>>() {}.type
            return GsonBuilder().create()
                .fromJson(
                    readJSONFromAsset(
                        context
                    ),
                    typeOfMap
                )
        }
    }
}

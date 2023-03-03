package com.autio.android_app.data.api.model.modelLegacy

import android.content.Context
import android.net.Uri
import com.autio.android_app.R
import kotlin.math.floor

data class NowPlayingMetadata(
    val id: String,
    val artUri: Uri,
    val title: String?,
    val subtitle: String?,
    val narrator: String?,
    val author: String?,
    val description: String?,
    val duration: Long,
    val durationText: String,
    val category: String
) {
    companion object {
        /**
         * Utility method to convert milliseconds to a display of minutes and seconds
         */
        fun timestampToMSS(
            context: Context,
            position: Long
        ): String {
            if (position < 0) return context.getString(
                R.string.duration_unknown
            )
            val totalSeconds =
                floor(
                    position / 1E3
                ).toInt()
            val minutes =
                totalSeconds / 60
            val remainingSeconds =
                totalSeconds - (minutes * 60)
            return context.getString(
                R.string.duration_format
            )
                .format(
                    minutes,
                    remainingSeconds
                )
        }
    }
}

package com.autio.android_app.extensions

import android.content.Context
import com.autio.android_app.R
import kotlin.math.floor

fun Long.timestampToMSS(
    context: Context
): String {
    if (this < 0) return context.getString(
        R.string.duration_unknown
    )
    val totalSeconds =
        floor(
            this / 1E3
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
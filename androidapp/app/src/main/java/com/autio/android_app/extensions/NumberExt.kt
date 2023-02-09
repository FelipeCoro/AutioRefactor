package com.autio.android_app.extensions

import android.content.Context
import android.util.TypedValue
import java.text.SimpleDateFormat
import java.util.*

fun Number.toPx(
    context: Context
): Number {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        context.resources.displayMetrics
    )
        .toInt()
}

fun Long.toDateTime(): String? {
    val sdf =
        SimpleDateFormat.getDateInstance()
    val netDate =
        Date(
            this
        )
    return sdf.format(
        netDate
    )
}
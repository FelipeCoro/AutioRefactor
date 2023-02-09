package com.autio.android_app.extensions

import android.net.Uri
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.*

/**
 * Checks if a [String] contains another in a case insensitive way
 */
fun String?.containsCaseInsensitive(
    other: String?
) =
    if (this != null && other != null) {
        lowercase(
            Locale.getDefault()
        ).contains(
            other.lowercase(
                Locale.getDefault()
            )
        )
    } else {
        this == other
    }

/**
 * Encodes a [String] with URL
 *
 * Returns empty when called on null
 */
inline val String?.urlEncoded: String
    get() = if (Charset.isSupported(
            "UTF-8"
        )
    ) {
        URLEncoder.encode(
            this
                ?: "",
            "UTF-8"
        )
    } else {
        URLEncoder.encode(
            this
                ?: ""
        )
    }

/**
 * Converts a potentially null [String] to a [Uri] falling back to [Uri.EMPTY]
 */
fun String?.toUri(): Uri =
    this?.let {
        Uri.parse(
            it
        )
    }
        ?: Uri.EMPTY
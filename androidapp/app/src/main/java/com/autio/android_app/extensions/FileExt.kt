package com.autio.android_app.extensions

import android.content.ContentResolver
import android.net.Uri
import java.io.File

/**
 * Returns a Content Uri for the AlbumArtContentProvider
 */
fun File.asAlbumArtContentUri(): Uri {
    return Uri.Builder()
        .scheme(
            ContentResolver.SCHEME_CONTENT
        )
        .authority(
            "com.autio.android_app"
        )
        .appendPath(
            this.path
        )
        .build()
}
package com.autio.android_app.player.library

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit

const val DOWNLOAD_TIMEOUT_SECONDS =
    30L

internal class AlbumArtContentProvider :
    ContentProvider() {

    companion object {
        private val uriMap =
            mutableMapOf<Uri, Uri>()

        fun mapUri(
            uri: Uri
        ): Uri {
            val path =
                uri.encodedPath?.substring(
                    1
                )
                    ?.replace(
                        '/',
                        ':'
                    )
                    ?: return Uri.EMPTY
            val contentUri =
                Uri.Builder()
                    .scheme(
                        ContentResolver.SCHEME_CONTENT
                    )
                    .authority(
                        "com.autio.android_app"
                    )
                    .path(
                        path
                    )
                    .build()
            uriMap[contentUri] =
                uri
            return contentUri
        }
    }

    override fun onCreate() =
        true

    override fun openFile(
        uri: Uri,
        mode: String
    ): ParcelFileDescriptor? {
        val context =
            this.context
                ?: return null
        val remoteUri =
            uriMap[uri]
                ?: throw FileNotFoundException(
                    uri.path
                )

        var file =
            File(
                context.cacheDir,
                uri.path
            )

        if (!file.exists()) {
            val cacheFile =
                Glide.with(
                    context
                )
                    .asFile()
                    .load(
                        remoteUri
                    )
                    .submit()
                    .get(
                        DOWNLOAD_TIMEOUT_SECONDS,
                        TimeUnit.SECONDS
                    )

            file =
                cacheFile
        }
        return ParcelFileDescriptor.open(
            file,
            ParcelFileDescriptor.MODE_READ_ONLY
        )
    }

    override fun insert(
        uri: Uri,
        values: ContentValues?
    ): Uri? =
        null

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? =
        null

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?
    ) =
        0

    override fun update(
        p0: Uri,
        p1: ContentValues?,
        p2: String?,
        p3: Array<out String>?
    ) =
        0

    override fun getType(
        uri: Uri
    ): String? =
        null
}
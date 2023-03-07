package com.autio.android_app.ui.stories.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Author(
    val id: Int,
    val name: String,
    val biography: String,
    val url: String?,
    val imageUrl: String?
):Parcelable

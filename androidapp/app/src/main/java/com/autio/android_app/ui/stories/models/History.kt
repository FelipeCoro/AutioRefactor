package com.autio.android_app.ui.stories.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class History(
    val storyId: Int,
    val playedAt: String
): Parcelable


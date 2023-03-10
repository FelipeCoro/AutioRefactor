package com.autio.android_app.ui.stories.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@kotlinx.serialization.Serializable
data class Category(
    var id: Int = 0,
    var firebaseId: String = "",
    var title: String = "",
    var order: Int = 0
) : Parcelable

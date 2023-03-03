package com.autio.android_app.ui.view.usecases.home.fragment.stories.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@kotlinx.serialization.Serializable
data class Category(
    var id: Int = 0,
    var firebaseId: String = "",
    var title: String = "",
    var order: Int = 0
)

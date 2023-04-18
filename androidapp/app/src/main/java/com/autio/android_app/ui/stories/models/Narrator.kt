package com.autio.android_app.ui.stories.models

import kotlinx.serialization.SerialName

data class Narrator(
    val id: Int = 0,
    val name: String = "",
    val biography: String = "",
    val url: String? = "",
    val imageUrl: String? = ""
)

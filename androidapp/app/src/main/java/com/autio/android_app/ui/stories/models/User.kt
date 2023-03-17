package com.autio.android_app.ui.stories.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class User(
    val id: Int,
    val name:String = "",
    val email:String = "",
    val apiToken: String,
    val isGuest: Boolean,
    var remainingStories: Int = 5,
    var userSubIsActive: Boolean = false,
    var isPremiumUser: Boolean = false
) : Parcelable

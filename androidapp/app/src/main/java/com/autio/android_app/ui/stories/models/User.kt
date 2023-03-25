package com.autio.android_app.ui.stories.models

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize


@Parcelize
data class User(
    val id: Int,
    val name: String = "",
    val email: String = "",
    val apiToken: String,
    var isGuest: Boolean,
    var remainingStories: Int = 5,
    var isPremiumUser: Boolean = false
) : Parcelable {
    @IgnoredOnParcel
    val bearerToken = "Bearer $apiToken"
    override fun toString(): String {
        return "User(id=$id, name='$name', email='$email', apiToken='$apiToken', isGuest=$isGuest, remainingStories=$remainingStories, isPremiumUser=$isPremiumUser, bearerToken='$bearerToken')"
    }


}

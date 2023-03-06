package com.autio.android_app.ui.stories.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AccountRequest(
    val email: String,

    val emailConfirmation: String,

    val password: String,

    val passwordConfirmation: String,

    val name: String

):Parcelable

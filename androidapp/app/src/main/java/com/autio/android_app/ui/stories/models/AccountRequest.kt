package com.autio.android_app.ui.stories.models

import com.google.gson.annotations.SerializedName

data class AccountRequest(
    val email: String,

    val emailConfirmation: String,

    val password: String,

    val passwordConfirmation: String,

    val name: String

)

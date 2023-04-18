package com.autio.android_app.ui.login.viewstates

import com.autio.android_app.ui.stories.models.User

sealed interface LoginViewState{
    object LoginError:LoginViewState
    data class LoginSuccess(val data:User):LoginViewState
    data class GuestLoginSuccess(val data:User):LoginViewState
}

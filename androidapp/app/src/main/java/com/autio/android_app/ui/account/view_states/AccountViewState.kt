package com.autio.android_app.ui.account.view_states

sealed interface AccountViewState {
    object OnSuccessPasswordChanged : AccountViewState
    object OnFailedPasswordChanged : AccountViewState
}

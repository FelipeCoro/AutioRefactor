package com.autio.android_app.ui.account.view_states

import com.autio.android_app.ui.stories.models.User

sealed interface AccountViewState {
    object OnSuccessPasswordChanged : AccountViewState
    object OnFailedPasswordChanged : AccountViewState
    data class OnUserDataFetched(val data : User):AccountViewState
}

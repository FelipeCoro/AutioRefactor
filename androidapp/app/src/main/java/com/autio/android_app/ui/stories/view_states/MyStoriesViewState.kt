package com.autio.android_app.ui.stories.view_states

import com.autio.android_app.ui.stories.models.User

sealed interface MyStoriesViewState {
    data class OnGetUser(val user: User) : MyStoriesViewState
    object OnGetUserFailed : MyStoriesViewState
}
package com.autio.android_app.ui.stories.view_states

sealed interface PlayerViewState {
    object OnNotPremiumUser : PlayerViewState
    object OnShareStoriesSuccess : PlayerViewState

    object OnHandleRewindClickSuccess : PlayerViewState
    data class OnChangeProgressSuccess(val progress: Int) : PlayerViewState
}

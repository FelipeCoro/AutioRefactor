package com.autio.android_app.ui.stories.view_states

sealed interface PlayerViewState {
    object OnNotPremiumUser : PlayerViewState
    object OnShareStoriesSuccess : PlayerViewState
}

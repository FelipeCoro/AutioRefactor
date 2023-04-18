package com.autio.android_app.ui.stories.view_states

import com.autio.android_app.ui.stories.models.Story

sealed interface BottomNavigationViewState {
    data class FetchedStoryToPlay(val story: Story) : BottomNavigationViewState
    object FetchedStoryToPlayFailed : BottomNavigationViewState
    data class OnPlayMediaSuccess(val id: Int) : BottomNavigationViewState

    data class RemainingStories(val remainingStories: Int) : BottomNavigationViewState
    object OnNotPremiumUser : BottomNavigationViewState
}


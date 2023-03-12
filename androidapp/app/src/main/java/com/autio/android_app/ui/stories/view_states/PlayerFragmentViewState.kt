package com.autio.android_app.ui.stories.view_states

import com.autio.android_app.ui.stories.models.Story

sealed interface PlayerViewState{
data class FetchedCurrentStory(val story: Story) : PlayerViewState
object FetchedStoriesByIdsFailed : PlayerViewState

}

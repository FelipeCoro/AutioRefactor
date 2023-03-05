package com.autio.android_app.ui.stories.view_states

import com.autio.android_app.ui.stories.models.Story

sealed interface StoryViewState {
    data class FetchedStoriesByIds(val stories: List<Story>) : StoryViewState
    object FetchedStoriesByIdsFailed : StoryViewState
    data class FetchedAllStories(val stories: List<Story>) : StoryViewState
    object FetchedAllStoriesFailed : StoryViewState
}
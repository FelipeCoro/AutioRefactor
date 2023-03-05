package com.autio.android_app.ui.stories.view_states

import com.autio.android_app.data.database.entities.MapPoint
import com.autio.android_app.ui.stories.models.Story

sealed interface StoryViewState {
    data class FetchedStoriesById(val stories: List<MapPoint>) : StoryViewState
    object FetchedStoriesByIdFailed : StoryViewState
    data class FetchedAllStories(val stories: List<Story>) : StoryViewState
    object FetchedAllStoriesFailed : StoryViewState
}
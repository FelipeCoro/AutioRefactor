package com.autio.android_app.ui.stories.view_states

import com.autio.android_app.ui.stories.models.Author
import com.autio.android_app.ui.stories.models.Story

sealed interface StoryViewState {
    data class FetchedStoriesByIds(val stories: List<Story>) : StoryViewState
    object FetchedStoriesByIdsFailed : StoryViewState
    data class FetchedAllStories(val stories: List<Story>) : StoryViewState
    object FetchedAllStoriesFailed : StoryViewState
    data class FetchedAuthor(val author: Author) : StoryViewState
    object FetchedAuthorFailed : StoryViewState
    object AddedBookmark : StoryViewState
    object RemovedBookmark : StoryViewState
    object FailedBookmark : StoryViewState
    object StoryLiked : StoryViewState
    object FailedLikedStory : StoryViewState
    object LikedRemoved : StoryViewState
    object FailedLikedRemoved : StoryViewState
    object StoryRemoved : StoryViewState
    object FailedStoryRemoved : StoryViewState

}
package com.autio.android_app.ui.stories.view_states

import com.autio.android_app.ui.stories.models.Author
import com.autio.android_app.ui.stories.models.Story

sealed interface StoryViewState {
    data class FetchedStoriesByIds(val stories: List<Story>) : StoryViewState
    object FetchedStoriesByIdsFailed : StoryViewState
    data class FetchedAllStories(val stories: List<Story>) : StoryViewState
    object FetchedAllStoriesFailed : StoryViewState

    data class FetchedAllDownloadedStories(val stories: List<Story>) : StoryViewState
    object FetchedAllDownloadedStoriesFailed : StoryViewState

    data class FetchedBookmarkedStories(val stories: List<Story>) : StoryViewState
    object FetchedBookmarkedStoriesFailed : StoryViewState

    data class StoryIsBookmarked (val status:Boolean):StoryViewState

    data class FetchedFavoriteStories(val stories: List<Story>) : StoryViewState
    object FetchedFavoriteStoriesFailed : StoryViewState

    data class FetchedStoriesHistory(val stories: List<Story>) : StoryViewState
    object FetchedStoriesHistoryFailed : StoryViewState

    data class FetchedAuthor(val author: Author) : StoryViewState
    object FetchedAuthorFailed : StoryViewState
    object AddedBookmark : StoryViewState
    object RemovedBookmark : StoryViewState
    object FailedBookmark : StoryViewState
    class StoryLiked(val likeCount: Int) : StoryViewState
    object FailedLikedStory : StoryViewState
    class LikedRemoved(val likeCount: Int) : StoryViewState
    object FailedLikedRemoved : StoryViewState
    object StoryDownloaded : StoryViewState
    object FailedStoryDownloaded : StoryViewState
    object StoryRemoved : StoryViewState
    object FailedStoryRemoved : StoryViewState
    data class StoryLikesCount(val storyLikesCount: Int) : StoryViewState
    object FailedStoryLikesCount : StoryViewState

    data class IsStoryLiked(val isLiked: Boolean) : StoryViewState


}

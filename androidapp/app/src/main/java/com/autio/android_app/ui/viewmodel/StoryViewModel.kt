package com.autio.android_app.ui.viewmodel

import androidx.lifecycle.*
import com.autio.android_app.data.database.repository.StoryRepository
import com.autio.android_app.data.model.history.History
import com.autio.android_app.data.model.story.DownloadedStory
import com.autio.android_app.data.model.story.Story
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StoryViewModel(
    private val storyRepository: StoryRepository
) : ViewModel() {

    fun getStoriesByIds(
        ids: Array<Int>
    ): LiveData<Array<Story>> =
        storyRepository.getStoriesByIds(
            ids
        )
            .asLiveData()

    val downloadedStories =
        storyRepository.getDownloadedStories.asLiveData()

    fun downloadStory(
        story: DownloadedStory
    ) =
        storyRepository.downloadStory(
            story
        )

    fun removeDownloadedStory(
        id: String
    ) =
        storyRepository.removeDownloadedStory(
            id
        )

    val bookmarkedStories =
        storyRepository.bookmarkedStories.asLiveData()

    fun bookmarkStory(
        id: String
    ) {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            storyRepository.bookmarkStory(
                id
            )
        }
    }

    fun removeBookmarkFromStory(
        id: String
    ) {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            storyRepository.removeBookmarkFromStory(
                id
            )
        }
    }

    val favoriteStories =
        storyRepository.favoriteStories.asLiveData()

    fun setLikeToStory(
        id: String
    ) {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            storyRepository.giveLikeToStory(
                id
            )
        }
    }

    fun removeLikeFromStory(
        id: String
    ) {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            storyRepository.removeLikeFromStory(
                id
            )
        }
    }

    val storiesHistory =
        storyRepository.history.asLiveData()

    fun addStoryToHistory(
        history: History
    ) {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            storyRepository.addStoryToHistory(
                history
            )
        }
    }

    fun removeStoryFromHistory(
        id: String
    ) {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            storyRepository.removeStoryFromHistory(
                id
            )
        }
    }

    fun clearStoryHistory() =
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            storyRepository.clearStoryHistory()
        }

    fun cacheRecordOfStory(
        storyId: String,
        recordUrl: String
    ) {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            storyRepository.cacheRecordOfStory(
                storyId,
                recordUrl
            )
        }
    }

    fun cacheRecordOfStory(
        storyId: Int,
        recordUrl: String
    ) {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            storyRepository.cacheRecordOfStory(
                storyId,
                recordUrl
            )
        }
    }

    class Factory(
        private val storyRepository: StoryRepository
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            if (modelClass.isAssignableFrom(
                    StoryViewModel::class.java
                )
            ) {
                @Suppress(
                    "unchecked_cast"
                )
                return StoryViewModel(
                    storyRepository
                ) as T
            }
            throw IllegalArgumentException(
                "Unknown ViewModel class"
            )
        }
    }
}
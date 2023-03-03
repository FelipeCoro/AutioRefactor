package com.autio.android_app.ui.viewmodel

import androidx.lifecycle.*
import com.autio.android_app.data.repository.datasource.local.AutioLocalDataSourceImpl
import com.autio.android_app.data.entities.history.History
import com.autio.android_app.data.entities.story.DownloadedStory
import com.autio.android_app.data.entities.story.Story
import kotlinx.coroutines.*

class StoryViewModel(
    private val autioLocalDataSourceImpl: AutioLocalDataSourceImpl
) : ViewModel() {

    val userCategories =
        autioLocalDataSourceImpl.userCategories.asLiveData()

    val allStories = autioLocalDataSourceImpl.allStories.asLiveData()

    suspend fun getAllStories() : List<Story> {
        return autioLocalDataSourceImpl.getAllStories()
    }

    fun getStoriesByIds(
        ids: Array<Int>
    ): LiveData<Array<Story>> =
        autioLocalDataSourceImpl.getStoriesByIds(
            ids
        )
            .asLiveData()

    val downloadedStories =
        autioLocalDataSourceImpl.getDownloadedStories.asLiveData()

    fun downloadStory(
        story: DownloadedStory
    ) =
        autioLocalDataSourceImpl.downloadStory(
            story
        )

    fun removeDownloadedStory(
        id: String
    ) =
        autioLocalDataSourceImpl.removeDownloadedStory(
            id
        )

    fun removeAllDownloads() =
        autioLocalDataSourceImpl.removeAllDownloads()

    val bookmarkedStories =
        autioLocalDataSourceImpl.bookmarkedStories.asLiveData()

    fun bookmarkStory(
        id: String
    ) {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            autioLocalDataSourceImpl.bookmarkStory(
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
            autioLocalDataSourceImpl.removeBookmarkFromStory(
                id
            )
        }
    }

    fun removeAllBookmarks() {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            autioLocalDataSourceImpl.removeAllBookmarks()
        }
    }

    val favoriteStories =
        autioLocalDataSourceImpl.favoriteStories.asLiveData()

    fun setLikeToStory(
        id: String
    ) {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            autioLocalDataSourceImpl.giveLikeToStory(
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
            autioLocalDataSourceImpl.removeLikeFromStory(
                id
            )
        }
    }

    val storiesHistory =
        autioLocalDataSourceImpl.history.asLiveData()

    fun addStoryToHistory(
        history: History
    ) {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            autioLocalDataSourceImpl.addStoryToHistory(
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
            autioLocalDataSourceImpl.removeStoryFromHistory(
                id
            )
        }
    }

    fun clearStoryHistory() =
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            autioLocalDataSourceImpl.clearStoryHistory()
        }

    fun cacheRecordOfStory(
        storyId: String,
        recordUrl: String
    ) {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            autioLocalDataSourceImpl.cacheRecordOfStory(
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
            autioLocalDataSourceImpl.cacheRecordOfStory(
                storyId,
                recordUrl
            )
        }
    }

    /**
     * Clears columns to be specific for a user
     * i. e. listened at story column
     */
    fun clearUserData() {
        viewModelScope.launch(
            Dispatchers.IO
        ) {
            autioLocalDataSourceImpl.clearUserData()
        }
    }

    class Factory(
        private val autioLocalDataSourceImpl: AutioLocalDataSourceImpl
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
                    autioLocalDataSourceImpl
                ) as T
            }
            throw IllegalArgumentException(
                "Unknown ViewModel class"
            )
        }
    }
}

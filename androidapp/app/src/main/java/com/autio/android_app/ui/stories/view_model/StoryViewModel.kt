package com.autio.android_app.ui.stories.view_model

import androidx.lifecycle.*
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.database.entities.HistoryEntity

import com.autio.android_app.data.database.entities.MapPoint
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.util.coroutines.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val autioRepository: AutioRepository,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher
) : ViewModel() {

    val userCategories = autioRepository.userCategories.asLiveData()
    val allStories = autioRepository.allStories.asLiveData()
    val downloadedStories = autioRepository.getDownloadedStories.asLiveData()
    val bookmarkedStories = autioRepository.bookmarkedStories.asLiveData()
    val storiesHistory = autioRepository.history.asLiveData()
    val favoriteStories = autioRepository.favoriteStories.asLiveData()


    fun getAllStories(): List<MapPoint> {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.getAllStories()
        }
    }

    fun getStoriesByIds(ids: List<Int>): LiveData<List<Story>> {
        autioRepository.getStoriesByIds(ids)
    }

    fun downloadStory(story: DownloadedStoryEntity) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.downloadStory(story)
        }
    }

    fun removeDownloadedStory(id: String) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.removeDownloadedStory(id)
        }
    }

    fun removeAllDownloads() {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.removeAllDownloads()
        }
    }


    fun bookmarkStory(id: String) {
        viewModelScope.launch(coroutineDispatcher) {
                autioRepository.bookmarkStory(id)
    }

    fun removeBookmarkFromStory(id: String) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.removeBookmarkFromStory(id) }
    }

    fun removeAllBookmarks() {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.removeAllBookmarks() }
    }


    fun setLikeToStory(id: String) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.giveLikeToStory(id)
        } }
    }

    fun removeLikeFromStory(id: String) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.removeLikeFromStory(id) }
    }

    fun addStoryToHistory(history: HistoryEntity) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.addStoryToHistory(history) }
    }

    fun removeStoryFromHistory(id: String) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.removeStoryFromHistory(id) }
    }

    fun clearStoryHistory() =
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.clearStoryHistory() }

    fun cacheRecordOfStory(storyId: String, recordUrl: String) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.cacheRecordOfStory(storyId, recordUrl)
        }
    }

    fun cacheRecordOfStory(storyId: Int, recordUrl: String) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.cacheRecordOfStory(storyId.toString(), recordUrl)
        }
    }

    /**
     * Clears columns to be specific for a user
     * i. e. listened at story column
     */
    fun clearUserData() {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.clearUserData()
        }
    }
}

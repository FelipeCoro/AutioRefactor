package com.autio.android_app.ui.stories.view_model

import androidx.lifecycle.*
import com.autio.android_app.data.repository.datasource.local.AutioLocalDataSource
import com.autio.android_app.data.repository.datasource.local.AutioLocalDataSourceImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val autioLocalDataSource: AutioLocalDataSource
) : ViewModel() {

    val userCategories = autioLocalDataSource.userCategories.asLiveData()

    val allStories = autioLocalDataSource.allStories.asLiveData()

    suspend fun getAllStories(): List<Story> {
        return autioLocalDataSource.getAllStories()
    }

    fun getStoriesByIds(ids: Array<Int>): LiveData<Array<Story>> =
        autioLocalDataSource.getStoriesByIds(ids).asLiveData()

    val downloadedStories = autioLocalDataSource.getDownloadedStories.asLiveData()

    fun downloadStory(story: DownloadedStory) = autioLocalDataSource.downloadStory(story)

    fun removeDownloadedStory(id: String) = autioLocalDataSource.removeDownloadedStory(id)

    fun removeAllDownloads() = autioLocalDataSource.removeAllDownloads()

    val bookmarkedStories = autioLocalDataSource.bookmarkedStories.asLiveData()

    fun bookmarkStory(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                autioLocalDataSource.bookmarkStory(id)
            }.onSuccess {

            }.onFailure {

            }
        }
    }

    fun removeBookmarkFromStory(id: String) {
        viewModelScope.launch(Dispatchers.IO) { autioLocalDataSource.removeBookmarkFromStory(id) }
    }

    fun removeAllBookmarks() {
        viewModelScope.launch(Dispatchers.IO) { autioLocalDataSource.removeAllBookmarks() }
    }

    val favoriteStories = autioLocalDataSource.favoriteStories.asLiveData()

    fun setLikeToStory(id: String) {
        viewModelScope.launch(Dispatchers.IO) { autioLocalDataSource.giveLikeToStory(id) }
    }

    fun removeLikeFromStory(id: String) {
        viewModelScope.launch(Dispatchers.IO) { autioLocalDataSource.removeLikeFromStory(id) }
    }

    val storiesHistory = autioLocalDataSource.history.asLiveData()

    fun addStoryToHistory(history: History) {
        viewModelScope.launch(Dispatchers.IO) { autioLocalDataSource.addStoryToHistory(history) }
    }

    fun removeStoryFromHistory(id: String) {
        viewModelScope.launch(Dispatchers.IO) { autioLocalDataSource.removeStoryFromHistory(id) }
    }

    fun clearStoryHistory() =
        viewModelScope.launch(Dispatchers.IO) { autioLocalDataSource.clearStoryHistory() }

    fun cacheRecordOfStory(storyId: String, recordUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            autioLocalDataSource.cacheRecordOfStory(storyId, recordUrl)
        }
    }

    fun cacheRecordOfStory(storyId: Int, recordUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            autioLocalDataSource.cacheRecordOfStory(storyId, recordUrl)
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
            autioLocalDataSource.clearUserData()
        }
    }
}

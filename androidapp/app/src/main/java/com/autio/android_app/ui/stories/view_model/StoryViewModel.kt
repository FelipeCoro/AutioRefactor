package com.autio.android_app.ui.stories.view_model

import androidx.lifecycle.*
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.ui.di.coroutines.IoDispatcher
import com.autio.android_app.ui.stories.models.Author
import com.autio.android_app.ui.stories.models.History
import com.autio.android_app.ui.stories.view_states.StoryViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val autioRepository: AutioRepository,
    private val prefRepository: PrefRepository,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _storyViewState = MutableLiveData<StoryViewState>()
    val storyViewState: LiveData<StoryViewState> = _storyViewState


    val userCategories = autioRepository.userCategories.asLiveData()
    val allStories = autioRepository.allStories.asLiveData()
    val downloadedStories = autioRepository.getDownloadedStories.asLiveData()
    val bookmarkedStories = autioRepository.bookmarkedStories.asLiveData()
    val storiesHistory = autioRepository.history.asLiveData()
    val favoriteStories = autioRepository.favoriteStories.asLiveData()

    private fun setViewState(newState: StoryViewState) {
        _storyViewState.postValue(newState)
    }

    fun getAllStories() {
        viewModelScope.launch(coroutineDispatcher) {
            kotlin.runCatching {
                autioRepository.getAllStories()
            }.onSuccess { result ->
                val stories = result.getOrNull() ?: listOf()
                setViewState(StoryViewState.FetchedAllStories(stories))
            }.onFailure {
                setViewState(StoryViewState.FetchedAllStoriesFailed)
            }
        }
    }

    fun getStoriesByIds(ids: List<Int>) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.getMapPointsByIds(ids).catch {
                setViewState(StoryViewState.FetchedStoriesByIdsFailed)
            }.collect {
                setViewState(StoryViewState.FetchedStoriesByIds(it))
            }
        }
    }

    fun downloadStory(story: DownloadedStoryEntity) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.downloadStory(story)
        }
    }

    fun getAuthorOfStory(xUserId: Int, apiToken: String, storyId: Int) {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                autioRepository.getAuthorOfStory(xUserId, apiToken, storyId)
            }.onSuccess { result ->
                val author = result.getOrNull()
                if (author != null) {
                    setViewState(StoryViewState.FetchedAuthor(author))
                    callContributor(author)
                }
            }.onFailure {
                setViewState(StoryViewState.FetchedAuthorFailed)
            }

        }
    }

    private fun callContributor(author: Author) {
        viewModelScope.launch(coroutineDispatcher) {
            val contributorApiResponse = autioRepository.getStoriesByContributor(
                prefRepository.userId, prefRepository.userApiToken, author.id, 1
            )
            contributorApiResponse.let { response ->
                val contributor = response.getOrNull()
                if (contributor != null) {
                    for (story in contributor.data) {
                        cacheRecordOfStory(
                            story.id, story.narrationUrl ?: ""
                        )
                    }
                    getStoriesByIds(contributor.data.map {
                        it.id
                    })
                }
            }
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
    }

    fun removeBookmarkFromStory(id: String) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.removeBookmarkFromStory(id)
        }
    }

    fun removeAllBookmarks() {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.removeAllBookmarks()
        }
    }

    fun setLikeToStory(id: String) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.giveLikeToStory(id)
        }
    }

    fun removeLikeFromStory(id: String) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.removeLikeFromStory(id)
        }
    }

    fun addStoryToHistory(history: History) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.addStoryToHistory(history)
        }
    }

    fun removeStoryFromHistory(id: String) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.removeStoryFromHistory(id)
        }
    }

    fun clearStoryHistory() = viewModelScope.launch(coroutineDispatcher) {
        autioRepository.clearStoryHistory()
    }

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



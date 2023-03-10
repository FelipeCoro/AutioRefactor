package com.autio.android_app.ui.stories.view_model

import androidx.lifecycle.*
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.ui.di.coroutines.IoDispatcher
import com.autio.android_app.ui.stories.models.Author
import com.autio.android_app.ui.stories.models.History
import com.autio.android_app.ui.stories.models.Story
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
    val downloadedStories = autioRepository.getDownloadedStories.asLiveData()
    val bookmarkedStories = autioRepository.bookmarkedStories.asLiveData()
    val storiesHistory = autioRepository.history.asLiveData()
    val favoriteStories = autioRepository.favoriteStories.asLiveData()

    private fun setViewState(newState: StoryViewState) {
        _storyViewState.postValue(newState)
    }

    fun getAllStories() {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.getAllStories().collect { result ->
                setViewState(
                    StoryViewState.FetchedAllStories(result ?: listOf())
                )
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

    fun downloadStory(story: Story) {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                autioRepository.downloadStory(story)
            }.onSuccess {
                setViewState(StoryViewState.StoryDownloaded)
            }.onFailure {
                setViewState(StoryViewState.FailedStoryDownloaded)
            }
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

    fun removeDownloadedStory(storyId: Int) {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                autioRepository.removeDownloadedStory(storyId)
            }.onSuccess {
                setViewState(StoryViewState.StoryRemoved)
            }.onFailure {
                setViewState(StoryViewState.FailedStoryRemoved)
            }
        }
    }

    fun removeAllDownloads() {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.removeAllDownloads()
        }
    }


    fun bookmarkStory(userId: Int, apiToken: String, storyId: Int) {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                autioRepository.bookmarkStory(userId, apiToken, storyId)
            }.onSuccess { result ->
                val bookmarked = result.getOrNull()
                bookmarked.let {
                    if (it == true) {
                        setViewState(StoryViewState.AddedBookmark)
                    } else setViewState(StoryViewState.FailedBookmark)
                }
            }.onFailure {
                setViewState(StoryViewState.FailedBookmark)
            }
        }
    }

    fun removeBookmarkFromStory(userId: Int, apiToken: String, storyId: Int) {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                autioRepository.removeBookmarkFromStory(userId, apiToken, storyId)
            }.onSuccess { result ->
                val bookmarked = result.getOrNull()
                bookmarked.let {
                    if (it == true) {
                        setViewState(StoryViewState.RemovedBookmark)
                    } else setViewState(StoryViewState.FailedBookmark)
                }
            }.onFailure {
                setViewState(StoryViewState.FailedBookmark)
            }
        }
    }

//TODO(Check with BackEnd for erase allBookmarks method)
// fun removeAllBookmarks() {
//     viewModelScope.launch(coroutineDispatcher) {
//         autioRepository.removeAllBookmarks()
//     }
// }

    fun giveLikeToStory(userId: Int, apiToken: String, storyId: Int) {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                autioRepository.giveLikeToStory(userId, apiToken, storyId)
            }.onSuccess { result ->
                val likedStory = result.getOrNull()
                likedStory.let {
                    if (it == true) {
                        setViewState(StoryViewState.StoryLiked)
                    } else setViewState(StoryViewState.FailedLikedStory)
                }
            }.onFailure {
                setViewState(StoryViewState.FailedLikedStory)
            }
        }
    }

    fun removeLikeFromStory(userId: Int, apiToken: String, storyId: Int) {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                autioRepository.removeLikeFromStory(userId, apiToken, storyId)
            }.onSuccess { result ->
                val removedLike = result.getOrNull()
                removedLike.let {
                    if (it == true) {
                        setViewState(StoryViewState.LikedRemoved)
                    } else setViewState(StoryViewState.FailedLikedRemoved)
                }
            }.onFailure {
                setViewState(StoryViewState.FailedLikedRemoved)
            }
        }
    }

    fun addStoryToHistory(history: History) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.addStoryToHistory(history)
        }
    }

    fun removeStoryFromHistory(id: Int) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.removeStoryFromHistory(id)
        }
    }

    fun clearStoryHistory() = viewModelScope.launch(coroutineDispatcher) {
        autioRepository.clearStoryHistory()
    }

    fun cacheRecordOfStory(storyId: Int, recordUrl: String) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.cacheRecordOfStory(storyId, recordUrl)
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



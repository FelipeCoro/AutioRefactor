package com.autio.android_app.ui.stories.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.domain.mappers.toModel
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.ui.di.coroutines.IoDispatcher
import com.autio.android_app.ui.stories.models.Author
import com.autio.android_app.ui.stories.models.History
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.ui.stories.view_states.StoryViewState
import com.google.android.gms.maps.model.LatLngBounds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val storiesHistory = autioRepository.history.asLiveData()
    val favoriteStories = autioRepository.favoriteStories.asLiveData()

    private fun setViewState(newState: StoryViewState) {
        _storyViewState.postValue(newState)
    }

    fun getAllStories() {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.getAllStories().collect { result ->
                setViewState(StoryViewState.FetchedAllStories(result ?: listOf()))
            }
        }
    }

    fun getStoriesByIds(userId: Int, apiToken: String, storyIds: List<Int>) {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                val result = autioRepository.getStoriesByIds(userId, apiToken, storyIds)
                result.getOrNull()
            }.onSuccess { stories ->
                stories?.let {
                    setViewState(StoryViewState.FetchedStoriesByIds(it))
                }
            }.onFailure {
                setViewState(StoryViewState.FetchedStoriesByIdsFailed)
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
                    getStoriesByIds(
                        prefRepository.userId,
                        prefRepository.userApiToken,
                        contributor.data.map { it.id })
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
                likedStory?.let { isItLiked ->
                    if (isItLiked.first) {
                        setViewState(StoryViewState.StoryLiked(isItLiked.second))
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
                removedLike?.let { isItLiked ->
                    if (!isItLiked.first) {
                        setViewState(StoryViewState.LikedRemoved(isItLiked.second))
                    } else setViewState(StoryViewState.FailedLikedRemoved)
                }
            }.onFailure {
                setViewState(StoryViewState.FailedLikedRemoved)
            }
        }
    }

    fun storyLikesCount(userId: Int, apiToken: String, storyId: Int) {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                autioRepository.storyLikesCount(userId, apiToken, storyId)
            }.onSuccess { result ->
                val storyLikes = result.getOrNull()
                storyLikes?.let {
                    setViewState(StoryViewState.StoryLikesCount(it))

                }
            }.onFailure {
                setViewState(StoryViewState.FailedStoryLikesCount)
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

    fun isStoryLiked(userId: Int, apiToken: String, storyId: Int) {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                autioRepository.isStoryLiked(userId, apiToken, storyId)
            }.onSuccess { result ->
                val isLiked = result.getOrNull()
                isLiked?.let {
                    setViewState(StoryViewState.IsStoryLiked(it))
                }
            }.onFailure {
                setViewState(StoryViewState.FailedStoryLikesCount)
            }
        }
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

    fun getStoriesInBounds(bounds: LatLngBounds) {
        viewModelScope.launch(coroutineDispatcher) {
            withContext(coroutineDispatcher) {
                val mapPoints =
                    autioRepository.getStoriesInLatLngBoundaries(bounds.southwest, bounds.northeast)
                val stories = mapPoints.map { it.toModel() }
                setViewState(StoryViewState.FetchedAllStories(stories))
            }
        }
    }

    fun getDownloadedStories() {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                autioRepository.getDownloadedStories()
            }.onSuccess { result ->
                val stories = result.getOrNull()
                stories?.let {
                    setViewState(StoryViewState.FetchedAllDownloadedStories(it))
                }
            }.onFailure {
                setViewState(StoryViewState.FetchedAllDownloadedStoriesFailed)

            }
        }
    }
}




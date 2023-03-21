package com.autio.android_app.ui.stories.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autio.android_app.data.database.entities.StoryEntity
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.domain.mappers.toModel
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.ui.di.coroutines.IoDispatcher
import com.autio.android_app.ui.stories.models.Author
import com.autio.android_app.ui.stories.models.History
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.ui.stories.view_states.PlayerViewState
import com.autio.android_app.ui.stories.view_states.StoryViewState
import com.google.android.gms.maps.model.LatLngBounds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val autioRepository: AutioRepository,
    private val prefRepository: PrefRepository,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher
) : ViewModel() {

    private lateinit var getStoriesJob: Job
    private val _storyViewState = MutableLiveData<StoryViewState>()

    val storyViewState: LiveData<StoryViewState> = _storyViewState


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

    fun getStoriesByIds(storyIds: List<Int>) {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                val result = autioRepository.getStoriesByIds(storyIds)
                result.getOrNull()
            }.onSuccess { stories ->
                stories?.let {
                    if (it.isNotEmpty()) {
                        setViewState(StoryViewState.FetchedStoriesByIds(it))
                    }
                }
            }.onFailure {
                setViewState(StoryViewState.FetchedStoriesByIdsFailed)
            }
        }
    }

    fun getBookmarkedStoriesByIds() {
        viewModelScope.launch(coroutineDispatcher) {
            val result = autioRepository.getUserBookmarkedStories()
            setViewState(StoryViewState.FetchedBookmarkedStories(result))
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

    fun getAuthorOfStory(storyId: Int) {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                autioRepository.getAuthorOfStory(storyId)
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
               author.id, 1
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
            val userAllowed = autioRepository.isUserAllowedToPlayStories()
            if (userAllowed) {
                autioRepository.removeAllDownloads()
            } else {
                setViewState(StoryViewState.OnNotPremiumUser)
            }
        }
    }

    fun bookmarkStory(storyId: Int) {
        viewModelScope.launch(coroutineDispatcher) {

            autioRepository.bookmarkStory(storyId)
            // }.onSuccess { result ->
            //  val bookmarked = result.getOrNull()
            //  bookmarked.let {
            //      if (it == true) {
            setViewState(StoryViewState.AddedBookmark)
            // } else setViewState(StoryViewState.FailedBookmark)
            // }
            // }.onFailure {
            //     setViewState(StoryViewState.FailedBookmark)
            // }
        }
    }

    fun removeBookmarkFromStory(storyId: Int) {
        viewModelScope.launch(coroutineDispatcher) {
            // runCatching {
            autioRepository.removeBookmarkFromStory(storyId)
            //  }.onSuccess { result ->
            //      //val bookmarked = result.getOrNull()
            //      //bookmarked.let {
            //    if (it == true) {
            setViewState(StoryViewState.RemovedBookmark)
            //      } else setViewState(StoryViewState.FailedBookmark)
            //  }
            // }.onFailure {
            //     setViewState(StoryViewState.RemovedBookmark)
            //     //setViewState(StoryViewState.FailedBookmark)
            // }
        }
    }

    fun removeAllBookmarks() {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.removeAllBookmarks()
        }
    }

    fun giveLikeToStory(storyId: Int) {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                autioRepository.giveLikeToStory(storyId)
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

    fun removeLikeFromStory(storyId: Int) {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                autioRepository.removeLikeFromStory(storyId)
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

    fun storyLikesCount(storyId: Int) {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                autioRepository.storyLikesCount(storyId)
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

    fun getAllFavoriteStories() {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                autioRepository.getUserFavoriteStories()
            }.onSuccess { result ->
                val likedStories = result.getOrNull()
                likedStories?.let {
                    setViewState(StoryViewState.FetchedFavoriteStories(it))
                }
            }.onFailure {
                setViewState(StoryViewState.FetchedFavoriteStoriesFailed)
            }
        }
    }

    fun isStoryLiked(storyId: Int) {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                autioRepository.isStoryLiked(storyId)
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

    fun removeAllLikedStories(stories: List<StoryEntity>) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.removeAllLikedStories(stories)
        }
    }

    fun isStoryBookmarked(storyId: Int) {
        viewModelScope.launch(coroutineDispatcher) {
            val stories = autioRepository.getUserBookmarkedStories()
            for (story in stories) {
                if (story.id == storyId) {
                    setViewState(StoryViewState.StoryIsBookmarked(true))
                } else {
                    setViewState(StoryViewState.StoryIsBookmarked(false))
                }
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
        if (::getStoriesJob.isInitialized && getStoriesJob.isActive) {
            getStoriesJob.cancel()
        }
        getStoriesJob = viewModelScope.launch(coroutineDispatcher) {
            withContext(coroutineDispatcher) {
                val mapPoints =
                    autioRepository.getStoriesInLatLngBoundaries(
                        bounds.southwest,
                        bounds.northeast
                    )
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

    fun getHistory() {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                autioRepository.getUserStoriesHistory()
            }.onSuccess { result ->
                val stories = result.getOrNull()
                stories?.let {
                    setViewState(StoryViewState.FetchedStoriesHistory(it))
                }
            }.onFailure {
                setViewState(StoryViewState.FetchedStoriesHistoryFailed)
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

}




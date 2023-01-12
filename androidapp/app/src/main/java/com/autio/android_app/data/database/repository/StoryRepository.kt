package com.autio.android_app.data.database.repository

import com.autio.android_app.data.database.dao.DownloadedStoryDao
import com.autio.android_app.data.database.dao.StoryDao
import com.autio.android_app.data.model.history.History
import com.autio.android_app.data.model.story.DownloadedStory
import com.autio.android_app.data.model.story.Story
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.Executors
import javax.inject.Inject

class StoryRepository @Inject constructor(
    private val storyDao: StoryDao,
    private val downloadedStoryDao: DownloadedStoryDao
) {
    private val executor =
        Executors.newSingleThreadExecutor()

    suspend fun getStoriesInLatLngBoundaries(
        swCoordinates: LatLng,
        neCoordinates: LatLng
    ): List<Story> {
        return storyDao.getStoriesInLatLngBoundaries(
            swCoordinates.latitude,
            swCoordinates.longitude,
            neCoordinates.latitude,
            neCoordinates.longitude
        )
    }

    suspend fun getStoryById(
        id: String
    ) =
        storyDao.getStoryById(
            id
        )

    fun getStoriesByIds(
        ids: Array<Int>
    ): Flow<Array<Story>> {
        return storyDao.readStoriesWithIds(
            ids
        )
    }

    suspend fun getLastModifiedStory(): Story? {
        return storyDao.readLastModifiedStory()
    }

    fun addStories(
        stories: Array<Story>
    ) {
        executor.execute {
            storyDao.addStories(
                stories
            )
        }
    }

    val bookmarkedStories =
        storyDao.getBookmarkedStories()

    fun setBookmarksDataToLocalStories(
        storiesIds: List<String>
    ) {
        executor.execute {
            storyDao.setBookmarksData(
                storiesIds
            )
        }
    }

    val favoriteStories =
        storyDao.getFavoriteStories()

    fun setLikesDataToLocalStories(
        storiesIds: List<String>
    ) {
        executor.execute {
            storyDao.setLikesData(
                storiesIds
            )
        }
    }

    suspend fun setListenedAtToLocalStories(
        storiesHistory: Array<History>
    ) {
        for (history in storiesHistory) {
            storyDao.setListenedAtData(
                history.storyId,
                history.playedAt
            )

            if (getDownloadedStoryById(
                    history.storyId
                ) != null
            ) {
                downloadedStoryDao.setListenedAtData(
                    history.storyId,
                    history.playedAt
                )
            }
        }
    }

    val history =
        storyDao.getHistory()

    suspend fun addStoryToHistory(
        history: History
    ) {
        storyDao.setListenedAtData(
            history.storyId,
            history.playedAt
        )

        if (getDownloadedStoryById(
                history.storyId
            ) != null
        ) {
            downloadedStoryDao.setListenedAtData(
                history.storyId,
                history.playedAt
            )
        }
    }

    suspend fun removeStoryFromHistory(
        id: String
    ) {
        storyDao.removeListenedAtData(
            id
        )

        if (getDownloadedStoryById(
                id
            ) != null
        ) {
            downloadedStoryDao.removeListenedAtData(
                id
            )
        }
    }

    fun clearStoryHistory() {
        storyDao.clearStoryHistory()
        downloadedStoryDao.clearStoryHistory()
    }

    suspend fun bookmarkStory(
        id: String
    ) {
        storyDao.setBookmarkToStory(
            id
        )

        if (getDownloadedStoryById(
                id
            ) != null
        ) {
            downloadedStoryDao.setBookmarkToStory(
                id
            )
        }
    }

    suspend fun removeBookmarkFromStory(
        id: String
    ) {
        storyDao.removeBookmarkFromStory(
            id
        )

        if (getDownloadedStoryById(
                id
            ) != null
        ) {
            downloadedStoryDao.removeBookmarkFromStory(
                id
            )
        }
    }

    suspend fun giveLikeToStory(
        id: String
    ) {
        storyDao.setLikeToStory(
            id
        )

        if (getDownloadedStoryById(
                id
            ) != null
        ) {
            downloadedStoryDao.setLikeToStory(
                id
            )
        }
    }

    suspend fun removeLikeFromStory(
        id: String
    ) {
        storyDao.removeLikeFromStory(
            id
        )

        if (getDownloadedStoryById(
                id
            ) != null
        ) {
            downloadedStoryDao.removeLikeFromStory(
                id
            )
        }
    }

    // Downloaded stories

    fun downloadStory(
        story: DownloadedStory
    ) {
        executor.execute {
            downloadedStoryDao.addStory(
                story
            )
        }
    }

    fun removeDownloadedStory(
        id: String
    ) {
        executor.execute {
            downloadedStoryDao.removeStory(
                id
            )
        }
    }

    suspend fun getDownloadedStoryById(
        id: String
    ): DownloadedStory? {
        return downloadedStoryDao.getStoryById(
            id
        )
    }

    val getDownloadedStories =
        downloadedStoryDao.readLiveStories()

    fun cacheRecordOfStory(
        storyId: String,
        recordUrl: String
    ) {
        executor.execute {
            storyDao.addRecordOfStory(
                storyId,
                recordUrl
            )
        }
    }

    fun cacheRecordOfStory(
        storyId: Int,
        recordUrl: String
    ) {
        executor.execute {
            storyDao.addRecordOfStory(
                storyId,
                recordUrl
            )
        }
    }

    fun deleteCachedData() {
        executor.execute {
            storyDao.deleteRecordUrls()
        }
    }
}
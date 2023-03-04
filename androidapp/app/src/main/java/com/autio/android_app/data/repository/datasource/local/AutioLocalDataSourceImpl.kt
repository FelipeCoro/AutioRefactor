package com.autio.android_app.data.repository.datasource.local

import com.autio.android_app.data.database.dao.CategoryDao
import com.autio.android_app.data.database.dao.DownloadedStoryDao
import com.autio.android_app.data.database.dao.StoryDao
import com.autio.android_app.data.database.entities.CategoryEntity
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.database.entities.HistoryEntity
import com.autio.android_app.data.database.entities.StoryEntity
import com.autio.android_app.ui.view.usecases.home.fragment.stories.models.Story
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.Executors
import javax.inject.Inject


class AutioLocalDataSourceImpl @Inject constructor(
    private val storyDao: StoryDao,
    private val downloadedStoryDao: DownloadedStoryDao,
    private val categoryDao: CategoryDao
) : AutioLocalDataSource {
    private val executor = Executors.newSingleThreadExecutor()
    val userCategories = categoryDao.readUserCategories()

    fun addUserCategories(categories: Array<CategoryEntity>) {
        executor.execute { categoryDao.addCategories(categories) }
    }

    suspend fun updateCategories(categories: Array<CategoryEntity>) {
        categoryDao.update(categories)
    }

    suspend fun getStoriesInLatLngBoundaries(
        swCoordinates: LatLng, neCoordinates: LatLng
    ): List<StoryEntity> {
        return storyDao.getStoriesInLatLngBoundaries(
            swCoordinates.latitude,
            swCoordinates.longitude,
            neCoordinates.latitude,
            neCoordinates.longitude
        )
    }

    val allStories = storyDao.readLiveStories()

    override suspend fun getAllStories(): List<Story> {
        return storyDao.allStories().map { }
    }

    override suspend fun getStoryById(id: String) = storyDao.getStoryById(id)

    override fun getStoriesByIds(ids: Array<Int>): Flow<Array<StoryEntity>> {
        return storyDao.readStoriesWithIds(ids)
    }

    override suspend fun getLastModifiedStory(): StoryEntity? {
        return storyDao.readLastModifiedStory()
    }

    override fun addStories(
        stories: Array<StoryEntity>
    ) {
        executor.execute { storyDao.addStories(stories) }
    }

    val bookmarkedStories = storyDao.getBookmarkedStories()

    override fun setBookmarksDataToLocalStories(storiesIds: List<String>) {
        executor.execute {
            storyDao.setBookmarksData(storiesIds)
        }
    }

    val favoriteStories = storyDao.getFavoriteStories()

    override fun setLikesDataToLocalStories(
        storiesIds: List<String>
    ) {
        executor.execute {
            storyDao.setLikesData(storiesIds)
        }
    }

    override suspend fun setListenedAtToLocalStories(
        storiesHistory: Array<HistoryEntity>
    ) {
        for (history in storiesHistory) {
            storyDao.setListenedAtData(
                history.storyId, history.playedAt
            )

            if (getDownloadedStoryById(
                    history.storyId
                ) != null
            ) {
                downloadedStoryDao.setListenedAtData(
                    history.storyId, history.playedAt
                )
            }
        }
    }

    val history = storyDao.getHistory()

    override suspend fun addStoryToHistory(
        history: HistoryEntity
    ) {
        storyDao.setListenedAtData(
            history.storyId, history.playedAt
        )

        if (getDownloadedStoryById(
                history.storyId
            ) != null
        ) {
            downloadedStoryDao.setListenedAtData(
                history.storyId, history.playedAt
            )
        }
    }

    override suspend fun markStoryAsListenedAtLeast30Secs(
        storyId: String
    ) {
        storyDao.markStoryAsListenedAtLeast30Secs(
            storyId
        )

        if (getDownloadedStoryById(
                storyId
            ) != null
        ) {
            downloadedStoryDao.markStoryAsListenedAtLeast30Secs(
                storyId
            )
        }
    }

    override suspend fun removeStoryFromHistory(
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

    override fun clearStoryHistory() {
        storyDao.clearStoryHistory()
        downloadedStoryDao.clearStoryHistory()
    }

    override suspend fun bookmarkStory(
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

    override suspend fun removeBookmarkFromStory(
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

    override fun removeAllBookmarks() {
        storyDao.removeAllBookmarks()
        downloadedStoryDao.removeAllBookmarks()
    }

    override suspend fun giveLikeToStory(
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

    override suspend fun removeLikeFromStory(
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

    override fun downloadStory(
        story: DownloadedStoryEntity
    ) {
        executor.execute {
            downloadedStoryDao.addStory(
                story
            )
        }
    }

    override fun removeDownloadedStory(
        id: String
    ) {
        executor.execute {
            downloadedStoryDao.removeStory(
                id
            )
        }
    }

    override fun removeAllDownloads() {
        executor.execute {
            downloadedStoryDao.clearTable()
        }
    }

    override suspend fun getDownloadedStoryById(
        id: String
    ): DownloadedStoryEntity? {
        return downloadedStoryDao.getStoryById(
            id
        )
    }

    val getDownloadedStories = downloadedStoryDao.readLiveStories()

    override fun cacheRecordOfStory(
        storyId: String, recordUrl: String
    ) {
        executor.execute {
            storyDao.addRecordOfStory(
                storyId, recordUrl
            )
        }
    }

    override fun cacheRecordOfStory(
        storyId: Int, recordUrl: String
    ) {
        executor.execute {
            storyDao.addRecordOfStory(
                storyId, recordUrl
            )
        }
    }

    override fun clearUserData() {
        executor.execute {
            storyDao.clearUserData()
            downloadedStoryDao.clearTable()
        }
    }

    override fun deleteCachedData() {
        executor.execute {
            storyDao.deleteRecordUrls()
        }
    }
}

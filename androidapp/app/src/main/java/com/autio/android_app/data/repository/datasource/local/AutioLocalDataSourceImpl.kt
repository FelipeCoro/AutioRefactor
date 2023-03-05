package com.autio.android_app.data.repository.datasource.local

import com.autio.android_app.data.database.dao.CategoryDao
import com.autio.android_app.data.database.dao.DownloadedStoryDao
import com.autio.android_app.data.database.dao.StoryDao
import com.autio.android_app.data.database.entities.CategoryEntity
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.database.entities.HistoryEntity
import com.autio.android_app.data.database.entities.MapPoint
import com.autio.android_app.ui.di.coroutines.IoDispatcher
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.concurrent.Executors
import javax.inject.Inject

class AutioLocalDataSourceImpl @Inject constructor(
    private val storyDao: StoryDao,
    private val downloadedStoryDao: DownloadedStoryDao,
    private val categoryDao: CategoryDao,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher
) : AutioLocalDataSource {
    private val executor = Executors.newSingleThreadExecutor()
    override val userCategories = categoryDao.readUserCategories()
    override val allLiveStories = storyDao.readLiveStories()
    override val getDownloadedStories = downloadedStoryDao.readLiveStories()
    override val bookmarkedStories = storyDao.getBookmarkedStories()
    override val favoriteStories = storyDao.getFavoriteStories()
    override val history = storyDao.getHistory()

    override suspend fun addUserCategories(categories: List<CategoryEntity>) {
        executor.execute { categoryDao.addCategories(categories) }
    }

    override suspend fun updateCategories(categories: List<CategoryEntity>) {
        categoryDao.update(categories)
    }

    override suspend fun getStoriesInLatLngBoundaries(
        swCoordinates: LatLng, neCoordinates: LatLng
    ): List<MapPoint> {
        return storyDao.getStoriesInLatLngBoundaries(
            swCoordinates.latitude,
            swCoordinates.longitude,
            neCoordinates.latitude,
            neCoordinates.longitude
        )
    }

    override suspend fun getAllStories(): Result<List<MapPoint>?> {
        return kotlin.runCatching { storyDao.allStories() }
            .onSuccess {
                Result.success(it)
            }.onFailure {
                Result.failure<List<MapPoint>?>(it)
            }
    }

    override suspend fun getMapPointById(id: String): Result<MapPoint?> {
        return kotlin.runCatching {
            storyDao.getMapPointById(id)
        }.onSuccess { Result.success(it) }.onFailure { Result.failure<MapPoint>(it) }
    }

    override suspend fun getMapPointsByIds(ids: List<Int>): Flow<List<MapPoint>> {
        val listMapPoints = mutableListOf<MapPoint>()
        ids.forEach { id ->
            storyDao.getMapPointById(id.toString())?.let {
                listMapPoints.add(it)
            }
        }
        return flowOf(listMapPoints)
    }

    override suspend fun getLastModifiedStory(): MapPoint? {
        return storyDao.readLastModifiedStory()
    }

    override suspend fun addStories(stories: List<MapPoint>) {
        executor.execute { storyDao.addStories(stories) }
    }

    override suspend fun setBookmarksDataToLocalStories(storiesIds: List<String>) {
        executor.execute {
            storyDao.setBookmarksData(storiesIds)
        }
    }

    override suspend fun setLikesDataToLocalStories(storiesIds: List<String>) {
        executor.execute {
            storyDao.setLikesData(storiesIds)
        }
    }

    override suspend fun setListenedAtToLocalStories(
        storiesHistory: List<HistoryEntity>
    ) {
        for (history in storiesHistory) {
            storyDao.setListenedAtData(history.storyId, history.playedAt)

            if (getDownloadedStoryById(history.storyId) != null) {
                downloadedStoryDao.setListenedAtData(
                    history.storyId, history.playedAt
                )
            }
        }
    }


    override suspend fun addStoryToHistory(history: HistoryEntity) {
        storyDao.setListenedAtData(
            history.storyId, history.playedAt
        )

        if (getDownloadedStoryById(history.storyId) != null) {
            downloadedStoryDao.setListenedAtData(
                history.storyId, history.playedAt
            )
        }
    }

    override suspend fun markStoryAsListenedAtLeast30Secs(storyId: String) {
        storyDao.markStoryAsListenedAtLeast30Secs(storyId)

        if (getDownloadedStoryById(storyId) != null) {
            downloadedStoryDao.markStoryAsListenedAtLeast30Secs(storyId)
        }
    }

    override suspend fun removeStoryFromHistory(id: String) {
        storyDao.removeListenedAtData(id)

        if (getDownloadedStoryById(id) != null) {
            downloadedStoryDao.removeListenedAtData(id)
        }
    }

    override suspend fun clearStoryHistory() {
        storyDao.clearStoryHistory()
        downloadedStoryDao.clearStoryHistory()
    }

    override suspend fun bookmarkStory(id: String) {
        storyDao.setBookmarkToStory(id)
        if (getDownloadedStoryById(id) != null) {
            downloadedStoryDao.setBookmarkToStory(id)
        }
    }

    override suspend fun removeBookmarkFromStory(id: String) {
        storyDao.removeBookmarkFromStory(id)

        if (getDownloadedStoryById(id) != null) {
            downloadedStoryDao.removeBookmarkFromStory(id)
        }
    }

    override suspend fun removeAllBookmarks() {
        storyDao.removeAllBookmarks()
        downloadedStoryDao.removeAllBookmarks()
    }

    override suspend fun giveLikeToStory(id: String) {
        storyDao.setLikeToStory(id)

        if (getDownloadedStoryById(id) != null) {
            downloadedStoryDao.setLikeToStory(id)
        }
    }

    override suspend fun removeLikeFromStory(id: String) {
        storyDao.removeLikeFromStory(id)

        if (getDownloadedStoryById(id) != null) {
            downloadedStoryDao.removeLikeFromStory(id)
        }
    }

    // Downloaded stories

    override suspend fun downloadStory(story: DownloadedStoryEntity) {
        executor.execute {
            downloadedStoryDao.addStory(story)
        }
    }

    override suspend fun removeDownloadedStory(id: String) {
        executor.execute {
            downloadedStoryDao.removeStory(id)
        }
    }

    override suspend fun removeAllDownloads() {
        executor.execute {
            downloadedStoryDao.clearTable()
        }
    }

    override suspend fun getDownloadedStoryById(id: String): DownloadedStoryEntity? {
        return downloadedStoryDao.getStoryById(id)
    }

    override suspend fun cacheRecordOfStory(storyId: String, recordUrl: String) {
        executor.execute {
            storyDao.addRecordOfStory(storyId, recordUrl)
        }
    }

    override suspend fun cacheRecordOfStory(storyId: Int, recordUrl: String) {
        executor.execute {
            storyDao.addRecordOfStory(storyId, recordUrl)
        }
    }

    override suspend fun clearUserData() {
        executor.execute {
            storyDao.clearUserData()
            downloadedStoryDao.clearTable()
        }
    }

    override suspend fun deleteCachedData() {
        executor.execute {
            storyDao.deleteRecordUrls()
        }
    }
}

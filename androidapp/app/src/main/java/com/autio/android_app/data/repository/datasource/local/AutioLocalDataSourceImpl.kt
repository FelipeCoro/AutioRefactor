package com.autio.android_app.data.repository.datasource.local

import com.autio.android_app.data.database.dao.CategoryDao
import com.autio.android_app.data.database.dao.DownloadedStoryDao
import com.autio.android_app.data.database.dao.MapPointDao
import com.autio.android_app.data.database.dao.StoryDao
import com.autio.android_app.data.database.entities.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.util.concurrent.Executors
import javax.inject.Inject

class AutioLocalDataSourceImpl @Inject constructor(
    private val mapPointDao: MapPointDao,
    private val downloadedStoryDao: DownloadedStoryDao,
    private val categoryDao: CategoryDao,
    private val storyDao: StoryDao
) : AutioLocalDataSource {
    private val executor = Executors.newSingleThreadExecutor()
    override val userCategories = categoryDao.readUserCategories()
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
    ): List<MapPointEntity> {
        return mapPointDao.getStoriesInLatLngBoundaries(
            swCoordinates.latitude,
            swCoordinates.longitude,
            neCoordinates.latitude,
            neCoordinates.longitude
        )
    }

    override fun getAllStories(): Flow<Result<List<MapPointEntity>?>> = flow {
        kotlin.runCatching { storyDao.readLiveStories() }
            .onSuccess {
                Result.success(it)
            }.onFailure {
                Result.failure<List<MapPointEntity>?>(it)
            }
    }

    override suspend fun getMapPointById(id: String): Result<MapPointEntity?> {
        return kotlin.runCatching {
            mapPointDao.getMapPointById(id)
        }.onSuccess { Result.success(it) }.onFailure { Result.failure<MapPointEntity>(it) }
    }

    override suspend fun getMapPointsByIds(ids: List<Int>): Flow<List<MapPointEntity>> {
        val listMapPointEntities = mutableListOf<MapPointEntity>()
        ids.forEach { id ->
            mapPointDao.getMapPointById(id.toString())?.let {
                listMapPointEntities.add(it)
            }
        }
        return flowOf(listMapPointEntities)
    }

    override suspend fun getLastModifiedStory(): Result<StoryEntity?> {

        return kotlin.runCatching {
            storyDao.readLastModifiedStory()
        }.onSuccess { Result.success(it) }.onFailure { Result.failure<MapPointEntity>(it) }
    }

    override suspend fun addStories(stories: List<MapPointEntity>) {
        executor.execute { mapPointDao.addStories(stories) }
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

    override suspend fun setListenedAtToLocalStories(storiesHistory: List<HistoryEntity>) {
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

    override suspend fun markStoryAsListenedAtLeast30Secs(storyId: Int) {
        storyDao.markStoryAsListenedAtLeast30Secs(storyId)

        if (getDownloadedStoryById(storyId) != null) {
            downloadedStoryDao.markStoryAsListenedAtLeast30Secs(storyId)
        }
    }

    override suspend fun removeStoryFromHistory(id: Int) {
        storyDao.removeListenedAtData(id)

        if (getDownloadedStoryById(id) != null) {
            downloadedStoryDao.removeListenedAtData(id)
        }
    }

    override suspend fun clearStoryHistory() {
        storyDao.clearStoryHistory()
        downloadedStoryDao.clearStoryHistory()
    }

    override suspend fun bookmarkStory(id: Int): DownloadedStoryEntity? {
        storyDao.setBookmarkToStory(id)
        val checkForStory = getDownloadedStoryById(id)
        return checkForStory?.let {
            downloadedStoryDao.setBookmarkToStory(id)
            checkForStory
        }

    }

    override suspend fun removeBookmarkFromStory(id: Int): DownloadedStoryEntity? {
        storyDao.removeBookmarkFromStory(id)
        val checkForStory = getDownloadedStoryById(id)
        return checkForStory?.let {
            downloadedStoryDao.removeBookmarkFromStory(id)
            checkForStory
        }
    }

    override suspend fun removeAllBookmarks() {
        storyDao.removeAllBookmarks()
        downloadedStoryDao.removeAllBookmarks()
    }

    override suspend fun giveLikeToStory(id: Int) {
        storyDao.setLikeToStory(id)

        if (getDownloadedStoryById(id) != null) {
            downloadedStoryDao.setLikeToStory(id)
        }
    }

    override suspend fun removeLikeFromStory(id: Int) {
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

    override suspend fun removeDownloadedStory(id: Int) {
        executor.execute {
            downloadedStoryDao.removeStory(id)
        }
    }

    override suspend fun removeAllDownloads() {
        executor.execute {
            downloadedStoryDao.clearTable()
        }
    }

    override suspend fun getDownloadedStoryById(id: Int): DownloadedStoryEntity? {
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

package com.autio.android_app.data.repository.datasource.local

import com.autio.android_app.data.database.dao.CategoryDao
import com.autio.android_app.data.database.dao.MapPointDao
import com.autio.android_app.data.database.dao.StoryDao
import com.autio.android_app.data.database.entities.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.concurrent.Executors
import javax.inject.Inject

class AutioLocalDataSourceImpl @Inject constructor(
    private val mapPointDao: MapPointDao,
    private val categoryDao: CategoryDao,
    private val storyDao: StoryDao
) : AutioLocalDataSource {
    private val executor = Executors.newSingleThreadExecutor()
    override val userCategories = categoryDao.readUserCategories()
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

    override suspend fun getAllStories() = mapPointDao.readLiveStories()


    override suspend fun getMapPointById(id: String): Result<MapPointEntity?> {
        return kotlin.runCatching {
            mapPointDao.getMapPointById(id)
        }.onSuccess { Result.success(it) }.onFailure { Result.failure<MapPointEntity>(it) }
    }

    override suspend fun getMapPointsByIds(ids: List<Int>): Result<List<MapPointEntity>> {
        val listMapPointEntities = mutableListOf<MapPointEntity>()
        ids.forEach { id ->
            mapPointDao.getMapPointById(id.toString())?.let {
                listMapPointEntities.add(it)
            }
        }
        return Result.success (listMapPointEntities)
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
        for (story in storiesHistory) {
            storyDao.setListenedAtData(story.storyId, story.playedAt)
//TODO(Check this)
            if (getDownloadedStoryById(story.storyId) != null) {
                storyDao.setListenedAtData(
                    story.storyId, story.playedAt
                )
            }
        }
    }


    override suspend fun addStoryToHistory(history: HistoryEntity) {
        storyDao.setListenedAtData(
            history.storyId, history.playedAt
        )

        if (getDownloadedStoryById(history.storyId) != null) {
            storyDao.setListenedAtData(
                history.storyId, history.playedAt
            )
        }
    }

    override suspend fun markStoryAsListenedAtLeast30Secs(storyId: Int) {
        storyDao.markStoryAsListenedAtLeast30Secs(storyId)

        if (getDownloadedStoryById(storyId) != null) {
            storyDao.markStoryAsListenedAtLeast30Secs(storyId)
        }
    }

    override suspend fun removeStoryFromHistory(id: Int) {
        storyDao.removeListenedAtData(id)

        if (getDownloadedStoryById(id) != null) {
            storyDao.removeListenedAtData(id)
        }
    }

    override suspend fun clearStoryHistory() {
        storyDao.clearStoryHistory()
        storyDao.clearStoryHistory()
    }

    override suspend fun bookmarkStory(id: Int): StoryEntity? {
        val checkForStory = getDownloadedStoryById(id)
        return checkForStory?.let {
            storyDao.setBookmarkToStory(id)
            checkForStory
        }

    }

    override suspend fun removeBookmarkFromStory(id: Int): StoryEntity? {
        storyDao.removeBookmarkFromStory(id)
        val checkForStory = getDownloadedStoryById(id)
        return checkForStory?.let {
            storyDao.removeBookmarkFromStory(id)
            checkForStory
        }
    }

    override suspend fun removeAllBookmarks() {
        storyDao.removeAllBookmarks()
        storyDao.removeAllBookmarks()
    }

    override suspend fun giveLikeToStory(id: Int) {
        storyDao.setLikeToStory(id)
    }

    override suspend fun removeLikeFromStory(id: Int) {
        storyDao.removeLikeFromStory(id)
    }

// Downloaded stories

    override suspend fun downloadStory(story: StoryEntity) {
           story.isDownloaded = true
            storyDao.addStory(story)

    }

    override suspend fun removeDownloadedStory(id: Int) {
        executor.execute {
            storyDao.removeStory(id)
        }
    }

    override suspend fun removeAllDownloads() {
        executor.execute {
            storyDao.clearTable()
        }
    }

    override suspend fun getDownloadedStoryById(id: Int): StoryEntity? {
        return storyDao.getStoryById(id)
    }

    override suspend fun getDownloadedStories():Result<List<StoryEntity>> {
      return  runCatching { storyDao.getDownloadedStories()
        }.onSuccess {
            Result.success(it)
        }.onFailure { emptyList<StoryEntity>() }

    }

    override suspend fun getUserBookmarkedStories(): Result<List<StoryEntity>> {
        return  runCatching { storyDao.getUserBookmarkedStories()
        }.onSuccess {
            Result.success(it)
        }.onFailure { emptyList<StoryEntity>() }

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
            storyDao.clearTable()
        }
    }

    override suspend fun deleteCachedData() {
        executor.execute {
            storyDao.deleteRecordUrls()
        }
    }
}

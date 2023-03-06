package com.autio.android_app.data.repository.datasource.local

import com.autio.android_app.data.database.entities.CategoryEntity
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.database.entities.HistoryEntity
import com.autio.android_app.data.database.entities.MapPointEntity
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

interface AutioLocalDataSource {
    val userCategories: Flow<List<CategoryEntity>>
    val allLiveStories: Flow<List<MapPointEntity>>
    val getDownloadedStories: Flow<List<DownloadedStoryEntity>>
    val bookmarkedStories: Flow<List<MapPointEntity>>
    val favoriteStories: Flow<List<MapPointEntity>>
    val history: Flow<List<MapPointEntity>>
    suspend fun addUserCategories(categories: List<CategoryEntity>)
    suspend fun updateCategories(categories: List<CategoryEntity>)
    suspend fun getStoriesInLatLngBoundaries(
        swCoordinates: LatLng, neCoordinates: LatLng
    ): List<MapPointEntity>

    suspend fun getAllStories(): Result<List<MapPointEntity>?>
    suspend fun getMapPointById(id: String): Result<MapPointEntity?>
    suspend fun getMapPointsByIds(ids: List<Int>): Flow<List<MapPointEntity>>
    suspend fun getLastModifiedStory(): Result<MapPointEntity?>
    suspend fun addStories(stories: List<MapPointEntity>)
    suspend fun setBookmarksDataToLocalStories(storiesIds: List<String>)
    suspend fun setLikesDataToLocalStories(storiesIds: List<String>)
    suspend fun setListenedAtToLocalStories(storiesHistory: List<HistoryEntity>)
    suspend fun addStoryToHistory(history: HistoryEntity)
    suspend fun markStoryAsListenedAtLeast30Secs(storyId: String)
    suspend fun removeStoryFromHistory(id: String)
    suspend fun clearStoryHistory()
    suspend fun bookmarkStory(id: String)
    suspend fun removeBookmarkFromStory(id: String)
    suspend fun removeAllBookmarks()
    suspend fun giveLikeToStory(id: String)
    suspend fun removeLikeFromStory(id: String)
    suspend fun downloadStory(story: DownloadedStoryEntity)
    suspend fun removeDownloadedStory(id: String)
    suspend fun removeAllDownloads()
    suspend fun getDownloadedStoryById(id: String): DownloadedStoryEntity?
    suspend fun cacheRecordOfStory(storyId: String, recordUrl: String)
    suspend fun cacheRecordOfStory(storyId: Int, recordUrl: String)
    suspend fun clearUserData()
    suspend fun deleteCachedData()
}

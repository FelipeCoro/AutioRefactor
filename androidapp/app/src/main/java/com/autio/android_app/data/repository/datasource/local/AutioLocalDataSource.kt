package com.autio.android_app.data.repository.datasource.local

import com.autio.android_app.data.database.entities.CategoryEntity
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.database.entities.HistoryEntity
import com.autio.android_app.data.database.entities.MapPoint
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

interface AutioLocalDataSource {
    val userCategories: Flow<List<CategoryEntity>>
    fun addUserCategories(categories: List<CategoryEntity>)
    suspend fun updateCategories(categories: List<CategoryEntity>)
    suspend fun getStoriesInLatLngBoundaries(
        swCoordinates: LatLng, neCoordinates: LatLng
    ): List<MapPoint>

    val allStories: Flow<List<MapPoint>>
    suspend fun getAllStories(): List<MapPoint>
    suspend fun getStoryById(id: String): MapPoint?
    fun getStoriesByIds(ids: List<Int>): Flow<List<MapPoint>>
    suspend fun getLastModifiedStory(): MapPoint?
    fun addStories(stories: List<MapPoint>)
    fun setBookmarksDataToLocalStories(storiesIds: List<String>)
    fun setLikesDataToLocalStories(storiesIds: List<String>)
    suspend fun setListenedAtToLocalStories(storiesHistory: List<HistoryEntity>)
    suspend fun addStoryToHistory(history: HistoryEntity)
    suspend fun markStoryAsListenedAtLeast30Secs(storyId: String)
    suspend fun removeStoryFromHistory(id: String)
    fun clearStoryHistory()
    suspend fun bookmarkStory(id: String)
    suspend fun removeBookmarkFromStory(id: String)
    fun removeAllBookmarks()
    suspend fun giveLikeToStory(id: String)
    suspend fun removeLikeFromStory(id: String)
    fun downloadStory(story: DownloadedStoryEntity)
    fun removeDownloadedStory(id: String)
    fun removeAllDownloads()
    suspend fun getDownloadedStoryById(id: String): DownloadedStoryEntity?
    fun cacheRecordOfStory(storyId: String, recordUrl: String)
    fun cacheRecordOfStory(storyId: Int, recordUrl: String)
    fun clearUserData()
    fun deleteCachedData()
}

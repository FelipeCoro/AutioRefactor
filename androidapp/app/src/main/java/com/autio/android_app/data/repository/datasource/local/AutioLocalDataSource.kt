package com.autio.android_app.data.repository.datasource.local

import com.autio.android_app.data.database.entities.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

interface AutioLocalDataSource {
    val userCategories: Flow<List<CategoryEntity>>
    val favoriteStories: Flow<List<StoryEntity>>
    val history: Flow<List<StoryEntity>>
    suspend fun addUserCategories(categories: List<CategoryEntity>)
    suspend fun updateCategories(categories: List<CategoryEntity>)
    suspend fun getStoriesInLatLngBoundaries(
        swCoordinates: LatLng, neCoordinates: LatLng
    ): List<MapPointEntity>

    suspend fun getAllStories(): Flow<List<MapPointEntity>?>
    suspend fun getMapPointById(id: String): Result<MapPointEntity?>
    suspend fun getMapPointsByIds(ids: List<Int>): Result<List<MapPointEntity>>
    suspend fun getLastModifiedStory(): Result<StoryEntity?>
    suspend fun addStories(stories: List<MapPointEntity>)
    suspend fun setBookmarksDataToLocalStories(storiesIds: List<String>)
    suspend fun setLikesDataToLocalStories(storiesIds: List<String>)
    suspend fun setListenedAtToLocalStories(storiesHistory: List<HistoryEntity>)
    suspend fun addStoryToHistory(history: HistoryEntity)
    suspend fun markStoryAsListenedAtLeast30Secs(storyId: Int)
    suspend fun removeStoryFromHistory(id: Int)
    suspend fun clearStoryHistory()
    suspend fun bookmarkStory(id: Int): StoryEntity?
    suspend fun removeBookmarkFromStory(id: Int): StoryEntity?
    suspend fun removeAllBookmarks()
    suspend fun giveLikeToStory(id: Int)
    suspend fun removeLikeFromStory(id: Int)
    suspend fun downloadStory(story: StoryEntity)
    suspend fun removeDownloadedStory(id: Int)
    suspend fun removeAllDownloads()
    suspend fun getDownloadedStoryById(id: Int): StoryEntity?
    suspend fun cacheRecordOfStory(storyId: String, recordUrl: String)
    suspend fun cacheRecordOfStory(storyId: Int, recordUrl: String)
    suspend fun getDownloadedStories():Result<List<StoryEntity>>
    suspend fun getUserBookmarkedStories(): Result<List<StoryEntity>>
    suspend fun clearUserData()
    suspend fun deleteCachedData()
}

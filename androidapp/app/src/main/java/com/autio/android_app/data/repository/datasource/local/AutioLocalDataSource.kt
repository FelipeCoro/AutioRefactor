package com.autio.android_app.data.repository.datasource.local

import com.autio.android_app.data.database.entities.CategoryEntity
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.database.entities.HistoryEntity
import com.autio.android_app.data.database.entities.StoryEntity
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

interface AutioLocalDataSource {
    val userCategories: Flow<List<CategoryEntity>>
    fun addUserCategories(categories: Array<CategoryEntity>)
    suspend fun updateCategories(categories: Array<CategoryEntity>)
    suspend fun getStoriesInLatLngBoundaries(
        swCoordinates: LatLng, neCoordinates: LatLng
    ): List<StoryEntity>

    val allStories: Flow<List<StoryEntity>>
    suspend fun getAllStories(): List<StoryEntity>
    suspend fun getStoryById(id: String): StoryEntity?
    fun getStoriesByIds(ids: Array<Int>): Flow<Array<StoryEntity>>
    suspend fun getLastModifiedStory(): StoryEntity?
    fun addStories(stories: Array<StoryEntity>)
    fun setBookmarksDataToLocalStories(storiesIds: List<String>)
    fun setLikesDataToLocalStories(storiesIds: List<String>)
    suspend fun setListenedAtToLocalStories(storiesHistory: Array<HistoryEntity>)
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

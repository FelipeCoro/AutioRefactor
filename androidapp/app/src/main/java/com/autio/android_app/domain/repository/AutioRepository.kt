package com.autio.android_app.domain.repository

import com.autio.android_app.data.api.model.account.LoginDto
import com.autio.android_app.data.api.model.account.LoginResponse
import com.autio.android_app.data.api.model.account.ProfileDto
import com.autio.android_app.data.api.model.story.PlaysDto
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.database.entities.HistoryEntity
import com.autio.android_app.data.database.entities.MapPoint
import com.autio.android_app.ui.stories.models.Category
import com.autio.android_app.ui.stories.models.Story
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface AutioRepository {
    val userCategories: Flow<List<Category>>
    val allStories: Flow<List<Story>>
    val getDownloadedStories: Flow<List<DownloadedStoryEntity>>
    val bookmarkedStories: Flow<List<MapPoint>>
    val favoriteStories: Flow<List<MapPoint>>
    val history: Flow<List<MapPoint>>

    suspend fun login(loginDto: LoginDto): Response<LoginResponse>
    suspend fun fetchUserData()
    suspend fun updateProfile(infoUser: ProfileDto, onSuccess: () -> Unit, onFailure: () -> Unit)

    suspend fun updateCategoriesOrder(
        infoUser: ProfileDto, onSuccess: () -> Unit, onFailure: () -> Unit
    )

    suspend fun getStoriesByIds(userId: Int, apiToken: String, storiesWithoutRecords: List<Story>)

    suspend fun getStoriesInLatLngBoundaries(
        swCoordinates: LatLng, neCoordinates: LatLng
    ): List<MapPoint>
    suspend fun postStoryPlayed(xUserId: Int, userApiToken: String, playsDto: PlaysDto)

    suspend fun getDownloadedStoryById(id: String): DownloadedStoryEntity?

    suspend fun downloadStory(story:DownloadedStoryEntity)

    suspend fun getAllStories():List<MapPoint>

    suspend fun removeDownloadedStory(id:String)

    suspend fun removeAllDownloads()

    suspend fun removeBookmarkFromStory(id:String)

    suspend fun removeAllBookmarks()

    suspend fun bookmarkStory(id: String)

    suspend fun giveLikeToStory(id: String)

    suspend fun removeLikeFromStory(id:String)

    suspend fun addStoryToHistory(historyEntity: HistoryEntity)

    suspend fun removeStoryFromHistory(id:String)

    suspend fun clearStoryHistory()

    suspend fun cacheRecordOfStory(storyId: String, recordUrl: String)

    suspend fun clearUserData()
}

package com.autio.android_app.domain.repository

import com.autio.android_app.data.api.model.account.GuestResponse
import com.autio.android_app.data.api.model.account.LoginDto
import com.autio.android_app.data.api.model.account.LoginResponse
import com.autio.android_app.data.api.model.account.ProfileDto
import com.autio.android_app.data.api.model.story.PlaysDto
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.database.entities.HistoryEntity
import com.autio.android_app.data.database.entities.MapPointEntity
import com.autio.android_app.ui.stories.models.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface AutioRepository {
    val userCategories: Flow<List<Category>>
    val allStories: Flow<List<Story>>
    val getDownloadedStories: Flow<List<DownloadedStoryEntity>>
    val bookmarkedStories: Flow<List<MapPointEntity>>
    val favoriteStories: Flow<List<MapPointEntity>>
    val history: Flow<List<MapPointEntity>>

    suspend fun createAccount(accountRequest: AccountRequest): Result<User>
    suspend fun login(loginRequest: LoginRequest): Result<User>
    suspend fun loginAsGuest(): Result<User>
    suspend fun fetchUserData()
    suspend fun updateProfile(infoUser: ProfileDto, onSuccess: () -> Unit, onFailure: () -> Unit)
    suspend fun updateCategoriesOrder(
        infoUser: ProfileDto,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    )

    suspend fun getMapPointById(id: String): Result<Story?>
    suspend fun getMapPointsByIds(ids: List<Int>): Flow<List<Story>>
    suspend fun getStoryById(xUserId: String, apiToken: String, id: String): Story
    suspend fun getStoriesByIds(userId: Int, apiToken: String, storiesWithoutRecords: List<Story>)
    suspend fun getStoriesInLatLngBoundaries(
        swCoordinates: LatLng,
        neCoordinates: LatLng
    ): List<MapPointEntity>

    suspend fun postStoryPlayed(xUserId: Int, userApiToken: String, playsDto: PlaysDto)

    suspend fun getDownloadedStoryById(id: String): DownloadedStoryEntity?

    suspend fun downloadStory(story: DownloadedStoryEntity)

    suspend fun getAllStories(): Result<List<Story>?>

    suspend fun removeDownloadedStory(id: String)

    suspend fun removeAllDownloads()

    suspend fun removeBookmarkFromStory(id: String)

    suspend fun removeAllBookmarks()

    suspend fun bookmarkStory(id: String)

    suspend fun giveLikeToStory(id: String)

    suspend fun removeLikeFromStory(id: String)

    suspend fun addStoryToHistory(history: History)

    suspend fun removeStoryFromHistory(id: String)

    suspend fun clearStoryHistory()

    suspend fun cacheRecordOfStory(storyId: String, recordUrl: String)

    suspend fun clearUserData()
    suspend fun getLastModifiedStory(): Result<Story?>

    suspend fun setLikesDataToLocalStories(storiesIds: List<String>)

    suspend fun setListenedAtToLocalStories(storiesHistory: List<HistoryEntity>)

    suspend fun setBookmarksDataToLocalStories(storiesIds: List<String>)
    suspend fun deleteCachedData()
    suspend fun addStories(stories: List<Story>)


}

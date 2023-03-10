package com.autio.android_app.domain.repository

import com.autio.android_app.data.api.model.account.ProfileDto
import com.autio.android_app.data.api.model.story.PlaysDto
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.database.entities.HistoryEntity
import com.autio.android_app.data.database.entities.MapPointEntity
import com.autio.android_app.data.database.entities.StoryEntity
import com.autio.android_app.ui.stories.models.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

interface AutioRepository {
    val userCategories: Flow<List<Category>>
    val getDownloadedStories: Flow<List<DownloadedStoryEntity>>
    val bookmarkedStories: Flow<List<StoryEntity>>
    val favoriteStories: Flow<List<StoryEntity>>
    val history: Flow<List<StoryEntity>>

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
    suspend fun getStoryById(xUserId: Int, apiToken: String, id: Int): Result<Story>
    suspend fun getStoriesByIds(userId: Int, apiToken: String, storiesWithoutRecords: List<Story>)
    suspend fun getStoriesInLatLngBoundaries(
        swCoordinates: LatLng,
        neCoordinates: LatLng
    ): List<MapPointEntity>

    suspend fun getAuthorOfStory(xUserId: Int, apiToken: String, storyId: Int): Result<Author>

    suspend fun getStoriesByContributor(
        xUserId: Int,
        apiToken: String,
        storyId: Int,
        page: Int
    ): Result<Contributor>

    suspend fun giveLikeToStory(xUserId: Int, apiToken: String, storyId: Int): Result<Boolean>

    suspend fun postStoryPlayed(xUserId: Int, userApiToken: String, playsDto: PlaysDto)

    suspend fun getDownloadedStoryById(id: Int): DownloadedStoryEntity?

    suspend fun downloadStory(story: DownloadedStoryEntity)

    suspend fun getAllStories(): Flow<List<Story>?>

    suspend fun getStoriesAfterModifiedDate(date:Int):List<Story>

    suspend fun removeDownloadedStory(id: Int)

    suspend fun removeAllDownloads()

    suspend fun removeBookmarkFromStory(
        userId: Int,
        apiToken: String,
        storyId: Int
    ): Result<Boolean>

    suspend fun bookmarkStory(userId: Int, apiToken: String, storyId: Int): Result<Boolean>

    suspend fun getUserBookmarks(firebaseId:Int):List<String>

    suspend fun getStoriesFromUserBookmarks(userId: Int, apiToken: String) :Result<List<Story>>

    //TODO(Same as with storyViewModel we need to have parallel methods to avoid contradictions)
    suspend fun removeAllBookmarks()

    suspend fun giveLikeToStory(id: Int)

    suspend fun getUserFavoriteStories(firebaseId: Int)

    suspend fun removeLikeFromStory(userId: Int, apiToken: String, storyId: Int): Result<Boolean>

    suspend fun likesByStory(userId: Int, apiToken: String, storyId: Int): Result<Int>
    suspend fun addStoryToHistory(history: History)

    suspend fun getUserStoriesHistory(firebaseId: Int)

    suspend fun removeStoryFromHistory(id: Int)

    suspend fun clearStoryHistory()

    suspend fun cacheRecordOfStory(storyId: Int, recordUrl: String)

    suspend fun clearUserData()
    suspend fun getLastModifiedStory(): Result<Story?>

    suspend fun setLikesDataToLocalStories(storiesIds: List<String>)

    suspend fun setListenedAtToLocalStories(storiesHistory: List<HistoryEntity>)

    suspend fun setBookmarksDataToLocalStories(storiesIds: List<String>)
    suspend fun deleteCachedData()
    suspend fun addStories(stories: List<Story>)


}

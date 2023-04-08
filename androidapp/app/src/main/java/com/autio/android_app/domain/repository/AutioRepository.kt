package com.autio.android_app.domain.repository

import com.autio.android_app.data.api.model.account.AndroidReceiptDto
import com.autio.android_app.data.api.model.account.ProfileDto
import com.autio.android_app.data.api.model.account.Receipt
import com.autio.android_app.data.database.entities.HistoryEntity
import com.autio.android_app.data.database.entities.MapPointEntity
import com.autio.android_app.data.database.entities.StoryEntity
import com.autio.android_app.ui.stories.models.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

interface AutioRepository {
    val userCategories: Flow<List<Category>>
    val favoriteStories: Flow<List<Story>>
    val history: Flow<List<Story>>
    suspend fun isPremiumUser(): Boolean

    suspend fun remainingStories(): Int

    suspend fun updateRemainingStories(): Int
    suspend fun createAccount(accountRequest: AccountRequest): Result<User>
    suspend fun login(loginRequest: LoginRequest): Result<User>

    //TODO(URGENT. This one will disappear with correct back end call)
    suspend fun updateSubStatus()
    suspend fun loginAsGuest(): Result<User>
    suspend fun fetchUserData(): Result<User?>
    suspend fun updateProfile(infoUser: ProfileDto, onSuccess: () -> Unit, onFailure: () -> Unit)
    suspend fun updateCategoriesOrder(
        infoUser: ProfileDto,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    )

    suspend fun getMapPointById(id: String): Result<Story?>
    suspend fun getMapPointsByIds(ids: List<Int>): Result<List<Story>>
    suspend fun getStoryById(storyId: Int): Result<Story>
    suspend fun getStoriesByIds(
        stories: List<Int>
    ): Result<List<Story>>

    suspend fun getStoriesInLatLngBoundaries(
        swCoordinates: LatLng,
        neCoordinates: LatLng
    ): List<MapPointEntity>

    suspend fun getAuthorOfStory(storyId: Int): Result<Author>

    suspend fun getStoriesByContributor(
        storyId: Int,
        page: Int
    ): Result<Contributor>

    suspend fun postStoryPlayed(
        story: Story,
        wasPresent: Boolean,
        autoPlay: Boolean,
        isDownloaded: Boolean,
        network: String
    )

    suspend fun getDownloadedStoryById(id: Int): StoryEntity?

    suspend fun getDatabaseStoryById(id: Int): StoryEntity?

    suspend fun downloadStory(story: Story)

    suspend fun getAllStories(): Flow<List<Story>?>

    suspend fun getStoriesAfterModifiedDate(date: Int): List<Story>

    suspend fun removeDownloadedStory(id: Int)

    suspend fun removeAllDownloads()

    suspend fun removeBookmarkFromStory(storyId: Int)

    suspend fun bookmarkStory(storyId: Int)

    //  suspend fun getUserBookmarks(userId: Int): List<String>

    suspend fun getUserBookmarkedStories(): List<Story>

    //TODO(Same as with storyViewModel we need to have parallel methods to avoid contradictions)
    suspend fun removeAllBookmarks(stories: List<Story>)

    suspend fun giveLikeToStory(storyId: Int): Result<Pair<Boolean, Int>>

    suspend fun getUserFavoriteStories(): Result<List<Story>>

    suspend fun removeLikeFromStory(storyId: Int): Result<Pair<Boolean, Int>>
    suspend fun addStoryToHistory(storyId: Int)
    suspend fun getUserStoriesHistory(): Result<List<Story>>
    suspend fun removeStoryFromHistory(storyId: Int)
    suspend fun clearStoryHistory()
    suspend fun cacheRecordOfStory(storyId: Int, recordUrl: String)
    suspend fun clearUserData()
    suspend fun getLastModifiedStory(): Result<Story?>
    suspend fun setLikesDataToLocalStories(storiesIds: List<String>)
    suspend fun setListenedAtToLocalStories(storiesHistory: List<HistoryEntity>)

    // suspend fun setBookmarksDataToLocalStories(storiesIds: List<String>)
    suspend fun getNarratorOfStory(storyId: Int): Result<Narrator>
    suspend fun storyLikesCount(storyId: Int): Result<Int>
    suspend fun isStoryLiked(storyId: Int): Result<Boolean>
    suspend fun removeAllLikedStories(stories: List<StoryEntity>)
    suspend fun deleteCachedData()
    suspend fun addStories(stories: List<Story>)
    suspend fun getDownloadedStories(): Result<List<Story>>
    suspend fun getUserAccount(): User?
    suspend fun updateUserProfile(profile: ProfileDto)
    suspend fun isUserLoggedIn(): Boolean

    suspend fun deleteAccount()

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Result<Boolean>

    suspend fun isUserAllowedToPlayStories(): Boolean

    suspend fun sendPurchaseReceipt(receipt: AndroidReceiptDto)


}

package com.autio.android_app.data.repository

import android.util.Log
import com.autio.android_app.data.api.model.account.AndroidReceiptDto
import com.autio.android_app.data.api.model.account.ChangePasswordDto
import com.autio.android_app.data.api.model.account.ProfileDto
import com.autio.android_app.data.database.entities.HistoryEntity
import com.autio.android_app.data.database.entities.MapPointEntity
import com.autio.android_app.data.database.entities.StoryEntity
import com.autio.android_app.data.repository.datasource.local.AutioLocalDataSource
import com.autio.android_app.data.repository.datasource.remote.AutioRemoteDataSource
import com.autio.android_app.domain.mappers.*
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.ui.di.coroutines.IoDispatcher
import com.autio.android_app.ui.stories.models.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

class AutioRepositoryImpl @Inject constructor(
    private val autioRemoteDataSource: AutioRemoteDataSource,
    private val autioLocalDataSource: AutioLocalDataSource,
    //private val prefRepository: PrefRepository,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : AutioRepository {

    override suspend fun isPremiumUser(): Boolean {
        var expired = true
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            val status = autioRemoteDataSource.checkSubscribedStatus(user.id, user.bearerToken)
            if (status.isSuccessful)
                expired = status.body()!!.expired
            //  handle never created sub

        }
        return expired
    }

    override suspend fun remainingStories(): Int {
        return autioLocalDataSource.getRemainingStories()
    }

    override suspend fun updateRemainingStories(): Int {
        return autioLocalDataSource.updateRemainingStories()
    }

    override val userCategories: Flow<List<Category>> =
        autioLocalDataSource.userCategories.transform { entities ->
            entities.onEach { it.toModel() }
        }

    override val favoriteStories: Flow<List<Story>>
        get() = autioLocalDataSource.favoriteStories.transform { entities ->
            entities.onEach { it.toModel() }
        }
    override val history: Flow<List<Story>>
        get() = autioLocalDataSource.history.transform { entities ->
            entities.onEach { it.toModel() }
        }


    override suspend fun createAccount(accountRequest: AccountRequest): Result<User> {
        val accountDto = accountRequest.toDTO()
        kotlin.runCatching {
            autioRemoteDataSource.createAccount(accountDto)
        }.onSuccess { response ->
            if (response.isSuccessful) {
                response.body()?.let {
                    val user = createUserAccount(it.toModel())
                    if (user != null) {
                        return Result.success(user)
                    }
                }
            }
        }.onFailure {
            Result.failure<User>(it)
        }

        return Result.failure(Error())

    }


    override suspend fun login(loginRequest: LoginRequest): Result<User> {
        kotlin.runCatching {
            val loginDto = loginRequest.toDTO()
            autioRemoteDataSource.login(loginDto)
        }.onSuccess { response ->
            if (response.isSuccessful) {
                response.body()?.let {
                    val user = it.toModel()
                    autioLocalDataSource.updateUserInformation(user)
                    return Result.success(user)
                }
            }
        }.onFailure {
            Result.failure<User>(it)
        }

        return Result.failure(Error())
    }

    override suspend fun updateSubStatus() {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            kotlin.runCatching {
                val result = autioRemoteDataSource.checkSubscribedStatus(user.id,user.bearerToken)
                user.isPremiumUser = !result.body()!!.expired
                autioLocalDataSource.updateUserInformation(user)
            }
        }
    }

    override suspend fun loginAsGuest(): Result<User> {
        //TODO(check if the user account from db is not guest otherwise return failure)
        //TODO(verify current listened stories/ if null call to create account and set token)
        kotlin.runCatching {
            val result = autioRemoteDataSource.createGuestAccount()
            if (result.isSuccessful) {
                result.body()
            } else {
                val throwable = Error(result.errorBody().toString())
                return Result.failure(throwable)
            }
        }.onSuccess { guestResponse ->
            guestResponse?.let {
                val user = createUserAccount(it.toModel())
                if (user != null) {
                    return Result.success(user)
                }
            }
        }.onFailure {
            return Result.failure(Error())
        }
        return Result.failure(Error())
    }

    private suspend fun createUserAccount(model: User): User? {
        val result = autioLocalDataSource.createUserAccount(model.toEntity())
        return if (result.isSuccess) {
            val guestAccount = result.getOrNull()?.toModel()
            Log.d("GUEST ACCOUNT CREATED", guestAccount.toString())
            guestAccount
        } else null
    }


    override suspend fun fetchUserData(): Result<User?> {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            kotlin.runCatching {
                autioRemoteDataSource.getProfileDataV2(user.id, user.bearerToken)
            }.onSuccess {
                val profile = it.body()
                if (profile?.categories != null) {
                    autioLocalDataSource.addUserCategories(profile.categories)
                }
            }.onFailure {
                //TODO(Handle this failure)
            }
        }
        return autioLocalDataSource.getUserAccount()
    }

    override suspend fun updateProfile(
        infoUser: ProfileDto, onSuccess: () -> Unit, onFailure: () -> Unit
    ) {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            runCatching {
                autioRemoteDataSource.updateProfileV2(user.id, user.bearerToken, infoUser)
            }.onSuccess {
                val profile = it.body()
                if (profile != null) {
                    updateUserProfile(profile)
                    onSuccess.invoke()
                } else onFailure.invoke()
            }.onFailure { onFailure.invoke() }
        }
        onFailure.invoke()
    }

    override suspend fun updateCategoriesOrder(
        infoUser: ProfileDto, onSuccess: () -> Unit, onFailure: () -> Unit
    ) {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            runCatching {
                autioRemoteDataSource.updateProfileV2(
                    user.id, user.bearerToken, infoUser
                )
            }.onSuccess {
                val profile = it.body()
                if (profile != null) {
                    autioLocalDataSource.updateCategories(infoUser.categories)
                    onSuccess.invoke()
                } else {
                    onFailure.invoke()
                }
            }.onFailure { onFailure.invoke() }
        } ?: onFailure.invoke()
    }

    override suspend fun getMapPointById(id: String): Result<Story?> {

        val result = autioLocalDataSource.getMapPointById(id)

        return if (result.isSuccess) {
            val story = result.let { mapPoint ->
                mapPoint.map { it?.toModel() }
            }
            story

        } else {
            val throwable = result.exceptionOrNull() ?: java.lang.Error()
            Result.failure(throwable)
        }
    }

    override suspend fun getMapPointsByIds(ids: List<Int>): Result<List<Story>> {

        val result = autioLocalDataSource.getMapPointsByIds(ids)

        return if (result.isSuccess) {
            val stories = result.map { mapPoints ->
                mapPoints.map { it.toModel() }
            }
            stories

        } else {
            val throwable = result.exceptionOrNull() ?: java.lang.Error()
            Result.failure(throwable)
        }
    }

    override suspend fun getStoryById(storyId: Int): Result<Story> {

        val userAccount = getUserAccount() ?: return Result.failure(Error())
        val result = autioRemoteDataSource.getStoryById(
            userAccount.id, userAccount.bearerToken, storyId
        )

        return if (result.isSuccessful) {
            val story = result.body()!!.toModel()
            autioLocalDataSource.cacheRecordOfStory(story.id, story.recordUrl)
            Result.success(story)
        } else {
            val throwable = Error(result.errorBody().toString())
            Result.failure(throwable)
        }

    }


    override suspend fun getStoriesByIds(
        stories: List<Int>
    ): Result<List<Story>> {

        val userAccount = getUserAccount()

        userAccount?.let { user ->
            val result =
                autioRemoteDataSource.getStoriesByIds(user.id, user.bearerToken, stories)
            //TODO(refactor this, simplify, re-accommodate logic)
            return if (result.isSuccessful) {
                val storiesFromService = result.body()!!.map { it.toModel() }
                for (story in storiesFromService) {
                    val isStoryLiked = autioRemoteDataSource.isStoryLikedByUser(
                        user.id,
                        user.bearerToken,
                        story.id
                    )
                    if (isStoryLiked.isSuccessful) {
                        story.isLiked = isStoryLiked.body()!!.isLiked
                    } else {
                        story.isLiked = false
                    }
                }
                for (story in storiesFromService) {
                    autioLocalDataSource.cacheRecordOfStory(
                        story.id, story.recordUrl
                    )
                }
                return Result.success(storiesFromService)
            } else {
                val throwable = Error(result.errorBody().toString())
                Result.failure(throwable)
            }
        }
        return Result.failure(Error(""))//TODO(COMPLETE THIS ERROR)
    }

    override suspend fun getAuthorOfStory(
        storyId: Int
    ): Result<Author> {

        val userAccount = getUserAccount()
        userAccount?.let { user ->
            val result = autioRemoteDataSource.getAuthorOfStory(user.id, user.bearerToken, storyId)
            return if (result.isSuccessful) {
                Result.success(result.body()!!.toModel())
            } else {
                val throwable = Error(result.errorBody().toString())
                Result.failure(throwable)
            }
        }
        return Result.failure(Error(""))//TODO(Fill this error)
    }

    override suspend fun getStoriesInLatLngBoundaries(
        swCoordinates: LatLng, neCoordinates: LatLng
    ): List<MapPointEntity> {
        return autioLocalDataSource.getStoriesInLatLngBoundaries(swCoordinates, neCoordinates)
    }

    override suspend fun getAllStories(): Flow<List<Story>?> =
        autioLocalDataSource.getAllStories().transform { listOfMapPoints ->
            val list = listOfMapPoints?.map { it.toModel() } ?: listOf()
            emit(list)
        }


    override suspend fun getStoriesByContributor(
        storyId: Int, page: Int
    ): Result<Contributor> {
        val userAccount = getUserAccount()

        userAccount?.let { user ->
            val result =
                autioRemoteDataSource.getStoriesByContributor(
                    user.id,
                    user.bearerToken,
                    storyId,
                    page
                )

            if (result.isSuccessful) {
                val contributor = result.let { contributorResponse ->
                    contributorResponse.body()!!.toModel()
                }
                return@let Result.success(contributor)
            } else {
                val throwable = Error(result.errorBody().toString())
                return@let Result.failure(throwable)
            }
        }
        return Result.failure(Error(""))//TODO(COMPLETE THHIS ERROR)
    }

    override suspend fun getDownloadedStoryById(id: Int): StoryEntity? {
        return autioLocalDataSource.getDownloadedStoryById(id)
    }

    override suspend fun getDatabaseStoryById(id: Int): StoryEntity? {
        return autioLocalDataSource.getDownloadedStoryById(id)
    }

    override suspend fun downloadStory(story: Story) {
        autioLocalDataSource.downloadStory(story.toStoryEntity())
    }

    override suspend fun getStoriesAfterModifiedDate(date: Int): List<Story> {
        TODO("Not yet implemented")
    }


    override suspend fun removeDownloadedStory(id: Int) {
        return autioLocalDataSource.removeDownloadedStory(id)
    }

    override suspend fun postStoryPlayed(
        story: Story,
        wasPresent: Boolean,
        autoPlay: Boolean,
        isDownloaded: Boolean,
        network: String
    ) {
        val userAccount = getUserAccount()
        userAccount?.let { account ->
            runCatching {
                autioRemoteDataSource.postStoryPlayed(
                    account.id, account.bearerToken, story.toPlaysDto(
                        wasPresent, autoPlay, isDownloaded, network
                    )
                )
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    autioLocalDataSource.markStoryAsListenedAtLeast30Secs(story.id)
                    userAccount.remainingStories = response.body()?.playsRemaining!!
                }
            }.onFailure { }
        }
    }

    override suspend fun giveLikeToStory(
        storyId: Int
    ): Result<Pair<Boolean, Int>> {
        autioLocalDataSource.giveLikeToStory(storyId)
        val userAccount = getUserAccount()
        var result = Pair(false, -1)
        userAccount?.let { user ->
            val remoteLike =
                autioRemoteDataSource.giveLikeToStory(user.id, user.bearerToken, storyId)
            val likeCount =
                autioRemoteDataSource.storyLikesCount(user.id, user.bearerToken, storyId)
            if (remoteLike.isSuccessful) {
                result =
                    Pair(
                        remoteLike.body()!!.liked.toString().toBoolean(),
                        likeCount.body()!!.likes
                    )

            } else {
                val throwable = Error(remoteLike.errorBody().toString())
                return@let Result.failure<Pair<Boolean, Int>>(throwable)
            }
        }
        return Result.success(result)
    }

    override suspend fun removeLikeFromStory(storyId: Int): Result<Pair<Boolean, Int>> {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            autioLocalDataSource.removeLikeFromStory(storyId)
            val remoteRemovedLike =
                autioRemoteDataSource.removeLikeFromStory(user.id, user.bearerToken, storyId)
            val likeCount =
                autioRemoteDataSource.storyLikesCount(user.id, user.bearerToken, storyId)
            return if (remoteRemovedLike.isSuccessful) {
                val result = Pair(
                    remoteRemovedLike.body()!!.liked.toString().toBoolean(),
                    likeCount.body()!!.likes
                )
                Result.success(result)
            } else {
                val throwable = Error(remoteRemovedLike.errorBody().toString())
                Result.failure(throwable)
            }
        }
        return Result.failure(Error(""))//TODO(Fill this error)
    }

    override suspend fun getUserFavoriteStories(): Result<List<Story>> {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            val likedStories = autioRemoteDataSource.likedStoriesByUser(user.id, user.bearerToken)
            return if (likedStories.isSuccessful) {
                val result = likedStories.body()!!.map { it.toModel() }
                Result.success(result)

            } else {
                val throwable = Error(likedStories.errorBody().toString())
                Result.failure(throwable)
            }
        }
        return Result.failure(Error(""))//TODO(COMPLETE THHIS ERROR)
    }

    override suspend fun removeAllDownloads() {
        autioLocalDataSource.removeAllDownloads()
    }

    override suspend fun removeBookmarkFromStory(storyId: Int) {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            val remoteBookmark =
                autioRemoteDataSource.removeBookmarkFromStory(user.id, user.bearerToken, storyId)
            if (remoteBookmark.isSuccessful) {
                Result.success(remoteBookmark.body().toString().toBoolean())
            } else {
                val throwable = Error(remoteBookmark.errorBody().toString())
                Result.failure(throwable)
            }
        }
    }

    override suspend fun bookmarkStory(storyId: Int) {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            //autioLocalDataSource.bookmarkStory(storyId)
            val result = autioRemoteDataSource.bookmarkStory(user.id, user.bearerToken, storyId)
            if (result.isSuccessful)
                Result.success(result.body().toString().toBoolean())
            else {
                val throwable = Error(result.errorBody().toString())
                Result.failure(throwable)
            }
        }
    }


    override suspend fun getUserBookmarkedStories(): List<Story> {
        var bookmarkedStories = emptyList<Story>()
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            val result =
                autioRemoteDataSource.getStoriesFromUserBookmarks(user.id, user.bearerToken)
            result.let { response ->
                response.body()?.let { stories ->
                    bookmarkedStories = stories.map { it.toModel() }
                } ?: emptyList<Story>()
            }
        }
        return bookmarkedStories
    }


    override suspend fun removeAllBookmarks(stories: List<Story>) {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            val storiesDto = stories.map { it.toDto() }
            autioRemoteDataSource.removeAllBookmarks(user.id, user.apiToken, storiesDto)
        }
    }

    override suspend fun storyLikesCount(storyId: Int): Result<Int> {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            val likesByStory =
                autioRemoteDataSource.storyLikesCount(user.id, user.bearerToken, storyId)
            return if (likesByStory.isSuccessful) {
                Result.success(likesByStory.body()!!.likes)
            } else {
                val throwable = Error(likesByStory.errorBody().toString())
                Result.failure(throwable)
            }
        }
        return Result.failure(Error(""))//TODO(Fill this error)
    }

    override suspend fun isStoryLiked(storyId: Int): Result<Boolean> {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            val isStoryLiked = autioRemoteDataSource.isStoryLikedByUser(
                user.id,
                user.bearerToken,
                storyId
            )
            return if (isStoryLiked.isSuccessful) {
                Result.success(isStoryLiked.body()!!.isLiked)
            } else {
                Result.success(false)
            }
        }
        return Result.failure(Error(""))//TODO(Fill this error)
    }


    override suspend fun addStoryToHistory(storyId: Int) {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            autioRemoteDataSource.addStoryToHistory(user.id, user.bearerToken, storyId)
        }
    }

    override suspend fun getUserStoriesHistory(): Result<List<Story>> {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            val storyHistory = autioRemoteDataSource.getUserHistory(user.id, user.bearerToken)

            return if (storyHistory.isSuccessful) {

                Result.success(storyHistory.body()!!.data.map { it.toModel() })
            } else {
                val throwable = Error(storyHistory.errorBody().toString())
                Result.failure(throwable)
            }
        }
        return Result.failure(Error(""))//TODO(Fill this error)
    }

    override suspend fun removeStoryFromHistory(storyId: Int) {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            autioRemoteDataSource.removeStoryFromHistory(user.id, user.bearerToken, storyId)
        }
    }

    override suspend fun clearStoryHistory() {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            autioRemoteDataSource.clearHistory(user.id, user.bearerToken)
        }
    }

    override suspend fun cacheRecordOfStory(storyId: Int, recordUrl: String) {
        autioLocalDataSource.cacheRecordOfStory(storyId, recordUrl)
    }

    override suspend fun clearUserData() {
        autioLocalDataSource.clearUserData()
    }

    override suspend fun getLastModifiedStory(): Result<Story?> {

        val result = autioLocalDataSource.getLastModifiedStory()

        return if (result.isSuccess) {
            val story = result.let { mapPoint ->
                mapPoint.map { it?.toModel() }
            }
            story

        } else {
            val throwable = result.exceptionOrNull() ?: java.lang.Error()
            Result.failure(throwable)
        }
    }

    override suspend fun setLikesDataToLocalStories(storiesIds: List<String>) {
        autioLocalDataSource.setLikesDataToLocalStories(storiesIds)
    }

    override suspend fun setListenedAtToLocalStories(storiesHistory: List<HistoryEntity>) {
        autioLocalDataSource.setLikesDataToLocalStories(storiesHistory.map { it.toString() }) //TODO(Check this mapping)
    }

// override suspend fun setBookmarksDataToLocalStories(storiesIds: List<String>) {
//     autioLocalDataSource.setBookmarksDataToLocalStories(storiesIds)
// }

    override suspend fun getNarratorOfStory(
        storyId: Int
    ): Result<Narrator> {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            val narratorResponse =
                autioRemoteDataSource.getNarratorOfStory(user.id, user.bearerToken, storyId)
            return if (narratorResponse.isSuccessful) {
                Result.success(narratorResponse.body()!!.toModel())
            } else {
                val throwable = Error(narratorResponse.errorBody().toString())
                Result.failure(throwable)
            }
        }
        return Result.failure(Error(""))//TODO(Fill this error)
    }

    override suspend fun deleteCachedData() {
        autioLocalDataSource.deleteCachedData()
    }

    override suspend fun removeAllLikedStories(stories: List<StoryEntity>) {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            for (story in stories) {
                val storyId = story.id
                autioLocalDataSource.removeLikeFromStory(storyId)
                autioRemoteDataSource.removeLikeFromStory(user.id, user.bearerToken, storyId)
                autioRemoteDataSource.storyLikesCount(user.id, user.bearerToken, storyId)
            }
        }
    }

    override suspend fun addStories(stories: List<Story>) {
        autioLocalDataSource.addStories(stories.map { it.toMapPointEntity() }) //TODO(check this method usage, user should not be able to "add/make" stories)
    }

    override suspend fun getDownloadedStories(): Result<List<Story>> {

        val result = autioLocalDataSource.getDownloadedStories()
        return if (result.isSuccess) {
            val stories = result.getOrNull()?.let { downloadedStories ->
                downloadedStories.map { it.toModel() }
            } ?: listOf()
            Result.success(stories)
        } else {
            val throwable = Error(result.toString())
            Result.failure(throwable)
        }
    }

    override suspend fun getUserAccount(): User? {
        val userResult = autioLocalDataSource.getUserAccount()
        return userResult.getOrNull()
    }

    override suspend fun updateUserProfile(profile: ProfileDto) {
        TODO("update user profile Not yet implemented")
    }

    override suspend fun isUserLoggedIn(): Boolean {
        val result = autioLocalDataSource.getUserAccount()
        return if (result.isSuccess) {
            result.getOrNull()?.isPremiumUser ?: false
        } else false
    }

    override suspend fun deleteAccount() {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            autioRemoteDataSource.deleteAccount(user.id, user.bearerToken)
        }
    }

    override suspend fun changePassword(
        currentPassword: String, newPassword: String, confirmPassword: String
    ): Result<Boolean> {
        val userAccount = getUserAccount()
        userAccount?.let {
            val passwordInfo = ChangePasswordDto(
                currentPassword, newPassword, confirmPassword
            )
            val result = autioRemoteDataSource.changePassword(
                userAccount.id, userAccount.bearerToken, passwordInfo
            )
            return if (result.isSuccessful) {
                val dtoResponse = result.body().toString()
                Result.success(true)
            } else {
                Result.failure(Error())
            }
        }
        return Result.failure(Error())
    }

    override suspend fun isUserAllowedToPlayStories(): Boolean {
        val user = getUserAccount()
        return user != null && (user.isPremiumUser || (user.isGuest && user.remainingStories > 0))
    }

    override suspend fun sendPurchaseReceipt(receipt: AndroidReceiptDto) {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            autioRemoteDataSource.sendPurchaseReceipt(user.id, user.bearerToken, receipt)
        }
    }
}








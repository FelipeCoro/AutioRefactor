package com.autio.android_app.data.repository

import android.util.Log
import com.autio.android_app.data.api.model.account.ChangePasswordDto
import com.autio.android_app.data.api.model.account.ProfileDto
import com.autio.android_app.data.database.entities.HistoryEntity
import com.autio.android_app.data.database.entities.MapPointEntity
import com.autio.android_app.data.database.entities.StoryEntity
import com.autio.android_app.data.repository.datasource.local.AutioLocalDataSource
import com.autio.android_app.data.repository.datasource.remote.AutioRemoteDataSource
import com.autio.android_app.domain.mappers.toDTO
import com.autio.android_app.domain.mappers.toEntity
import com.autio.android_app.domain.mappers.toMapPointEntity
import com.autio.android_app.domain.mappers.toModel
import com.autio.android_app.domain.mappers.toPlaysDto
import com.autio.android_app.domain.mappers.toStoryEntity
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.ui.di.coroutines.IoDispatcher
import com.autio.android_app.ui.stories.models.AccountRequest
import com.autio.android_app.ui.stories.models.Author
import com.autio.android_app.ui.stories.models.Category
import com.autio.android_app.ui.stories.models.Contributor
import com.autio.android_app.ui.stories.models.History
import com.autio.android_app.ui.stories.models.LoginRequest
import com.autio.android_app.ui.stories.models.Narrator
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.ui.stories.models.User
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

class AutioRepositoryImpl @Inject constructor(
    private val autioRemoteDataSource: AutioRemoteDataSource,
    private val autioLocalDataSource: AutioLocalDataSource,
    //private val prefRepository: PrefRepository,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher
) : AutioRepository {

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
        val result = autioRemoteDataSource.createAccount(accountDto)

        return if (result.isSuccessful) {
            val user = result.let { accountCreateResponse ->
                accountCreateResponse.body()!!.toModel()
            }
            Result.success(user)

        } else {
            val throwable = Error(result.errorBody().toString())
            Result.failure(throwable)
        }
    }

    override suspend fun login(loginRequest: LoginRequest): Result<User> {
        kotlin.runCatching {
            val loginDto = loginRequest.toDTO()
            autioRemoteDataSource.login(loginDto)
        }.onSuccess { response ->
            if (response.isSuccessful) {
                val userAccount = getUserAccount()
                userAccount?.let {
                    response.body()?.let {
                        val user = it.toModel()
                        autioLocalDataSource.updateUserInformation(user)
                        return Result.success(user)
                    }
                }
            }
        }.onFailure {
            Result.failure<User>(it)
        }

        return Result.failure(Error())
    }

    override suspend fun loginAsGuest(): Result<User> {
        //TODO(check if the user account from db is not guest otherwise return failure)
        //TODO(verify current listened stories/ if null call to create account and set token)
        val userAccount = getUserAccount()
        kotlin.runCatching {
            val result = autioRemoteDataSource.createGuestAccount()
            if (result.isSuccessful) result.body() else {
                val throwable = Error(result.errorBody().toString())
                return Result.failure(throwable)
            }
        }.onSuccess { guestResponse ->
            guestResponse?.let {
                val user = createUserAccount(it.toModel())
                Result.success(user)
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


    override suspend fun fetchUserData() {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            kotlin.runCatching {
                autioRemoteDataSource.getProfileDataV2(user.id, user.apiToken)
            }.onSuccess {
                val profile = it.body()
                if (profile?.categories != null) {
                    autioLocalDataSource.addUserCategories(profile.categories)
                }
            }.onFailure { }
        }
    }

    override suspend fun updateProfile(
        infoUser: ProfileDto, onSuccess: () -> Unit, onFailure: () -> Unit
    ) {
        val userAccount = getUserAccount()
        userAccount?.let { user ->
            runCatching {
                autioRemoteDataSource.updateProfileV2(user.id, user.apiToken, infoUser)
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
                    user.id, user.apiToken, infoUser
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
        userId: Int, apiToken: String, stories: List<Int>
    ): Result<List<Story>> {
        val result = autioRemoteDataSource.getStoriesByIds(
            userId,
            apiToken,
            stories
        )
        //TODO(refactor this, simplify, re-accommodate logic)
        return if (result.isSuccessful) {
            val storiesFromService = result.body()!!.map { it.toModel() }
            for (story in storiesFromService) {
                val isStoryLiked = autioRemoteDataSource.isStoryLikedByUser(
                    userId,
                    apiToken,
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

    override suspend fun getAuthorOfStory(
        xUserId: Int,
        apiToken: String,
        storyId: Int
    ): Result<Author> {

        val result = autioRemoteDataSource.getAuthorOfStory(xUserId, apiToken, storyId)

        return if (result.isSuccessful) {
            val author = result.let { authorResponse ->
                authorResponse.body()!!.toModel()
            }
            Result.success(author)
        } else {
            val throwable = Error(result.errorBody().toString())
            Result.failure(throwable)
        }
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
        xUserId: Int, apiToken: String, storyId: Int, page: Int
    ): Result<Contributor> {
        val result =
            autioRemoteDataSource.getStoriesByContributor(xUserId, apiToken, storyId, page)

        return if (result.isSuccessful) {
            val contributor = result.let { contributorResponse ->
                contributorResponse.body()!!.toModel()
            }
            Result.success(contributor)
        } else {
            val throwable = Error(result.errorBody().toString())
            Result.failure(throwable)
        }
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
        xUserId: Int,
        userApiToken: String,
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
        userId: Int, apiToken: String, storyId: Int
    ): Result<Pair<Boolean, Int>> {
        autioLocalDataSource.giveLikeToStory(storyId)
        val remoteLike = autioRemoteDataSource.giveLikeToStory(userId, apiToken, storyId)
        val likeCount = autioRemoteDataSource.storyLikesCount(userId, apiToken, storyId)
        return if (remoteLike.isSuccessful) {
            val result =
                Pair(remoteLike.body()!!.liked.toString().toBoolean(), likeCount.body()!!.likes)
            Result.success(result)
        } else {
            val throwable = Error(remoteLike.errorBody().toString())
            Result.failure(throwable)
        }
    }

    override suspend fun removeLikeFromStory(
        userId: Int, apiToken: String, storyId: Int
    ): Result<Pair<Boolean, Int>> {
        autioLocalDataSource.removeLikeFromStory(storyId)
        val remoteRemovedLike = autioRemoteDataSource.removeLikeFromStory(userId, apiToken, storyId)
        val likeCount = autioRemoteDataSource.storyLikesCount(userId, apiToken, storyId)
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

    override suspend fun getUserFavoriteStories(
        userId: Int,
        apiToken: String
    ): Result<List<Story>> {
        val likedStories = autioRemoteDataSource.likedStoriesByUser(userId, apiToken)
        return if (likedStories.isSuccessful) {
            val result = likedStories.body()!!.map { it.toModel() }
            Result.success(result)

        } else {
            val throwable = Error(likedStories.errorBody().toString())
            Result.failure(throwable)
        }
    }

    override suspend fun removeAllDownloads() {
        autioLocalDataSource.removeAllDownloads()
    }

    override suspend fun removeBookmarkFromStory(
        userId: Int, apiToken: String, storyId: Int
    ) {
         autioLocalDataSource.removeBookmarkFromStory(storyId)
      // return bookedMarkedStory?.let {
      //     val remoteBookmark =
      //         autioRemoteDataSource.removeBookmarkFromStory(userId, apiToken, storyId)
      //     if (remoteBookmark.isSuccessful) {
       //   //     Result.success(remoteBookmark.body().toString().toBoolean())
       //    } else {
       //        val throwable = Error(remoteBookmark.errorBody().toString())
       //        Result.failure(throwable)
       //    }
       //} ?: Result.failure(Error())
    }

    override suspend fun bookmarkStory(
        userId: Int, apiToken: String, storyId: Int
    ) {
       autioLocalDataSource.bookmarkStory(storyId)
        // val remoteBookmark = //autioRemoteDataSource.bookmarkStory(userId, apiToken, storyId) //TODO(FOR REMOTE CACHING)
      //  return if (remoteBookmark) {
      //      Result.success(remoteBookmark.body().toString().toBoolean())
      //  } else {
      //      val throwable = Error(remoteBookmark.errorBody().toString())
      //      Result.failure(throwable)
      //  }
    }



    override suspend fun getUserBookmarkedStories(userId: Int, apiToken: String ): List<Story> {

        //val result = autioRemoteDataSource.getStoriesFromUserBookmarks(userId, apiToken)(TODO(FOR REMOTE CACHING))
        return  autioLocalDataSource.getUserBookmarkedStories().map { it.toModel() }

    }


    override suspend fun removeAllBookmarks() {
        autioLocalDataSource.removeAllBookmarks()

    }

    override suspend fun giveLikeToStory(id: Int) {
        autioLocalDataSource.giveLikeToStory(id)
    }

    override suspend fun storyLikesCount(
        userId: Int,
        apiToken: String,
        storyId: Int
    ): Result<Int> {

        val likesByStory = autioRemoteDataSource.storyLikesCount(userId, apiToken, storyId)
        return if (likesByStory.isSuccessful) {
            Result.success(likesByStory.body()!!.likes)
        } else {
            val throwable = Error(likesByStory.errorBody().toString())
            Result.failure(throwable)
        }
    }

    override suspend fun isStoryLiked(
        userId: Int,
        apiToken: String,
        storyId: Int
    ): Result<Boolean> {
        val isStoryLiked = autioRemoteDataSource.isStoryLikedByUser(
            userId,
            apiToken,
            storyId
        )
        return if (isStoryLiked.isSuccessful) {
            Result.success(isStoryLiked.body()!!.isLiked)
        } else {
            Result.success(false)
        }
    }


    override suspend fun addStoryToHistory(history: History) {
        autioLocalDataSource.addStoryToHistory(history.toMapPointEntity())
    }

    override suspend fun getUserStoriesHistory(
        userId: Int,
        userApiToken: String
    ): Result<List<Story>> {
        val storyHistory = autioRemoteDataSource.getUserHistory(userId, userApiToken)
        return if (storyHistory.isSuccessful) {
            Result.success(storyHistory.body()!!.map { it.toModel() })
        } else {
            val throwable = Error(storyHistory.errorBody().toString())
            Result.failure(throwable)
        }
    }

    override suspend fun removeStoryFromHistory(id: Int) {
        autioLocalDataSource.removeStoryFromHistory(id)
    }

    override suspend fun clearStoryHistory() {
        autioLocalDataSource.clearStoryHistory()
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
        userId: Int,
        apiToken: String,
        storyId: Int
    ): Result<Narrator> {

        val narratorResponse = autioRemoteDataSource.getNarratorOfStory(userId, apiToken, storyId)
        return if (narratorResponse.isSuccessful) {
            Result.success(narratorResponse.body()!!.toModel())
        } else {
            val throwable = Error(narratorResponse.errorBody().toString())
            Result.failure(throwable)
        }
    }

    override suspend fun deleteCachedData() {
        autioLocalDataSource.deleteCachedData()
    }

    override suspend fun removeAllLikedStories(userId: Int, apiToken: String,stories:List<StoryEntity>) {
        for(story in stories) {
            val storyId = story.id
            autioLocalDataSource.removeLikeFromStory(storyId)
            autioRemoteDataSource.removeLikeFromStory(userId, apiToken, storyId)
            autioRemoteDataSource.storyLikesCount(userId, apiToken, storyId)
        }}

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

    override suspend fun changePassword(
        currentPassword: String, newPassword: String, confirmPassword: String
    ): Result<Boolean> {
        val userAccount = getUserAccount()
        userAccount?.let {
            val passwordInfo = ChangePasswordDto(
                currentPassword, newPassword, confirmPassword
            )
            val result = autioRemoteDataSource.changePassword(
                userAccount.id, userAccount.apiToken, passwordInfo
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
}








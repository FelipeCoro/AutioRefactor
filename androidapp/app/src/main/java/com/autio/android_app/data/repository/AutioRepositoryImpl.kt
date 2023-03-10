package com.autio.android_app.data.repository

import com.autio.android_app.data.api.model.account.ProfileDto
import com.autio.android_app.data.api.model.story.PlaysDto
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.database.entities.HistoryEntity
import com.autio.android_app.data.database.entities.MapPointEntity
import com.autio.android_app.data.database.entities.StoryEntity
import com.autio.android_app.data.repository.datasource.local.AutioLocalDataSource
import com.autio.android_app.data.repository.datasource.remote.AutioRemoteDataSource
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.domain.mappers.toDTO
import com.autio.android_app.domain.mappers.toMapPointEntity
import com.autio.android_app.domain.mappers.toModel
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.ui.di.coroutines.IoDispatcher
import com.autio.android_app.ui.stories.models.AccountRequest
import com.autio.android_app.ui.stories.models.Author
import com.autio.android_app.ui.stories.models.Category
import com.autio.android_app.ui.stories.models.Contributor
import com.autio.android_app.ui.stories.models.History
import com.autio.android_app.ui.stories.models.LoginRequest
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.ui.stories.models.User
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

class AutioRepositoryImpl @Inject constructor(
    private val autioRemoteDataSource: AutioRemoteDataSource,
    private val autioLocalDataSource: AutioLocalDataSource,
    private val prefRepository: PrefRepository,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher
) : AutioRepository {

    override val userCategories: Flow<List<Category>> =
        autioLocalDataSource.userCategories.transform { entities ->
            entities.onEach { it.toModel() }
        }


    override val getDownloadedStories: Flow<List<DownloadedStoryEntity>>
        get() = autioLocalDataSource.getDownloadedStories.transform { entities ->

            //    entities.onEach { TODO("Create downloadedStoryEntityTODownloadedStory  Mapper and collect") }
        }
    override val bookmarkedStories: Flow<List<StoryEntity>>
        get() = autioLocalDataSource.bookmarkedStories
    override val favoriteStories: Flow<List<StoryEntity>>
        get() = autioLocalDataSource.favoriteStories
    override val history: Flow<List<StoryEntity>>
        get() = autioLocalDataSource.history

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

        val loginDto = loginRequest.toDTO()
        val result = autioRemoteDataSource.login(loginDto)

        return if (result.isSuccessful) {
            val user = result.let { loginResponse ->
                loginResponse.body()!!.toModel()
            }
            Result.success(user)

        } else {
            val throwable = Error(result.errorBody().toString())
            Result.failure(throwable)
        }


    }

    override suspend fun loginAsGuest(): Result<User> {

        val result = autioRemoteDataSource.createGuestAccount()

        return if (result.isSuccessful) {
            val user = result.let { guestResponse ->
                guestResponse.body()!!.toModel()
            }
            Result.success(user)

        } else {
            val throwable = Error(result.errorBody().toString())
            Result.failure(throwable)
        }

    }


    override suspend fun fetchUserData() {
        kotlin.runCatching {
            autioRemoteDataSource.getProfileDataV2(
                prefRepository.userId, prefRepository.userApiToken
            )
        }.onSuccess {
            val profile = it.body()
            if (profile?.categories != null) {
                autioLocalDataSource.addUserCategories(
                    profile.categories
                )
            }
        }.onFailure { }
    }

    override suspend fun updateProfile(
        infoUser: ProfileDto, onSuccess: () -> Unit, onFailure: () -> Unit
    ) {
        runCatching {
            autioRemoteDataSource.updateProfileV2(
                prefRepository.userId, prefRepository.userApiToken, infoUser
            )
        }.onSuccess {
            val profile = it.body()
            if (profile != null) {
                prefRepository.userName = profile.name
                prefRepository.userEmail = profile.email
                onSuccess.invoke()
            } else onFailure.invoke()
        }.onFailure { onFailure.invoke() }
    }

    override suspend fun updateCategoriesOrder(
        infoUser: ProfileDto, onSuccess: () -> Unit, onFailure: () -> Unit
    ) {
        runCatching {
            autioRemoteDataSource.updateProfileV2(
                prefRepository.userId, prefRepository.userApiToken, infoUser
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

    override suspend fun getMapPointsByIds(ids: List<Int>): Flow<List<Story>> {

        val result = autioLocalDataSource.getMapPointsByIds(ids)

        result.map { entities ->
            entities.onEach { it.toModel() }
        }

        val tempFlow: Flow<List<Story>> = emptyFlow()
        return tempFlow

        /* return if (result.isSuccess) {
             result.transform { entities ->
                 entities.onEach { it.toModel() }
             }
         } else {
             val throwable = result.exceptionOrNull() ?: java.lang.Error()
             Result.failure(throwable)
         }*/
        TODO("RETUR FLOW CORRECTLY")
    }

    override suspend fun getStoryById(xUserId: Int, apiToken: String, id: Int): Result <Story> {

        val result =  autioRemoteDataSource.getStoriesByIds(xUserId, apiToken, listOf(id))

        return if(result.isSuccessful) {
            val story = result.let { storyResponse ->
                storyResponse.body()!!.map { it.toModel()}
            }
            autioLocalDataSource.cacheRecordOfStory(story[0].id , story[0].recordUrl)
            Result.success(story[0])


        } else {
            val throwable = Error(result.errorBody().toString())
            Result.failure(throwable)
        }

    }


    override suspend fun getStoriesByIds(
        userId: Int, apiToken: String, storiesWithoutRecords: List<Story>
    ) {
        runCatching {
            autioRemoteDataSource.getStoriesByIds(
                userId,
                apiToken,
                storiesWithoutRecords.map { it.id })
        }.onSuccess {
            val storiesFromService = it.body()
            if (storiesFromService != null) {
                for (story in storiesFromService) {
                    autioLocalDataSource.cacheRecordOfStory(
                        story.id, story.recordUrl
                    )
                }
            }
        }.onFailure { }
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

    override suspend fun getDownloadedStoryById(id: Int): DownloadedStoryEntity? {
        return autioLocalDataSource.getDownloadedStoryById(id)
    }

    override suspend fun downloadStory(story: DownloadedStoryEntity) {
        autioLocalDataSource.downloadStory(TODO("Map back from DownloadedStoryEntity to a DownloadedHistory Domain Model"))
    }

    override suspend fun getStoriesAfterModifiedDate(date: Int): List<Story> {
        TODO("Not yet implemented")
    }


    override suspend fun removeDownloadedStory(id: Int) {
        return autioLocalDataSource.removeDownloadedStory(id)
    }

    override suspend fun postStoryPlayed(
        xUserId: Int, userApiToken: String, playsDto: PlaysDto
    ) {
        runCatching {
            autioRemoteDataSource.postStoryPlayed(
                prefRepository.userId, prefRepository.userApiToken, playsDto
            )
        }.onSuccess { response ->
            if (response.isSuccessful) {
                if (playsDto.firebaseId != null) {
                    autioLocalDataSource.markStoryAsListenedAtLeast30Secs(playsDto.firebaseId)
                }
                prefRepository.remainingStories = response.body()?.playsRemaining!!
            }
        }.onFailure { }
    }

    override suspend fun giveLikeToStory(
        userId: Int, apiToken: String, storyId: Int
    ): Result<Boolean> {
        val likedStory = autioLocalDataSource.giveLikeToStory(storyId)
        return likedStory.let {
            val remoteLike =
                autioRemoteDataSource.giveLikeToStory(userId, apiToken, storyId)
            if (remoteLike.isSuccessful) {
                Result.success(remoteLike.body().toString().toBoolean())
            } else {
                val throwable = Error(remoteLike.errorBody().toString())
                Result.failure(throwable)
            }
        }
    }

    override suspend fun removeAllDownloads() {
        autioLocalDataSource.removeAllDownloads()
    }

    override suspend fun removeBookmarkFromStory(
        userId: Int, apiToken: String, storyId: Int
    ): Result<Boolean> {
        val bookedMarkedStory = autioLocalDataSource.removeBookmarkFromStory(storyId)
        return bookedMarkedStory?.let {
            val remoteBookmark =
                autioRemoteDataSource.removeBookmarkFromStory(userId, apiToken, storyId)
            if (remoteBookmark.isSuccessful) {
                Result.success(remoteBookmark.body().toString().toBoolean())
            } else {
                val throwable = Error(remoteBookmark.errorBody().toString())
                Result.failure(throwable)
            }
        } ?: Result.failure(Error())
    }

    override suspend fun bookmarkStory(
        userId: Int, apiToken: String, storyId: Int
    ): Result<Boolean> {
        val bookedMarkedStory = autioLocalDataSource.bookmarkStory(storyId)
        return bookedMarkedStory?.let {
            val remoteBookmark =
                autioRemoteDataSource.bookmarkStory(userId, apiToken, storyId)
            if (remoteBookmark.isSuccessful) {
                Result.success(remoteBookmark.body().toString().toBoolean())
            } else {
                val throwable = Error(remoteBookmark.errorBody().toString())
                Result.failure(throwable)
            }
        } ?: Result.failure(Error())
    }

    override suspend fun getUserBookmarks(firebaseId: Int): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun getStoriesFromUserBookmarks(
        userId: Int, apiToken: String
    ): Result<List<Story>> {

        val result = autioRemoteDataSource.getStoriesFromUserBookmarks(userId, apiToken)

        return if (result.isSuccessful) {
            val stories = result.body()?.let { storiesResponse ->
                storiesResponse.map { it.toModel() }
            } ?: listOf()
            Result.success(stories)
        } else {
            val throwable = Error(result.errorBody().toString())
            Result.failure(throwable)
        }
    }


    override suspend fun removeAllBookmarks() {
        autioLocalDataSource.removeAllBookmarks()

    }

    override suspend fun getUserFavoriteStories(firebaseId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun giveLikeToStory(id: Int) {
        autioLocalDataSource.giveLikeToStory(id)
    }

    override suspend fun removeLikeFromStory(
        userId: Int, apiToken: String, storyId: Int
    ): Result<Boolean> {
        val removedLike = autioLocalDataSource.removeLikeFromStory(storyId)
        return removedLike.let {
            val remoteRemovedLike =
                autioRemoteDataSource.removeLikeFromStory(userId, apiToken, storyId)
            if (remoteRemovedLike.isSuccessful) {
                Result.success(remoteRemovedLike.body().toString().toBoolean())
            } else {
                val throwable = Error(remoteRemovedLike.errorBody().toString())
                Result.failure(throwable)
            }
        }
    }

    override suspend fun likesByStory(
        userId: Int,
        apiToken: String,
        storyId: Int
    ): Result<Int> {

        val likesByStory = autioRemoteDataSource.likesByStory(userId, apiToken, storyId)
        return if (likesByStory.isSuccessful) {
            Result.success(likesByStory.body().toString().toInt())
        } else {
            val throwable = Error(likesByStory.errorBody().toString())
            Result.failure(throwable)
        }
    }


    override suspend fun addStoryToHistory(history: History) {
        autioLocalDataSource.addStoryToHistory(history.toMapPointEntity())
    }

    override suspend fun getUserStoriesHistory(firebaseId: Int) {
        TODO("Not yet implemented")
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

    override suspend fun setBookmarksDataToLocalStories(storiesIds: List<String>) {
        autioLocalDataSource.setBookmarksDataToLocalStories(storiesIds)
    }

    override suspend fun deleteCachedData() {
        autioLocalDataSource.deleteCachedData()
    }

    override suspend fun addStories(stories: List<Story>) {
        autioLocalDataSource.addStories(stories.map { it.toMapPointEntity() }) //TODO(check this method usage, user should not be able to "add/make" stories)
    }
}








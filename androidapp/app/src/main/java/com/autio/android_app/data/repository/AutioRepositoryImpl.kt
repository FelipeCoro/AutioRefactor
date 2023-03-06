package com.autio.android_app.data.repository

import com.autio.android_app.data.api.model.account.LoginDto
import com.autio.android_app.data.api.model.account.LoginResponse
import com.autio.android_app.data.api.model.account.ProfileDto
import com.autio.android_app.data.api.model.story.PlaysDto
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.database.entities.HistoryEntity
import com.autio.android_app.data.database.entities.MapPointEntity
import com.autio.android_app.data.repository.datasource.local.AutioLocalDataSource
import com.autio.android_app.data.repository.datasource.remote.AutioRemoteDataSource
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.domain.mappers.toDto
import com.autio.android_app.domain.mappers.toEntity
import com.autio.android_app.domain.mappers.toModel
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.ui.stories.models.Category
import com.autio.android_app.ui.stories.models.History
import com.autio.android_app.ui.stories.models.Story
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.*
import kotlinx.serialization.descriptors.StructureKind
import retrofit2.Response
import javax.inject.Inject

class AutioRepositoryImpl @Inject constructor(
    private val autioRemoteDataSource: AutioRemoteDataSource,
    private val autioLocalDataSource: AutioLocalDataSource,
    private val prefRepository: PrefRepository,
) : AutioRepository {

    override val userCategories: Flow<List<Category>> =
        autioLocalDataSource.userCategories.transform { entities ->
            entities.onEach { it.toModel() }
        }

    override val allStories: Flow<List<Story>>
        get() = autioLocalDataSource.allLiveStories.transform { entities ->
            entities.onEach { it.toModel() }
        }

    override val getDownloadedStories: Flow<List<DownloadedStoryEntity>>
        get() = autioLocalDataSource.getDownloadedStories.transform { entities ->

            entities.onEach { TODO("Create downloadedStoryEntityTODownloadedStory  Mapper and collect") }
        }
    override val bookmarkedStories: Flow<List<MapPointEntity>>
        get() = TODO("Not yet implemented")
    override val favoriteStories: Flow<List<MapPointEntity>>
        get() = TODO("Not yet implemented")
    override val history: Flow<List<MapPointEntity>>
        get() = TODO("Not yet implemented")

    override suspend fun login(loginDto: LoginDto): Response<LoginResponse> {
        return autioRemoteDataSource.login(loginDto)
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

    override suspend fun getStoryById(xUserId: String, apiToken: String, id: String):Story {
        runCatching {
            autioRemoteDataSource.getStoryById(xUserId,
                apiToken,
                id)
        }.onSuccess {
            val story = it.body()
            if (story != null) {
                    autioLocalDataSource.cacheRecordOfStory(
                        story.id, story.recordUrl
                    )
                }
            }.onFailure { }

        return Story() //TODO(TEMP FIX)
    }


    override suspend fun getStoriesByIds(
        userId: Int, apiToken: String, storiesWithoutRecords: List<Story>
    ) {
        runCatching {
            autioRemoteDataSource.getStoriesByIds(userId,
                apiToken,
                storiesWithoutRecords.map { it.originalId })
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

    override suspend fun getStoriesInLatLngBoundaries(
        swCoordinates: LatLng, neCoordinates: LatLng
    ): List<MapPointEntity> {
        return autioLocalDataSource.getStoriesInLatLngBoundaries(swCoordinates, neCoordinates)
    }

    override suspend fun getDownloadedStoryById(id: String): DownloadedStoryEntity? {
        return autioLocalDataSource.getDownloadedStoryById(id)
    }

    override suspend fun downloadStory(story: DownloadedStoryEntity) {
        autioLocalDataSource.downloadStory(TODO("Map back from DownloadedStoryEntity to a DownloadedHistory Domain Model"))
    }

    override suspend fun getAllStories(): Result<List<Story>?> {
        val result = autioLocalDataSource.getAllStories()

        return if (result.isSuccess) {
            val stories = result.getOrNull()?.let { mapPoints ->
                mapPoints.map { it.toModel() }
            } ?: listOf()
            Result.success(stories)
        } else {
            val throwable = result.exceptionOrNull() ?: java.lang.Error()
            Result.failure(throwable)
        }
    }


    override suspend fun removeDownloadedStory(id: String) {
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

    override suspend fun removeAllDownloads() {
        autioLocalDataSource.removeAllDownloads()
    }

    override suspend fun removeBookmarkFromStory(id: String) {
        autioLocalDataSource.removeBookmarkFromStory(id)
    }

    override suspend fun removeAllBookmarks() {
        autioLocalDataSource.removeAllBookmarks()
    }

    override suspend fun bookmarkStory(id: String) {
        autioLocalDataSource.bookmarkStory(id)
    }

    override suspend fun giveLikeToStory(id: String) {
        autioLocalDataSource.giveLikeToStory(id)
    }

    override suspend fun removeLikeFromStory(id: String) {
        autioLocalDataSource.removeLikeFromStory(id)
    }

    override suspend fun addStoryToHistory(history: History) {
        autioLocalDataSource.addStoryToHistory(history.toEntity())
    }

    override suspend fun removeStoryFromHistory(id: String) {
        autioLocalDataSource.removeStoryFromHistory(id)
    }

    override suspend fun clearStoryHistory() {
        autioLocalDataSource.clearStoryHistory()
    }

    override suspend fun cacheRecordOfStory(storyId: String, recordUrl: String) {
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
       autioLocalDataSource.addStories(stories.map { it.toEntity() }) //TODO(check this method usage, user should not be able to "add/make" stories)
    }
}








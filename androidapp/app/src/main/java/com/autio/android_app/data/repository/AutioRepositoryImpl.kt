package com.autio.android_app.data.repository

import com.autio.android_app.data.api.model.account.LoginDto
import com.autio.android_app.data.api.model.account.LoginResponse
import com.autio.android_app.data.api.model.account.ProfileDto
import com.autio.android_app.data.database.entities.StoryEntity
import com.autio.android_app.data.repository.datasource.local.AutioLocalDataSource
import com.autio.android_app.data.repository.datasource.remote.AutioRemoteDataSource
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.ui.stories.models.Category
import com.autio.android_app.ui.stories.models.Story
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject

class AutioRepositoryImpl @Inject constructor(
    private val autioRemoteDataSource: AutioRemoteDataSource,
    private val autioLocalDataSource: AutioLocalDataSource,
    private val prefRepository: PrefRepository,
) : AutioRepository {

    override val userCategories: Flow<List<Category>>
        get() = autioRemoteDataSource.getCategories()
    override val allStories: Flow<List<Story>>
        get() = TODO("Not yet implemented")


    override suspend fun login(loginDto: LoginDto): Response<LoginResponse> {
        return autioRemoteDataSource.login(loginDto)
    }

    override suspend fun fetchUserData() {
        kotlin.runCatching {
            autioRemoteDataSource.getProfileDataV2(
                prefRepository.userId,
                prefRepository.userApiToken
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
        infoUser: ProfileDto,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        runCatching {
            autioRemoteDataSource.updateProfileV2(
                prefRepository.userId,
                prefRepository.userApiToken,
                infoUser
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
        infoUser: ProfileDto,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        runCatching {
            autioRemoteDataSource.updateProfileV2(
                prefRepository.userId,
                prefRepository.userApiToken,
                infoUser
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

    override suspend fun getStoriesByIds(
        userId: Int,
        apiToken: String,
        storiesWithoutRecords: List<Story>
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
        swCoordinates: LatLng,
        neCoordinates: LatLng
    ): List<StoryEntity> {
      return autioLocalDataSource.getStoriesInLatLngBoundaries(swCoordinates, neCoordinates)
    }


}






package com.autio.android_app.domain.repository

import com.autio.android_app.data.api.model.account.LoginDto
import com.autio.android_app.data.api.model.account.LoginResponse
import com.autio.android_app.data.api.model.account.ProfileDto
import com.autio.android_app.data.api.model.story.PlaysDto
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.database.entities.StoryEntity
import com.autio.android_app.ui.stories.models.Category
import com.autio.android_app.ui.stories.models.Story
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import retrofit2.Response

interface AutioRepository {
    val userCategories: Flow<List<Category>>
    val allStories: Flow<List<Story>>
    suspend fun login(loginDto: LoginDto): Response<LoginResponse>
    suspend fun fetchUserData()
    suspend fun updateProfile(infoUser: ProfileDto, onSuccess: () -> Unit, onFailure: () -> Unit)

    suspend fun updateCategoriesOrder(
        infoUser: ProfileDto, onSuccess: () -> Unit, onFailure: () -> Unit
    )

    suspend fun getStoriesByIds(userId: Int, apiToken: String, storiesWithoutRecords: List<Story>)

    suspend fun getStoriesInLatLngBoundaries(
        swCoordinates: LatLng, neCoordinates: LatLng
    ): List<StoryEntity>
    suspend fun postStoryPlayed(xUserId: Int, userApiToken: String, playsDto: PlaysDto)

    suspend fun getDownloadedStoryById(id: String): DownloadedStoryEntity?
}

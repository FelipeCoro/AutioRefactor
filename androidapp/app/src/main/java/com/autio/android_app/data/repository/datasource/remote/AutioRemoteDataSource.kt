package com.autio.android_app.data.repository.datasource.remote

import com.autio.android_app.data.api.model.account.ChangePasswordDto
import com.autio.android_app.data.api.model.account.ChangePasswordResponse
import com.autio.android_app.data.api.model.account.CreateAccountDto
import com.autio.android_app.data.api.model.account.GuestResponse
import com.autio.android_app.data.api.model.account.LoginDto
import com.autio.android_app.data.api.model.account.LoginResponse
import com.autio.android_app.data.api.model.bookmarks.AddBookmarkResponse
import com.autio.android_app.data.api.model.bookmarks.RemoveBookmarkResponse
import com.autio.android_app.data.api.model.history.AddHistoryResponse
import com.autio.android_app.data.api.model.history.ClearHistoryResponse
import com.autio.android_app.data.api.model.history.RemoveHistoryResponse
import com.autio.android_app.data.api.model.story.*
import retrofit2.Response

interface AutioRemoteDataSource {

    suspend fun login(loginDto: LoginDto): Response<LoginResponse>

    suspend fun createGuestAccount(): Response<GuestResponse>

    suspend fun createAccount(
        createAccountDto: CreateAccountDto
    ): Response<LoginResponse>

    suspend fun changePassword(
        xUserId: Int,
        apiToken: String,
        changePasswordDto: ChangePasswordDto
    ): Response<ChangePasswordResponse>

    suspend fun deleteAccount(
        xUserId: Int,
        apiToken: String,
    )

    suspend fun getProfileData(
        xUserId: Int,
        apiToken: String,
        userId: Int,
    )

    suspend fun getProfileDataV2(
        xUserId: Int,
        apiToken: String,
        userId: Int = xUserId,
    ): Response<com.autio.android_app.data.api.model.account.ProfileDto>

    suspend fun updateProfile(
        xUserId: Int,
        apiToken: String,
        userId: Int,
        profileDto: com.autio.android_app.data.api.model.account.ProfileDto
    ): Response<com.autio.android_app.data.api.model.account.ProfileDto>

    suspend fun updateProfileV2(
        xUserId: Int,
        apiToken: String,
        profileDto: com.autio.android_app.data.api.model.account.ProfileDto,
        userId: Int = xUserId,
    ): Response<com.autio.android_app.data.api.model.account.ProfileDto>

    suspend fun getStoriesByIds(
        xUserId: Int,
        apiToken: String,
        ids: List<Int>
    ): Response<List<StoryDto>>

    suspend fun getStoryById(
        xUserId: Int,
        apiToken: String,
        id: Int
    ): Response<StoryDto>

    suspend fun getStoriesSinceDate(
        xUserId: Int,
        apiToken: String,
        date: Int,
        page: Int
    ): Response<List<StoryDto>>

    suspend fun getStoriesDiff(
        xUserId: Int,
        apiToken: String,
        date: String
    ): Response<List<StoryDto>>

    suspend fun getStoriesByContributor(
        xUserId: Int,
        apiToken: String,
        contributorId: Int,
        page: Int,
    ): Response<ContributorResponse>

    suspend fun getAuthorOfStory(
        xUserId: Int,
        apiToken: String,
        storyId: Int,
    ): Response<AuthorDto>

    suspend fun getNarratorOfStory(
        xUserId: Int,
        apiToken: String,
        storyId: Int,
    ): Response<NarratorDto>

    suspend fun getCategories(
        xUserId: Int,
        apiToken: String,
    ): Response<List<StoryCategoryDto>>

    suspend fun postStoryPlayed(
        xUserId: Int,
        apiToken: String,
        playsDto: PlaysDto
    ): Response<PlaysResponse>

    suspend fun likedStoriesByUser(
        xUserId: Int,
        apiToken: String
    ): Response<List<StoryDto>>

    suspend fun isStoryLikedByUser(
        xUserId: Int,
        apiToken: String,
        storyId: Int
    ): Response<StoryLikedResponse>

    suspend fun giveLikeToStory(
        xUserId: Int,
        apiToken: String,
        storyId: Int
    ): Response<LikeResponse>

    suspend fun removeLikeFromStory(
        xUserId: Int,
        apiToken: String,
        storyId: Int
    ): Response<LikeResponse>

    suspend fun  storyLikesCount(
        xUserId: Int,
        apiToken: String,
        storyId: Int
    ):Response<StoryLikesResponse>

    suspend fun getUserHistory(
        xUserId: Int,
        apiToken: String
    ): Response<List<StoryDto>>

    suspend fun addStoryToHistory(
        xUserId: Int,
        apiToken: String,
        storyId: Int
    ): Response<AddHistoryResponse>

    suspend fun clearHistory(
        xUserId: Int,
        apiToken: String
    ): Response<ClearHistoryResponse>

    suspend fun removeStoryFromHistory(
        xUserId: Int,
        apiToken: String,
        storyId: Int
    ): Response<RemoveHistoryResponse>

    suspend fun getStoriesFromUserBookmarks(
        xUserId: Int,
        apiToken: String
    ): Response<List<StoryDto>>

    suspend fun bookmarkStory(
        xUserId: Int,
        apiToken: String,
        storyId: Int
    ): Response<AddBookmarkResponse>

    suspend fun removeBookmarkFromStory(
        xUserId: Int,
        apiToken: String,
        storyId: Int
    ): Response<RemoveBookmarkResponse>

}

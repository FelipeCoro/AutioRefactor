package com.autio.android_app.data.repository.datasource.remote

import com.autio.android_app.data.model.account.ChangePasswordDto
import com.autio.android_app.data.model.account.ChangePasswordResponse
import com.autio.android_app.data.model.account.CreateAccountDto
import com.autio.android_app.data.model.account.GuestResponse
import com.autio.android_app.data.model.account.LoginDto
import com.autio.android_app.data.model.account.LoginResponse
import com.autio.android_app.data.model.account.ProfileDto
import com.autio.android_app.data.model.api_response.AddBookmarkResponse
import com.autio.android_app.data.model.api_response.AddHistoryResponse
import com.autio.android_app.data.model.api_response.ClearHistoryResponse
import com.autio.android_app.data.model.api_response.ContributorApiResponse
import com.autio.android_app.data.model.api_response.LikeResponse
import com.autio.android_app.data.model.api_response.PlaysResponse
import com.autio.android_app.data.model.api_response.RemoveBookmarkResponse
import com.autio.android_app.data.model.api_response.RemoveHistoryResponse
import com.autio.android_app.data.model.api_response.StoryLikedResponse
import com.autio.android_app.data.model.api_response.StoryLikesResponse
import com.autio.android_app.data.model.author.Author
import com.autio.android_app.data.model.category.StoryCategory
import com.autio.android_app.data.model.narrator.Narrator
import com.autio.android_app.data.model.plays.PlaysDto
import com.autio.android_app.data.model.story.Story
import retrofit2.Response

interface AutioRemoteDataSource {

    suspend fun login(
        loginDto: LoginDto
    ): Response<LoginResponse>

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
        userId: Int,
    ): Response<ProfileDto>

    suspend fun updateProfile(
        xUserId: Int,
        apiToken: String,
        userId: Int,
        profileDto: ProfileDto
    ): Response<ProfileDto>

    suspend fun updateProfileV2(
        xUserId: Int,
        apiToken: String,
        userId: Int,
        profileDto: ProfileDto
    ): Response<ProfileDto>

    suspend fun getStoriesByIds(
        xUserId: Int,
        apiToken: String,
        ids: List<Int>
    ): Response<List<Story>>

    suspend fun getStoriesSinceDate(
        xUserId: Int,
        apiToken: String,
        date: Int,
        page: Int
    ): Response<List<Story>>

    suspend fun getStoriesDiff(
        xUserId: Int,
        apiToken: String,
        date: String
    ): Response<List<Story>>

    suspend fun getStoriesByContributor(
        xUserId: Int,
        apiToken: String,
        contributorId: Int,
        page: Int,
    ): Response<ContributorApiResponse>

    suspend fun getAuthorOfStory(
        xUserId: Int,
        apiToken: String,
        storyId: Int,
    ): Response<Author>

    suspend fun getNarratorOfStory(
        xUserId: Int,
        apiToken: String,
        storyId: Int,
    ): Response<Narrator>

    suspend fun getCategories(
        xUserId: Int,
        apiToken: String,
    ): Response<List<StoryCategory>>

    suspend fun postStoryPlayed(
        xUserId: Int,
        apiToken: String,
        playsDto: PlaysDto
    ): Response<PlaysResponse>

    suspend fun likedStoriesByUser(
        xUserId: Int,
        apiToken: String
    ): Response<List<Story>>

    suspend fun isStoryLikedByUser(
        xUserId: Int,
        apiToken: String,
        storyId: Int
    ): Response<StoryLikedResponse>

    suspend fun likesByStory(
        xUserId: Int,
        apiToken: String,
        storyId: Int
    ): Response<StoryLikesResponse>

    suspend fun giveLikeToStory(
        xUserId: Int,
        apiToken: String,
        storyId: Int
    ): Response<LikeResponse>

    suspend fun removeLikeFromStory(
        xUserId: Int,
        apiToken: String,
        storyId: Int
    ) : Response<LikeResponse>

    suspend fun getUserHistory(
        xUserId: Int,
        apiToken: String
    ) : Response<List<Story>>

    suspend fun addStoryToHistory(
        xUserId: Int,
        apiToken: String,
        storyId: Int
    ) : Response<AddHistoryResponse>

    suspend fun clearHistory(
        xUserId: Int,
        apiToken: String
    ) : Response<ClearHistoryResponse>

    suspend fun removeStoryFromHistory(
        xUserId: Int,
        apiToken: String,
        storyId: Int
    ) : Response<RemoveHistoryResponse>

    suspend fun getStoriesFromUserBookmarks(
        xUserId: Int,
        apiToken: String
    ) : Response<List<Story>>

    suspend fun bookmarkStory(
        xUserId: Int,
        apiToken: String,
        storyId: Int
    ) : Response<AddBookmarkResponse>

    suspend fun removeBookmarkFromStory(
        xUserId: Int,
        apiToken: String,
        storyId: Int
    ) : Response<RemoveBookmarkResponse>

}

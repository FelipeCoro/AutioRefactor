package com.autio.android_app.data.repository.datasource.remote

import com.autio.android_app.data.api.ApiClient
import com.autio.android_app.data.api.model.account.*
import com.autio.android_app.data.api.model.bookmarks.RemoveBookmarkResponse
import com.autio.android_app.data.api.model.bookmarks.AddBookmarkResponse
import com.autio.android_app.data.api.model.history.AddHistoryResponse
import com.autio.android_app.data.api.model.history.ClearHistoryResponse
import com.autio.android_app.data.api.model.history.RemoveHistoryResponse
import com.autio.android_app.data.api.model.story.*
import retrofit2.Response
import javax.inject.Inject

class AutioRemoteDataSourceImpl @Inject constructor(
    private val apiClient: ApiClient
) : AutioRemoteDataSource {

    override suspend fun login(loginDto: LoginDto): Response<LoginResponse> {
        return apiClient.login(loginDto)
    }

    override suspend fun createGuestAccount(): Response<GuestResponse> {
        return apiClient.createGuestAccount()
    }

    override suspend fun createAccount(createAccountDto: CreateAccountDto): Response<LoginResponse> {
        return apiClient.createAccount(createAccountDto)
    }

    override suspend fun changePassword(
        xUserId: Int,
        apiToken: String,
        changePasswordDto: com.autio.android_app.data.api.model.account.ChangePasswordDto
    ): Response<com.autio.android_app.data.api.model.account.ChangePasswordResponse> {
        return apiClient.changePassword(xUserId, apiToken, changePasswordDto)
    }

    override suspend fun deleteAccount(xUserId: Int, apiToken: String) {
        return apiClient.deleteAccount(xUserId, apiToken)
    }

    override suspend fun getProfileData(xUserId: Int, apiToken: String, userId: Int) {
        return apiClient.getProfileData(xUserId, apiToken, userId)
    }

    override suspend fun getProfileDataV2(
        xUserId: Int, apiToken: String, userId: Int
    ): Response<ProfileDto> {
        return apiClient.getProfileDataV2(xUserId, apiToken, userId)
    }

    override suspend fun updateProfile(
        xUserId: Int, apiToken: String, userId: Int, profileDto: ProfileDto
    ): Response<ProfileDto> {
        return apiClient.updateProfile(xUserId, apiToken, userId, profileDto)
    }

    override suspend fun updateProfileV2(
        xUserId: Int, apiToken: String, profileDto: ProfileDto, userId: Int
    ): Response<ProfileDto> {
        return apiClient.updateProfileV2(xUserId, apiToken, userId, profileDto)
    }

    override suspend fun getStoriesByIds(
        xUserId: Int, apiToken: String, ids: List<Int>
    ): Response<List<StoryDto>> {
        return Response.success(apiClient.getStoriesByIds(xUserId, apiToken, ids).body()!!)
    }

    override suspend fun getStoryById(
        xUserId: Int, apiToken: String, id: Int
    ): Response<StoryDto> {
        return apiClient.getStoryById(xUserId, apiToken, id)
    }


    override suspend fun getStoriesSinceDate(
        xUserId: Int, apiToken: String, date: Int, page: Int
    ): Response<List<StoryDto>> {
        return apiClient.getStoriesSinceDate(xUserId, apiToken, date, page)
    }

    override suspend fun getStoriesDiff(
        xUserId: Int, apiToken: String, date: String
    ): Response<List<StoryDto>> {
        return apiClient.getStoriesDiff(xUserId, apiToken, date)
    }

    override suspend fun getStoriesByContributor(
        xUserId: Int, apiToken: String, contributorId: Int, page: Int
    ): Response<ContributorResponse> {
        return apiClient.getStoriesByContributor(xUserId, apiToken, contributorId, page)
    }

    override suspend fun getAuthorOfStory(
        xUserId: Int, apiToken: String, storyId: Int
    ): Response<AuthorDto> {
        return apiClient.getAuthorOfStory(xUserId, apiToken, storyId)
    }

    override suspend fun getNarratorOfStory(
        xUserId: Int, apiToken: String, storyId: Int
    ): Response<NarratorDto> {
        return apiClient.getNarratorOfStory(xUserId, apiToken, storyId)
    }

    override suspend fun getCategories(
        xUserId: Int, apiToken: String
    ): Response<List<StoryCategoryDto>> {
        return apiClient.getCategories(xUserId, apiToken)
    }

    override suspend fun postStoryPlayed(
        xUserId: Int, apiToken: String, playsDto: PlaysDto
    ): Response<PlaysResponse> {
        return apiClient.postStoryPlayed(xUserId, apiToken, playsDto)
    }

    override suspend fun likedStoriesByUser(
        xUserId: Int, apiToken: String
    ): Response<List<StoryDto>> {
        return apiClient.likedStoriesByUser(xUserId, apiToken)
    }

    override suspend fun isStoryLikedByUser(
        xUserId: Int, apiToken: String, storyId: Int
    ): Response<StoryLikedResponse> {
        return apiClient.isStoryLikedByUser(xUserId, apiToken, storyId)
    }

    override suspend fun likesByStory(
        xUserId: Int, apiToken: String, storyId: Int
    ): Response<StoryLikesResponse> {
        return apiClient.likesByStory(xUserId, apiToken, storyId)
    }

    override suspend fun giveLikeToStory(
        xUserId: Int, apiToken: String, storyId: Int
    ): Response<LikeResponse> {
        return apiClient.giveLikeToStory(xUserId, apiToken, storyId)
    }

    override suspend fun removeLikeFromStory(
        xUserId: Int, apiToken: String, storyId: Int
    ): Response<LikeResponse> {
        return apiClient.removeLikeFromStory(xUserId, apiToken, storyId)
    }

    override suspend fun getUserHistory(
        xUserId: Int, apiToken: String
    ): Response<List<StoryDto>> {
        return apiClient.getUserHistory(xUserId, apiToken)
    }

    override suspend fun addStoryToHistory(
        xUserId: Int, apiToken: String, storyId: Int
    ): Response<AddHistoryResponse> {
        return apiClient.addStoryToHistory(xUserId, apiToken, storyId)
    }

    override suspend fun clearHistory(
        xUserId: Int, apiToken: String
    ): Response<ClearHistoryResponse> {
        return apiClient.clearHistory(xUserId, apiToken)
    }

    override suspend fun removeStoryFromHistory(
        xUserId: Int, apiToken: String, storyId: Int
    ): Response<RemoveHistoryResponse> {
        return apiClient.removeStoryFromHistory(xUserId, apiToken, storyId)
    }

    override suspend fun getStoriesFromUserBookmarks(
        xUserId: Int, apiToken: String
    ): Response<List<StoryDto>> {
        return apiClient.getStoriesFromUserBookmarks(xUserId, apiToken)
    }

    override suspend fun bookmarkStory(
        xUserId: Int, apiToken: String, storyId: Int
    ): Response<AddBookmarkResponse> {
        return apiClient.bookmarkStory(xUserId, apiToken, storyId)
    }

    override suspend fun removeBookmarkFromStory(
        xUserId: Int, apiToken: String, storyId: Int
    ): Response<RemoveBookmarkResponse> {
        return apiClient.removeBookmarkFromStory(xUserId, apiToken, storyId)
    }

}

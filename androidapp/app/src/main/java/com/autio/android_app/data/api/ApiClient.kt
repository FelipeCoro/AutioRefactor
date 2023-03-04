package com.autio.android_app.data.api

import com.autio.android_app.data.api.model.bookmarks.RemoveBookmarkResponse
import com.autio.android_app.data.api.model.bookmarks.AddBookmarkResponse
import com.autio.android_app.data.api.model.history.RemoveHistoryResponse
import com.autio.android_app.data.api.model.history.ClearHistoryResponse
import com.autio.android_app.data.api.model.history.AddHistoryResponse
import com.autio.android_app.data.api.model.story.StoryLikedResponse
import com.autio.android_app.data.api.model.account.*
import com.autio.android_app.data.api.model.story.PlaysDto
import com.autio.android_app.data.api.model.story.*
import retrofit2.Response
import retrofit2.http.*

interface ApiClient {

    // ACCOUNT CALLS
    /**
     * Authenticates user in backend and returns the user's
     * profile data
     * @param loginDto object including the email and password
     */
    @POST("/api/v1/login")
    suspend fun login(@Body loginDto: LoginDto): Response<LoginResponse>

    /**
     * Authenticates user as guest and returns the guest's
     * data
     */
    @POST("/api/v1/guests")
    suspend fun createGuestAccount(): Response<GuestResponse>

    /**
     * Creates user in backend and returns the user's
     * profile data
     * @param createAccountDto object including user's data (name, email, password)
     */
    @POST("/api/v1/accounts")
    suspend fun createAccount(@Body createAccountDto: CreateAccountDto): Response<LoginResponse>

    /**
     * Changes password for future authentication
     * @param changePasswordDto includes the current password for validation and the new password to update
     */
    @POST("/api/v1/passwords/change")
    suspend fun changePassword(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
        @Body changePasswordDto: ChangePasswordDto
    ): Response<ChangePasswordResponse>

    // TODO: Check which is the correct call for this and create ResponseObject
    /**
     * Deletes the account and its associated data
     */
    @POST("/api/v1/delete-account")
    suspend fun deleteAccount(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
    )

    // PROFILE CALLS
    @Deprecated(
        "Returns any user's profile matching the passed id (generally used for obtaining the logged user's data)",
        ReplaceWith("getProfileDataV2")
    )
    @GET("/api/v1/{user_id}")
    suspend fun getProfileData(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
        @Path("user_id") userId: Int,
    )

    /**
     * Returns any user's profile matching the passed
     * id (generally used for obtaining the logged user's
     * data)
     * @param userId user's id
     */
    @GET("/api/v1/usersV2/{user_id}")
    suspend fun getProfileDataV2(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
        @Path("user_id") userId: Int = xUserId,
    ): Response<ProfileDto>

    @Deprecated(
        "Updates user's profile with new displaying data and preferences on stories' categories",
        ReplaceWith(
            "updateProfileV2"
        )
    )
    @POST("/api/v1/users")
    suspend fun updateProfile(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
        @Path("user_id") userId: Int,
        @Body profileDto: ProfileDto
    ): Response<ProfileDto>

    /**
     * Updates user's profile with new displaying data
     * and preferences on stories' categories
     * @param profileDto includes email, name and preferred ordered categories to update
     */
    @PUT("/api/v1/usersV2/{user_id}")
    suspend fun updateProfileV2(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
        @Path("user_id") userId: Int = xUserId,
        @Body profileDto: ProfileDto
    ): Response<ProfileDto>
    // STORY CALLS

    /**
     * Returns a list with data of all stories matching the ids inside the query
     * @param ids list of ids of each story that should be fetched
     */
    @GET("/api/v1/stories/by-ids-v2")
    suspend fun getStoriesByIds(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
        @Query("ids[]") ids: List<Int>
    ): Response<List<StoryDto>>

    @Deprecated(
        "Captures stories and its content after a certain datetime", ReplaceWith("getStoriesDiff")
    )
    @GET("/api/v1/stories/diff")
    suspend fun getStoriesSinceDate(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
        @Query("date") date: Int,
        @Query("page") page: Int
    ): Response<List<StoryDto>>

    /**
     * Captures stories and its content after a certain date
     * @param date date and time from which the filter should start from
     */
    @GET("/api/v1/stories/diff")
    suspend fun getStoriesDiff(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
        @Query("date") date: String
    ): Response<List<StoryDto>>

    @GET("/api/v1/contributors/{contributor_id}/latest-stories")
    suspend fun getStoriesByContributor(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
        @Path("contributor_id") contributorId: Int,
        @Query("page") page: Int,
    ): Response<ContributorResponse>

    @GET("/api/v1/stories/{story_id}/author")
    suspend fun getAuthorOfStory(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
        @Path("story_id") storyId: Int,
    ): Response<AuthorDto>

    @GET("/api/v1/stories/{story_id}/narrator")
    suspend fun getNarratorOfStory(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
        @Path("story_id") storyId: Int,
    ): Response<NarratorDto>

    @GET("/api/v1/categories")
    suspend fun getCategories(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
    ): Response<List<StoryCategoryDto>>

    @POST("/api/v1/plays")
    suspend fun postStoryPlayed(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
        @Body playsDto: PlaysDto
    ): Response<PlaysResponse>

    // FAVORITE CALLS
    @GET("/api/v1/likes")
    suspend fun likedStoriesByUser(
        @Header("X-User-Id") xUserId: Int, @Header("Authorization") apiToken: String
    ): Response<List<StoryDto>>

    @GET("/api/v1/likes/{story_id}/is-liked")
    suspend fun isStoryLikedByUser(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
        @Path("story_id") storyId: Int
    ): Response<StoryLikedResponse>

    @GET("/api/v1/likes/{story_id}/count")
    suspend fun likesByStory(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
        @Path("story_id") storyId: Int
    ): Response<StoryLikesResponse>

    @POST("/api/v1/likes/{story_id}")
    suspend fun giveLikeToStory(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
        @Path("story_id") storyId: Int
    ): Response<LikeResponse>

    @DELETE("/api/v1/likes/{story_id}")
    suspend fun removeLikeFromStory(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
        @Path("story_id") storyId: Int
    ): Response<LikeResponse>

    // HISTORY CALLS
    @GET("/api/v1/playedHistory")
    suspend fun getUserHistory(
        @Header("X-User-Id") xUserId: Int, @Header("Authorization") apiToken: String
    ): Response<List<StoryDto>>

    @PUT("/api/v1/playedHistory/{story_id}")
    suspend fun addStoryToHistory(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
        @Path("story_id") storyId: Int
    ): Response<AddHistoryResponse>

    @DELETE("/api/v1/playedHistory/all")
    suspend fun clearHistory(
        @Header("X-User-Id") xUserId: Int, @Header("Authorization") apiToken: String
    ): Response<ClearHistoryResponse>

    @DELETE("/api/v1/playedHistory/{story_id}")
    suspend fun removeStoryFromHistory(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
        @Path("story_id") storyId: Int
    ): Response<RemoveHistoryResponse>

    // BOOKMARK CALLS

    @GET("/api/v1/library")
    suspend fun getStoriesFromUserBookmarks(
        @Header("X-User-Id") xUserId: Int, @Header("Authorization") apiToken: String
    ): Response<List<StoryDto>>

    @POST("/api/v1/library/{user_id}")
    suspend fun bookmarkStory(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
        @Path("story_id") storyId: Int
    ): Response<AddBookmarkResponse>

    @DELETE("/api/v1/library/{user_id}")
    suspend fun removeBookmarkFromStory(
        @Header("X-User-Id") xUserId: Int,
        @Header("Authorization") apiToken: String,
        @Path("story_id") storyId: Int
    ): Response<RemoveBookmarkResponse>
}

package com.autio.android_app.data.repository

import com.autio.android_app.data.model.account.*
import com.autio.android_app.data.model.api_response.*
import com.autio.android_app.data.model.author.Author
import com.autio.android_app.data.model.category.StoryCategory
import com.autio.android_app.data.model.narrator.Narrator
import com.autio.android_app.data.model.plays.PlaysDto
import com.autio.android_app.data.model.story.Story
import retrofit2.Call
import retrofit2.http.*

interface ApiClient {

    // ACCOUNT CALLS

    /**
     * Authenticates user in backend and returns the user's
     * profile data
     * @param loginDto object including the email and password
     */
    @POST(
        "/api/v1/login"
    )
    fun login(
        @Body loginDto: LoginDto
    ): Call<LoginResponse>

    /**
     * Authenticates user as guest and returns the guest's
     * data
     */
    @POST(
        "/api/v1/guests"
    )
    fun createGuestAccount(): Call<GuestResponse>

    /**
     * Creates user in backend and returns the user's
     * profile data
     * @param createAccountDto object including user's data (name, email, password)
     */
    @POST(
        "/api/v1/accounts"
    )
    fun createAccount(
        @Body createAccountDto: CreateAccountDto
    ): Call<LoginResponse>

    /**
     * Changes password for future authentication
     * @param changePasswordDto includes the current password for validation and the new password to update
     */
    @POST(
        "/api/v1/passwords/change"
    )
    fun changePassword(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Body changePasswordDto: ChangePasswordDto
    ): Call<ChangePasswordResponse>

    // TODO: Check which is the correct call for this and create ResponseObject
    /**
     * Deletes the account and its associated data
     */
    @POST(
        "/api/v1/delete-account"
    )
    fun deleteAccount(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
    )

    // PROFILE CALLS

    @Deprecated(
        "Returns any user's profile matching the passed id (generally used for obtaining the logged user's data)",
        ReplaceWith(
            "getProfileDataV2"
        )
    )
    @GET(
        "/api/v1/{user_id}"
    )
    fun getProfileData(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Path(
            "user_id"
        ) userId: Int,
    )

    /**
     * Returns any user's profile matching the passed
     * id (generally used for obtaining the logged user's
     * data)
     * @param userId user's id
     */
    @GET(
        "/api/v1/usersV2/{user_id}"
    )
    fun getProfileDataV2(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Path(
            "user_id"
        ) userId: Int,
    ): Call<ProfileDto>

    @Deprecated(
        "Updates user's profile with new displaying data and preferences on stories' categories",
        ReplaceWith(
            "updateProfileV2"
        )
    )
    @POST(
        "/api/v1/users"
    )
    fun updateProfile(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Path(
            "user_id"
        ) userId: Int,
        @Body profileDto: ProfileDto
    ): Call<ProfileDto>

    /**
     * Updates user's profile with new displaying data
     * and preferences on stories' categories
     * @param profileDto includes email, name and preferred ordered categories to update
     */
    @PUT(
        "/api/v1/usersV2/{user_id}"
    )
    fun updateProfileV2(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Path(
            "user_id"
        ) userId: Int,
        @Body profileDto: ProfileDto
    ): Call<ProfileDto>

    // STORY CALLS

    /**
     * Returns a list with data of all stories matching the ids inside the query
     * @param ids list of ids of each story that should be fetched
     */
    @GET(
        "/api/v1/stories/by-ids-v2"
    )
    fun getStoriesByIds(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Query(
            "ids[]"
        ) ids: List<Int>
    ): Call<List<Story>>

    @Deprecated(
        "Captures stories and its content after a certain datetime",
        ReplaceWith(
            "getStoriesDiff"
        )
    )
    @GET(
        "/api/v1/stories/diff"
    )
    fun getStoriesSinceDate(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Query(
            "date"
        ) date: Int,
        @Query(
            "page"
        ) page: Int
    ): Call<List<Story>>

    /**
     * Captures stories and its content after a certain date
     * @param date date and time from which the filter should start from
     */
    @GET(
        "/api/v1/stories/diff"
    )
    fun getStoriesDiff(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Query(
            "date"
        ) date: String
    ): Call<List<Story>>

    @GET(
        "/api/v1/contributors/{contributor_id}/latest-stories"
    )
    fun getStoriesByContributor(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Path(
            "contributor_id"
        ) contributorId: Int,
        @Query(
            "page"
        ) page: Int,
    ): Call<ContributorApiResponse>

    @GET(
        "/api/v1/stories/{story_id}/author"
    )
    fun getAuthorOfStory(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Path(
            "story_id"
        ) storyId: Int,
    ): Call<Author>

    @GET(
        "/api/v1/stories/{story_id}/narrator"
    )
    fun getNarratorOfStory(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Path(
            "story_id"
        ) storyId: Int,
    ): Call<Narrator>

    @GET(
        "/api/v1/categories"
    )
    fun getCategories(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
    ): Call<List<StoryCategory>>

    @POST(
        "/api/v1/plays"
    )
    fun postStoryPlayed(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Body playsDto: PlaysDto
    ): Call<PlaysResponse>

    // FAVORITE CALLS

    @GET(
        "/api/v1/likes"
    )
    fun likedStoriesByUser(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String
    ): Call<List<Story>>

    @GET(
        "/api/v1/likes/{story_id}/is-liked"
    )
    fun isStoryLikedByUser(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Path(
            "story_id"
        ) storyId: Int
    ): Call<StoryLikedResponse>

    @GET(
        "/api/v1/likes/{story_id}/count"
    )
    fun likesByStory(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Path(
            "story_id"
        ) storyId: Int
    ): Call<StoryLikesResponse>

    @POST(
        "/api/v1/likes/{story_id}"
    )
    fun giveLikeToStory(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Path(
            "story_id"
        ) storyId: Int
    ): Call<LikeResponse>

    @DELETE(
        "/api/v1/likes/{story_id}"
    )
    fun removeLikeFromStory(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Path(
            "story_id"
        ) storyId: Int
    ) : Call<LikeResponse>

    // HISTORY CALLS

    @GET(
        "/api/v1/playedHistory"
    )
    fun getUserHistory(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String
    ) : Call<List<Story>>

    @PUT(
        "/api/v1/playedHistory/{story_id}"
    )
    fun addStoryToHistory(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Path(
            "story_id"
        ) storyId: Int
    ) : Call<AddHistoryResponse>

    @DELETE(
        "/api/v1/playedHistory/all"
    )
    fun clearHistory(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String
    ) : Call<ClearHistoryResponse>

    @DELETE(
        "/api/v1/playedHistory/{story_id}"
    )
    fun removeStoryFromHistory(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Path(
            "story_id"
        ) storyId: Int
    ) : Call<RemoveHistoryResponse>

    // BOOKMARK CALLS

    @GET(
        "/api/v1/library"
    )
    fun getStoriesFromUserBookmarks(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String
    ) : Call<List<Story>>

    @POST(
        "/api/v1/library/{user_id}"
    )
    fun bookmarkStory(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Path(
            "story_id"
        ) storyId: Int
    ) : Call<AddBookmarkResponse>

    @DELETE(
        "/api/v1/library/{user_id}"
    )
    fun removeBookmarkFromStory(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Path(
            "story_id"
        ) storyId: Int
    ) : Call<RemoveBookmarkResponse>
}
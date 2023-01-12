package com.autio.android_app.data.repository

import com.autio.android_app.data.model.account.*
import com.autio.android_app.data.model.api_response.ContributorApiResponse
import com.autio.android_app.data.model.api_response.PlaysResponse
import com.autio.android_app.data.model.author.Author
import com.autio.android_app.data.model.category.StoryCategory
import com.autio.android_app.data.model.narrator.Narrator
import com.autio.android_app.data.model.plays.PlaysDto
import com.autio.android_app.data.model.story.Story
import retrofit2.Call
import retrofit2.http.*

interface ApiClient {
    @Headers(
        "Content-Type: application/json",
    )
    @POST(
        "/api/v1/login"
    )
    fun login(
        @Body loginDto: LoginDto
    ): Call<LoginResponse>

    @Headers(
        "Content-Type: application/json",
    )
    @POST(
        "/api/v1/guests"
    )
    fun guest(): Call<GuestResponse>

    @Headers(
        "Content-Type: application/json",
    )
    @POST(
        "/api/v1/accounts"
    )
    fun createAccount(
        @Body createAccountDto: CreateAccountDto
    ): Call<LoginResponse>


    @Headers(
        "Content-Type: application/json",
    )
    @POST(
        "/api/v1/users/{user_id}"
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
        @Body updateProfileDto: UpdateProfileDto
    ): Call<UpdateProfileDto>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
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

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @GET("/api/v1/stories/by-ids-v2")
    fun getStoriesByIds(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Query(
            "ids[]"
        ) ids : List<Int>
    ): Call<List<Story>>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
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
        @Query("page") page: Int,
    ): Call<List<Story>>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @GET("/api/v1/contributors/{contributor_id}/latest-stories")
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
        @Query("page") page: Int,
    ): Call<ContributorApiResponse>

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @GET("/api/v1/stories/{story_id}/author")
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

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @GET("/api/v1/stories/{story_id}/narrator")
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

    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @GET("api/v1/categories")
    fun getCategories(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
    ): Call<List<StoryCategory>>

    @POST("api/v1/plays")
    fun postStoryPlayed(
        @Header(
            "X-User-Id"
        ) xUserId: Int,
        @Header(
            "Authorization"
        ) apiToken: String,
        @Body playsDto: PlaysDto
    ): Call<PlaysResponse>
}
package com.autio.android_app.data.repository

import com.autio.android_app.core.RetrofitHelper
import com.autio.android_app.data.model.account.*
import com.autio.android_app.data.model.author.Author
import com.autio.android_app.data.model.category.StoryCategory
import com.autio.android_app.data.model.narrator.Narrator
import com.autio.android_app.data.model.story.Story
import com.autio.android_app.data.model.story.StoryDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiService {
    private val retrofit =
        RetrofitHelper.buildService(
            ApiClient::class.java
        )

    // Authentication

    /**
     * Gives access to user to make requests to API based on a bearer token
     * and user's data included in the [LoginResponse]
     * If the [onResult] is given a "null" value, then it is assumed
     * the credentials were not valid
     */
    fun login(
        loginDto: LoginDto,
        onResult: (LoginResponse?) -> Unit
    ) {
        retrofit.login(
            loginDto
        )
            .enqueue(
                object :
                    Callback<LoginResponse> {
                    override fun onFailure(
                        call: Call<LoginResponse>,
                        t: Throwable
                    ) {
                        onResult(
                            null
                        )
                    }

                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        if (response.isSuccessful) {
                            val userInfo =
                                response.body()
                            onResult(
                                userInfo
                            )
                        }
                    }
                })
    }

    /**
     * Authenticates a user as a guest
     * The difference from the [login] process is that the created user
     * has no data besides of a bearer token and an id for making limited requests to
     * server.
     */
    fun guest(
        onResult: (GuestResponse?) -> Unit
    ) {
        retrofit.guest()
            .enqueue(
                object :
                    Callback<GuestResponse> {
                    override fun onResponse(
                        call: Call<GuestResponse>,
                        response: Response<GuestResponse>
                    ) {
                        if (response.isSuccessful) {
                            val guestInfo =
                                response.body()
                            onResult(
                                guestInfo
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<GuestResponse>,
                        t: Throwable
                    ) {
                        onResult(
                            null
                        )
                    }
                })
    }

    /**
     * Creates an account for the user and then it follows the same process
     * in the [login] section. If the [onResult] is given a "null" value,
     * it is assumed the email is already being used by an existing account.
     */
    fun createAccount(
        createAccountDto: CreateAccountDto,
        onResult: (LoginResponse?) -> Unit
    ) {
        retrofit.createAccount(
            createAccountDto
        )
            .enqueue(
                object :
                    Callback<LoginResponse> {
                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        if (response.isSuccessful) {
                            val userInfo =
                                response.body()
                            onResult(
                                userInfo
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<LoginResponse>,
                        t: Throwable
                    ) {
                        onResult(
                            null
                        )
                    }

                })
    }

    fun updateProfile(
        xUserId: Int,
        apiToken: String,
        userId: Int,
        updateProfileDto: UpdateProfileDto,
        onResult: (UpdateProfileDto?) -> Unit
    ) {
        retrofit.updateProfile(
            xUserId,
            apiToken,
            userId,
            updateProfileDto
        )
            .enqueue(
                object :
                    Callback<UpdateProfileDto> {
                    override fun onResponse(
                        call: Call<UpdateProfileDto>,
                        response: Response<UpdateProfileDto>
                    ) {
                        if (response.isSuccessful) {
                            val userInfo =
                                response.body()
                            onResult(
                                userInfo
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<UpdateProfileDto>,
                        t: Throwable
                    ) {
                        onResult(
                            null
                        )
                    }
                })
    }

    fun changePassword(
        xUserId: Int,
        apiToken: String,
        changePasswordDto: ChangePasswordDto,
        onResult: (ChangePasswordResponse?) -> Unit
    ) {
        retrofit.changePassword(
            xUserId,
            apiToken,
            changePasswordDto
        )
            .enqueue(
                object :
                    Callback<ChangePasswordResponse> {
                    override fun onResponse(
                        call: Call<ChangePasswordResponse>,
                        response: Response<ChangePasswordResponse>
                    ) {
                        if (response.isSuccessful) {
                            val res =
                                response.body()
                            onResult(
                                res
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ChangePasswordResponse>,
                        t: Throwable
                    ) {
                        onResult(
                            null
                        )
                    }
                })
    }

    // Stories requests

    /**
     * Capture stories by a list of [ids] passed to the [onResult]
     */
    fun getStoriesByIds(
        xUserId: Int,
        apiToken: String,
        ids: ArrayList<String>,
        onResult: (List<Story>?) -> Unit
    ) {
        retrofit.getStoriesByIds(
            xUserId,
            apiToken,
            ids.joinToString(
                ","
            )
        )
            .enqueue(
                object :
                    Callback<List<Story>> {
                    override fun onResponse(
                        call: Call<List<Story>>,
                        response: Response<List<Story>>
                    ) {
                        if (response.isSuccessful) {
                            val stories =
                                response.body()
                            onResult(
                                stories
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<List<Story>>,
                        t: Throwable
                    ) =
                        onResult(
                            null
                        )
                })
    }

    /**
     * Capture stories after a certain epoch [date]
     * The API integrates pagination, so a [page] is also required
     * This function should be used for capturing the stories
     * that will be shown in the maps' view
     */
    fun getStoriesSinceDate(
        xUserId: Int,
        apiToken: String,
        date: Int,
        page: Int,
        onResult: (List<Story>?) -> Unit
    ) =
        retrofit.getStoriesSinceDate(
            xUserId,
            apiToken,
            date,
            page
        )
            .enqueue(
                object :
                    Callback<List<Story>> {
                    override fun onResponse(
                        call: Call<List<Story>>,
                        response: Response<List<Story>>
                    ) {
                        if (response.isSuccessful) {
                            val stories =
                                response.body()
                            onResult(
                                stories
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<List<Story>>,
                        t: Throwable
                    ) =
                        onResult(
                            null
                        )
                }
            )

    fun getStoriesByContributor(
        xUserId: Int,
        apiToken: String,
        contributorId: Int,
        page: Int,
        onResult: (List<Story>?) -> Unit
    ) =
        retrofit.getStoriesByContributor(
            xUserId,
            apiToken,
            contributorId,
            page
        )
            .enqueue(
                object :
                    Callback<List<Story>> {
                    override fun onResponse(
                        call: Call<List<Story>>,
                        response: Response<List<Story>>
                    ) {
                        if (response.isSuccessful) {
                            val stories =
                                response.body()
                            onResult(
                                stories
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<List<Story>>,
                        t: Throwable
                    ) =
                        onResult(
                            null
                        )
                }
            )

    fun getAuthorOfStory(
        xUserId: Int,
        apiToken: String,
        storyDto: StoryDto,
        onResult: (Author?) -> Unit
    ) =
        retrofit.getAuthorOfStory(
            xUserId,
            apiToken,
            storyDto
        )
            .enqueue(
                object :
                    Callback<Author> {
                    override fun onResponse(
                        call: Call<Author>,
                        response: Response<Author>
                    ) {
                        if (response.isSuccessful) {
                            val author =
                                response.body()
                            onResult(
                                author
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<Author>,
                        t: Throwable
                    ) =
                        onResult(
                            null
                        )
                })

    fun getNarratorOfStory(
        xUserId: Int,
        apiToken: String,
        storyDto: StoryDto,
        onResult: (Narrator?) -> Unit
    ) =
        retrofit.getNarratorOfStory(
            xUserId,
            apiToken,
            storyDto
        )
            .enqueue(
                object :
                    Callback<Narrator> {
                    override fun onResponse(
                        call: Call<Narrator>,
                        response: Response<Narrator>
                    ) {
                        if (response.isSuccessful) {
                            val narrator =
                                response.body()
                            onResult(
                                narrator
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<Narrator>,
                        t: Throwable
                    ) =
                        onResult(
                            null
                        )
                })

    fun getStoryCategories(
        xUserId: Int,
        apiToken: String,
        onResult: (List<StoryCategory>?) -> Unit
    ) =
        retrofit.getCategories(
            xUserId,
            apiToken
        )
            .enqueue(
                object :
                    Callback<List<StoryCategory>> {
                    override fun onResponse(
                        call: Call<List<StoryCategory>>,
                        response: Response<List<StoryCategory>>
                    ) {
                        if (response.isSuccessful) {
                            val categories =
                                response.body()
                            onResult(
                                categories
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<List<StoryCategory>>,
                        t: Throwable
                    ) =
                        onResult(
                            null
                        )
                })
}
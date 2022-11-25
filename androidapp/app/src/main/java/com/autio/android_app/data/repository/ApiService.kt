package com.autio.android_app.data.repository

import android.util.Log
import com.autio.android_app.core.RetrofitHelper
import com.autio.android_app.data.model.account.*
import com.autio.android_app.data.model.story.StoryDto
import com.autio.android_app.data.model.story.StoryResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiService {
    private val retrofit =
        RetrofitHelper.buildService(
            ApiClient::class.java
        )

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
                        if (!response.isSuccessful) {
                            onResult(
                                null
                            )
                        } else {
                            val userInfo =
                                response.body()
                            onResult(
                                userInfo
                            )
                        }
                    }
                })
    }

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
                        if (!response.isSuccessful) {
                            if (response.errorBody()
                                    ?.contentType()
                                    ?.subtype()
                                    .equals(
                                        "application/json"
                                    )
                            ) {
                                Log.i(
                                    "SIGN IN:",
                                    "-------------Error message----------------"
                                )
                            }
                        }
                        val guestInfo =
                            response.body()
                        onResult(
                            guestInfo
                        )
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
                        if (!response.isSuccessful) {
                            if (response.errorBody()
                                    ?.contentType()
                                    ?.subtype()
                                    .equals(
                                        "application/json"
                                    )
                            ) {
                                Log.i(
                                    "CREATE ACCOUNT:",
                                    "-------------Error message----------------"
                                )
                            }
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
                        if (!response.isSuccessful) {
                            if (response.errorBody()
                                    ?.contentType()
                                    ?.subtype()
                                    .equals(
                                        "application/json"
                                    )
                            ) {
                                Log.i(
                                    "UPDATE PROFILE:",
                                    "------------Error message-----------"
                                )
                            }
                            val userInfo =
                                response.body()
                            Log.i(
                                "UPDATE PROFILE:",
                                userInfo.toString()
                            )
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
                        Log.i(
                            "NEW PASSWORD:",
                            "-------------------Success on call-----------------"
                        )
                        if (!response.isSuccessful) {
                            if (response.errorBody()
                                    ?.contentType()
                                    ?.subtype()
                                    .equals(
                                        "application/json"
                                    )
                            ) {
                                Log.i(
                                    "NEW PASSWORD:",
                                    "-------------Error message----------------"
                                )
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<ChangePasswordResponse>,
                        t: Throwable
                    ) {
                        onResult(
                            null
                        )
                        Log.i(
                            "NEW PASSWORD:",
                            "-------------------Fail Response-----------------"
                        )
                    }

                })
    }

    fun getStoriesByIds(
        xUserId: Int,
        apiToken: String,
        storyDto: StoryDto,
        onResult: (List<StoryResponse>?) -> Unit
    ) {
        retrofit.getStoriesByIds(
            xUserId,
            apiToken,
            storyDto
        )
            .enqueue(
                object :
                    Callback<List<StoryResponse>> {
                    override fun onResponse(
                        call: Call<List<StoryResponse>>,
                        response: Response<List<StoryResponse>>
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
                        call: Call<List<StoryResponse>>,
                        t: Throwable
                    ) {
                        Log.d(
                            "STORIES",
                            "-------------------Fail Response-----------------\n${t.message}"
                        )
                    }
                })
    }
}
package com.autio.android_app.data.repository

import com.autio.android_app.data.model.account.*
import retrofit2.Call
import retrofit2.http.*

interface ApiClient {

    @Headers(
        "Content-Type: application/json",
    )
    @POST("/api/v1/login")
    fun login(@Body loginDto: LoginDto): Call<LoginResponse>

    @Headers(
        "Content-Type: application/json",
    )
    @POST("/api/v1/guests")
    fun guest(): Call<GuestResponse>

    @Headers(
        "Content-Type: application/json",
    )
    @POST("/api/v1/accounts")
    fun createAccount(@Body createAccountDto: CreateAccountDto): Call<LoginResponse>


    @Headers(
        "Content-Type: application/json",
    )
    @POST("/api/v1/users/{user_id}")
    fun updateProfile(
        @Header("X-User-Id") xUserId:Int,
        @Header("Authorization") apiToken:String,
        @Path("user_id") userId: Int,
        @Body updateProfileDto: UpdateProfileDto
    ): Call<UpdateProfileDto>


    @Headers(
        "Content-Type: application/json",
        "Accept: application/json"
    )
    @POST("/api/v1/passwords/change")
    fun changePassword(
        @Header("X-User-Id") xUserId:Int,
        @Header("Authorization") apiToken:String,
        @Body changePasswordDto: ChangePasswordDto
    ):Call<ChangePasswordResponse>
}
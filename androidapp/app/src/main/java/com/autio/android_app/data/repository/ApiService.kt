package com.autio.android_app.data.repository

import android.util.Log
import com.autio.android_app.core.RetrofitHelper
import com.autio.android_app.data.model.account.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApiService {

    private val retrofit = RetrofitHelper.buildService(ApiClient::class.java)

    fun login(loginDto: LoginDto, onResult: (LoginResponse?) -> Unit) {
        retrofit.login(loginDto).enqueue(
            object : Callback<LoginResponse> {
                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    onResult(null)
                    Log.i("SIGN IN:", "-------------------Fail Response-----------------")
                }

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    Log.i("SIGN IN:", "-------------------Success on call-----------------")
                    if (!response.isSuccessful) {
                        Log.e("SIGN IN:", response.errorBody()?.string() ?: "Unknown error")
                        if (response.errorBody()?.contentType()?.subtype()
                                .equals("application/json")
                        ) {
                            Log.i("SIGN IN:", "-------------Error message----------------")
                        }
                    }
                    val userInfo = response.body()
                    Log.i("SIGN IN:", userInfo.toString())
                    onResult(userInfo)
                }
            }
        )
    }

    fun guest(onResult: (GuestResponse?) -> Unit) {
        retrofit.guest().enqueue(
            object : Callback<GuestResponse> {
                override fun onResponse(
                    call: Call<GuestResponse>,
                    response: Response<GuestResponse>
                ) {
                    Log.i("SIGN IN:", "-------------------Success on call-----------------")
                    if (!response.isSuccessful) {
                        if (response.errorBody()?.contentType()?.subtype()
                                .equals("application/json")
                        ) {
                            Log.i("SIGN IN:", "-------------Error message----------------")
                        }
                    }
                    val guestInfo = response.body()
                    Log.i("SIGN IN:", guestInfo.toString())
                    onResult(guestInfo)
                }

                override fun onFailure(call: Call<GuestResponse>, t: Throwable) {
                    onResult(null)
                    Log.i("SIGN IN:", "-------------------Fail Response-----------------")
                }

            }
        )
    }

    fun createAccount(createAccountDto: CreateAccountDto, onResult: (LoginResponse?) -> Unit) {
        retrofit.createAccount(createAccountDto).enqueue(
            object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    Log.i("CREATE ACCOUNT:", "-------------------SUCCESS Response-----------------")
                    if (!response.isSuccessful) {
                        if (response.errorBody()?.contentType()?.subtype()
                                .equals("application/json")
                        ) {
                            Log.i("CREATE ACCOUNT:", "-------------Error message----------------")
                        }
                        val userInfo = response.body()
                        Log.i("CREATE ACCOUNT:", userInfo.toString())
                        onResult(userInfo)
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    onResult(null)
                    Log.i("CREATE ACCOUNT:", "-------------------Fail Response-----------------")
                }

            }
        )
    }

    fun updateProfile(
        xUserId: Int,
        apiToken: String,
        userId: Int,
        updateProfileDto: UpdateProfileDto,
        onResult: (UpdateProfileDto?) -> Unit
    ) {
        retrofit.updateProfile(xUserId, apiToken, userId, updateProfileDto).enqueue(
            object : Callback<UpdateProfileDto> {
                override fun onResponse(
                    call: Call<UpdateProfileDto>,
                    response: Response<UpdateProfileDto>
                ) {
                    Log.i("UPDATE PROFILE:", "---------------Update Response--------------")
                    if (!response.isSuccessful) {
                        if (response.errorBody()?.contentType()?.subtype()
                                .equals("application/json")
                        ) {
                            Log.i("UPDATE PROFILE:", "------------Error message-----------")
                        }
                        val userInfo = response.body()
                        Log.i("UPDATE PROFILE:", userInfo.toString())
                        onResult(userInfo)
                    }
                }

                override fun onFailure(call: Call<UpdateProfileDto>, t: Throwable) {
                    onResult(null)
                    Log.i("UPDATE PROFILE:", "-------------------Fail Response-----------------")
                }

            }
        )
    }

    fun changePassword(
        xUserId: Int,
        apiToken: String,
        changePasswordDto: ChangePasswordDto,
        onResult: (ChangePasswordResponse?) -> Unit
    ) {
        retrofit.changePassword(xUserId, apiToken, changePasswordDto).enqueue(
            object : Callback<ChangePasswordResponse> {
                override fun onResponse(
                    call: Call<ChangePasswordResponse>,
                    response: Response<ChangePasswordResponse>
                ) {
                    Log.i("NEW PASSWORD:", "-------------------Success on call-----------------")
                    if (!response.isSuccessful) {
                        if (response.errorBody()?.contentType()?.subtype()
                                .equals("application/json")
                        ) {
                            Log.i("NEW PASSWORD:", "-------------Error message----------------")
                        }
                    }
                }

                override fun onFailure(call: Call<ChangePasswordResponse>, t: Throwable) {
                    onResult(null)
                    Log.i("NEW PASSWORD:", "-------------------Fail Response-----------------")
                }

            }
        )
    }
}
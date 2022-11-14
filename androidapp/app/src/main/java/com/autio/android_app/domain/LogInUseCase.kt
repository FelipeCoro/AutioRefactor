package com.autio.android_app.domain

import android.util.Log
import com.autio.android_app.data.model.account.LoginDto
import com.autio.android_app.data.model.account.LoginResponse
import com.autio.android_app.data.repository.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LogInUseCase {

    private val api =
        ApiService()


    fun login(
        loginDto: LoginDto,
        onResult: (LoginResponse?) -> Unit
    ) {
        api.login(
            loginDto
        ) {
            object :
                Callback<LoginResponse> {
                override fun onFailure(
                    call: Call<LoginResponse>,
                    t: Throwable
                ) {
                    onResult(
                        null
                    )
                    Log.i(
                        "SIGN IN:",
                        "-------------------Fail Response-----------------"
                    )
                }

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    Log.i(
                        "SIGN IN:",
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
                                "SIGN IN:",
                                "-------------Error message----------------"
                            )
                        }
                    }
                    val userInfo =
                        response.body()
                    Log.i(
                        "SIGN IN:",
                        userInfo.toString()
                    )
                    onResult(
                        userInfo
                    )
                }
            }
        }
    }
}
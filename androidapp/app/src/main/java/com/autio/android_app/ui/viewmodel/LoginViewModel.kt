package com.autio.android_app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autio.android_app.data.model.account.LoginDto
import com.autio.android_app.data.model.account.LoginResponse
import com.autio.android_app.domain.LogInUseCase
import kotlinx.coroutines.launch

class LoginViewModel :
    ViewModel() {

    private val loginModel =
        MutableLiveData<LoginResponse>()
    private val logInUseCase =
        LogInUseCase()

    fun login(
        loginDto: LoginDto
    ) {
        viewModelScope.launch {
            logInUseCase.login(
                loginDto
            ) {
                if (it != null) {
                    loginModel.postValue(
                        it
                    )
                } else {
                    Log.i(
                        "SIGN IN:",
                        "error view Model"
                    )
                }
            }
        }
    }
}
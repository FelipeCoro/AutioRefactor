package com.autio.android_app.ui.view.usecases.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.autio.android_app.data.model.account.CreateAccountDto
import com.autio.android_app.data.model.account.LoginResponse
import com.autio.android_app.data.repository.ApiService
import com.autio.android_app.data.repository.PrefRepository
import com.autio.android_app.databinding.ActivitySignUpBinding
import com.autio.android_app.ui.view.usecases.home.BottomNavigation
import com.autio.android_app.util.Utils

class SignUpActivity :
    AppCompatActivity() {

    private val apiService =
        ApiService()
    private lateinit var binding: ActivitySignUpBinding
    private val prefRepository by lazy {
        PrefRepository(
            this
        )
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )
        binding =
            ActivitySignUpBinding.inflate(
                layoutInflater
            )
        setContentView(
            binding.root
        )
        setListeners()
    }

    private fun setListeners() {
        binding.btnSignup.setOnClickListener {
            createUser()
        }
        binding.btnCancel.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun createUser() {
        if (Utils.checkEmptyField(
                binding.tvName
            ) ||
            Utils.checkEmptyField(
                binding.tvEmail
            ) ||
            Utils.checkEmptyField(
                binding.tvPassword
            )
        ) {
            Utils.pleaseFillText(
                this
            )
        } else {
            val name =
                "${binding.tvName.text}"
            val password =
                "${binding.tvPassword.text}"
            val email =
                "${binding.tvEmail.text}"
            val createAccountDto =
                CreateAccountDto(
                    email,
                    email,
                    password,
                    password,
                    name
                )
            apiService.createAccount(
                createAccountDto
            ) {
                Log.i(
                    "CREATE ACCOUNT:",
                    "---------Im here----------"
                )
                if (it != null) {
                    saveUserInfo(
                        it
                    )
                    startActivity(
                        Intent(
                            this,
                            BottomNavigation::class.java
                        )
                    )
                    finish()
                } else {
                    Utils.showError(
                        this
                    )
                }
            }
        }
    }

    private fun saveUserInfo(
        loginResponse: LoginResponse
    ) {
        prefRepository.setIsUserGuest(
            false
        )
        prefRepository.setUserId(
            loginResponse.id!!
        )
        prefRepository.setUserApiToken(
            loginResponse.apiToken!!
        )
        prefRepository.setUserName(
            loginResponse.name!!
        )
        prefRepository.setUserEmail(
            loginResponse.email!!
        )
    }
}
package com.autio.android_app.ui.view.usecases.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.autio.android_app.data.model.account.GuestResponse
import com.autio.android_app.data.model.account.LoginDto
import com.autio.android_app.data.model.account.LoginResponse
import com.autio.android_app.data.repository.ApiService
import com.autio.android_app.data.repository.PrefRepository
import com.autio.android_app.databinding.ActivitySignInBinding
import com.autio.android_app.ui.view.usecases.home.BottomNavigation
import com.autio.android_app.util.checkEmptyField
import com.autio.android_app.util.pleaseFillText
import com.autio.android_app.util.showError

class SignInActivity :
    AppCompatActivity() {
    private val prefRepository by lazy {
        PrefRepository(
            this
        )
    }

    private lateinit var binding: ActivitySignInBinding
    private val apiService =
        ApiService()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )
        binding =
            ActivitySignInBinding.inflate(
                layoutInflater
            )
        setContentView(
            binding.root
        )
        setListeners()
    }

    private fun setListeners() {
        binding.btnSignIn.setOnClickListener {
            loginUser()
        }
        binding.btnGuestMode.setOnClickListener {
            loginGuest()
        }
        binding.btnCancel.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showLoadingView() {
        binding.flLoading.root.visibility =
            View.VISIBLE
    }

    private fun hideLoadingView() {
        binding.flLoading.root.visibility =
            View.GONE
    }

    private fun loginUser() {
        if (checkEmptyField(
                binding.editTextEmail
            ) || checkEmptyField(
                binding.editTextPassword
            )
        ) {
            pleaseFillText(
                this
            )
        } else {
            showLoadingView()
            val email =
                binding.editTextEmail.text.toString()
            val password =
                binding.editTextPassword.text.toString()
            val loginRequest =
                LoginDto(
                    email,
                    password
                )
            apiService.login(
                loginRequest
            ) {
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
                    hideLoadingView()
                    showError(
                        this,
                        "The user and/or password are incorrect"
                    )
                }
            }
        }
    }

    private fun loginGuest() {
        showLoadingView()
        apiService.guest {
            if (it != null) {
                saveGuestInfo(
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
                hideLoadingView()
                showError(
                    this
                )
            }
        }
    }

    private fun saveGuestInfo(
        guestResponse: GuestResponse
    ) {
        prefRepository.isUserGuest =
            true
        prefRepository.userId =
            guestResponse.id
        prefRepository.firebaseKey =
            guestResponse.firebaseKey
        prefRepository.userApiToken =
            guestResponse.apiToken
        prefRepository.remainingStories = 5
    }

    private fun saveUserInfo(
        loginResponse: LoginResponse
    ) {
        prefRepository.isUserGuest =
            false
        prefRepository.userId =
            loginResponse.id!!
        prefRepository.userApiToken =
            loginResponse.apiToken!!
        prefRepository.userName =
            loginResponse.name!!
        prefRepository.userEmail =
            loginResponse.email!!
        prefRepository.firebaseKey =
            loginResponse.firebaseKey!!
        prefRepository.remainingStories = -1
    }

}
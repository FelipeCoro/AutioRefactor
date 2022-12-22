package com.autio.android_app.ui.view.usecases.login

import android.content.Intent
import android.os.Bundle
import android.view.View
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

    private fun showLoadingView() {
        binding.flLoading.root.visibility = View.VISIBLE
    }

    private fun hideLoadingView() {
        binding.flLoading.root.visibility = View.GONE
    }

    private fun createUser() {
        if (Utils.checkEmptyFormFields(
                arrayOf(
                    binding.tvName,
                    binding.tvEmail,
                    binding.tvPassword
                )
            )
        ) {
            Utils.pleaseFillText(
                this
            )
        } else {
            showLoadingView()
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
                    Utils.showError(
                        this,
                        "The mail is already associated to another account"
                    )
                }
            }
        }
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
    }
}
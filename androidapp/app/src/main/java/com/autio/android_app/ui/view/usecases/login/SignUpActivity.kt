package com.autio.android_app.ui.view.usecases.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.autio.android_app.data.repository.ApiService
import com.autio.android_app.data.repository.legacy.PrefRepository
import com.autio.android_app.databinding.ActivitySignUpBinding
import com.autio.android_app.ui.view.usecases.home.BottomNavigation
import com.autio.android_app.ui.viewmodel.PurchaseViewModel
import com.autio.android_app.util.InjectorUtils
import com.autio.android_app.util.checkEmptyFormFields
import com.autio.android_app.util.pleaseFillText
import com.autio.android_app.util.showError

class SignUpActivity :
    AppCompatActivity() {
    private val prefRepository by lazy {
        PrefRepository(
            this
        )
    }

    private val purchaseViewModel by viewModels<PurchaseViewModel> {
        InjectorUtils.providePurchaseViewModel(
            this
        )
    }

    private lateinit var binding: ActivitySignUpBinding

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
        binding.flLoading.root.visibility =
            View.VISIBLE
    }

    private fun hideLoadingView() {
        binding.flLoading.root.visibility =
            View.GONE
    }

    private fun createUser() {
        if (checkEmptyFormFields(
                arrayOf(
                    binding.tvName,
                    binding.tvEmail,
                    binding.tvPassword
                )
            )
        ) {
            pleaseFillText(
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
                com.autio.android_app.data.api.model.account.CreateAccountDto(
                    email,
                    email,
                    password,
                    password,
                    name
                )
            ApiService.createAccount(
                createAccountDto
            ) {
                if (it != null) {
                    saveUserInfo(
                        it
                    )
                    purchaseViewModel.login(
                        "${it.id}"
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
                        "The mail is already associated to another account"
                    )
                }
            }
        }
    }

    private fun saveUserInfo(
        loginResponse: com.autio.android_app.data.api.model.account.LoginResponse
    ) {
        prefRepository.isUserGuest =
            false
        prefRepository.userId =
            loginResponse.id!!
        prefRepository.firebaseKey =
            loginResponse.firebaseKey!!
        prefRepository.userApiToken =
            loginResponse.apiToken!!
        prefRepository.userName =
            loginResponse.name!!
        prefRepository.userEmail =
            loginResponse.email!!
    }
}

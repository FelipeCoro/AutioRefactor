package com.autio.android_app.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.autio.android_app.data.repository.ApiService
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.ActivitySignInBinding
import com.autio.android_app.ui.viewmodel.PurchaseViewModel
import com.autio.android_app.util.InjectorUtils
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

    private val purchaseViewModel by viewModels<PurchaseViewModel> {
        InjectorUtils.providePurchaseViewModel(
            this
        )
    }

    private lateinit var binding: ActivitySignInBinding

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
                com.autio.android_app.data.api.model.account.LoginDto(
                    email,
                    password
                )
            ApiService.login(
                loginRequest
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
                            com.autio.android_app.ui.stories.BottomNavigation::class.java
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
        ApiService.loginAsGuest {
            if (it != null) {
                saveGuestInfo(
                    it
                )
                purchaseViewModel.login(
                    "${it.id}"
                )
                startActivity(
                    Intent(
                        this,
                        com.autio.android_app.ui.stories.BottomNavigation::class.java
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

    /**
     * Sets guest's data in the shared preferences
     */
    private fun saveGuestInfo(
        guestResponse: com.autio.android_app.data.api.model.account.GuestResponse
    ) {
        prefRepository.isUserGuest =
            true
        prefRepository.userId =
            guestResponse.id
        prefRepository.firebaseKey =
            guestResponse.firebaseKey
        prefRepository.userApiToken =
            guestResponse.apiToken
        prefRepository.remainingStories =
            5
    }

    /**
     * Saves user's data in the shared preferences
     */
    private fun saveUserInfo(
        loginResponse: com.autio.android_app.data.api.model.account.LoginResponse
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
        prefRepository.remainingStories =
            -1
    }
}

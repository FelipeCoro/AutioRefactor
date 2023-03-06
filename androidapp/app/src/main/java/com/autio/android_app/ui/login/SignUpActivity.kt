package com.autio.android_app.ui.login

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.autio.android_app.data.api.ApiClient
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.ActivitySignUpBinding
import com.autio.android_app.ui.viewmodel.PurchaseViewModel
import com.autio.android_app.util.checkEmptyFormFields
import com.autio.android_app.util.pleaseFillText
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    @Inject
    lateinit var prefRepository: PrefRepository

    //TODO(Move service calls)
    @Inject
    lateinit var apiClient: ApiClient

    private val purchaseViewModel: PurchaseViewModel by viewModels()
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )
        binding = ActivitySignUpBinding.inflate(
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
        if (checkEmptyFormFields(
                arrayOf(
                    binding.tvName, binding.tvEmail, binding.tvPassword
                )
            )
        ) {
            pleaseFillText(
                this
            )
        } else {
            showLoadingView()
            val name = "${binding.tvName.text}"
            val password = "${binding.tvPassword.text}"
            val email = "${binding.tvEmail.text}"
            val createAccountDto = com.autio.android_app.data.api.model.account.CreateAccountDto(
                email, email, password, password, name
            )

            //TODO(Had to comment out to run)
            /*
            apiClient.createAccount(
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
                            com.autio.android_app.ui.stories.BottomNavigation::class.java
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
            }*/
        }
    }

    private fun saveUserInfo(
        loginResponse: com.autio.android_app.data.api.model.account.LoginResponse
    ) {
        prefRepository.isUserGuest = false
        prefRepository.userId = loginResponse.id!!
        prefRepository.firebaseKey = loginResponse.firebaseKey!!
        prefRepository.userApiToken = loginResponse.apiToken!!
        prefRepository.userName = loginResponse.name!!
        prefRepository.userEmail = loginResponse.email!!
    }
}

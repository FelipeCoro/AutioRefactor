package com.autio.android_app.ui.login.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.autio.android_app.R
import com.autio.android_app.databinding.FragmentSignInBinding
import com.autio.android_app.ui.login.viewmodels.LoginViewModel
import com.autio.android_app.ui.login.viewstates.LoginViewState
import com.autio.android_app.ui.stories.models.LoginRequest
import com.autio.android_app.ui.stories.models.User
import com.autio.android_app.ui.subscribe.view_model.PurchaseViewModel
import com.autio.android_app.ui.subscribe.view_states.PurchaseViewState
import com.autio.android_app.util.checkEmptyField
import com.autio.android_app.util.pleaseFillText
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.log

@AndroidEntryPoint
class SignInFragment : Fragment() {

    private val purchaseViewModel: PurchaseViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var binding: FragmentSignInBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.fragment_sign_in, container, false
        )
        binding.viewModel = loginViewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setListeners()
        bindObservables()
    }

    private fun bindObservables() {
        purchaseViewModel.viewState.observe(viewLifecycleOwner, ::handlePurchaseViewState)
        loginViewModel.viewState.observe(viewLifecycleOwner, ::handleLoginViewState)
    }

    private fun setListeners() {
        binding.btnSignIn.setOnClickListener { signInUser() }
        binding.btnGuestMode.setOnClickListener { loginViewModel.loginGuest() }
        binding.btnForgotPassword.setOnClickListener {passwordReset()}
        binding.btnCancel.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_loginFragment)
        }
    }

    private fun signInUser() {
        if (checkEmptyField(binding.editTextEmail) || checkEmptyField(binding.editTextPassword)) {
            context?.let { pleaseFillText(it) }
        } else {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            val loginRequest = LoginRequest(email, password)

            purchaseViewModel.login(loginRequest)
        }
    }

    private fun handlePurchaseViewState(viewState: PurchaseViewState?) {
        when (viewState) {
            is PurchaseViewState.OnLoginSuccess -> handleOnLoginSuccess(viewState.data)
            is PurchaseViewState.OnLoginFailed -> handleOnLoginFailed()
            else -> showError()
        }
    }

    private fun handleLoginViewState(viewState: LoginViewState?) {
        when (viewState) {
            is LoginViewState.GuestLoginSuccess -> handleOnLoginSuccess(viewState.data)
            else -> handleOnLoginFailed()
        }
    }

    private fun handleOnLoginFailed() {
        //TODO(handle login Failed)
        val errorMessage = getString(R.string.sign_in_error_text)
    }

    private fun handleOnLoginSuccess(user: User) {
        findNavController().navigate(R.id.action_signInFragment_to_bottomNavigation)
    }

    private fun showError() {
        //TODO (Handle Error)

    }

    private fun passwordReset() {
        val browse = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.passwordResetLink)))
        startActivity(browse)
    }
}



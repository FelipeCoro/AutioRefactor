package com.autio.android_app.ui.login.fragments

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
import com.autio.android_app.ui.stories.models.LoginRequest
import com.autio.android_app.ui.viewmodel.PurchaseViewModel
import com.autio.android_app.ui.viewmodel.PurchaseViewState
import com.autio.android_app.util.checkEmptyField
import com.autio.android_app.util.pleaseFillText
import dagger.hilt.android.AndroidEntryPoint

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setListeners()
        bindObservables()
    }

    private fun bindObservables() {
        purchaseViewModel.viewState.observe(viewLifecycleOwner, ::handleViewState)
    }

    private fun setListeners() {
        binding.btnSignIn.setOnClickListener { loginUser() }
        binding.btnGuestMode.setOnClickListener { loginViewModel.loginGuest() }
        binding.btnCancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showLoadingView() {
        binding.flLoading.root.visibility = View.VISIBLE
    }

    private fun hideLoadingView() {
        binding.flLoading.root.visibility = View.GONE
    }

    private fun loginUser() {
        if (checkEmptyField(binding.editTextEmail) || checkEmptyField(binding.editTextPassword)
        ) {
            context?.let { pleaseFillText(it) }
        } else {
            showLoadingView()
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            val loginRequest = LoginRequest(email, password)

            purchaseViewModel.login(loginRequest)
        }
    }

    private fun handleViewState(viewState: PurchaseViewState?) {
        when (viewState) {
            is PurchaseViewState.ErrorViewState -> showError()
            else -> showSuccess(viewState)
        }

    }

    private fun showSuccess(user: PurchaseViewState?) {
        findNavController().navigate(R.id.action_signInFragment_to_bottomNavigation)
    }

    private fun showError() {
        //TODO (Handle Error)
        hideLoadingView()
        val savedMessage = "The user and/or password are incorrect"
    }
}



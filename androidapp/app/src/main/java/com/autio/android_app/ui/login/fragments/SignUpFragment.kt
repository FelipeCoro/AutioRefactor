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
import com.autio.android_app.databinding.FragmentSignUpBinding
import com.autio.android_app.ui.login.viewmodels.LoginViewModel
import com.autio.android_app.ui.stories.models.AccountRequest
import com.autio.android_app.ui.viewmodel.PurchaseViewModel
import com.autio.android_app.ui.viewmodel.PurchaseViewState
import com.autio.android_app.util.checkEmptyFormFields
import com.autio.android_app.util.pleaseFillText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private val purchaseViewModel: PurchaseViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var binding: FragmentSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater, R.layout.fragment_sign_up, container, false
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
        binding.btnSignup.setOnClickListener {
            createUser()
        }
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

    private fun createUser() {
        if (checkEmptyFormFields(
                arrayOf(
                    binding.tvName, binding.tvEmail, binding.tvPassword
                )
            )
        ) {
            context?.let {
                pleaseFillText(it)
            }
        } else {
            showLoadingView()
            val name = "${binding.tvName.text}"
            val password = "${binding.tvPassword.text}"
            val email = "${binding.tvEmail.text}"
            val createAccountRequest = AccountRequest(
                email, email, password, password, name
            )

            purchaseViewModel.createAccount(createAccountRequest)
        }
    }

    private fun handleViewState(viewState: PurchaseViewState?) {
        when (viewState) {
            is PurchaseViewState.ErrorViewState -> showError(viewState.exception)
            else -> showSuccess(viewState)
        }

    }

    private fun showSuccess(user: PurchaseViewState?) {
        findNavController().navigate(R.id.action_signUpFragment_to_bottomNavigation)
    }

    private fun showError(exception: Exception) {
        //TODO (Handle Error)
        hideLoadingView()
        val savedMessage = "The mail is already associated to another account"


    }
}

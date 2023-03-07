package com.autio.android_app.ui.login.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.Datasource
import com.autio.android_app.databinding.FragmentLoginBinding
import com.autio.android_app.extensions.setAutomaticScroll
import com.autio.android_app.ui.login.viewmodels.LoginViewModel
import com.autio.android_app.ui.login.viewmodels.LoginViewState
import com.autio.android_app.ui.stories.adapter.ImageAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var binding: FragmentLoginBinding
    private lateinit var firstRecyclerView: RecyclerView
    private lateinit var secondRecyclerView: RecyclerView
    private lateinit var thirdRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.fragment_login,
            container,
            false
        )
        setUpBackgroundAnimation()
        setListeners()
        bindObservables()


        return binding.root
    }

    private fun bindObservables() {
        loginViewModel.viewState.observe(viewLifecycleOwner, ::handleViewState)
    }

    private fun setUpBackgroundAnimation() {
        val imageDataset = Datasource().loadLocationViews()
        firstRecyclerView = binding.rvFirstColumn
        secondRecyclerView = binding.rvSecondColumn
        thirdRecyclerView = binding.rvThirdColumn

        firstRecyclerView.adapter = ImageAdapter(imageDataset.shuffled())
        firstRecyclerView.layoutManager!!.scrollToPosition(Integer.MAX_VALUE / 2)

        secondRecyclerView.adapter = ImageAdapter(imageDataset.shuffled())
        secondRecyclerView.layoutManager!!.scrollToPosition(Integer.MAX_VALUE / 2)

        thirdRecyclerView.adapter = ImageAdapter(imageDataset.shuffled())
        thirdRecyclerView.layoutManager!!.scrollToPosition(Integer.MAX_VALUE / 2)

        firstRecyclerView.setAutomaticScroll()
        secondRecyclerView.setAutomaticScroll(ScrollView.FOCUS_UP)
        thirdRecyclerView.setAutomaticScroll()
    }

    private fun setListeners() {
        binding.btnSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signInFragment)
        }
        binding.btnSignup.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }
        binding.btnLoginAsGuest.setOnClickListener {
            showLoadingView()
            loginViewModel.loginGuest()
        }
    }

    private fun showLoadingView() {
        binding.flLoading.root.visibility = View.VISIBLE
    }

    private fun hideLoadingView() {
        binding.flLoading.root.visibility = View.GONE
    }


    private fun handleViewState(viewState: LoginViewState?) {
        when (viewState) {
            is LoginViewState.ErrorViewState -> showError(viewState.exception)
            else -> showSuccess(viewState)
        }

    }

    private fun showSuccess(user: LoginViewState?) {
        findNavController().navigate(R.id.action_loginFragment_to_bottomNavigation)
    }


    private fun showError(exception: Exception) {
        //TODO (Handle Error)
        hideLoadingView()
    }
}
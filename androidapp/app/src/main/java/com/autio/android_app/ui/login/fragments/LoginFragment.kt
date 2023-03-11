package com.autio.android_app.ui.login.fragments

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

    private lateinit var binding: FragmentLoginBinding

    private val loginViewModel: LoginViewModel by viewModels()
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setListeners()
        bindObservables()
        initView()
    }

    private fun initView() {
        setUpBackgroundAnimation()
    }

    private fun bindObservables() {
        loginViewModel.viewState.observe(viewLifecycleOwner, ::handleViewState)
    }

    private fun setUpBackgroundAnimation() {
        val imageDataset = Datasource().loadLocationViews()
        val scrollingPosition = Integer.MAX_VALUE / 2
        firstRecyclerView = binding.rvFirstColumn
        secondRecyclerView = binding.rvSecondColumn
        thirdRecyclerView = binding.rvThirdColumn

        firstRecyclerView.adapter = ImageAdapter(imageDataset.shuffled())
        firstRecyclerView.layoutManager?.scrollToPosition(scrollingPosition)

        secondRecyclerView.adapter = ImageAdapter(imageDataset.shuffled())
        secondRecyclerView.layoutManager?.scrollToPosition(scrollingPosition)

        thirdRecyclerView.adapter = ImageAdapter(imageDataset.shuffled())
        thirdRecyclerView.layoutManager?.scrollToPosition(scrollingPosition)

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
            loginViewModel.loginGuest()
        }
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
    }
}

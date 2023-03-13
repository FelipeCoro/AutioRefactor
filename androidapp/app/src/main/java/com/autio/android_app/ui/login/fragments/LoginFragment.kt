package com.autio.android_app.ui.login.fragments

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.Datasource
import com.autio.android_app.databinding.FragmentLoginBinding
import com.autio.android_app.extensions.setAutomaticScroll
import com.autio.android_app.ui.login.viewmodels.LoginViewModel
import com.autio.android_app.ui.login.viewstates.LoginViewState
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
        binding.viewModel = loginViewModel
        binding.lifecycleOwner = viewLifecycleOwner
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

        val smoothScroller: RecyclerView.SmoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int = LinearSmoothScroller.SNAP_TO_START
            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                return MILLISECONDS_PER_INCH / (displayMetrics?.densityDpi ?: 0f).toFloat()
            }
        }
        val smoothScroller2: RecyclerView.SmoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int = LinearSmoothScroller.SNAP_TO_START
            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                return MILLISECONDS_PER_INCH / (displayMetrics?.densityDpi ?: 0f).toFloat()
            }
        }
        val smoothScroller3: RecyclerView.SmoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int = LinearSmoothScroller.SNAP_TO_START
            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                return MILLISECONDS_PER_INCH / (displayMetrics?.densityDpi ?: 0f).toFloat()
            }
        }

        val imageDataset = Datasource().loadLocationViews()
        val scrollingPosition = Integer.MAX_VALUE / 2
        smoothScroller.targetPosition = scrollingPosition;
        smoothScroller2.targetPosition = scrollingPosition;
        smoothScroller3.targetPosition = scrollingPosition;
        firstRecyclerView = binding.rvFirstColumn
        secondRecyclerView = binding.rvSecondColumn
        thirdRecyclerView = binding.rvThirdColumn

        thirdRecyclerView.adapter = ImageAdapter(imageDataset.shuffled())
        firstRecyclerView.adapter = ImageAdapter(imageDataset.shuffled())
        secondRecyclerView.adapter = ImageAdapter(imageDataset.shuffled())


        firstRecyclerView.layoutManager?.startSmoothScroll(smoothScroller)
        secondRecyclerView.layoutManager?.startSmoothScroll(smoothScroller2)
        thirdRecyclerView.layoutManager?.startSmoothScroll(smoothScroller3)

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
            loginViewModel.isLoading.set(true)
            loginViewModel.loginGuest()
        }
    }

    private fun handleViewState(viewState: LoginViewState?) {
        when (viewState) {
            is LoginViewState.LoginError -> showError()
            else -> showSuccess(viewState)
        }
    }

    private fun showSuccess(user: LoginViewState?) {
        findNavController().navigate(R.id.action_loginFragment_to_bottomNavigation)
    }

    private fun showError() {
        //TODO (Handle Error)
    }

    companion object {
        const val MILLISECONDS_PER_INCH = 1550f
    }

}

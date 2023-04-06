package com.autio.android_app.ui.onboarding.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.autio.android_app.R
import com.autio.android_app.databinding.FragmentWelcomeExplorerBinding
import com.autio.android_app.ui.stories.BottomNavigation
import com.autio.android_app.ui.stories.models.User
import com.autio.android_app.ui.subscribe.view_model.PurchaseViewModel
import com.autio.android_app.ui.subscribe.view_states.PurchaseViewState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WelcomeExplorerFragment : Fragment() {

    private lateinit var binding: FragmentWelcomeExplorerBinding
    private lateinit var viewPager: ViewPager2
    private val purchaseViewModel: PurchaseViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeExplorerBinding.inflate(
            inflater, container, false
        )
        if (requireActivity().findViewById<ViewPager2>(R.id.viewPager) != null) {
            viewPager = requireActivity().findViewById(R.id.viewPager)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonLetsGo.setOnClickListener {
            if (viewPager != null) {
                viewPager.currentItem += 1
            }
        }
        bindObservables()
        initView()
    }

    private fun bindObservables() {
        purchaseViewModel.viewState.observe(viewLifecycleOwner, ::handlePurchaseViewState)
    }

    private fun initView() {
        if (isOnBoardingFinished()) {
            isUserLoggedIn()
        }
        startAnimation()
    }

    private fun startAnimation() {
        binding.tvWelcome.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).duration = 2000
        }
        binding.ivDivider.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(2000).withEndAction {
                binding.tvAppDescription.apply {
                    alpha = 0f
                    visibility = View.VISIBLE
                    animate().alpha(1f).setDuration(2000).withEndAction {
                        binding.buttonLetsGo.apply {
                            alpha = 0f
                            visibility = View.VISIBLE
                            animate().alpha(1f).duration = 2000
                        }
                    }
                }
            }
        }
    }

    private fun handlePurchaseViewState(viewState: PurchaseViewState?) {
        when (viewState) {
            is PurchaseViewState.FetchedUserSuccess -> handleHandleActiveUserSuccess(viewState.data)
            is PurchaseViewState.UserNotLoggedIn ->handleNewUser()
            else -> {}
        }
    }


    private fun isOnBoardingFinished(): Boolean {
        val sharedPreferences =
            activity?.getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return sharedPreferences?.getBoolean("Finished", false) ?: false
    }

    private fun isUserLoggedIn() {
        purchaseViewModel.getUserInfo()
    }

    private fun handleNewUser(){
        if (viewPager != null) {
            viewPager.currentItem += 1
        }
    }

    private fun handleHandleActiveUserSuccess(user: User) {
        if (!user.isGuest) {
            startActivity(Intent(activity, BottomNavigation::class.java))
            activity?.finish()
        } else if (viewPager != null) {
            viewPager.currentItem += 1
        }
    }
}

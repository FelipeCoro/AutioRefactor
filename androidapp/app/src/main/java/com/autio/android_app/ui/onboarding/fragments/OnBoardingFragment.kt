package com.autio.android_app.ui.onboarding.fragments

import android.Manifest
import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.autio.android_app.R
import com.autio.android_app.databinding.FragmentViewPagerBinding
import com.autio.android_app.ui.onboarding.adapters.ViewPagerAdapter
import com.autio.android_app.ui.onboarding.view_models.OnBoardingViewModel
import com.autio.android_app.ui.onboarding.view_states.OnBoardingViewState
import com.autio.android_app.ui.onboarding.view_states.OnBoardingViewState.NavigateToAllowNotifications
import com.autio.android_app.ui.onboarding.view_states.OnBoardingViewState.NavigateToBackgroundLocation
import com.autio.android_app.ui.onboarding.view_states.OnBoardingViewState.NavigateToHome
import com.autio.android_app.ui.onboarding.view_states.OnBoardingViewState.NavigateToInAppLocationPermission
import com.autio.android_app.ui.onboarding.view_states.OnBoardingViewState.NavigateToLogin
import com.autio.android_app.ui.stories.BottomNavigation
import com.autio.android_app.util.PermissionsManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingFragment : Fragment() {

    lateinit var permissionsManager: PermissionsManager
    private lateinit var binding: FragmentViewPagerBinding
    private val viewModel: OnBoardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionsManager =
            PermissionsManager(requireActivity(), requireActivity().activityResultRegistry)
        lifecycle.addObserver(permissionsManager)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_view_pager, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initOnBoardingViewPager()
        bindObservables()
        initView()
    }

    private fun initOnBoardingViewPager() {
        val adapter = ViewPagerAdapter(
            listOf(
                WelcomeExplorerFragment(),
                NotificationsFragment(),
                InAppLocationFragment(),
                BackgroundLocationFragment()
            ), requireActivity().supportFragmentManager, lifecycle
        )
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false
    }

    private fun bindObservables() {
        viewModel.viewState.observe(viewLifecycleOwner, ::handleViewModel)
    }

    private fun handleViewModel(onBoardingViewState: OnBoardingViewState?) {
        onBoardingViewState?.let { viewState ->
            when (viewState) {
                is NavigateToAllowNotifications -> navigateToNotificationsFragment()
                is NavigateToInAppLocationPermission -> navigateToInAppLocationPermissionFragment()
                is NavigateToBackgroundLocation -> navigateToBackgroundLocation()
                is NavigateToHome -> navigateToHome()
                is NavigateToLogin -> navigateToLogin()
            }
        }
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.loginFragment)
    }

    private fun navigateToHome() {
        startActivity(Intent(activity, BottomNavigation::class.java))
        activity?.finish()
    }

    private fun initView() {
        val notifyPermission = validateNotificationsPermission()
        val inAppPermission = validateInAppLocationPermission()
        val backgroundPermission = validateBackgroundLocation()
        viewModel.initView(
            isOnBoardingFinished(), notifyPermission, inAppPermission, backgroundPermission
        )
    }

    private fun isOnBoardingFinished(): Boolean {
        val sharedPreferences = activity?.getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return sharedPreferences?.getBoolean("Finished", false) ?: false
    }

    private fun validateBackgroundLocation(): Boolean {
        return permissionsManager.checkPermission(ACCESS_BACKGROUND_LOCATION)
    }

    private fun validateInAppLocationPermission(): Boolean {
        return permissionsManager.checkPermissions(
            listOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
        )
    }

    private fun validateNotificationsPermission(): Boolean {
        return requireContext().let {
            if (android.os.Build.VERSION.SDK_INT < 33) {
                true //we don't need to ask for this versions for push notifications
            } else {
                permissionsManager.checkPermission(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun navigateToNotificationsFragment() {
        setViewPagerPage(1)
    }

    private fun navigateToInAppLocationPermissionFragment() {
        setViewPagerPage(2)
    }

    private fun navigateToBackgroundLocation() {
        setViewPagerPage(3)
    }

    private fun setViewPagerPage(pageIndex: Int) {
        binding.viewPager.currentItem = pageIndex
    }
}

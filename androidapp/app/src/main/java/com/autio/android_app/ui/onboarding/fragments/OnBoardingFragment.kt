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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navArgs
import com.autio.android_app.R
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.FragmentViewPagerBinding
import com.autio.android_app.ui.onboarding.adapters.ViewPagerAdapter
import com.autio.android_app.ui.stories.BottomNavigation
import com.autio.android_app.util.PermissionsManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnBoardingFragment : Fragment() {

    @Inject
    lateinit var prefRepository: PrefRepository
    lateinit var permissionsManager: PermissionsManager
    private lateinit var binding: FragmentViewPagerBinding
    val args: OnBoardingFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionsManager =
            PermissionsManager(requireActivity(), requireActivity().activityResultRegistry)
        lifecycle.addObserver(permissionsManager)

        if(args.goToSignUpOrIn == 0){
            findNavController().navigate(R.id.signIn)
        }
        else if (args.goToSignUpOrIn == 1){
            findNavController().navigate(R.id.signUp)
        }

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

        validateViewPagerStartingPoint()
    }

    private fun validateViewPagerStartingPoint() {
        val notifications = validateNotificationsPermission()
        val inAppPermissions = validateInAppLocationPermission()
        val backgroundLocation = validateBackgroundLocation()

        if (!notifications) {
            navigateToNotificationsFragment(inAppPermissions, backgroundLocation)
            return
        }
        if (!inAppPermissions) {
            navigateToInAppLocationPermissionFragment(backgroundLocation)
            return
        }
        if (!backgroundLocation) {
            navigateToBackgroundLocation()
            return
        }
        navigateToAuthentication()
    }

    private fun navigateToAuthentication() {
        if (isOnBoardingFinished()) {
            if (isUserLoggedIn()) {
                startActivity(Intent(activity, BottomNavigation::class.java))
                activity?.finish()
            } else findNavController().navigate(R.id.loginFragment)
        }
    }

    private fun isOnBoardingFinished(): Boolean {
        val sharedPreferences =
            activity?.getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
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
        } ?: false
    }

    private fun navigateToNotificationsFragment(
        inAppPermission: Boolean, backgroundLocation: Boolean
    ) {
        setViewPagerPage(0)
    }

    private fun navigateToInAppLocationPermissionFragment(backgroundLocation: Boolean) {
        setViewPagerPage(2)
    }

    private fun navigateToBackgroundLocation() {
        setViewPagerPage(3)
    }

    private fun setViewPagerPage(pageIndex: Int) {
        binding.viewPager.currentItem = pageIndex
    }

    private fun isUserLoggedIn() =
        prefRepository.userApiToken.isNotEmpty() //TODO(Shouldn't this be if its NOT empty?)
}

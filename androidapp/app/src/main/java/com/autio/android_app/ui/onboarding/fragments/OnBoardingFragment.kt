package com.autio.android_app.ui.onboarding.fragments

import android.Manifest
import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.autio.android_app.R
import com.autio.android_app.databinding.FragmentViewPagerBinding
import com.autio.android_app.ui.onboarding.adapters.ViewPagerAdapter
import com.autio.android_app.ui.stories.BottomNavigation
import com.autio.android_app.util.PermissionsManager

class OnBoardingFragment : Fragment() {


    lateinit var permissionsManager: PermissionsManager

    private lateinit var binding: FragmentViewPagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionsManager =
            PermissionsManager(requireActivity(), requireActivity().activityResultRegistry)
        if (::permissionsManager.isInitialized) {
            lifecycle.addObserver(permissionsManager)
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
                NotificationsFragment(), InAppLocationFragment(), BackgroundLocationFragment()
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

        navigateToBottomActivity()

    }

    private fun navigateToBottomActivity() {
        val intent = Intent(context, BottomNavigation::class.java)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
        startActivity(intent)
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
        setViewPagerPage(1)
    }

    private fun navigateToBackgroundLocation() {
        setViewPagerPage(2)
    }

    private fun setViewPagerPage(pageIndex: Int) {
        binding.viewPager.currentItem = pageIndex
    }
}

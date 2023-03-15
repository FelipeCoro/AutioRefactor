package com.autio.android_app.ui.onboarding.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.autio.android_app.R
import com.autio.android_app.databinding.FragmentInAppLocationBinding
import com.autio.android_app.util.ON_BOARDING_SHARE_PREFERENCES_FILE_NAME
import com.autio.android_app.util.SHARED_PREFERENCES_FINISHED_ON_BOARDING_FLAG
import com.autio.android_app.util.TrackingUtility

class InAppLocationFragment : Fragment() {

    private lateinit var binding: FragmentInAppLocationBinding

    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_in_app_location, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewPager = requireActivity().findViewById(R.id.viewPager)
        binding.buttonLocationPermission.setOnClickListener {
            requestPermission()
        }

        with(binding) {
            termsOfServiceLocation.setOnClickListener { openTerms() }
            privacyPolicyLocation.setOnClickListener { openPrivacy() }
            securityLocation.setOnClickListener { openSecurity() }
        }
    }

    private fun requestPermission() {
        if (TrackingUtility.hasCoreLocationPermissions(requireContext())) {
            moveToNextStep()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private fun moveToNextStep() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            goToBackgroundLocationPermission()
        } else {
            goToAuthentication()
        }
    }

    private fun goToAuthentication() {
        onBoardingFinished()
        val nav = findNavController()
        nav.navigate(R.id.action_onBoardingFragment_to_loginFragment)
        activity?.finish()
    }

    private fun goToBackgroundLocationPermission() {
        viewPager.currentItem += 1
    }

    private fun onBoardingFinished() {
        val sharedPreferences =
            requireActivity().getSharedPreferences(
                ON_BOARDING_SHARE_PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE
            )
        sharedPreferences.edit {
            putBoolean(SHARED_PREFERENCES_FINISHED_ON_BOARDING_FLAG, true)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsResults ->
            val allGranted = permissionsResults.all { it.value }
            if (allGranted) {
                moveToNextStep()
            } else {
                showWarningDialog()
            }
        }

    private fun showWarningDialog() {
        AlertDialog.Builder(requireActivity()).apply {
            setMessage(getString(R.string.in_app_location_warning_dialog_permissions_text))
            setPositiveButton(
                getString(R.string.subscribe_fragment_positive_button_dialog)
            ) { _, _ -> moveToNextStep() }
            setNegativeButton(
                getString(R.string.subscribe_fragment_request_again_negative_button_dialog)
            ) { _, _ -> requestPermission() }
            create()
        }.show()
    }
    private fun openTerms() {
        val browse = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.termsOfServiceLink)))
        startActivity(browse)
    }

    private fun openPrivacy() {
        val browse = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_Link)))
        startActivity(browse)
    }

    private fun openSecurity() {
        val browse = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.securityLink)))
        startActivity(browse)
    }
}

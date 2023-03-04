package com.autio.android_app.ui.onboarding.screens

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.autio.android_app.R
import com.autio.android_app.databinding.FragmentInAppLocationBinding
import com.autio.android_app.ui.view.usecases.subscribe.SubscribeActivity
import com.autio.android_app.util.TrackingUtility

class InAppLocationFragment :
    Fragment() {

    private var _binding: FragmentInAppLocationBinding? =
        null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentInAppLocationBinding.inflate(
                inflater,
                container,
                false
            )

        viewPager =
            requireActivity().findViewById(
                R.id.viewPager
            )

        binding.buttonLocationPermission.setOnClickListener {
            requestPermission()
        }

        return binding.root
    }

    private fun requestPermission() {
        if (TrackingUtility.hasCoreLocationPermissions(
                requireContext()
            )
        ) {
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
            goToSubscribeActivity()
        }
    }

    private fun goToBackgroundLocationPermission() {
        viewPager.currentItem =
            2
    }

    private fun goToSubscribeActivity() {
        val subscribeIntent =
            Intent(
                activity,
                SubscribeActivity::class.java
            )
        startActivity(
            subscribeIntent
        )
        onBoardingFinished()
        activity?.finish()
    }

    private fun onBoardingFinished() {
        val sharedPreferences =
            requireActivity().getSharedPreferences(
                "onBoarding",
                Context.MODE_PRIVATE
            )
        val editor =
            sharedPreferences.edit()
        editor.putBoolean(
            "Finished",
            true
        )
        editor.apply()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsResults ->
            val allGranted =
                permissionsResults.all { it.value }
            if (allGranted) {
                moveToNextStep()
            } else {
                showWarningDialog()
            }
        }

    private fun showWarningDialog() {
        AlertDialog.Builder(
            requireActivity()
        )
            .apply {
                setMessage(
                    "For a full experience of the app, accepting the " +
                            "requested permissions are necessary. Are you sure want to continue?" +
                            " You can change this later in your Settings."
                )
                setPositiveButton(
                    "Continue"
                ) { _, _ ->
                    moveToNextStep()
                }
                setNegativeButton(
                    "Request again"
                ) { _, _ -> requestPermission() }
                create()
            }
            .show()
    }
}
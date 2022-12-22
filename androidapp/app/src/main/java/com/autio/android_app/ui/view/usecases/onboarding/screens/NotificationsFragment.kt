package com.autio.android_app.ui.view.usecases.onboarding.screens

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.autio.android_app.R
import com.autio.android_app.databinding.FragmentNotificationsBinding
import com.autio.android_app.util.TrackingUtility

class NotificationsFragment :
    Fragment() {

    private var _binding: FragmentNotificationsBinding? =
        null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentNotificationsBinding.inflate(
                inflater,
                container,
                false
            )

        viewPager =
            requireActivity().findViewById(
                R.id.viewPager
            )

        binding.buttonNotificationPermission.setOnClickListener {
            requestPermission()
        }
        return binding.root
    }

    private fun goToLocationPermission() {
        viewPager.currentItem =
            1
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT < 33 || TrackingUtility.hasNotificationPermissions(
                requireContext()
            )
        ) {
            goToLocationPermission()
        } else {
            requestPermissionLauncher.launch(
                POST_NOTIFICATIONS
            )
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { permissionResult ->
            if (permissionResult) {
                goToLocationPermission()
            } else {
                showWarningDialog()
            }
        }

    private fun showWarningDialog(
    ) {
        AlertDialog.Builder(
            requireActivity()
        )
            .setMessage(
                "For a full experience of the app, accepting the " +
                        "requested permissions are necessary. Are you sure want to continue?" +
                        " You can change this later in your Settings."
            )
            .setPositiveButton(
                "Continue"
            ) { _, _ -> goToLocationPermission() }
            .setNegativeButton(
                "Request again"
            ) { _, _ -> requestPermission() }
            .create()
            .show()
    }
}
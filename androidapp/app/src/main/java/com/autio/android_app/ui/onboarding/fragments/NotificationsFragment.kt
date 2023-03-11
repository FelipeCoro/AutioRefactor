package com.autio.android_app.ui.onboarding.fragments

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

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(
            inflater, container, false
        )

        viewPager = requireActivity().findViewById(R.id.viewPager)

        binding.buttonNotificationPermission.setOnClickListener {
            requestPermission()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requestPermission()
    }

    private fun goToLocationPermission() {
        viewPager.currentItem = viewPager.currentItem + 1
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT < 33) {
            goToLocationPermission()
        } else {
            requestPermissionLauncher.launch(POST_NOTIFICATIONS)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
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
        ).setMessage(
            getString(R.string.notification_fragment_dialog_text)
        ).setPositiveButton(
            getString(R.string.notification_fragment_dialog_positive_button_text)
        ) { _, _ -> goToLocationPermission() }.setNegativeButton(
            getString(R.string.notification_fragment_dialog_negative_button_text)
        ) { _, _ -> requestPermission() }.create().show()
    }
}

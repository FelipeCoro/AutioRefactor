package com.autio.android_app.ui.onboarding.screens

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.autio.android_app.databinding.FragmentBackgroundLocationBinding
import com.autio.android_app.ui.subscribe.SubscribeActivity
import com.autio.android_app.util.TrackingUtility

class BackgroundLocationFragment : Fragment() {
    private var _binding: FragmentBackgroundLocationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBackgroundLocationBinding.inflate(
            inflater, container, false
        )

        binding.buttonLocationPermission.setOnClickListener {
            requestPermission()
        }
        return binding.root
    }

    private fun requestPermission() {
        if (TrackingUtility.hasBackgroundLocationPermission(requireContext())) {
            goToSubscribeActivity()
        } else {
            requestPermissionLauncher.launch(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    private fun goToSubscribeActivity() {
        val subscribeIntent = Intent(activity, SubscribeActivity::class.java)
        startActivity(subscribeIntent)
        onBoardingFinished()
        activity?.finish()
    }

    private fun onBoardingFinished() {
        val sharedPreferences = requireActivity().getSharedPreferences(
            "onBoarding", Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putBoolean("Finished", true)
        editor.apply()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permissionsResult ->
        if (permissionsResult) {
            goToSubscribeActivity()
        } else {
            showWarningDialog()
        }
    }

    private fun showWarningDialog() {
        AlertDialog.Builder(
            requireActivity()
        ).apply {
            setMessage(
                "For a full experience of the app, accepting the " + "requested permissions are necessary. Are you sure want to continue?" + " You can change this later in your Settings."
            )
            setPositiveButton("Continue") { _, _ -> goToSubscribeActivity() }
            setNegativeButton("Request again") { _, _ -> requestPermission() }
            create()
        }.show()
    }
}
package com.autio.android_app.ui.onboarding.fragments

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.autio.android_app.R
import com.autio.android_app.databinding.FragmentBackgroundLocationBinding
import com.autio.android_app.util.TrackingUtility
import com.autio.android_app.util.resources.DeepLinkingActions.Companion.LoginFragmentDeepLinkingAction
import com.autio.android_app.util.resources.getDeepLinkingNavigationRequest

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
            goToSubscribeFragment()
        } else {
            requestPermissionLauncher.launch(ACCESS_BACKGROUND_LOCATION)
        }
    }

    private fun goToSubscribeFragment() {
        onBoardingFinished()
        val action = getDeepLinkingNavigationRequest(LoginFragmentDeepLinkingAction)
        findNavController().navigate(action)
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
            goToSubscribeFragment()
        } else {
            showWarningDialog()
        }
    }

    private fun showWarningDialog() {
        AlertDialog.Builder(
            requireActivity()
        ).apply {
            setMessage(getString(R.string.background_location_fragment_main_dialog_text))
            setPositiveButton(getString(R.string.background_location_fragment_positive_button_dialog_text)) { _, _ -> goToSubscribeFragment() }
            setNegativeButton(getString(R.string.background_location_fragment_negative_button_dialog_text)) { _, _ -> requestPermission() }
            create()
        }.show()
    }
}
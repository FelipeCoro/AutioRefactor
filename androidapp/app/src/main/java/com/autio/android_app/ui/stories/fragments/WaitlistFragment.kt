package com.autio.android_app.ui.stories.fragments

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.autio.android_app.R
import com.autio.android_app.databinding.WaitlistWallBinding
import com.autio.android_app.util.PermissionsManager


class WaitlistFragment : Fragment() {


    private lateinit var binding: WaitlistWallBinding
    lateinit var permissionsManager: PermissionsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionsManager =
            PermissionsManager(requireActivity(), requireActivity().activityResultRegistry)
        lifecycle.addObserver(permissionsManager)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.waitlist_wall, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        with(binding) {
            buttonEnableNotifications.setOnClickListener { navigate() }
            termsOfService.setOnClickListener { openTerms() }
            privacyPolicy.setOnClickListener { openPrivacy() }
            security.setOnClickListener { openSecurity() }
        }
        if (validateNotificationsPermission()) {
            navigateToDone()
        }
    }


    private fun requestPermission() {
        if (Build.VERSION.SDK_INT < 33) {
            navigateToDone()
        } else {
            permissionsManager.requestPermission(
                Manifest.permission.POST_NOTIFICATIONS,
                { navigateToDone() },
                { navigateToDone() },
                { navigateToDone() }
            )
        }
    }


    private fun navigate() {
        if (validateNotificationsPermission()) {
            navigateToDone()
        } else {
            requestPermission()
        }
    }

    private fun navigateToDone() {
        findNavController().navigate(R.id.action_waitlistFragment_to_waitlistFragmentResponse)
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

    private fun validateNotificationsPermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT < 33) {
            true //we don't need to ask for this versions for push notifications
        } else {
            permissionsManager.checkPermission(Manifest.permission.POST_NOTIFICATIONS)
        }

    }
}

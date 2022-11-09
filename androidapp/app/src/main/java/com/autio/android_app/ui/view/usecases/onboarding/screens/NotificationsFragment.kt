package com.autio.android_app.ui.view.usecases.onboarding.screens

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.viewpager2.widget.ViewPager2
import com.autio.android_app.R
import com.autio.android_app.data.repository.PrefRepository
import com.autio.android_app.databinding.FragmentNotificationsBinding
import com.autio.android_app.util.Constants
import com.autio.android_app.util.TrackingUtility
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class NotificationsFragment : Fragment(), EasyPermissions.PermissionCallbacks{

    private val prefRepository by lazy { PrefRepository(requireContext()) }
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)

        binding.buttonNotificationPermission.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 33) {
                requestPermission()
            } else {
                if (viewPager != null) {
                    goToLocationPermission(viewPager)
                }
            }
        }
        return binding.root
    }

    private fun goToLocationPermission(viewPager2: ViewPager2){
        viewPager2.currentItem = 1
    }

    @RequiresApi(33)
    private fun requestPermission(){
        if(TrackingUtility.hasNotificationPermissions(requireContext())){
            return
        }
        EasyPermissions.requestPermissions(
            this,
            "Notifications Permission",
            Constants.REQUEST_CODE_NOTIFICATION_PERMISSION,
//            Manifest.permission.POST_NOTIFICATIONS,
        )
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)
        if (viewPager != null) {
            goToLocationPermission(viewPager)
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)

        if (EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            AppSettingsDialog.Builder(this).build().show()
        }else{
            if (Build.VERSION.SDK_INT >= 33) {
                requestPermission()
            } else {
                if (viewPager != null) {
                    goToLocationPermission(viewPager)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }

}
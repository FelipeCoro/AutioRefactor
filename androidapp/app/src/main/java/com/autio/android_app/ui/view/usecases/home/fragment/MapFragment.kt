package com.autio.android_app.ui.view.usecases.home.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.autio.android_app.R
import com.autio.android_app.databinding.FragmentMapBinding
import com.autio.android_app.ui.view.usecases.subscribe.SubscribeActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.tasks.CancellationTokenSource


class MapFragment :
    Fragment(),
    OnMapReadyCallback {

    private var _binding: FragmentMapBinding? =
        null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val priority =
        LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    private val cancellationTokenSource =
        CancellationTokenSource()
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentMapBinding.inflate(
                inflater,
                container,
                false
            )
        setListeners()

//        val mapFragment =
//            binding.maps as SupportMapFragment
        val mapFragment =
            childFragmentManager.findFragmentById(
                R.id.maps
            ) as SupportMapFragment
        mapFragment.getMapAsync(
            this
        )

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(
                requireContext()
            )

        return binding.root
    }


    private fun setListeners() {
        binding.relativeLayoutSeePlans.setOnClickListener {
            val subscribeIntent =
                Intent(
                    activity,
                    SubscribeActivity::class.java
                )
            startActivity(
                subscribeIntent
            )
        }
    }

    override fun onMapReady(
        googleMap: GoogleMap
    ) {
        map =
            googleMap
    }

}


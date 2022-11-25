package com.autio.android_app.ui.view.usecases.home.fragment.map

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.autio.android_app.R
import com.autio.android_app.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions


class MapFragment :
    Fragment(),
    OnMapReadyCallback {

    private var _binding: FragmentMapBinding? =
        null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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
        binding.imgMapView.setOnClickListener {
            changeDrawableFillColor(
                it,
                R.color.autio_blue
            )
            changeDrawableFillColor(
                binding.imgPlaylistView
            )
            binding.maps.visibility =
                View.VISIBLE
            binding.playlist.visibility =
                View.GONE
        }

        binding.imgPlaylistView.setOnClickListener {
            changeDrawableFillColor(
                binding.imgMapView
            )
            changeDrawableFillColor(
                it,
                R.color.autio_blue
            )
            binding.maps.visibility =
                View.GONE
            binding.playlist.visibility =
                View.VISIBLE
        }
    }

    override fun onMapReady(
        googleMap: GoogleMap
    ) {
        try {
            setGoogleLogoNewPosition()
            val success =
                googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.style_json
                    )
                )
            if (!success) Log.e(
                "MapFragment",
                "Style parsing failed"
            )
        } catch (exception: Resources.NotFoundException) {
            Log.e(
                "MapFragment",
                "Can't find style",
                exception
            )
        }
    }

    private fun setGoogleLogoNewPosition() {
        val googleLogo: View =
            binding.maps.findViewWithTag(
                "GoogleWatermark"
            )
        val glLayoutParams =
            googleLogo.layoutParams as RelativeLayout.LayoutParams
        glLayoutParams.addRule(
            RelativeLayout.ALIGN_PARENT_BOTTOM,
            0
        )
        glLayoutParams.addRule(
            RelativeLayout.ALIGN_PARENT_LEFT,
            0
        )
        glLayoutParams.addRule(
            RelativeLayout.ALIGN_PARENT_START,
            0
        )
        glLayoutParams.addRule(
            RelativeLayout.ALIGN_PARENT_TOP,
            RelativeLayout.TRUE
        )
        glLayoutParams.addRule(
            RelativeLayout.ALIGN_PARENT_END,
            RelativeLayout.TRUE
        )
        googleLogo.layoutParams =
            glLayoutParams
    }

    private fun changeDrawableFillColor(
        imageView: View,
        color: Int = R.color.autio_blue_40
    ) {
        (imageView as ImageView).setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                color
            )
        )
    }
}


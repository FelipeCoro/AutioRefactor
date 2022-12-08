package com.autio.android_app.ui.view.usecases.home.fragment.map

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.autio.android_app.R
import com.autio.android_app.data.model.story.Story
import com.autio.android_app.databinding.FragmentMapBinding
import com.autio.android_app.player.MediaBrowserAdapter
import com.autio.android_app.player.StoryLibrary
import com.autio.android_app.ui.view.usecases.home.BottomNavigation
import com.autio.android_app.ui.viewmodel.StoryViewModel
import com.autio.android_app.util.Utils.getIconFromDrawable
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

class MapFragment :
    Fragment(),
    OnMapReadyCallback {

    private var _binding: FragmentMapBinding? =
        null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var storyViewModel: StoryViewModel
    private val stories =
        arrayListOf<Story>()

    private lateinit var mediaBrowserAdapter: MediaBrowserAdapter

    private var map: GoogleMap? =
        null
    private var cameraPosition: CameraPosition? =
        null

    // The entry point to the Places API.
    private lateinit var placesClient: PlacesClient

    private var locationPermissionGranted =
        false
    private var locationBackgroundPermissionGranted =
        false

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? =
        null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )
        if (savedInstanceState != null) {
            lastKnownLocation =
                savedInstanceState.getParcelable(
                    KEY_LOCATION
                )
            cameraPosition =
                savedInstanceState.getParcelable(
                    KEY_CAMERA_POSITION
                )
        }
        Places.initialize(
            requireContext(),
            resources.getString(
                R.string.google_maps_key
            )
        )
        placesClient =
            Places.createClient(
                requireContext()
            )

        fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(
                requireActivity()
            )
    }

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

        mediaBrowserAdapter =
            (requireActivity() as BottomNavigation).mediaBrowserAdapter

        val mapFragment =
            childFragmentManager.findFragmentById(
                R.id.maps
            ) as SupportMapFragment?
        mapFragment?.getMapAsync(
            this
        )

        setListeners()

        return binding.root
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(
            outState
        )
        map?.let { map ->
            outState.putParcelable(
                KEY_CAMERA_POSITION,
                map.cameraPosition
            )
            outState.putParcelable(
                KEY_LOCATION,
                lastKnownLocation
            )
        }
    }

    private fun setListeners() {
        binding.imgMapView.setOnClickListener {
            changeDrawableFillColor(
                it,
                R.color.tab_view_icon_active
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
                it,
                R.color.tab_view_icon_active
            )
            changeDrawableFillColor(
                binding.imgMapView
            )
            binding.maps.visibility =
                View.GONE
            binding.playlist.visibility =
                View.VISIBLE
            binding.playlist.bringToFront()
        }
    }

    @RequiresApi(
        Build.VERSION_CODES.Q
    )
    override fun onMapReady(
        map: GoogleMap
    ) {
        this.map =
            map
        // Sets the light or dark mode of the map from the "map_style.json" inside
        // raw resources
        setMapLayout()

        // Prompt the user for permission
        getLocationPermission()

        // Turn on the My Location layer and the related control on the map
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()

        setGoogleLogoNewPosition()

        storyViewModel =
            ViewModelProvider(
                this
            )[StoryViewModel::class.java]
        storyViewModel.getAllStories()
            .observe(
                viewLifecycleOwner
            ) { t ->
                stories.addAll(
                    t
                )
                val pinDrawable =
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_non_listened_pin,
                        null
                    )
                val pinIcon =
                    getIconFromDrawable(
                        pinDrawable
                    )
                for (story in t) {
                    addStoryMarker(
                        story,
                        pinIcon
                    )
                }
            }

        this.map!!.setOnMarkerClickListener { marker ->
            val story =
                stories.firstOrNull { marker.title == it.title }

            if (story != null) {
                mediaBrowserAdapter.addToQueue(story)
            }

            // Return false to indicate event was not consumed
            // and to return default behaviour to occur
            false
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

    /**
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult =
                    fusedLocationClient.lastLocation
                locationResult.addOnCompleteListener(
                    requireActivity()
                ) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation =
                            task.result
                        if (lastKnownLocation != null) {
                            map?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ),
                                    DEFAULT_ZOOM.toFloat()
                                )
                            )
                            binding.cardLocationIcon.setOnClickListener {
                                map?.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            lastKnownLocation!!.latitude,
                                            lastKnownLocation!!.longitude
                                        ),
                                        DEFAULT_ZOOM.toFloat()
                                    )
                                )
                            }
                        }
                    } else {
//                        map?.moveCamera(CameraUpdateFactory
//                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        binding.cardLocationIcon.setOnClickListener {

                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(
                "Exception: %s",
                e.message,
                e
            )
        }
    }

    @RequiresApi(
        Build.VERSION_CODES.Q
    )
    private fun updateLocationUI() {
        if (map == null) return
        try {
            if (locationPermissionGranted) {
                binding.cardLocationIcon.visibility =
                    View.VISIBLE
                if (!locationBackgroundPermissionGranted) {
                    showLocationPermissionAlwaysEnabledSnackBar()
                }
            } else {
                binding.cardLocationIcon.visibility =
                    View.GONE
                lastKnownLocation =
                    null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e(
                "Exception: %s",
                e.message,
                e
            )
        }
    }

    private fun setMapLayout() {
        try {
            val success =
                map?.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.map_style
                    )
                )
            if (success != true) {
                Log.e(
                    TAG,
                    "Style parsing failed"
                )
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(
                TAG,
                "Can't find style. Error: ",
                e
            )
        }
    }

    /**
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
    @RequiresApi(
        Build.VERSION_CODES.Q
    )
    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted =
                true
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationBackgroundPermissionGranted =
                    true
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    ACCESS_FINE_LOCATION
                ),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @RequiresApi(
        Build.VERSION_CODES.Q
    )
    @Deprecated(
        "Deprecated in Java"
    )
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted =
            false
        locationBackgroundPermissionGranted =
            false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted =
                        true
                }
            }
            PERMISSIONS_REQUEST_BACKGROUND_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationBackgroundPermissionGranted =
                        true
                }
            }
            else -> super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
        }
        updateLocationUI()
    }

    @RequiresApi(
        Build.VERSION_CODES.Q
    )
    private fun showLocationPermissionAlwaysEnabledSnackBar() {
        val inflater: View =
            layoutInflater.inflate(
                R.layout.location_permission_snackbar,
                binding.mapsRoot,
                false
            )
        inflater.setOnClickListener {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    ACCESS_BACKGROUND_LOCATION
                ),
                PERMISSIONS_REQUEST_BACKGROUND_LOCATION
            )
            inflater.visibility =
                View.GONE
        }
        binding.mapsRoot.addView(
            inflater
        )
        val set =
            ConstraintSet()
        set.apply {
            clone(
                binding.mapsRoot
            )
            connect(
                inflater.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                40
            )
            applyTo(
                binding.mapsRoot
            )
        }
    }

    private fun changeDrawableFillColor(
        imageView: View,
        color: Int = R.color.tab_view_icon_inactive
    ) {
        (imageView as ImageView).setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                color
            )
        )
    }

    private fun addStoryMarker(
        story: Story,
        customIcon: BitmapDescriptor? = null
    ) {
        val latLng =
            LatLng(
                story.lat,
                story.lon
            )
        val marker =
            MarkerOptions().apply {
                position(
                    latLng
                )
                title(
                    story.title
                )
                if (customIcon != null) {
                    icon(
                        customIcon
                    )
                }
            }
        this.map?.addMarker(
            marker
        )
    }

    companion object {
        private val TAG =
            MapFragment::class.java.simpleName
        private const val DEFAULT_ZOOM =
            15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION =
            1
        private const val PERMISSIONS_REQUEST_BACKGROUND_LOCATION =
            2

        // Keys for storing activity state
        private const val KEY_CAMERA_POSITION =
            "camera_position"
        private const val KEY_LOCATION =
            "location"
    }
}


package com.autio.android_app.ui.view.usecases.home.fragment.map

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.res.Resources
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.autio.android_app.R
import com.autio.android_app.data.model.story.Story
import com.autio.android_app.data.model.story.StoryClusterItem
import com.autio.android_app.data.model.story.StoryClusterRenderer
import com.autio.android_app.databinding.FragmentMapBinding
import com.autio.android_app.ui.viewmodel.BottomNavigationViewModel
import com.autio.android_app.ui.viewmodel.MapFragmentViewModel
import com.autio.android_app.ui.viewmodel.StoryViewModel
import com.autio.android_app.util.InjectorUtils
import com.autio.android_app.util.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.clustering.ClusterManager

class MapFragment :
    Fragment(),
    OnMapReadyCallback {

    private val bottomNavigationViewModel by activityViewModels<BottomNavigationViewModel> {
        InjectorUtils.provideBottomNavigationViewModel(
            requireContext()
        )
    }
    private val mapFragmentViewModel by viewModels<MapFragmentViewModel> {
        InjectorUtils.provideMapFragmentViewModel(
            requireContext(),
            mediaId
        )
    }

    private lateinit var mediaId: String
    private lateinit var binding: FragmentMapBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var clusterManager: ClusterManager<StoryClusterItem>

    private val visibleMarkers =
        HashMap<String, StoryClusterItem>()
    private lateinit var storyViewModel: StoryViewModel
    private val stories =
        arrayListOf<Story>()

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
    private var lastKnownCameraZoom : Double? =
        null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )
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
        binding =
            FragmentMapBinding.inflate(
                inflater,
                container,
                false
            )

        storyViewModel =
            ViewModelProvider(
                this
            )[StoryViewModel::class.java].also {
                it.getLiveStories()
                    .observe(
                        viewLifecycleOwner
                    ) { t ->
                        stories.addAll(
                            t
                        )
                    }
            }

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

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )
        if (savedInstanceState != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                lastKnownLocation =
                    savedInstanceState.getParcelable(
                        KEY_LOCATION,
                        Location::class.java
                    )
                cameraPosition =
                    savedInstanceState.getParcelable(
                        KEY_CAMERA_POSITION,
                        CameraPosition::class.java
                    )
                lastKnownCameraZoom =
                    savedInstanceState.getParcelable(
                        KEY_CAMERA_ZOOM,
                        Double::class.java
                    )
            } else {
                lastKnownLocation =
                    savedInstanceState.getParcelable(
                        KEY_LOCATION
                    )
                cameraPosition =
                    savedInstanceState.getParcelable(
                        KEY_CAMERA_POSITION
                    )
            }
        }

        // Always true, but lets lint know that as well
        mediaId =
            arguments?.getString(
                MEDIA_ID_ARG
            )
                ?: return

        // Loading indicator for when data is available
//        mapFragmentViewModel.mediaItems.observe(
//            viewLifecycleOwner
//        ) { list ->
//        }

        mapFragmentViewModel.networkError.observe(
            viewLifecycleOwner
        ) { error ->
            if (error) {
                // TODO: Show missing network snackbar
            } else {
                // TODO: Hide snackbar
            }
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(
            outState
        )
        map?.let { map ->
            Log.d(TAG, "${map.cameraPosition}")
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

    @SuppressLint(
        "PotentialBehaviorOverride"
    )
    override fun onMapReady(
        map: GoogleMap
    ) {
        map.clear()
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

        this.map!!.setOnCameraMoveListener {
            cameraPosition = map.cameraPosition
            val bounds =
                map.projection.visibleRegion.latLngBounds
            for (story in stories) {
                val markerPoint =
                    LatLng(
                        story.lat,
                        story.lon
                    )
                if (bounds.contains(
                        markerPoint
                    )
                ) {
                    if (!visibleMarkers.containsKey(
                            story.id
                        )
                    ) {
                        addStoryMarker(
                            story
                        )
                        bottomNavigationViewModel.setStoryInView(
                            story
                        )
                    }
                } else if (visibleMarkers.containsKey(
                        story.id
                    )
                ) {
                    removeStoryMarker(
                        visibleMarkers[story.id]!!
                    )
                    visibleMarkers.remove(
                        story.id
                    )
                    bottomNavigationViewModel.removeStoryFromView(
                        story
                    )
                }
            }
        }
        clusterManager =
            ClusterManager<StoryClusterItem>(
                requireContext(),
                this.map!!
            )
        clusterManager.renderer =
            StoryClusterRenderer(
                requireContext(),
                this.map!!,
                clusterManager
            )
        this.map!!.setOnMarkerClickListener(
            clusterManager
        )
        clusterManager.setOnClusterClickListener {
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    it.position,
                    map.cameraPosition.zoom + 1
                ),
                300,
                null
            )
            true
        }
        clusterManager.setOnClusterItemClickListener { storyItem ->
            bottomNavigationViewModel.storyClicked(
                storyItem.getStory()
            )
            // Return false to indicate event was not consumed
            // and to return default behaviour to occur
            false
        }
        clusterManager.setAnimation(
            true
        )
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
                            if (cameraPosition != null) {
                                map?.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        cameraPosition!!.target,
                                        cameraPosition!!.zoom
                                    )
                                )
                            } else {
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

                            binding.cardLocationIcon.setOnClickListener {
                                map?.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(
                                            lastKnownLocation!!.latitude,
                                            lastKnownLocation!!.longitude
                                        ),
                                        DEFAULT_ZOOM.toFloat(),

                                    ),
                                    300,
                                    null
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

    private fun updateLocationUI() {
        if (map == null) return
        try {
            if (locationPermissionGranted) {
                binding.cardLocationIcon.visibility =
                    View.VISIBLE
                if (!TrackingUtility.hasBackgroundLocationPermission(
                        requireContext()
                    ) && !locationBackgroundPermissionGranted
                ) {
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
    private fun getLocationPermission() {
        if (TrackingUtility.hasCoreLocationPermissions(
                requireContext()
            )
        ) {
            locationPermissionGranted =
                true
            if (TrackingUtility.hasBackgroundLocationPermission(
                    requireContext()
                )
            ) {
                locationBackgroundPermissionGranted =
                    true
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    ACCESS_FINE_LOCATION
                )
            )
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionResults ->
            if (permissionResults.keys.contains(
                    ACCESS_FINE_LOCATION
                )
            ) {
                if (permissionResults[ACCESS_FINE_LOCATION]!!) {
                    locationPermissionGranted =
                        true
                }
            }
            if (permissionResults.keys.contains(
                    ACCESS_BACKGROUND_LOCATION
                )
            ) {
                if (permissionResults[ACCESS_BACKGROUND_LOCATION]!!) {
                    locationBackgroundPermissionGranted =
                        true
                }
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
            requestPermissionLauncher.launch(
                arrayOf(
                    ACCESS_BACKGROUND_LOCATION
                )
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
        story: Story
    ) {
        val clusterItem =
            StoryClusterItem(
                story
            )
        visibleMarkers[story.id] =
            clusterItem
        clusterManager.addItem(
            clusterItem
        )
        clusterManager.cluster()
    }

    private fun removeStoryMarker(
        storyMarker: StoryClusterItem
    ) {
        clusterManager.removeItem(
            storyMarker
        )
        clusterManager.cluster()
    }

    companion object {
        private val TAG =
            MapFragment::class.simpleName
        private const val DEFAULT_ZOOM =
            15

        // Keys for storing activity state
        private const val KEY_CAMERA_POSITION =
            "camera_position"
        private const val KEY_LOCATION =
            "location"
        private const val KEY_CAMERA_ZOOM =
            "camera_zoom"

        fun newInstance(
            mediaId: String
        ): MapFragment {
            return MapFragment().apply {
                arguments =
                    Bundle().apply {
                        putString(
                            MEDIA_ID_ARG,
                            mediaId
                        )
                    }
            }
        }
    }
}

private const val MEDIA_ID_ARG =
    "com.autio.android_app.ui.view.usecases.home.fragment.MediaItemFragment.MEDIA_ID"

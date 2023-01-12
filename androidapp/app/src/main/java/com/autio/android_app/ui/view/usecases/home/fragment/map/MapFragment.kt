package com.autio.android_app.ui.view.usecases.home.fragment.map

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.*
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.contains
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.autio.android_app.R
import com.autio.android_app.data.model.story.Story
import com.autio.android_app.data.model.story.StoryClusterItem
import com.autio.android_app.data.model.story.StoryClusterRenderer
import com.autio.android_app.data.repository.FirebaseStoryRepository
import com.autio.android_app.data.repository.PrefRepository
import com.autio.android_app.databinding.FragmentMapBinding
import com.autio.android_app.ui.view.usecases.home.BottomNavigation
import com.autio.android_app.ui.viewmodel.BottomNavigationViewModel
import com.autio.android_app.ui.viewmodel.MapFragmentViewModel
import com.autio.android_app.ui.viewmodel.StoryViewModel
import com.autio.android_app.util.*
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MapFragment :
    Fragment(),
    OnMapReadyCallback {
    private val prefRepository by lazy {
        PrefRepository(
            requireContext()
        )
    }

    private val bottomNavigationViewModel by activityViewModels<BottomNavigationViewModel>()
    private val mapFragmentViewModel by viewModels<MapFragmentViewModel> {
        InjectorUtils.provideMapFragmentViewModel(
            requireContext(),
            mediaId
        )
    }
    private val storyViewModel by viewModels<StoryViewModel> {
        InjectorUtils.provideStoryViewModel(
            requireContext()
        )
    }

    private lateinit var mediaId: String
    private lateinit var binding: FragmentMapBinding

    private lateinit var activityLayout: ConstraintLayout

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var clusterManager: ClusterManager<StoryClusterItem>

    private val visibleMarkers =
        HashMap<String, StoryClusterItem>()

    private var map: GoogleMap? =
        null
    private var clusterRenderer: StoryClusterRenderer? =
        null

    private var _selectedStory: StoryClusterItem? =
        null

    private var storyDisplayTimer: Timer? =
        null
    private var cameraIdleTimer: Timer? =
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

    private lateinit var snackBarView: View
    private var feedbackJob: Job? =
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

        snackBarView =
            layoutInflater.inflate(
                R.layout.feedback_snackbar,
                binding.root,
                false
            )

        activityLayout =
            requireActivity().findViewById(
                R.id.activityRoot
            )

        lifecycleScope.launch {
            val mapFragment =
                childFragmentManager.findFragmentById(
                    R.id.maps
                ) as SupportMapFragment?
            mapFragment?.getMapAsync(
                this@MapFragment
            )
        }

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clusterManager.clearItems()
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
                binding.ivPlaylistIcon
            )
            binding.cardLocationIcon.visibility =
                View.VISIBLE
            binding.maps.visibility =
                View.VISIBLE
            binding.playlist.visibility =
                View.GONE
        }

        binding.ivPlaylistIcon.setOnClickListener {
            changeDrawableFillColor(
                it,
                R.color.tab_view_icon_active
            )
            changeDrawableFillColor(
                binding.imgMapView
            )
            binding.cardLocationIcon.visibility =
                View.GONE
            binding.maps.visibility =
                View.GONE
            binding.playlist.visibility =
                View.VISIBLE
            hideSelectedStoryComponent()
            storyDisplayTimer?.cancelTimer()
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
        map.uiSettings.isMyLocationButtonEnabled =
            false
        map.uiSettings.isRotateGesturesEnabled =
            false
        map.uiSettings.isMapToolbarEnabled =
            false
        map.resetMinMaxZoomPreference()
        map.setMaxZoomPreference(
            20F
        )
        map.setMinZoomPreference(
            8F
        )
        this.map =
            map

        lifecycleScope.launch {
            mapFragmentViewModel.changeLatLngBounds(
                this@MapFragment.map!!.projection.visibleRegion.latLngBounds
            )
        }

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

        mapFragmentViewModel.storiesInScreen.observe(
            viewLifecycleOwner
        ) {
            removeNotVisibleMarkers(
                it
            )

            for (story in it) {
                if (visibleMarkers[story.id] == null) {
                    addStoryMarker(
                        story
                    )
                }
            }
            clusterManager.cluster()
        }

        this.map!!.setOnCameraMoveListener {
            clusterRenderer?.onCameraMove()
            cameraPosition =
                map.cameraPosition
            val bounds =
                map.projection.visibleRegion.latLngBounds
            lifecycleScope.launch {
                mapFragmentViewModel.changeLatLngBounds(
                    bounds
                )
            }
        }
        this.map!!.setOnCameraIdleListener {
            lifecycleScope.launch(
                Dispatchers.IO
            ) {
                bottomNavigationViewModel.fetchRecordsOfStories()
            }
            cameraIdleTimer =
                Timer()
            cameraIdleTimer?.startTimer()
            cameraIdleTimer?.isActive?.observe(
                this
            ) {
                if (it == false) {
                    // TODO: Move camera to the currently playing story marker
//                    moveCamera()
                    Log.d(
                        TAG,
                        "camera faces current playing story"
                    )
                }
            }
        }
        clusterManager =
            ClusterManager<StoryClusterItem>(
                requireContext(),
                this.map!!,
            )
        clusterManager.setAnimation(
            false
        )
        clusterRenderer =
            StoryClusterRenderer(
                requireContext(),
                this.map!!,
                clusterManager,
                this.map!!.cameraPosition.zoom,
                18F
            )
        clusterManager.renderer =
            clusterRenderer
        clusterManager.addItems(
            visibleMarkers.values
        )
        clusterManager.setOnClusterClickListener {
            moveCamera(
                it.position,
                map.cameraPosition.zoom + 1
            )
            true
        }
        clusterManager.setOnClusterItemClickListener { storyItem ->
            _selectedStory =
                storyItem
            tapClusterItem(
                storyItem
            )
            false
        }
        clusterManager.setAnimation(
            true
        )
        this.map!!.setOnMarkerClickListener(
            clusterManager
        )
    }

    private fun highlightClusterItem(
        storyClusterItem: StoryClusterItem
    ) {

    }

    private fun unhighlightClusterItem(
        storyClusterItem: StoryClusterItem
    ) {

    }

    /**
     * Sets Google's logo in a visible part since Google Maps Terms of Service
     * requires the app to display it as referred in this link
     * https://cloud.google.com/maps-platform/terms/
     */
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
                                moveCamera()
                            }
                            binding.cardLocationIcon.setOnClickListener {
                                moveCamera()
                            }
                        }
                    } else {
                        moveCamera(
                            DEFAULT_LOCATION_LAT,
                            DEFAULT_LOCATION_LNG
                        )
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
                map!!.isMyLocationEnabled =
                    true
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
     * Moves camera to a certain position with an animation
     *
     * @param [position] moves camera so this point is located
     * at the center
     */
    private fun moveCamera(
        position: LatLng,
        zoom: Float? = null
    ) {
        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                position,
                zoom
                    ?: DEFAULT_ZOOM.toFloat()
            ),
            200,
            null
        )
    }

    /**
     * Positions camera over a point
     * This call won't work if the user's location permission
     * is not granted and both latitude/longitude are null
     *
     * @param [latitude] latitude of point
     * @param [longitude] longitude of point
     * @param [duration] time in milliseconds it will take to position
     * the camera on the point to simulate an animation, by default is 0
     * @param [zoom] zoom the camera will have over the point
     */
    private fun moveCamera(
        latitude: Double? = null,
        longitude: Double? = null,
        duration: Int? = null,
        zoom: Float? = null
    ) {
        val destinationLat =
            latitude
                ?: lastKnownLocation?.latitude
        val destinationLng =
            longitude
                ?: lastKnownLocation?.longitude
        if ((destinationLat) == null || (destinationLng) == null
        ) return
        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    destinationLat,
                    destinationLng
                ),
                zoom
                    ?: DEFAULT_ZOOM.toFloat()
            ),
            duration
                ?: 100,
            null
        )
    }

    /**
     * Displays the story's data in a floating card
     * for the user to tap on
     * Tapping on the play/pause icon will make the story
     * call [playMediaId]
     */
    private fun tapClusterItem(
        clusterItem: StoryClusterItem
    ) {
        storyDisplayTimer?.cancelTimer()
        storyDisplayTimer =
            Timer()
        val story =
            clusterItem.getStory()
        binding.tvSelectedStoryTitle.text =
            story.title
        binding.tvSelectedStoryNarrator.text =
            story.narrator
        bottomNavigationViewModel.mediaButtonRes.observe(
            this
        ) { res ->
            if (story.id == bottomNavigationViewModel.playingStory.value?.id) {
                binding.btnSelectedStoryPlay.setImageResource(
                    res
                )
            } else {
                binding.btnSelectedStoryPlay.setImageResource(
                    R.drawable.ic_player_play
                )
            }
        }
        binding.btnSelectedStoryPlay.setOnClickListener {
            showPaywallOrProceedWithNormalProcess {
                bottomNavigationViewModel.playMediaId(
                    story.id
                )
            }
        }
        if (story.isBookmarked == true) {
            binding.btnSelectedStoryBookmark.apply {
                setImageResource(
                    R.drawable.ic_player_bookmark_filled
                )
                setOnClickListener {
                    showPaywallOrProceedWithNormalProcess {
                        FirebaseStoryRepository.removeBookmark(
                            prefRepository.firebaseKey,
                            story.id,
                            onSuccessListener = {
                                storyViewModel.removeBookmarkFromStory(
                                    story.id
                                )
                                showFeedbackSnackBar(
                                    "Removed From Bookmarks"
                                )
                            },
                            onFailureListener = {
                                showFeedbackSnackBar(
                                    "Connection Failure"
                                )
                            }
                        )
                    }
                }
            }
        } else {
            binding.btnSelectedStoryBookmark.apply {
                setImageResource(
                    R.drawable.ic_player_bookmark
                )
                setOnClickListener {
                    showPaywallOrProceedWithNormalProcess {
                        FirebaseStoryRepository.bookmarkStory(
                            prefRepository.firebaseKey,
                            story.id,
                            story.title,
                            onSuccessListener = {
                                storyViewModel.bookmarkStory(
                                    story.id
                                )
                                showFeedbackSnackBar(
                                    "Added To Bookmarks"
                                )
                            },
                            onFailureListener = {
                                showFeedbackSnackBar(
                                    "Connection Failure"
                                )
                            }
                        )
                    }
                }
            }

        }
        showSelectedStoryComponent()
        highlightClusterItem(
            clusterItem
        )
        storyDisplayTimer?.startTimer()
        storyDisplayTimer?.isActive?.observe(
            this
        ) {
            if (it == false) {
                hideSelectedStoryComponent()
                unhighlightClusterItem(
                    clusterItem
                )
            }
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
        val inflater =
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

        bottomNavigationViewModel.setStoryInView(
            story
        )
    }

    private fun removeNotVisibleMarkers(
        visibleStories: List<Story>
    ) {
        val iterator =
            visibleMarkers.iterator()
        while (iterator.hasNext()) {
            val marker =
                iterator.next()
            if (!visibleStories.map { it.id }
                    .contains(
                        marker.key
                    )) {
                bottomNavigationViewModel.removeStoryFromView(
                    marker.value.getStory()
                )
                clusterManager.removeItem(
                    marker.value
                )
                iterator.remove()
            }
        }
    }

    private fun hideSelectedStoryComponent() {
        binding.floatingSelectedStory.animate()
            .alpha(
                0.0f
            )
            .withEndAction {
                binding.floatingSelectedStory.visibility =
                    View.GONE
            }
    }

    private fun showSelectedStoryComponent() {
        if (binding.floatingSelectedStory.visibility != View.VISIBLE) {
            binding.floatingSelectedStory.visibility =
                View.VISIBLE
            binding.floatingSelectedStory.animate()
                .alpha(
                    1.0f
                )
        }
    }

    private fun showFeedbackSnackBar(
        feedback: String
    ) {
        cancelJob()
        snackBarView.alpha =
            1F
        snackBarView.findViewById<TextView>(
            R.id.tvFeedback
        ).text =
            feedback
        activityLayout.addView(
            snackBarView
        )
        feedbackJob =
            lifecycleScope.launch {
                delay(
                    2000
                )
                snackBarView.animate()
                    .alpha(
                        0F
                    )
                    .withEndAction {
                        activityLayout.removeView(
                            snackBarView
                        )
                    }
            }
    }

    private fun showPaywallOrProceedWithNormalProcess(
        normalProcess: () -> Unit
    ) {
        if (prefRepository.remainingStories <= 0) {
            (requireActivity() as BottomNavigation).showPayWall()
        } else {
            normalProcess.invoke()
        }
    }

    private fun cancelJob() {
        if (activityLayout.contains(
                snackBarView
            )
        ) {
            activityLayout.removeView(
                snackBarView
            )
        }
        feedbackJob?.cancel()
    }
}

private val TAG =
    MapFragment::class.simpleName
private const val DEFAULT_ZOOM =
    15

// Keys for storing activity state
private const val KEY_CAMERA_POSITION =
    "camera_position"
private const val KEY_LOCATION =
    "location"
private const val MEDIA_ID_ARG =
    "com.autio.android_app.ui.view.usecases.home.fragment.MediaItemFragment.MEDIA_ID"

package com.autio.android_app.ui.stories.fragments

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.*
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.contains
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.api.model.StoryOption
import com.autio.android_app.data.api.model.modelLegacy.StoryClusterRenderer
import com.autio.android_app.data.api.model.pendings.StoryClusterItem
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.FragmentMapBinding
import com.autio.android_app.domain.mappers.toDto
import com.autio.android_app.domain.mappers.toModel
import com.autio.android_app.extensions.findNearestToCoordinates
import com.autio.android_app.extensions.toPx
import com.autio.android_app.ui.stories.adapter.StoryAdapter
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.ui.stories.view_model.BottomNavigationViewModel
import com.autio.android_app.ui.stories.view_model.StoryViewModel
import com.autio.android_app.ui.stories.view_states.StoryViewState
import com.autio.android_app.ui.viewmodel.MapFragmentViewModel
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {

    private val bottomNavigationViewModel: BottomNavigationViewModel by activityViewModels()
    private val mapFragmentViewModel: MapFragmentViewModel by viewModels()
    private val storyViewModel: StoryViewModel by viewModels()

    @Inject
    lateinit var prefRepository: PrefRepository

    private lateinit var mediaId: String
    private lateinit var binding: FragmentMapBinding
    private lateinit var activityLayout: ConstraintLayout
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var clusterManager: ClusterManager<StoryClusterItem>
    private lateinit var map: GoogleMap

    private var highlightedItem: StoryClusterItem? = null
    private val markers = mutableMapOf<Int, StoryClusterItem>()
    private var storyDisplayTimer: Timer? = null
    private var cameraIdleTimer: Timer? = null
    private var cameraPosition: CameraPosition? = null

    // The entry point to the Places API.
    private lateinit var placesClient: PlacesClient

    var locationPermissionGranted = false
    private var locationBackgroundPermissionGranted = false

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null

    private lateinit var storyAdapter: StoryAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var locationPermissionSnackBar: View
    private val set = ConstraintSet()

    private lateinit var snackBarView: View
    private var feedbackJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Places.initialize(requireContext(), resources.getString(R.string.google_maps_key))
        placesClient = Places.createClient(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        bindObservers()
        binding = FragmentMapBinding.inflate(inflater, container, false)

        snackBarView = layoutInflater.inflate(
            R.layout.feedback_snackbar, binding.root as ViewGroup, false
        )

        activityLayout = requireActivity().findViewById(R.id.activityRoot)

        recyclerView = binding.layoutPlaylist.rvMapPlaylist
        storyAdapter = StoryAdapter(
            bottomNavigationViewModel.playingStory, onStoryPlay = { id ->
                UtilsClass(prefRepository).showPaywallOrProceedWithNormalProcess(requireActivity()) {
                    bottomNavigationViewModel.playMediaId(id)
                }
            }, onOptionClick = ::onOptionClicked, shouldPinLocationBeShown = true
        )
        recyclerView.adapter = storyAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val mapFragment = childFragmentManager.findFragmentById(R.id.maps) as? SupportMapFragment


        lifecycleScope.launchWhenCreated {
            mapFragment?.getMapAsync(this@MapFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            lastKnownLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                savedInstanceState.getParcelable(KEY_LOCATION, Location::class.java)
            } else {
                savedInstanceState.getParcelable(KEY_LOCATION)
            }
            cameraPosition = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                savedInstanceState.getParcelable(KEY_CAMERA_POSITION, CameraPosition::class.java)
            } else {
                savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
            }
        }

        // Always true, but lets lint know that as well
        mediaId = arguments?.getString(MEDIA_ID_ARG) ?: return

        clusterManager = ClusterManager<StoryClusterItem>(requireContext(), map)

        setListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (this::clusterManager.isInitialized)
            clusterManager.clearItems() //TODO(This breaks the fragment for a not initialized lateInit, but could be necessary)
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(
            outState
        )
        outState.putParcelable(
            KEY_CAMERA_POSITION, map.cameraPosition
        )
        outState.putParcelable(
            KEY_LOCATION, lastKnownLocation
        )
    }

    fun bindObservers() {
        storyViewModel.storyViewState.observe(viewLifecycleOwner, ::handleViewState)
    }

    private fun handleViewState(viewState: StoryViewState?) {
        when (viewState) {
            is StoryViewState.AddedBookmark -> addedBookmark()
            is StoryViewState.RemovedBookmark -> removeBookmark()
            is StoryViewState.StoryLiked -> storyLiked()
            is StoryViewState.LikedRemoved -> likedRemoved()
            is StoryViewState.StoryRemoved -> removedFromDownload()
            is StoryViewState.FetchedAllStories -> addClusteredMarkers(viewState.stories)
            else -> viewStateError() //TODO(Ideally have error handling for each error, ideally would be not to have so many viewstates)
        }
    }
    private fun addedBookmark() {
        showFeedbackSnackBar("Added To Bookmarks")
    }

    private fun removeBookmark() {
        showFeedbackSnackBar("Removed From Bookmarks")
    }

    private fun storyLiked() {
        showFeedbackSnackBar("Added To Favorites")
    }

    private fun likedRemoved() {
        showFeedbackSnackBar("Removed From Favorites")
    }

    private fun removedFromDownload() {
        showFeedbackSnackBar("Story Removed From My Device")
    }

    private fun viewStateError() {
        //TODO(Macro error handling for viewstate error)
        showFeedbackSnackBar("Connection Failure")
    }



    private fun setListeners() {
        with(
            binding
        ) {
            cvToggleMapPlaylist.setOnClickListener {
                mapFragmentViewModel.displayListForStoriesInScreen()
            }
            mapFragmentViewModel.isListDisplaying.observe(
                viewLifecycleOwner
            ) {
                if (it) {
                    ivToggleIcon.setImageResource(
                        R.drawable.ic_map
                    )
                    layoutPlaylist.root.visibility = View.VISIBLE
                    val animate = TranslateAnimation(
                        0f,  // fromXDelta
                        0f,  // toXDelta
                        binding.root.height.toFloat(),  // fromYDelta
                        binding.cvToggleMapPlaylist.translationY  // toYDelta
                    )
                    animate.duration = 500
                    animate.fillAfter = true
                    layoutPlaylist.root.startAnimation(
                        animate
                    )
                } else {
                    ivToggleIcon.setImageResource(
                        R.drawable.ic_map_list
                    )
                    val animate = TranslateAnimation(
                        0f,  // fromXDelta
                        0f,  // toXDelta
                        0f,  // fromYDelta
                        binding.root.height.toFloat(), // toYDelta
                    )
                    animate.duration = 500
                    layoutPlaylist.root.startAnimation(
                        animate
                    )
                    layoutPlaylist.root.visibility = View.GONE
                }
            }
        }
    }

    @SuppressLint(
        "PotentialBehaviorOverride"
    )
    override fun onMapReady(
        googleMap: GoogleMap
    ) {
        googleMap.clear()
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        googleMap.uiSettings.isRotateGesturesEnabled = false
        googleMap.uiSettings.isMapToolbarEnabled = false
        map = googleMap

        // Sets the light or dark mode of the map from the "map_style.json" inside
        // raw resources
        setMapLayout()

        // Prompt the user for permission
        getLocationPermission()

        // Turn on the My Location layer and the related control on the map
        updateLocationUI()

        setGoogleLogoNewPosition()

        storyViewModel.getAllStories()


        bottomNavigationViewModel.playingStory.observe(viewLifecycleOwner) {
            it?.let {
                updateMarker(it, markers[it.id]!!)
            }
        }

        mapFragmentViewModel.storiesInScreen.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                binding.layoutPlaylist.rvMapPlaylist.adapter = storyAdapter
                storyAdapter.submitList(it)
            }
        }

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()



    }

    private fun highlightClusterItem(storyClusterItem: StoryClusterItem) {
        if (highlightedItem != null) {
            unhighlightClusterItem(highlightedItem!!)
        }
        highlightedItem = storyClusterItem.apply {
            val originalBitmap = bitmap
            val marker = this.marker
            if (marker != null && originalBitmap != null) {
                val largerBitmap = getLargerBitmap(originalBitmap)
                val largerBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(largerBitmap)

                marker.setIcon(largerBitmapDescriptor)
            }
        }
    }

    private fun unhighlightClusterItem(storyClusterItem: StoryClusterItem) {
        val largerBitmap = storyClusterItem.bitmap
        largerBitmap?.let {
            val originalBitmap = getOriginalBitmap(it)
            val smallerBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(originalBitmap)
            storyClusterItem.marker!!.setIcon(smallerBitmapDescriptor)
        }
    }

    private fun getLargerBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width * 1.5
        val height = bitmap.height * 1.5
        return Bitmap.createScaledBitmap(bitmap, width.toInt(), height.toInt(), false)
    }

    private fun getOriginalBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    private fun getTransformationBitmap(
        start: Bitmap, end: Bitmap, fraction: Float
    ): Bitmap {
        val width = start.width
        val height = start.height

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint()
        paint.isFilterBitmap = true

        canvas.drawBitmap(start, 0f, 0f, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
        paint.alpha = (255 * fraction).toInt()
        canvas.drawBitmap(end, 0f, 0f, paint)
        paint.xfermode = null
        paint.alpha = 255

        return bitmap
    }

    /**
     * Sets Google's logo in a visible part since Google Maps Terms of Service
     * requires the app to display it as referred in this link
     * https://cloud.google.com/maps-platform/terms/
     */
    private fun setGoogleLogoNewPosition() {
        val googleLogo: View = binding.maps.findViewWithTag("GoogleWatermark")
        val glLayoutParams = googleLogo.layoutParams as RelativeLayout.LayoutParams
        glLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0)
        glLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0)
        glLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0)
        glLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
        glLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
        googleLogo.layoutParams = glLayoutParams
    }

    /**
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            if (cameraPosition != null) {
                                map.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        cameraPosition!!.target, cameraPosition!!.zoom
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
                        moveCamera(DEFAULT_LOCATION_LAT, DEFAULT_LOCATION_LNG)
                    }
                }
            } else {
                moveCamera(DEFAULT_LOCATION_LAT, DEFAULT_LOCATION_LNG)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, e.message, e)
        }
    }

    fun updateLocationUI() {
        try {
            if (locationPermissionGranted) {
                map.isMyLocationEnabled = true
                binding.cardLocationIcon.visibility = View.VISIBLE
                if (!TrackingUtility.hasBackgroundLocationPermission(
                        requireContext()
                    ) && !locationBackgroundPermissionGranted
                ) {
                    showLocationPermissionAlwaysEnabledSnackBar()
                }
            } else {
                binding.cardLocationIcon.visibility = View.GONE
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, e.message, e)
        }
    }

    /**
     * Style the map layout whether light mode or
     * night mode are active
     */
    private fun setMapLayout() {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    /**
     * Moves camera to a certain position with an animation
     *
     * @param [position] moves camera so this point is located
     * at the center
     */
    private fun moveCamera(
        position: LatLng, zoom: Float? = null
    ) {
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                position, zoom ?: DEFAULT_ZOOM.toFloat()
            ), 200, null
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
        val destinationLat = latitude ?: lastKnownLocation?.latitude
        val destinationLng = longitude ?: lastKnownLocation?.longitude
        if ((destinationLat) == null || (destinationLng) == null) return
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
            LatLng(destinationLat, destinationLng), zoom ?: DEFAULT_ZOOM.toFloat()
        ), duration ?: 100, object : GoogleMap.CancelableCallback {
            override fun onCancel() {
                updateMapBounds(map)
            }

            override fun onFinish() {
                updateMapBounds(map)
            }

        })
    }

    /**
     * Displays the story's data in a floating card
     * for the user to tap on
     * Tapping on the play/pause icon will make the story
     * call playMediaId
     */
    private fun tapClusterItem(clusterItem: StoryClusterItem) {
        storyDisplayTimer?.cancelTimer()
        storyDisplayTimer = Timer()
        val storyDto = clusterItem.story
        binding.tvSelectedStoryTitle.text = storyDto.title
        binding.tvSelectedStoryNarrator.text = storyDto.narrator
        bottomNavigationViewModel.mediaButtonRes.observe(this) { res ->
            if (storyDto.id == bottomNavigationViewModel.playingStory.value?.id) {
                binding.btnSelectedStoryPlay.setImageResource(res)
            } else {
                binding.btnSelectedStoryPlay.setImageResource(R.drawable.ic_player_play)
            }
        }
        binding.btnSelectedStoryPlay.setOnClickListener {
       //     UtilsClass(prefRepository).showPaywallOrProceedWithNormalProcess(requireActivity()) { //TODO(Commented this out cause it get bringing me back to paywall, need to check logic later)
                bottomNavigationViewModel.playMediaId(storyDto.id)
            //}
        }
        binding.btnSelectedStoryInfo.setOnClickListener {
            storyDisplayTimer?.pauseTimer()
            showStoryOptions(requireContext(),
                binding.root as ViewGroup,
                it,
                storyDto.toModel(),
                arrayListOf(
                    if (storyDto.isBookmarked == true) StoryOption.REMOVE_BOOKMARK else StoryOption.BOOKMARK,
                    if (storyDto.isLiked == true) StoryOption.REMOVE_LIKE else StoryOption.LIKE,
                    StoryOption.DOWNLOAD,
                    StoryOption.DIRECTIONS,
                    StoryOption.SHARE
                ),
                onOptionClick = ::onOptionClicked,
                onDismiss = { storyDisplayTimer?.finishTimer() })
        }
        showSelectedStoryComponent()
        highlightClusterItem(clusterItem)
        storyDisplayTimer?.startTimer()
        storyDisplayTimer?.isActive?.observe(this) {
            if (it == false) {
                hideSelectedStoryComponent()
            }
        }
    }

    /**
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
    private fun getLocationPermission() {
        if (TrackingUtility.hasCoreLocationPermissions(requireContext())) {
            locationPermissionGranted = true
            if (TrackingUtility.hasBackgroundLocationPermission(requireContext())) {
                locationBackgroundPermissionGranted = true
            }
        } else {
//            requestPermissionLauncher.launch(
//                arrayOf(
//                    ACCESS_FINE_LOCATION
//                )
//            )
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionResults ->
        if (permissionResults.keys.contains(ACCESS_BACKGROUND_LOCATION)) {
            if (permissionResults[ACCESS_BACKGROUND_LOCATION]!!) {
                locationBackgroundPermissionGranted = true
                hideLocationPermissionAlwaysEnabledSnackBar()
            }
        }
        updateLocationUI()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showLocationPermissionAlwaysEnabledSnackBar() {
        locationPermissionSnackBar = layoutInflater.inflate(
            R.layout.location_permission_snackbar, binding.mapsRoot, false
        )
        locationPermissionSnackBar.setOnClickListener {
            requestPermissionLauncher.launch(
                arrayOf(ACCESS_BACKGROUND_LOCATION)
            )
        }
        binding.mapsRoot.addView(locationPermissionSnackBar)
        val marginTopSnackBar = 30.toPx(requireContext()).toInt()
        val marginTopButtons = 12.toPx(requireContext()).toInt()
        set.apply {
            clone(binding.mapsRoot)
            connect(
                R.id.rlPermissionSnackBar,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                marginTopSnackBar
            )
            connect(
                R.id.card_location_icon,
                ConstraintSet.TOP,
                R.id.rlPermissionSnackBar,
                ConstraintSet.BOTTOM,
                marginTopButtons
            )
            connect(
                R.id.cvToggleMapPlaylist,
                ConstraintSet.TOP,
                R.id.rlPermissionSnackBar,
                ConstraintSet.BOTTOM,
                marginTopButtons
            )
            applyTo(
                binding.mapsRoot
            )
        }
    }

    private fun hideLocationPermissionAlwaysEnabledSnackBar() {
        binding.mapsRoot.removeView(locationPermissionSnackBar)
        val marginTop = 30.toPx(requireContext()).toInt()
        set.apply {
            clone(binding.mapsRoot)
            connect(
                R.id.card_location_icon,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                marginTop
            )
            connect(
                R.id.cvToggleMapPlaylist,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                marginTop
            )
            applyTo(binding.mapsRoot)
        }
    }

    /**
     * Adds markers to the map with clustering support. As response to the viewModel liveData.
     */
    private fun addClusteredMarkers(stories: List<Story>){
        clusterManager = ClusterManager<StoryClusterItem>(requireContext(), map)
        clusterManager.setAnimation(false)
        val clusterRenderer = StoryClusterRenderer(requireContext(), map, clusterManager)
        clusterManager.renderer = clusterRenderer
        markers.putAll(stories.associate { it.id to StoryClusterItem(it.toDto()) })

        clusterManager.addItems(markers.values)
        clusterManager.cluster()
        clusterManager.setOnClusterItemClickListener { storyItem ->
            tapClusterItem(storyItem)
            false
        }
        clusterManager.setOnClusterClickListener {
            moveCamera(it.position, map.cameraPosition.zoom + 1)
            true
        }

        map.setOnCameraMoveStartedListener {
            cameraIdleTimer?.cancelTimer()
        }
        map.setOnCameraIdleListener {
            cameraPosition = map.cameraPosition
            // Call clusterManager.onCameraIdle() when the camera stops moving so that re-clustering
            // can be performed when the camera stops moving
            clusterManager.onCameraIdle()
            updateMapBounds(map)


            mapFragmentViewModel.fetchRecordsOfStories(
                prefRepository.userId, prefRepository.userApiToken //TODO(No userID or Apitoken are being fetched)
            )
        }
        cameraIdleTimer = Timer(15000)
        cameraIdleTimer?.startTimer()
        cameraIdleTimer?.isActive?.observe(this@MapFragment) {
            if (it == false) {
                val center = map.cameraPosition.target
                val nearestStory =
                    mapFragmentViewModel.storiesInScreen.value?.findNearestToCoordinates(
                        center
                    )
                if (nearestStory != null) {
                    moveCamera(
                        latitude = nearestStory.lat,
                        longitude = nearestStory.lng,
                        zoom = map.cameraPosition.zoom
                    )
                    if (clusterRenderer.getMarker(markers[nearestStory.id]!!) != null) {
                        highlightClusterItem(markers[nearestStory.id]!!)
                    }
                }
            }
        }

    }

    private fun updateMarker(story: Story, item: StoryClusterItem) {
        item.updateStory(story.toDto())
        clusterManager.updateItem(item)
        clusterManager.cluster()
    }

    private fun updateMapBounds(map: GoogleMap) {
        val bounds = map.projection.visibleRegion.latLngBounds

        lifecycleScope.launch(Dispatchers.IO) {
            mapFragmentViewModel.changeLatLngBounds(bounds)
        }

    }

    private fun hideSelectedStoryComponent() {
        binding.floatingSelectedStory.apply {
            animate().alpha(0.0f).withEndAction {
                visibility = View.GONE
            }
        }
    }

    private fun showSelectedStoryComponent() {
        val player = activityLayout.findViewById<LinearLayout>(R.id.persistentPlayer)
        val displayingMessage = activityLayout.findViewById<FrameLayout>(R.id.flImportantMessage)
        val totalHeight = -player.height - displayingMessage.height - 24F
        binding.floatingSelectedStory.apply {
            if (visibility != View.VISIBLE) {
                translationY = totalHeight
                visibility = View.VISIBLE
                animate().alpha(1.0f)
            }
        }
    }

    private fun onOptionClicked(option: StoryOption, story: Story) {
        storyDisplayTimer?.finishTimer()
        UtilsClass(prefRepository).showPaywallOrProceedWithNormalProcess(requireActivity(), true) {
            when (option) {
                StoryOption.BOOKMARK -> {

                    storyViewModel.bookmarkStory(
                        prefRepository.userId,
                        prefRepository.userApiToken,
                        story.id
                    )
                }
                StoryOption.REMOVE_BOOKMARK -> {

                    storyViewModel.removeBookmarkFromStory(
                        prefRepository.userId,
                        prefRepository.userApiToken,
                        story.id
                    )
                }
                StoryOption.LIKE -> {

                    storyViewModel.giveLikeToStory(
                        prefRepository.userId,
                        prefRepository.userApiToken,
                        story.id
                    )
                }
                StoryOption.REMOVE_LIKE -> {
                    storyViewModel.removeLikeFromStory(
                        prefRepository.userId,
                        prefRepository.userApiToken,
                        story.id
                    )
                }
                StoryOption.DOWNLOAD -> lifecycleScope.launch {
                    try {
                        val downloadedStory =
                            DownloadedStoryEntity.fromStory(requireContext(), story.toDto())
                        storyViewModel.downloadStory(downloadedStory!!)
                        val updatedStory = story.copy(isDownloaded = true)
//                        updateMarker( updatedStory )
                        showFeedbackSnackBar("Story Saved To My Device")
                    } catch (e: Exception) {
                        Log.e("BookmarksFragment", "exception: ", e)
                        showFeedbackSnackBar("Failed Downloading Story")
                    }
                }
                StoryOption.REMOVE_DOWNLOAD -> {
                    storyViewModel.removeDownloadedStory(story.id)
                    val updatedStory = story.copy(isDownloaded = false)
//                    updateMarker( updatedStory )
                    showFeedbackSnackBar("Story Removed From My Device")
                }
                StoryOption.DIRECTIONS -> openLocationInMapsApp(
                    requireActivity(), story.lat, story.lng
                )
                StoryOption.SHARE -> {
                    shareStory(requireContext(), story.id)
                }
                else -> Log.d(TAG, "No option available")
            }
        }
    }

    private fun showFeedbackSnackBar(feedback: String) {
        cancelJob()
        snackBarView.alpha = 1F
        snackBarView.findViewById<TextView>(R.id.tvFeedback).text = feedback
        activityLayout.addView(snackBarView)
        feedbackJob = lifecycleScope.launch {
            delay(2000)
            snackBarView.animate().alpha(0F).withEndAction {
                activityLayout.removeView(snackBarView)
            }
        }
    }

    private fun cancelJob() {
        if (activityLayout.contains(snackBarView)) {
            activityLayout.removeView(snackBarView)
        }
        feedbackJob?.cancel()
    }
}

private val TAG = MapFragment::class.simpleName
private const val DEFAULT_ZOOM = 15

// Keys for storing activity state
private const val KEY_CAMERA_POSITION = "camera_position"
private const val KEY_LOCATION = "location"
private const val MEDIA_ID_ARG =
    "com.autio.android_app.ui.view.usecases.home.fragment.MediaItemFragment.MEDIA_ID"

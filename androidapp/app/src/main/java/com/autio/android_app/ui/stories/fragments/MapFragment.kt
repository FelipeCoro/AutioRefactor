package com.autio.android_app.ui.stories.fragments

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.api.model.StoryOption
import com.autio.android_app.data.api.model.modelLegacy.StoryClusterRenderer
import com.autio.android_app.data.api.model.pendings.StoryClusterItem
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.FragmentMapBinding
import com.autio.android_app.extensions.findNearestToCoordinates
import com.autio.android_app.ui.stories.adapter.StoryAdapter
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.ui.stories.view_model.BottomNavigationViewModel
import com.autio.android_app.ui.stories.view_model.PlayerFragmentViewModel
import com.autio.android_app.ui.stories.view_model.StoryViewModel
import com.autio.android_app.ui.stories.view_states.BottomNavigationViewState
import com.autio.android_app.ui.stories.view_states.StoryViewState
import com.autio.android_app.ui.viewmodel.MapFragmentViewModel
import com.autio.android_app.util.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.clustering.ClusterManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment(), OnMapReadyCallback {

    private val bottomNavigationViewModel: BottomNavigationViewModel by activityViewModels()
    private val mapFragmentViewModel: MapFragmentViewModel by viewModels()
    private val storyViewModel: StoryViewModel by viewModels()
    private val playerViewModel: PlayerFragmentViewModel by viewModels()

    @Inject
    lateinit var prefRepository: PrefRepository

    private lateinit var mediaId: String
    private lateinit var binding: FragmentMapBinding

    //private lateinit var activityLayout: ConstraintLayout
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
    private var lastKnownLocation: Location? = null

    private lateinit var storyAdapter: StoryAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var locationPermissionSnackBar: View
    private val set = ConstraintSet()


    private var feedbackJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Places.initialize(requireContext(), resources.getString(R.string.google_maps_key))
        placesClient = Places.createClient(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        bindObservers()
        bindArguments(savedInstanceState)
        setListeners()
        initView()
    }

    private fun initView() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.maps) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        //TODO(Implement showpaywall logic where if the 5 stories are out, showPaywall())
        recyclerView = binding.layoutPlaylist.rvMapPlaylist
        storyAdapter = StoryAdapter(
            bottomNavigationViewModel.playingStory, onStoryPlay = { id ->
                bottomNavigationViewModel.shouldPlayMedia(id)
            }, onOptionClick = ::optionClicked, shouldPinLocationBeShown = true, viewLifecycleOwner
        )
        recyclerView.adapter = storyAdapter
        // Always true, but lets lint know that as well
        mediaId = arguments?.getString(MEDIA_ID_ARG) ?: return
        clusterManager = ClusterManager<StoryClusterItem>(requireContext(), map)
    }

    private fun bindArguments(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            lastKnownLocation = getLastKnownLocation(it)
            cameraPosition = getCameraPosition(it)
        }
    }

    private fun getLastKnownLocation(savedInstanceState: Bundle) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            savedInstanceState.getParcelable(KEY_LOCATION, Location::class.java)
        } else savedInstanceState.getParcelable(KEY_LOCATION)

    private fun getCameraPosition(savedInstanceState: Bundle) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            savedInstanceState.getParcelable(KEY_CAMERA_POSITION, CameraPosition::class.java)
        } else savedInstanceState.getParcelable(KEY_CAMERA_POSITION)


    override fun onPause() {
        super.onPause()
        if (this::clusterManager.isInitialized) {
            clusterManager.clearItems()

        }
        if (::map.isInitialized) {
            map.clear()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (this::clusterManager.isInitialized) {
            clusterManager.clearItems()

        }
        if (::map.isInitialized) {
            map.clear()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::map.isInitialized) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
    }

    private fun bindObservers() {
        storyViewModel.storyViewState.observe(viewLifecycleOwner, ::handleViewState)
        bottomNavigationViewModel.bottomNavigationViewState.observe(
            viewLifecycleOwner,
            ::handleBottomNavViewState
        )

        bottomNavigationViewModel.playingStory.observe(viewLifecycleOwner) { story ->
            story?.let {
                markers[story.id]?.let { it -> updateMarker(story, it) }
            }
        }

        mapFragmentViewModel.storiesInScreen.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                binding.layoutPlaylist.rvMapPlaylist.adapter = storyAdapter
                storyAdapter.submitList(it)
            }
        }
        mapFragmentViewModel.isListDisplaying.observe(viewLifecycleOwner) {
            if (it) {
                binding.ivToggleIcon.setImageResource(R.drawable.ic_map)
                binding.layoutPlaylist.root.visibility = View.VISIBLE
                val animate = TranslateAnimation(
                    0f,  // fromXDelta
                    0f,  // toXDelta
                    binding.root.height.toFloat(),  // fromYDelta
                    binding.cvToggleMapPlaylist.translationY  // toYDelta
                )
                animate.duration = 500
                animate.fillAfter = true
                binding.layoutPlaylist.root.startAnimation(animate)
            } else {
                binding.ivToggleIcon.setImageResource(R.drawable.ic_map_list)
                val animate = TranslateAnimation(
                    0f,  // fromXDelta
                    0f,  // toXDelta
                    0f,  // fromYDelta
                    binding.root.height.toFloat(), // toYDelta
                )
                animate.duration = 500
                binding.layoutPlaylist.root.startAnimation(animate)
                binding.layoutPlaylist.root.visibility = View.GONE
            }
        }
    }

    private fun setListeners() {
        binding.cvToggleMapPlaylist.setOnClickListener {
            mapFragmentViewModel.displayListForStoriesInScreen()
        }
    }

    private fun handleViewState(viewState: StoryViewState?) {
        when (viewState) {
            is StoryViewState.FetchedAllStories -> addClusteredMarkers(viewState.stories)
            is StoryViewState.AddedBookmark -> showFeedbackSnackBar(getString(R.string.map_fragment_feedback_added_to_bookmarks))
            is StoryViewState.RemovedBookmark -> showFeedbackSnackBar(getString(R.string.map_fragment_feedback_remove_from_bookmarks))
            is StoryViewState.StoryLiked -> showFeedbackSnackBar(getString(R.string.map_fragment_feedback_added_to_favorites))
            is StoryViewState.LikedRemoved -> showFeedbackSnackBar(getString(R.string.map_fragment_feedback_removed_from_favorites))
            is StoryViewState.StoryDownloaded -> showFeedbackSnackBar(getString(R.string.map_fragment_feedback_story_saved_to_my_device))
            is StoryViewState.StoryRemoved -> showFeedbackSnackBar(getString(R.string.map_fragment_feedback_story_removed_from_my_device))
            else -> showFeedbackSnackBar(getString(R.string.map_fragment_feedback_connection_failure)) //TODO(Ideally have error handling for each error)
        }
    }

    private fun handleBottomNavViewState(viewState: BottomNavigationViewState?) {
        when (viewState) {
            is BottomNavigationViewState.OnPlayMediaSuccess -> handlePLayMediaSuccess(viewState.id)
            is BottomNavigationViewState.FetchedStoryToPlay -> {}
            else -> showFeedbackSnackBar(getString(R.string.map_fragment_feedback_connection_failure)) //TODO(Ideally have error handling for each error)
        }
    }

    private fun handlePLayMediaSuccess(id: Int) {
        bottomNavigationViewModel.playMediaId(id)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // googleMap.clear()
        with(googleMap.uiSettings) {
            isMyLocationButtonEnabled = false
            isRotateGesturesEnabled = false
            isMapToolbarEnabled = false
        }

        map = googleMap
        customizeMap()
        updateLocationUI()
        getDeviceLocation()
        val bounds = map.projection.visibleRegion.latLngBounds
        storyViewModel.getStoriesInBounds(bounds)
    }

    private fun updateMapWithStories() {
        map.clear()
        val bounds = map.projection.visibleRegion.latLngBounds
        storyViewModel.getStoriesInBounds(bounds)
    }

    private fun customizeMap() {
        setMapLayout()
        setGoogleLogoNewPosition()
    }

    /*
    private fun highlightClusterItem(storyClusterItem: StoryClusterItem?) {
        highlightedItem?.let {
            unhighlightClusterItem(it)
        }

        highlightedItem = storyClusterItem?.apply {
            val originalBitmap = bitmap
            val marker = this.marker
            if (marker != null && originalBitmap != null) {
                val largerBitmap = getLargerBitmap(originalBitmap)
                val largerBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(largerBitmap)
                try {
                    marker.setIcon(largerBitmapDescriptor)
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun unhighlightClusterItem(storyClusterItem: StoryClusterItem?) {
        val largerBitmap = storyClusterItem?.bitmap
        largerBitmap?.let {
            val originalBitmap = getOriginalBitmap(it)
            val smallerBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(originalBitmap)
            try {
                storyClusterItem.marker?.setIcon(smallerBitmapDescriptor)
            } catch (_: Exception) {
            }
        }
    }
    */

/*
    private fun getLargerBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width * 2.5
        val height = bitmap.height * 2.5F
        return Bitmap.createScaledBitmap(bitmap, width.toInt(), height.toInt(), false)
    }

    private fun getOriginalBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }*/

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
        val googleLogo: View? = binding.maps.findViewWithTag("GoogleWatermark")
        val glLayoutParams = googleLogo?.layoutParams as RelativeLayout.LayoutParams?
        glLayoutParams?.let {
            it.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0)
            it.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0)
            it.addRule(RelativeLayout.ALIGN_PARENT_START, 0)
            it.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
            it.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
            googleLogo?.layoutParams = glLayoutParams
        }
    }

    /**
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
    private fun getDeviceLocation() {
        if (locationPermissionGranted) { //TODO(CHANGE THIS TO FALSE FOR TESTING)
            moveCameraToLastKnownLocation()
        } else {
            moveCamera(DEFAULT_LOCATION_LAT, DEFAULT_LOCATION_LNG)
        }
    }

    private fun moveCameraToLastKnownLocation() {
        try {
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
                        binding.cardLocationIcon.setOnClickListener { moveCamera() }
                    }
                } else {
                    moveCamera(DEFAULT_LOCATION_LAT, DEFAULT_LOCATION_LNG)
                }
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
//                if (!TrackingUtility.hasBackgroundLocationPermission(
//                        requireContext()
//                    ) && !locationBackgroundPermissionGranted
//                ) {
//                    showLocationPermissionAlwaysEnabledSnackBar()
//                }
            } else {
                binding.cardLocationIcon.visibility = View.GONE
                lastKnownLocation = null
                //getLocationPermission()
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
    private fun moveCamera(position: LatLng, zoom: Float? = null) {
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                position, zoom ?: DEFAULT_ZOOM.toFloat()
            ), 100, null
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
     fun tapClusterItem(clusterItem: StoryClusterItem) {
        storyDisplayTimer?.cancelTimer()
        storyDisplayTimer = Timer()
        val story = clusterItem.story
        binding.tvSelectedStoryTitle.text = story.title
        binding.tvSelectedStoryNarrator.text = story.narrator
        playerViewModel.setCurrentStory(story)
        //bottomNavigationViewModel.mediaButtonRes.observe(this) { res ->
        //   if (story.id == bottomNavigationViewModel.playingStory.value?.id) {
        //       binding.btnSelectedStoryPlay.setImageResource(res)
        //   } else {
        //       binding.btnSelectedStoryPlay.setImageResource(R.drawable.ic_player_play)
        //   }
        // }
        binding.btnSelectedStoryPlay.setOnClickListener {
            //     UtilsClass(prefRepository).showPaywallOrProceedWithNormalProcess(requireActivity()) { //TODO(Commented this out cause it get bringing me back to paywall, need to check logic later)
            bottomNavigationViewModel.playMediaId(story.id)
            //}
        }
        binding.btnSelectedStoryInfo.setOnClickListener {
            showStoryOptions(requireContext(),
                binding.root as ViewGroup,
                it,
                story,
                arrayListOf(
                    //   if (story.isBookmarked == true) StoryOption.REMOVE_BOOKMARK else StoryOption.BOOKMARK,
                    //   if (story.isLiked == true) StoryOption.REMOVE_LIKE else StoryOption.LIKE,
                    //   StoryOption.DOWNLOAD,
                    StoryOption.DIRECTIONS,
                    StoryOption.SHARE
                ),
                onOptionClick = ::optionClicked,
                onDismiss = { storyDisplayTimer?.finishTimer() })
        }
        getFloatingComponentHeight()?.let { showFloatingComponent(it) }
        // highlightClusterItem(clusterItem)
//        storyDisplayTimer?.startTimer()
//        storyDisplayTimer?.isActive?.observe(this) {
//            if (it == false) {
//                hideSelectedStoryComponent()
//            }
//        }
    }

    /**
     * Adds markers to the map with clustering support. As response to the viewModel liveData.
     */
    private fun addClusteredMarkers(stories: List<Story>) {
        if (!::map.isInitialized) return
        map.clear()
        clusterManager = ClusterManager<StoryClusterItem>(requireContext(), map)
        clusterManager.setAnimation(true)
        val clusterRenderer = StoryClusterRenderer(requireContext(), map, clusterManager, this)
        clusterManager.renderer = clusterRenderer

        val center = map.cameraPosition.target
        val nearestStory = stories.findNearestToCoordinates(center)
        nearestStory?.let {
            clusterRenderer.nearestStory = markers[it.id]
        }

        val storiesMap = stories.associate { it.id to StoryClusterItem(it) }
        markers.putAll(storiesMap)

        clusterManager.addItems(markers.values)
        clusterManager.cluster()
        clusterManager.setOnClusterItemClickListener { storyItem ->
            tapClusterItem(storyItem)
            true //TODO(false)
        }
        clusterManager.setOnClusterClickListener {
            moveCamera(it.position, map.cameraPosition.zoom + 2)
            true
        }

        map.setOnCameraIdleListener {
            updateMapWithStories()
        }

        //TODO(MOVE to a view_state return from viewModel when map updates)
        // highLightNearestStory(clusterRenderer, stories)
    }

    //TODO(MOVE to a view_state return from viewModel when map updates)
    /* private fun highLightNearestStory(clusterRenderer: StoryClusterRenderer, stories: List<Story>) {
         val center = map.cameraPosition.target
         val nearestStory = stories.findNearestToCoordinates(center)
         if (nearestStory != null) {
             markers[nearestStory.id]?.let { storyClusterItem ->
                 highlightClusterItem(storyClusterItem)
             }
         }
     }*/

    private fun handleMapCameraIdle() {
        cameraPosition = map.cameraPosition
        // Call clusterManager.onCameraIdle() when the camera stops moving so that re-clustering
        // can be performed when the camera stops moving
        clusterManager.onCameraIdle()
        updateMapBounds(map)

        mapFragmentViewModel.fetchRecordsOfStories()
    }

    private fun updateMarker(story: Story, item: StoryClusterItem?) {
        item?.updateStory(story)
        clusterManager.updateItem(item)
        clusterManager.cluster()
    }

    private fun updateMapBounds(map: GoogleMap) {
        val bounds = map.projection.visibleRegion.latLngBounds
        mapFragmentViewModel.changeLatLngBounds(bounds)
    }

    private fun hideSelectedStoryComponent() {
        binding.floatingSelectedStory.apply {
            animate().alpha(0.0f).withEndAction {
                visibility = View.GONE
            }
        }
    }

    private fun optionClicked(option: StoryOption, story: Story) {
        activity?.let { verifiedActivity ->
            context?.let { verifiedContext ->
                onOptionClicked(
                    option, story, storyViewModel, verifiedActivity, verifiedContext
                )
            }
        }
    }

    private fun showFloatingComponent(totalHeight: Float) {
        binding.floatingSelectedStory.apply {
            if (visibility != View.VISIBLE) {
                translationY = totalHeight
                visibility = View.VISIBLE
                animate().alpha(1.0f)
            }
        }
    }


    private fun cancelJob() {
        /* if (activityLayout.contains(snackBarView)) {
             activityLayout.removeView(snackBarView)
         }*/
        feedbackJob?.cancel()
    }


    companion object {
        private val TAG = MapFragment::class.simpleName
        private const val DEFAULT_ZOOM = 15

        // Keys for storing activity state
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
        private const val MEDIA_ID_ARG = "fragment.MediaItemFragment.MEDIA_ID"
    }

}

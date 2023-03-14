package com.autio.android_app.ui.stories

import android.Manifest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.get
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.autio.android_app.R
import com.autio.android_app.databinding.ActivityBottomNavigationBinding
import com.autio.android_app.ui.network_monitor.NetworkManager
import com.autio.android_app.ui.stories.fragments.MapFragment
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.ui.stories.view_model.BottomNavigationViewModel
import com.autio.android_app.ui.stories.view_states.BottomNavigationViewState
import com.autio.android_app.ui.subscribe.view_model.PurchaseViewModel
import com.autio.android_app.util.Constants
import com.autio.android_app.util.TrackingUtility
import com.google.android.gms.cast.framework.CastContext
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BottomNavigation : AppCompatActivity() {

    @Inject
    lateinit var networkManager: NetworkManager

    private val bottomNavigationViewModel: BottomNavigationViewModel by viewModels()
    private val purchaseViewModel: PurchaseViewModel by viewModels()

    private lateinit var binding: ActivityBottomNavigationBinding
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var snackBarView: View

    private var castContext: CastContext? = null
    private var isNetworkAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_bottom_navigation)
        bindNetworkManager()
        bindObservables()

        snackBarView = layoutInflater.inflate(
            R.layout.feedback_snackbar, binding.root as ViewGroup, false
        )

        setListeners()
        initView()
    }

    private fun bindNetworkManager() {
        lifecycleScope.launchWhenResumed {
            networkManager.networkStatus.collect {
                isNetworkAvailable = it.isConnected
                updateConnectionUI(isNetworkAvailable)
            }
        }
    }

    private fun initView() {
        // castContext = CastContext.getSharedInstance(this) //TODO(Should we cast)
        volumeControlStream = AudioManager.STREAM_MUSIC
        purchaseViewModel.getUserInfo()
        updateSnackBarMessageDisplay()
        updateAvailableStoriesUI(bottomNavigationViewModel.initialRemainingStories)
        showPlayerComponent()
    }

    private fun bindObservables() {
        bottomNavigationViewModel.bottomNavigationViewState.observe(this, ::handleViewState)

        bottomNavigationViewModel.mediaButtonRes.observe(this) { res ->
            binding.btnFloatingPlayerPlay.setImageResource(res)
        }
        bottomNavigationViewModel.playingStory.observe(this) {
            bottomNavigationViewModel.postPlay()
        }
        bottomNavigationViewModel.remainingStoriesLiveData.observe(this) {
            updateAvailableStoriesUI(it)
        }
        purchaseViewModel.customerInfo.observe(this) {
            it?.let {
                binding.storiesFreePlansBanner.visibility =
                    if (it.entitlements[Constants.REVENUE_CAT_ENTITLEMENT]?.isActive == true) GONE
                    else VISIBLE
            }
        }
    }

    private fun handleViewState(viewState: BottomNavigationViewState?) {
        when (viewState) {
            is BottomNavigationViewState.FetchedStoryToPlay -> handleSuccessViewState(viewState.story)
            else -> {}
        }
    }

    private fun handleSuccessViewState(mediaItem: Story) {
        updatePlayer(mediaItem)
    }

    override fun onStart() {
        super.onStart()
        updateConnectionUI(isNetworkAvailable)
    }

    override fun onStop() {
        super.onStop()
        bottomNavigationViewModel.clearRoomCache()
    }

    override fun onResume() {
        super.onResume()
        bottomNavigationViewModel.onCreate()
    }

    fun showUpButton() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun hideUpButton() {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun setListeners() {
        setupNavigationListener()
        binding.storiesFreePlansBanner.setOnClickListener {
            showPayWall()
        }
        binding.btnFloatingPlayerPlay.setOnClickListener {
            bottomNavigationViewModel.playingStory.value?.let {
                bottomNavigationViewModel.playMediaId(it.id)
            }
        }
    }

    private fun setupNavigationListener() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_container) as NavHostFragment
        navController = navHostFragment.navController
        setupWithNavController(binding.bottomNavigationView, navController)
        navController.addOnDestinationChangedListener { controller, destination, _ ->
            val subscribeFragmentDestination = controller.graph[R.id.subscribeFragment].label
            if (destination.label ==  controller.graph[R.id.player].label ) {
                hidePlayerComponent() //TODO(had to disable this BECAUSE WE WHERE LOSING IT ON THE OTHER FRAGMENTS)
            }
            else if (destination == subscribeFragmentDestination){
                showUiElements(false)}
                else  {
                    showUiElements(true)
                }
        }
    }


    fun showPayWall() {
        navController.navigate(R.id.subscribeFragment)
    }

    private fun showUiElements(isVisible: Boolean) {
        binding.persistentPlayer.isVisible = isVisible
        binding.storiesFreePlansBanner.isVisible = isVisible
        binding.rlAllowNotifications.isVisible = isVisible
        binding.bottomNavigationView.isVisible = isVisible
    }
    private fun updateSnackBarMessageDisplay() {
        with(binding) {
            if (!TrackingUtility.hasCoreLocationPermissions(this@BottomNavigation)) {
                rlAllowLocationAccess.visibility = VISIBLE
                rlAllowLocationAccess.setOnClickListener {
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                }
                ivCloseLocationMessage.setOnClickListener {
                    rlAllowLocationAccess.visibility = GONE
                }
            }
            if (Build.VERSION.SDK_INT >= 33 && !TrackingUtility.hasNotificationPermissions(
                    this@BottomNavigation
                )
            ) {
                rlAllowNotifications.visibility = VISIBLE
                rlAllowNotifications.setOnClickListener {
                    requestPermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                }
                ivCloseNotificationsMessage.setOnClickListener {
                    rlAllowNotifications.visibility = GONE
                }
            }
        }
    }

    private fun updateAvailableStoriesUI(remainingStories: Int) {
        with(binding) {


            val tickMarks = arrayOf(
                tickMark1, tickMark2, tickMark3, tickMark4, tickMark5
            )


          //  if (remainingStories < 0 && purchaseViewModel.customerInfo.value?.entitlements?.get(Constants.REVENUE_CAT_ENTITLEMENT)?.isActive == true) {//TODO(User with an active plan ironically have -1 remainingStories, somewher here we should check isUserSubcribed [From RevenueCAT]))
            if (remainingStories < 0){
                llTickMarks.visibility = GONE
             //   showPayWall() //TODO(UNCOMMENT)
            } else {
                for ((i, tickMark) in tickMarks.withIndex()) {
                    if (remainingStories >= i + 1) {
                        tickMark.setBackgroundColor(
                            ResourcesCompat.getColor(
                                resources, R.color.contrasting_text, null
                            )
                        )
                    } else {
                        tickMark.setBackgroundColor(
                            ResourcesCompat.getColor(
                                resources, R.color.autio_blue_20, null
                            )
                        )
                    }
                }
                llTickMarks.visibility = VISIBLE
            }
        }
    }

    private fun updateConnectionUI(
        connectionAvailable: Boolean
    ) {
        binding.rlNoInternetConnection.visibility =
            if (!connectionAvailable) VISIBLE else GONE
    }

    private fun updatePlayer(story: Story?) {
        with(binding) {
            tvFloatingPlayerTitle.text = story?.title ?: resources.getText(
                R.string.no_story_loaded
            )
            tvFloatingPlayerNarrator.text = story?.narrator
            tvFloatingPlayerNarrator.visibility =
                if (story?.narrator?.isNotEmpty() == true) VISIBLE else GONE
        }
    }

    private fun hidePlayerComponent() {
       //binding.persistentPlayer.animate().alpha(0.0f)
       //    .translationY(binding.persistentPlayer.height.toFloat())
       //    .withEndAction {
              //  binding.mainContainer.requestLayout()
               binding.persistentPlayer.visibility = GONE
           // }
    }

    private fun showPlayerComponent() {

        binding.persistentPlayer.visibility = VISIBLE
        binding.persistentPlayer.animate().alpha(1.0f)
            //TODO(FIX ANIMATION )
             .translationYBy(-binding.persistentPlayer.height.toFloat())
                    .withEndAction {
                        binding.mainContainer.requestLayout()
                    }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionResults ->
        if (permissionResults.keys.contains(Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (permissionResults[Manifest.permission.ACCESS_FINE_LOCATION]!!) {
                binding.rlAllowLocationAccess.visibility = GONE
                val mapFragment =
                    navHostFragment.childFragmentManager.fragments.first() as? MapFragment
                if (mapFragment != null) {
                    mapFragment.locationPermissionGranted = true
                    mapFragment.updateLocationUI()
                }
            }
        }
        if (permissionResults.keys.contains(Manifest.permission.POST_NOTIFICATIONS)) {
            if (permissionResults[Manifest.permission.POST_NOTIFICATIONS]!!) {
                binding.rlAllowNotifications.visibility = GONE
            }
        }
    }

    private lateinit var snackBarJob: Job
    fun showFeedbackSnackBar(feedback: String) {
        if (::snackBarJob.isInitialized && snackBarJob.isActive) {
            snackBarJob.cancel()
        }
        snackBarView.alpha = 1F
        snackBarView.findViewById<TextView>(R.id.snack_bar_text).text = feedback
        binding.activityLayout.removeView(snackBarView)
        binding.activityLayout.addView(snackBarView)
        snackBarJob = lifecycleScope.launch {
            delay(1500)
            snackBarView.animate().alpha(0F).withEndAction {
                binding.activityLayout.removeView(snackBarView)
            }
        }
    }

    fun getFloatingComponentHeight(): Float {
        val player = binding.root.findViewById<LinearLayout>(R.id.persistentPlayer)
        val displayingMessage = binding.root.findViewById<FrameLayout>(R.id.flImportantMessage)
        val totalHeight = -player.height - displayingMessage.height - 24F
        return totalHeight
    }
}

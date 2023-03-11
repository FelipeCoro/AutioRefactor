package com.autio.android_app.ui.stories

import android.Manifest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
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
import com.autio.android_app.ui.subscribe.view_model.PurchaseViewModel
import com.autio.android_app.util.Constants
import com.autio.android_app.util.TrackingUtility
import com.google.android.gms.cast.framework.CastContext
import dagger.hilt.android.AndroidEntryPoint
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

    private var castContext: CastContext? = null
    private var isNetworkAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        purchaseViewModel.getUserInfo()

        bindObservables()

       // castContext = CastContext.getSharedInstance(this) //TODO(Should we cast)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_bottom_navigation)

        updateSnackBarMessageDisplay()

        updateAvailableStoriesUI(
            bottomNavigationViewModel.initialRemainingStories
        )

        setListeners()
        volumeControlStream = AudioManager.STREAM_MUSIC

        bottomNavigationViewModel.mediaButtonRes.observe(this) { res ->
            binding.btnFloatingPlayerPlay.setImageResource(res)
        }
    }

    private fun bindObservables() {
            lifecycleScope.launchWhenResumed {
                networkManager.networkStatus.collect {
                    isNetworkAvailable = it.isConnected
                    updateConnectionUI(isNetworkAvailable)
                }
            }

        bottomNavigationViewModel.playingStory.observe(this) { mediaItem ->
            updatePlayer(mediaItem)
        }
        bottomNavigationViewModel.remainingStoriesLiveData.observe(this) {
            updateAvailableStoriesUI(
                it
            )
        }
        purchaseViewModel.customerInfo.observe(
            this
        ) {
            it?.let {
                binding.rlSeePlans.visibility =
                    if (it.entitlements[Constants.REVENUE_CAT_ENTITLEMENT]?.isActive == true) GONE
                    else VISIBLE
            }
        }
    }

    override fun onStart() {
        super.onStart()
        updateConnectionUI(
            isNetworkAvailable
        )
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
        supportActionBar?.setDisplayHomeAsUpEnabled(
            true
        )
    }

    fun hideUpButton() {
        supportActionBar?.setDisplayHomeAsUpEnabled(
            false
        )
    }

    private fun setListeners() {
        navHostFragment = supportFragmentManager.findFragmentById(
            R.id.main_container
        ) as NavHostFragment
        navController = navHostFragment.navController
        setupWithNavController(
            binding.bottomNavigationView, navController
        )
        navController.addOnDestinationChangedListener { controller, destination, _ ->
            if (controller.graph[R.id.player] == destination) {
                hidePlayerComponent()
            } else if (binding.persistentPlayer.visibility != VISIBLE) {
                showPlayerComponent()
            }
        }
        binding.rlSeePlans.setOnClickListener {
            showPayWall()
        }
        binding.btnFloatingPlayerPlay.setOnClickListener {
            bottomNavigationViewModel.playingStory.value?.let {
                bottomNavigationViewModel.playMediaId(it.id)
            }
        }
    }

    fun showPayWall() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_container) as NavHostFragment
        val nav = navHostFragment.navController
        nav.navigate(R.id.action_global_subscribeFragment)
    }

    private fun updateSnackBarMessageDisplay() {
        with(binding) {
            if (!TrackingUtility.hasCoreLocationPermissions(
                    this@BottomNavigation
                )
            ) {
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
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    )
                }
                ivCloseNotificationsMessage.setOnClickListener {
                    rlAllowNotifications.visibility = GONE
                }
            }
        }
    }

    private fun updateAvailableStoriesUI(
        remainingStories: Int
    ) {
        with(
            binding
        ) {
            val tickMarks = arrayOf(
                tickMark1, tickMark2, tickMark3, tickMark4, tickMark5
            )

            if (remainingStories < 0) {
                llTickMarks.visibility = GONE
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
        binding.persistentPlayer.animate().alpha(0.0f)
            .translationY(binding.persistentPlayer.height.toFloat())
            .withEndAction {
                binding.mainContainer.requestLayout()
                binding.persistentPlayer.visibility = GONE
            }
    }

    private fun showPlayerComponent() {
        binding.persistentPlayer.visibility = VISIBLE
        binding.persistentPlayer.animate().alpha(1.0f)
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
}

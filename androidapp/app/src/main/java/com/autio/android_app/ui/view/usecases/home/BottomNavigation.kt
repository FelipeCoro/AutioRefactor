package com.autio.android_app.ui.view.usecases.home

import android.Manifest
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.get
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.autio.android_app.R
import com.autio.android_app.data.entities.story.Story
import com.autio.android_app.databinding.ActivityBottomNavigationBinding
import com.autio.android_app.ui.view.usecases.home.fragment.MapFragment
import com.autio.android_app.ui.view.usecases.subscribe.SubscribeActivity
import com.autio.android_app.ui.viewmodel.BottomNavigationViewModel
import com.autio.android_app.ui.viewmodel.MyState
import com.autio.android_app.ui.viewmodel.NetworkStatusViewModel
import com.autio.android_app.ui.viewmodel.PurchaseViewModel
import com.autio.android_app.util.Constants
import com.autio.android_app.util.InjectorUtils
import com.autio.android_app.util.TrackingUtility
import com.google.android.gms.cast.framework.CastContext

class BottomNavigation :
    AppCompatActivity() {

    private val bottomNavigationViewModel by viewModels<BottomNavigationViewModel> {
        InjectorUtils.provideBottomNavigationViewModel(
            this
        )
    }

    private val purchaseViewModel by viewModels<PurchaseViewModel> {
        InjectorUtils.providePurchaseViewModel(
            this
        )
    }

    private val networkViewModel by viewModels<NetworkStatusViewModel> {
        InjectorUtils.provideNetworkStatusViewModel(
            this
        )
    }
    private var castContext: CastContext? =
        null

    private lateinit var binding: ActivityBottomNavigationBinding
    private lateinit var navController: NavController

    private lateinit var navHostFragment: NavHostFragment

    private var connected =
        false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        purchaseViewModel.getUserInfo()

        networkViewModel.state.observe(
            this
        ) { state ->
            connected =
                when (state) {
                    MyState.Fetched -> true
                    MyState.Error -> false
                }
            updateConnectionUI(
                connected
            )
        }

        castContext =
            CastContext.getSharedInstance(
                this
            )

        binding =
            ActivityBottomNavigationBinding.inflate(
                layoutInflater
            )
        setContentView(
            binding.root
        )

        updateSnackBarMessageDisplay()

        updateAvailableStoriesUI(
            bottomNavigationViewModel.initialRemainingStories
        )

        purchaseViewModel.customerInfo.observe(
            this
        ) {
            it?.let {
                binding.rlSeePlans.visibility =
                    if (it.entitlements[Constants.REVENUE_CAT_ENTITLEMENT]?.isActive == true)
                        GONE
                    else VISIBLE
            }
        }

        bottomNavigationViewModel.remainingStoriesLiveData.observe(
            this
        ) {
            updateAvailableStoriesUI(
                it
            )
        }

        setListeners()

        volumeControlStream =
            AudioManager.STREAM_MUSIC

        bottomNavigationViewModel.playingStory.observe(
            this
        ) { mediaItem ->
            updatePlayer(
                mediaItem
            )
        }
        bottomNavigationViewModel.mediaButtonRes.observe(
            this
        ) { res ->
            binding.btnFloatingPlayerPlay.setImageResource(
                res
            )
        }
    }

    override fun onStart() {
        super.onStart()
        updateConnectionUI(
            connected
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
        navHostFragment =
            supportFragmentManager.findFragmentById(
                R.id.mainContainer
            ) as NavHostFragment
        navController =
            navHostFragment.navController
        setupWithNavController(
            binding.bottomNavigationView,
            navController
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
                bottomNavigationViewModel.playMediaId(
                    it.id
                )
            }
        }
    }

    fun showPayWall() {
        val subscribeIntent =
            Intent(
                this,
                SubscribeActivity::class.java
            )
        subscribeIntent.putExtra(
            "ACTIVITY_NAME",
            BottomNavigation::class.simpleName
        )
        startActivity(
            subscribeIntent
        )
    }

    private fun updateSnackBarMessageDisplay() {
        with(
            binding
        ) {
            if (!TrackingUtility.hasCoreLocationPermissions(
                    this@BottomNavigation
                )
            ) {
                rlAllowLocationAccess.visibility =
                    VISIBLE
                rlAllowLocationAccess.setOnClickListener {
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    )
                }
                ivCloseLocationMessage.setOnClickListener {
                    rlAllowLocationAccess.visibility =
                        GONE
                }
            }
            if (Build.VERSION.SDK_INT >= 33 && !TrackingUtility.hasNotificationPermissions(
                    this@BottomNavigation
                )
            ) {
                rlAllowNotifications.visibility =
                    VISIBLE
                rlAllowNotifications.setOnClickListener {
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    )
                }
                ivCloseNotificationsMessage.setOnClickListener {
                    rlAllowNotifications.visibility =
                        GONE
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
            val tickMarks =
                arrayOf(
                    tickMark1,
                    tickMark2,
                    tickMark3,
                    tickMark4,
                    tickMark5
                )

            if (remainingStories < 0) {
                llTickMarks.visibility =
                    GONE
            } else {
                for ((i, tickMark) in tickMarks.withIndex()) {
                    if (remainingStories >= i + 1) {
                        tickMark.setBackgroundColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.contrasting_text,
                                null
                            )
                        )
                    } else {
                        tickMark.setBackgroundColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.autio_blue_20,
                                null
                            )
                        )
                    }
                }
                llTickMarks.visibility =
                    VISIBLE
            }
        }
    }

    private fun updateConnectionUI(
        connectionAvailable: Boolean
    ) {
        binding.rlNoInternetConnection.visibility =
            if (!connectionAvailable)
                VISIBLE
            else
                GONE
    }

    private fun updatePlayer(
        story: Story?
    ) {
        with(
            binding
        ) {
            tvFloatingPlayerTitle.text =
                story?.title
                    ?: resources.getText(
                        R.string.no_story_loaded
                    )
            tvFloatingPlayerNarrator.text =
                story?.narrator
            tvFloatingPlayerNarrator.visibility =
                if (story?.narrator?.isNotEmpty() == true) VISIBLE else GONE
        }
    }

    private fun hidePlayerComponent() {
        binding.persistentPlayer.animate()
            .alpha(
                0.0f
            )
            .translationY(
                binding.persistentPlayer.height.toFloat()
            )
            .withEndAction {
                binding.mainContainer.requestLayout()
                binding.persistentPlayer.visibility =
                    GONE
            }
    }

    private fun showPlayerComponent() {
        binding.persistentPlayer.visibility =
            VISIBLE
        binding.persistentPlayer.animate()
            .alpha(
                1.0f
            )
            .translationYBy(
                -binding.persistentPlayer.height.toFloat()
            )
            .withEndAction {
                binding.mainContainer.requestLayout()
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
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                if (permissionResults[Manifest.permission.ACCESS_FINE_LOCATION]!!) {
                    binding.rlAllowLocationAccess.visibility =
                        GONE
                    val mapFragment =
                        navHostFragment.childFragmentManager.fragments.first() as? MapFragment
                    if (mapFragment != null) {
                        mapFragment.locationPermissionGranted =
                            true
                        mapFragment.updateLocationUI()
                    }
                }
            }
            if (permissionResults.keys.contains(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                if (permissionResults[Manifest.permission.POST_NOTIFICATIONS]!!) {
                    binding.rlAllowNotifications.visibility =
                        GONE
                }
            }
        }
}

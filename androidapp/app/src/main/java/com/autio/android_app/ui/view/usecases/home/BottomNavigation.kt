package com.autio.android_app.ui.view.usecases.home

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.get
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.autio.android_app.R
import com.autio.android_app.data.model.story.Story
import com.autio.android_app.databinding.ActivityBottomNavigationBinding
import com.autio.android_app.ui.view.usecases.subscribe.SubscribeActivity
import com.autio.android_app.ui.viewmodel.BottomNavigationViewModel
import com.autio.android_app.ui.viewmodel.MyState
import com.autio.android_app.ui.viewmodel.NetworkStatusViewModel
import com.autio.android_app.util.InjectorUtils
import com.google.android.gms.cast.framework.CastContext

class BottomNavigation :
    AppCompatActivity() {

    private val bottomNavigationViewModel by viewModels<BottomNavigationViewModel> {
        InjectorUtils.provideBottomNavigationViewModel(
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

    private var connected =
        false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

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

        updateAvailableStoriesUI(
            bottomNavigationViewModel.initialRemainingStories
        )
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
        val navHostFragment =
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
            } else if (binding.floatingPersistentPlayer.visibility != View.VISIBLE) {
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
        startActivity(
            subscribeIntent
        )
    }

    private fun updateAvailableStoriesUI(
        remainingStories: Int
    ) {
        with(
            binding
        ) {
            val tickMarks = arrayOf(
                tickMark1,
                tickMark2,
                tickMark3,
                tickMark4,
                tickMark5
            )

            if (remainingStories < 0) {
                llTickMarks.visibility =
                    View.GONE
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
                    View.VISIBLE
            }
        }
    }

    private fun updateConnectionUI(
        connectionAvailable: Boolean
    ) {
        if (!connectionAvailable) {
            binding.tvFeedbackMessage.text =
                resources.getString(
                    R.string.snack_bar_no_connection
                )
            binding.rlStatusFeedback.apply {
                setBackgroundColor(
                    ContextCompat.getColor(
                        this@BottomNavigation,
                        R.color.autio_blue_20
                    )
                )
                visibility =
                    View.VISIBLE
            }
        } else {
            binding.rlStatusFeedback.visibility =
                View.GONE
        }
    }

    private fun updatePlayer(
        story: Story?
    ) {
        binding.tvFloatingPlayerTitle.text =
            story?.title
        binding.tvFloatingPlayerNarrator.text =
            story?.narrator
    }

    private fun hidePlayerComponent() {
        binding.floatingPersistentPlayer.animate()
            .alpha(
                0.0f
            )
            .translationY(
                binding.floatingPersistentPlayer.height.toFloat()
            )
            .withEndAction {
                binding.mainContainer.requestLayout()
                binding.floatingPersistentPlayer.visibility =
                    View.GONE
            }
    }

    private fun showPlayerComponent() {
        binding.floatingPersistentPlayer.visibility =
            View.VISIBLE
        binding.floatingPersistentPlayer.animate()
            .alpha(
                1.0f
            )
            .translationYBy(
                -binding.floatingPersistentPlayer.height.toFloat()
            )
            .withEndAction {
                binding.mainContainer.requestLayout()
            }
    }
}
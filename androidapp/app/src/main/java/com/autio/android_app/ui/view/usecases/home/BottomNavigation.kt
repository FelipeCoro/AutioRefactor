package com.autio.android_app.ui.view.usecases.home

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.get
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.autio.android_app.R
import com.autio.android_app.data.model.story.Story
import com.autio.android_app.data.repository.ApiService
import com.autio.android_app.data.repository.PrefRepository
import com.autio.android_app.databinding.ActivityBottomNavigationBinding
import com.autio.android_app.player.PlayerService
import com.autio.android_app.ui.view.usecases.home.fragment.map.MapFragment
import com.autio.android_app.ui.view.usecases.subscribe.SubscribeActivity
import com.autio.android_app.ui.viewmodel.BottomNavigationViewModel
import com.autio.android_app.ui.viewmodel.StoryViewModel
import com.autio.android_app.util.InjectorUtils
import com.google.android.gms.cast.framework.CastContext
import kotlinx.coroutines.launch

class BottomNavigation :
    AppCompatActivity() {

    private val viewModel by viewModels<BottomNavigationViewModel> {
        InjectorUtils.provideBottomNavigationViewModel(
            this
        )
    }
    private var castContext: CastContext? =
        null

    private val prefRepository by lazy {
        PrefRepository(
            this
        )
    }

    private lateinit var binding: ActivityBottomNavigationBinding
    private lateinit var navController: NavController

    private lateinit var storyViewModel: StoryViewModel

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

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

        setListeners()

        volumeControlStream =
            AudioManager.STREAM_MUSIC

        /**
         * Observe [BottomNavigationViewModel.navigateToFragment] for [Event]s that request a
         * fragment swap
         */
        viewModel.navigateToFragment.observe(
            this
        ) {
            Log.d(
                TAG,
                "navigateToFragment: ${it.peekContent()}"
            )
            it.getContentIfNotHandled()
                ?.let { fragmentRequest ->
                    val transaction =
                        supportFragmentManager.beginTransaction()
                    transaction.replace(
                        R.id.mainContainer,
                        fragmentRequest.fragment,
                        fragmentRequest.tag
                    )
                    if (fragmentRequest.backStack) transaction.addToBackStack(
                        null
                    )
                    transaction.commit()
                }
        }

        viewModel.currentStory.observe(
            this
        ) { mediaItem ->
            updatePlayer(
                mediaItem
            )
        }
        viewModel.mediaButtonRes.observe(
            this
        ) { res ->
            binding.btnFloatingPlayerPlay.setImageResource(
                res
            )
        }

        /**
         * Observe changes to the [BottomNavigationViewModel.rootMediaId]
         * When app starts, and UI connect to [PlayerService], this will be updated
         * and app will show the initial map with media items
         */
//        viewModel.rootMediaId.observe(this) {
//            rootMediaId ->
//                rootMediaId?.let {
//                    navigateToMediaItem(it)
//                }
//        }

//        viewModel.navigateToMediaItem.observe(this) {
//            it?.getContentIfNotHandled()?.let { mediaId ->
//                navigateToMediaItem(mediaId)
//            }
//        }

        storyViewModel =
            ViewModelProvider(
                this
            )[StoryViewModel::class.java]

        lifecycleScope.launch {
            requestStories()
        }
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
            Log.d(
                TAG,
                "destinationAdd: $destination"
            )
            if (controller.graph[R.id.player] == destination) {
                hidePlayerComponent()
            } else if (binding.floatingPersistentPlayer.visibility != View.VISIBLE) {
                showPlayerComponent()
            }
        }
        binding.relativeLayoutSeePlans.setOnClickListener {
            val subscribeIntent =
                Intent(
                    this,
                    SubscribeActivity::class.java
                )
            startActivity(
                subscribeIntent
            )
        }
        binding.btnFloatingPlayerPlay.setOnClickListener {
            viewModel.currentStory.value?.let {
                viewModel.playMediaId(
                    it.id
                )
            }
        }
    }

    private fun navigateToMediaItem(
        mediaId: String
    ) {
        Log.d(
            TAG,
            "navigate to $mediaId"
        )
        var fragment: MapFragment? =
            getBrowseFragment(
                mediaId
            )
        if (fragment == null) {
            fragment =
                MapFragment.newInstance(
                    mediaId
                )
            // If this is not the top level root, we add it to the fragment
            // back stack, so actionbar toggle and back will work appropriately
            viewModel.showFragment(
                fragment,
                !isRootId(
                    mediaId
                ),
                mediaId
            )
        }
    }

    private fun isRootId(
        mediaId: String
    ) =
        mediaId == viewModel.rootMediaId.value

    private fun getBrowseFragment(
        mediaId: String
    ): MapFragment? {
        return supportFragmentManager.findFragmentByTag(
            mediaId
        ) as? MapFragment
    }

    private fun updatePlayer(
        story: Story?
    ) {
        binding.tvFloatingPlayerTitle.text =
            story?.title
        binding.tvFloatingPlayerNarrator.text =
            story?.narrator
    }

    private fun requestStories() {
        // TODO (Marshysaurus): Remove dummy ids
        var dummyIds =
            ArrayList(
                (1..100).map { it })
        repeat (20) {
            ApiService().getStoriesByIds(
                getUserId(),
                getApiToken(),
                dummyIds.map { it.toString() }.toList()
            ) {
                if (it != null) {
                    storyViewModel.addStories(
                        it
                    )
                }
            }
            dummyIds = ArrayList(dummyIds.map { it + 100 })
        }
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

    private fun getUserId(): Int =
        prefRepository.userId

    private fun getApiToken(): String =
        "Bearer " + prefRepository.userApiToken

    companion object {
        val TAG =
            BottomNavigation::class.simpleName
    }
}
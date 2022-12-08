package com.autio.android_app.ui.view.usecases.home

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.get
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.autio.android_app.R
import com.autio.android_app.data.model.story.Story
import com.autio.android_app.data.repository.ApiService
import com.autio.android_app.data.repository.PrefRepository
import com.autio.android_app.databinding.ActivityBottomNavigationBinding
import com.autio.android_app.player.MediaBrowserAdapter
import com.autio.android_app.player.StoryLibrary
import com.autio.android_app.ui.view.usecases.subscribe.SubscribeActivity
import com.autio.android_app.ui.viewmodel.StoryViewModel

class BottomNavigation :
    AppCompatActivity() {

    private val prefRepository by lazy {
        PrefRepository(
            this
        )
    }

    private lateinit var binding: ActivityBottomNavigationBinding
    private lateinit var navController: NavController

    private lateinit var storyViewModel: StoryViewModel

    private var currentState =
        STATE_PAUSED

    private lateinit var stories: LiveData<Array<Story>>

    lateinit var mediaBrowserAdapter: MediaBrowserAdapter

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )
        binding =
            ActivityBottomNavigationBinding.inflate(
                layoutInflater
            )
        setContentView(
            binding.root
        )
        mediaBrowserAdapter =
            MediaBrowserAdapter(
                this
            )
        setListeners()

        storyViewModel =
            ViewModelProvider(
                this
            )[StoryViewModel::class.java]

        requestStories()
    }

    override fun onStart() {
        super.onStart()
        mediaBrowserAdapter.onStart()
    }

    override fun onStop() {
        super.onStop()
        mediaBrowserAdapter.onStop()
    }

    private fun setListeners() {
        mediaBrowserAdapter.addListener(
            object :
                MediaBrowserAdapter.MediaBrowserChangeListener() {
                override fun onConnected(
                    mediaController: MediaControllerCompat
                ) {
                    Log.d(
                        TAG,
                        "onConnected: "
                    )
                }

                override fun onPlaybackStateChanged(
                    playbackState: PlaybackStateCompat?
                ) {
                    Log.d(
                        TAG,
                        "onPlaybackStateChanged: $playbackState"
                    )
                    currentState =
                        if (playbackState?.state == STATE_PLAYING) {
                            binding.btnFloatingPlayerPlay.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.ic_player_pause,
                                    null
                                )
                            )
                            STATE_PLAYING
                        } else {
                            binding.btnFloatingPlayerPlay.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                    resources,
                                    R.drawable.ic_player_play,
                                    null
                                )
                            )
                            STATE_PAUSED
                        }
                }

                override fun onMetadataChanged(
                    mediaMetadata: MediaMetadataCompat?
                ) {
                    if (mediaMetadata == null) {
                        Log.d(
                            TAG,
                            "onMetadataChanged: Metadata is null!"
                        )
                        return
                    }
                    binding.tvFloatingPlayerTitle.text =
                        mediaMetadata.getString(
                            MediaMetadataCompat.METADATA_KEY_TITLE
                        )
                    binding.tvFloatingPlayerNarrator.text =
                        mediaMetadata.getString(
                            MediaMetadataCompat.METADATA_KEY_ARTIST
                        )
                }
            })
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
            if (currentState == STATE_PLAYING) {
                mediaBrowserAdapter.getTransportControls()
                    .pause()
            } else {
                Log.d(
                    TAG,
                    "Playing now..."
                )
                mediaBrowserAdapter.getTransportControls()
                    .play()
            }
        }

        stories =
            ViewModelProvider(
                this
            )[StoryViewModel::class.java].getStoriesByIds(
                arrayOf(
                    "3"
                )
            )
    }

    private fun requestStories() {
        // TODO (Marshysaurus): Remove dummy ids
        val dummyIds =
            ArrayList(
                (1..100).map { t -> t.toString() })
        ApiService().getStoriesByIds(
            getUserId(),
            getApiToken(),
            dummyIds as ArrayList<String>
        ) {
            if (it != null) {
                storyViewModel.addStories(
                    it
                )
                StoryLibrary.addStoriesToLibrary(it)
            }
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

        const val SAVED_MEDIA_ID = "com.autio.android_app.MEDIA_ID"
        const val SAVED_MEDIA_DESCRIPTION = "com.autio.android_app.CURRENT_MEDIA_DESCRIPTION"
        const val FRAGMENT_PLAYER_TAG = "autio_player_container"
        const val FRAGMENT_LIST_TAG = "autio_list_container"

        private const val STATE_PAUSED =
            PlaybackStateCompat.STATE_PAUSED
        private const val STATE_PLAYING =
            PlaybackStateCompat.STATE_PLAYING
        private const val STATE_BUFFERING =
            PlaybackStateCompat.STATE_BUFFERING
        private const val STATE_CONNECTING =
            PlaybackStateCompat.STATE_CONNECTING
        private const val STATE_ERROR =
            PlaybackStateCompat.STATE_ERROR
        private const val STATE_FAST_FORWARDING =
            PlaybackStateCompat.STATE_FAST_FORWARDING
        private const val STATE_NONE =
            PlaybackStateCompat.STATE_NONE
        private const val STATE_REWINDING =
            PlaybackStateCompat.STATE_REWINDING
        private const val STATE_SKIPPING_TO_NEXT =
            PlaybackStateCompat.STATE_SKIPPING_TO_NEXT
        private const val STATE_SKIPPING_TO_PREVIOUS =
            PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS
        private const val STATE_SKIPPING_TO_QUEUE_ITEM =
            PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM
        private const val STATE_STOPPED =
            PlaybackStateCompat.STATE_STOPPED
    }
}
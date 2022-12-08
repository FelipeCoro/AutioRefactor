package com.autio.android_app.ui.view.usecases.home.fragment

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.autio.android_app.R
import com.autio.android_app.data.model.story.Story
import com.autio.android_app.databinding.FragmentPlayerBinding
import com.autio.android_app.player.MediaBrowserAdapter
import com.autio.android_app.player.StoryLibrary
import com.autio.android_app.ui.viewmodel.StoryViewModel
import com.autio.android_app.util.Utils.getIconFromDrawable
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class PlayerFragment :
    Fragment(),
    OnMapReadyCallback {

    private var _binding: FragmentPlayerBinding? =
        null
    private val binding get() = _binding!!

    private var currentState =
        STATE_PAUSED

    private lateinit var mediaBrowserAdapter: MediaBrowserAdapter

    private lateinit var stories: LiveData<Array<Story>>
    private var currentStory: Story? =
        null

    private var map: GoogleMap? =
        null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )
        val dummyIds =
            arrayOf(
                "3"
            )

        mediaBrowserAdapter =
            MediaBrowserAdapter(
                requireContext()
            ).apply {
                addListener(
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
                                    binding.btnPlay.setImageDrawable(
                                        ResourcesCompat.getDrawable(
                                            resources,
                                            R.drawable.ic_player_pause,
                                            null
                                        )
                                    )
                                    STATE_PLAYING
                                } else {
                                    binding.btnPlay.setImageDrawable(
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
                                binding.tvStoryTitle.text = getText(R.string.no_story_loaded)
                                binding.tvStoryNarrator.visibility = View.GONE
                                binding.tvStoryAuthor.visibility = View.GONE
                                binding.tvStoryCategory.visibility = View.GONE
                                return
                            }
                            binding.tvStoryTitle.text =
                                mediaMetadata.getString(
                                    MediaMetadataCompat.METADATA_KEY_TITLE
                                )
                            binding.tvStoryNarrator.apply {
                                text = mediaMetadata.getString(
                                    MediaMetadataCompat.METADATA_KEY_ARTIST
                                )
                                visibility = View.VISIBLE
                            }
                            binding.tvStoryAuthor.apply {
                                text = mediaMetadata.getString(
                                    MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST
                                )
                                visibility = View.VISIBLE
                            }
                            binding.tvStoryCategory.apply {
                                text = mediaMetadata.getString(
                                    MediaMetadataCompat.METADATA_KEY_GENRE
                                )
                                visibility = View.VISIBLE
                            }
                            StoryLibrary.getStoryBitmap(
                                requireContext(),
                                mediaMetadata.getString(
                                    MediaMetadataCompat.METADATA_KEY_MEDIA_ID
                                )
                            ) {
                                activity?.runOnUiThread {
                                    binding.ivStoryImage.setImageBitmap(
                                        it
                                    )
                                }
                            }
                        }
                    })
            }

        stories =
            ViewModelProvider(
                this
            )[StoryViewModel::class.java].getStoriesByIds(
                dummyIds
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentPlayerBinding.inflate(
                inflater,
                container,
                false
            )

        binding.btnPlay.setOnClickListener {
            if (currentState == STATE_PLAYING) {
                mediaBrowserAdapter.getTransportControls()
                    .pause()
            } else {
                Log.d(TAG, "Playing now...")
                mediaBrowserAdapter.getTransportControls().play()
            }
        }

        val mapFragment =
            childFragmentManager.findFragmentById(
                R.id.maps
            ) as SupportMapFragment?
        mapFragment?.getMapAsync(
            this
        )
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        mediaBrowserAdapter.onStart()
    }

    override fun onStop() {
        super.onStop()
        mediaBrowserAdapter.onStop()
    }

    override fun onMapReady(
        map: GoogleMap
    ) {
        map.uiSettings.isScrollGesturesEnabled =
            false
        this.map =
            map

        stories
            .observe(
                viewLifecycleOwner
            ) { t ->
                currentStory =
                    t.firstOrNull()
                val pinDrawable =
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_non_listened_pin,
                        null
                    )
                val pinIcon =
                    getIconFromDrawable(
                        pinDrawable
                    )
                addStoryMarker(
                    t.first(),
                    pinIcon
                )
            }
    }

    private fun addStoryMarker(
        story: Story,
        customIcon: BitmapDescriptor? = null
    ) {
        val latLng =
            LatLng(
                story.lat,
                story.lon
            )
        val marker =
            MarkerOptions().apply {
                position(
                    latLng
                )
                title(
                    story.title
                )
                if (customIcon != null) {
                    icon(
                        customIcon
                    )
                }
            }
        this.map?.apply {
            addMarker(
                marker
            )
            moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        story.lat,
                        story.lon
                    ),
                    15f
                )
            )
        }
    }

    companion object {
        val TAG =
            PlayerFragment::class.simpleName

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
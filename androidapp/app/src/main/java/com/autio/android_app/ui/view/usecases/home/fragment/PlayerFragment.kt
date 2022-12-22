package com.autio.android_app.ui.view.usecases.home.fragment

import android.content.res.Resources
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.autio.android_app.R
import com.autio.android_app.data.model.story.NowPlayingMetadata
import com.autio.android_app.data.model.story.Story
import com.autio.android_app.databinding.FragmentPlayerBinding
import com.autio.android_app.extensions.timestampToMSS
import com.autio.android_app.ui.viewmodel.BottomNavigationViewModel
import com.autio.android_app.ui.viewmodel.PlayerFragmentViewModel
import com.autio.android_app.util.InjectorUtils
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

class PlayerFragment :
    Fragment(),
    OnMapReadyCallback {

    private val bottomNavigationViewModel by activityViewModels<BottomNavigationViewModel> {
        InjectorUtils.provideBottomNavigationViewModel(
            requireContext()
        )
    }
    private val playerFragmentViewModel by viewModels<PlayerFragmentViewModel> {
        InjectorUtils.providePlayerFragmentViewModel(
            requireContext()
        )
    }

    lateinit var binding: FragmentPlayerBinding

    private var map: GoogleMap? =
        null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentPlayerBinding.inflate(
                inflater,
                container,
                false
            )

        val mapFragment =
            childFragmentManager.findFragmentById(
                R.id.player_map
            ) as SupportMapFragment?
        mapFragment?.getMapAsync(
            this
        )
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

        val context =
            activity
                ?: return

        playerFragmentViewModel.storyLikes.observe(
            viewLifecycleOwner
        ) {
            binding.tvNumberOfLikes.text =
                it.size.toString()
        }
        playerFragmentViewModel.currentStory.observe(
            viewLifecycleOwner
        ) { mediaItem ->
            updateUI(
                view,
                mediaItem
            )
        }
        playerFragmentViewModel.speedButtonRes.observe(
            viewLifecycleOwner
        ) { res ->
            binding.btnChangeSpeed.setImageResource(
                res
            )
        }
        playerFragmentViewModel.mediaButtonRes.observe(
            viewLifecycleOwner
        ) { res ->
            binding.btnPlay.setImageResource(
                res
            )
        }
        playerFragmentViewModel.mediaPosition.observe(
            viewLifecycleOwner
        ) { pos ->
            binding.tvNowPlayingSeek.text =
                NowPlayingMetadata.timestampToMSS(
                    context,
                    pos
                )
            binding.sBTrack.progress =
                (pos / 1000)
                    .toInt()
        }

        binding.sBTrack.setOnSeekBarChangeListener(
            object :
                SeekBar.OnSeekBarChangeListener {
                override fun onStartTrackingTouch(
                    p0: SeekBar?
                ) {
                }

                override fun onStopTrackingTouch(
                    p0: SeekBar?
                ) {
                }

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        bottomNavigationViewModel.setPlaybackPosition(
                            progress
                        )
                    }
                }
            })

        binding.btnChangeSpeed.setOnClickListener {
            bottomNavigationViewModel.changePlaybackSpeed()
        }

        binding.btnRewind.setOnClickListener {
            bottomNavigationViewModel.rewindFifteenSeconds()
        }

        binding.btnPlay.setOnClickListener {
            playerFragmentViewModel.currentStory.value?.let {
                bottomNavigationViewModel.playMediaId(
                    it.id
                )
            }
        }

        binding.tvNowPlayingDuration.text =
            NowPlayingMetadata.timestampToMSS(
                context,
                0L
            )
        binding.tvNowPlayingSeek.text =
            NowPlayingMetadata.timestampToMSS(
                context,
                0L
            )
    }

    private fun updateUI(
        view: View,
        story: Story?
    ) =
        with(
            binding
        ) {
            if (story != null) {
                if (story.imageUrl?.isNotEmpty() == true) {
                    val uri =
                        Uri.parse(
                            story.imageUrl
                        )
                    Glide.with(
                        view
                    )
                        .load(
                            uri
                        )
                        .into(
                            ivStoryImage
                        )
                }
                sBTrack.max =
                    story.duration
                tvStoryTitle.text =
                    story.title
                tvStoryAuthor.apply {
                    text =
                        getString(
                            R.string.story_author,
                            story.author
                        )
                    paintFlags =
                        Paint.UNDERLINE_TEXT_FLAG
                    visibility =
                        View.VISIBLE
                }
                tvStoryNarrator.apply {
                    text =
                        story.narrator
                    visibility =
                        View.VISIBLE
                }
                tvStoryCategory.apply {
                    text =
                        story.category.title
                    visibility =
                        View.VISIBLE
                }
                tvNowPlayingDuration.text =
                    (story.duration * 1000L)
                        .timestampToMSS(
                            requireContext()
                        )
                mapCard.visibility =
                    View.VISIBLE
            } else {
                ivStoryImage.setImageResource(
                    0
                )
                tvStoryTitle.text =
                    requireContext().resources.getResourceName(
                        R.string.no_story_loaded
                    )
                tvStoryAuthor.visibility =
                    View.GONE
                tvStoryNarrator.visibility =
                    View.GONE
                tvStoryCategory.visibility =
                    View.GONE
                tvNowPlayingDuration.text =
                    requireContext().getString(
                        R.string.duration_unknown
                    )
                mapCard.visibility =
                    View.GONE
            }
        }

    override fun onMapReady(
        map: GoogleMap
    ) {
        map.uiSettings.isScrollGesturesEnabled =
            false
        this.map =
            map

        setMapLayout()

        playerFragmentViewModel.currentStory.value?.let {
            addStoryMarker(
                it
            )
        }
    }

    private fun setMapLayout() {
        try {
            map?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
        } catch (e: Resources.NotFoundException) {
            return
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

        fun newInstance() =
            PlayerFragment()
    }
}
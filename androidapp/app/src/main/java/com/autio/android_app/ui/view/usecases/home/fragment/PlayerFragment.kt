package com.autio.android_app.ui.view.usecases.home.fragment

import android.content.Context
import android.content.res.Resources
import android.graphics.Paint
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.contains
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.model.OptionClickListener
import com.autio.android_app.data.model.StoryOption
import com.autio.android_app.data.model.story.DownloadedStory
import com.autio.android_app.data.model.story.NowPlayingMetadata
import com.autio.android_app.data.model.story.Story
import com.autio.android_app.data.repository.FirebaseStoryRepository
import com.autio.android_app.data.repository.PrefRepository
import com.autio.android_app.databinding.FragmentPlayerBinding
import com.autio.android_app.extensions.getAddress
import com.autio.android_app.extensions.timestampToMSS
import com.autio.android_app.ui.view.usecases.home.BottomNavigation
import com.autio.android_app.ui.view.usecases.home.adapter.StoryOptionsAdapter
import com.autio.android_app.ui.viewmodel.BottomNavigationViewModel
import com.autio.android_app.ui.viewmodel.PlayerFragmentViewModel
import com.autio.android_app.ui.viewmodel.StoryViewModel
import com.autio.android_app.util.*
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.*
import java.util.*
import java.util.regex.Pattern

class PlayerFragment :
    Fragment(),
    OnMapReadyCallback,
    FragmentManager.OnBackStackChangedListener {
    private val prefRepository by lazy {
        PrefRepository(
            requireContext()
        )
    }

    private val bottomNavigationViewModel by activityViewModels<BottomNavigationViewModel>()
    private val playerFragmentViewModel by viewModels<PlayerFragmentViewModel> {
        InjectorUtils.providePlayerFragmentViewModel(
            requireContext()
        )
    }
    private val storyViewModel by viewModels<StoryViewModel> {
        InjectorUtils.provideStoryViewModel(
            requireContext()
        )
    }

    private lateinit var binding: FragmentPlayerBinding

    private lateinit var activityLayout: ConstraintLayout

    private var map: GoogleMap? =
        null

    private lateinit var snackBarView: View
    private var feedbackJob: Job? =
        null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )
        // listen to backstack changes
        requireActivity().supportFragmentManager.addOnBackStackChangedListener(
            this
        )
    }

    override fun onBackStackChanged() {
        if (activity != null) {
            // enable Up button only if there are entries on the backstack
            if (requireActivity().supportFragmentManager.backStackEntryCount < 1) {
                (requireActivity() as BottomNavigation).hideUpButton()
            }
        }
    }

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

        snackBarView =
            layoutInflater.inflate(
                R.layout.feedback_snackbar,
                binding.root,
                false
            )

        lifecycleScope.launch {
            val mapFragment =
                childFragmentManager.findFragmentById(
                    R.id.player_map
                ) as SupportMapFragment?
            mapFragment?.getMapAsync(
                this@PlayerFragment
            )
        }

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

        activityLayout =
            requireActivity().findViewById(
                R.id.activityRoot
            )

        playerFragmentViewModel.storyLikes.observe(
            viewLifecycleOwner
        ) { likes ->
            if (likes[prefRepository.firebaseKey] == true) {
                binding.btnHeart.setImageResource(
                    R.drawable.ic_heart_filled
                )
            } else {
                binding.btnHeart.setImageResource(
                    R.drawable.ic_heart
                )
            }
            binding.tvNumberOfLikes.text =
                likes.filter { it.value }.size.toString()
            playerFragmentViewModel.currentStory.value?.let { story ->
                binding.btnHeart.setOnClickListener {
                    showPaywallOrProceedWithNormalProcess {
                        if (likes[prefRepository.firebaseKey] == true) {
                            FirebaseStoryRepository.removeLikeFromStory(
                                story.id,
                                prefRepository.firebaseKey,
                                onSuccessListener = {
                                    storyViewModel.removeLikeFromStory(
                                        story.id
                                    )
                                    showFeedbackSnackBar(
                                        "Removed From Favorites"
                                    )
                                },
                                onFailureListener = {
                                    showFeedbackSnackBar(
                                        "Connection Failure"
                                    )
                                }
                            )
                        } else {
                            FirebaseStoryRepository.giveLikeToStory(
                                story.id,
                                prefRepository.firebaseKey,
                                onSuccessListener = {
                                    storyViewModel.setLikeToStory(
                                        story.id
                                    )
                                    showFeedbackSnackBar(
                                        "Added To Favorites"
                                    )
                                },
                                onFailureListener = {
                                    showFeedbackSnackBar(
                                        "Connection Failure"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
        playerFragmentViewModel.isStoryBookmarked.observe(
            viewLifecycleOwner
        ) { isBookmarked ->
            if (isBookmarked) {
                binding.btnBookmark.setImageResource(
                    R.drawable.ic_player_bookmark_filled
                )
            } else {
                binding.btnBookmark.setImageResource(
                    R.drawable.ic_player_bookmark
                )
            }
            playerFragmentViewModel.currentStory.value?.let { story ->
                binding.btnBookmark.setOnClickListener {
                    showPaywallOrProceedWithNormalProcess {
                        if (isBookmarked) {
                            FirebaseStoryRepository.removeBookmark(
                                prefRepository.firebaseKey,
                                story.id,
                                onSuccessListener = {
                                    storyViewModel.removeBookmarkFromStory(
                                        story.id
                                    )
                                    showFeedbackSnackBar(
                                        "Removed From Bookmarks"
                                    )
                                },
                                onFailureListener = {
                                    showFeedbackSnackBar(
                                        "Connection Failure"
                                    )
                                }
                            )
                        } else {
                            FirebaseStoryRepository.bookmarkStory(
                                prefRepository.firebaseKey,
                                story.id,
                                story.title,
                                onSuccessListener = {
                                    storyViewModel.bookmarkStory(
                                        story.id
                                    )
                                    showFeedbackSnackBar(
                                        "Added To Bookmarks"
                                    )
                                },
                                onFailureListener = {
                                    showFeedbackSnackBar(
                                        "Connection Failure"
                                    )
                                }
                            )
                        }
                    }
                }
            }
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
                        showPaywallOrProceedWithNormalProcess {
                            bottomNavigationViewModel.setPlaybackPosition(
                                progress
                            )
                        }
                    }
                }
            })

        binding.btnChangeSpeed.setOnClickListener {
            playerFragmentViewModel.changePlaybackSpeed()
        }

        binding.btnRewind.setOnClickListener {
            showPaywallOrProceedWithNormalProcess {
                bottomNavigationViewModel.rewindFifteenSeconds()
            }
        }

        binding.btnPlay.setOnClickListener {
            playerFragmentViewModel.currentStory.value?.let {
                bottomNavigationViewModel.playMediaId(
                    it.id
                )
            }
        }

        binding.btnNext.setOnClickListener {
            showPaywallOrProceedWithNormalProcess {
                bottomNavigationViewModel.skipToNextStory()
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
                val bundle =
                    Bundle()
                bundle.putInt(
                    STORY_ID_ARG,
                    story.originalId
                )
                if (story.imageUrl.isNotEmpty()) {
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
                sBTrack.isEnabled =
                    true
                sBTrack.max =
                    story.duration
                tvStoryTitle.text =
                    story.title
                tvStoryAuthor.apply {
                    text =
                        story.author
                    paintFlags =
                        Paint.UNDERLINE_TEXT_FLAG
                    visibility =
                        View.VISIBLE
                    setOnClickListener {
                        findNavController().navigate(
                            R.id.action_player_to_author_details,
                            bundle
                        )
                    }
                }
                tvStoryNarrator.apply {
                    text =
                        story.narrator
                    paintFlags =
                        Paint.UNDERLINE_TEXT_FLAG
                    visibility =
                        View.VISIBLE
                    setOnClickListener {
                        findNavController().navigate(
                            R.id.action_player_to_narrator_details,
                            bundle
                        )
                    }
                }
                lifecycleScope.launch {
                    withContext(
                        Dispatchers.IO
                    ) {
                        Geocoder(
                            requireContext(),
                            Locale.US
                        ).getAddress(
                            story.lat,
                            story.lon
                        ) { address ->
                            lifecycleScope.launch {
                                withContext(
                                    Dispatchers.Main
                                ) {
                                    if (address != null) {
                                        val stateCode =
                                            getUSStateCode(
                                                address
                                            )
                                        binding.tvStoryLocation.text =
                                            resources.getString(
                                                R.string.address,
                                                address.subAdminArea,
                                                stateCode
                                            )
                                        binding.tvStoryLocation.visibility =
                                            View.VISIBLE
                                    }
                                }
                            }
                        }
                        FirebaseStoryRepository.getCategory(
                            story.category?.id
                        ) { category ->
                            lifecycleScope.launch {
                                withContext(
                                    Dispatchers.Main
                                ) {
                                    Constants.categoryIcons[category.title.uppercase()]?.let {
                                        ivCategoryIcon.setImageResource(
                                            it
                                        )
                                    }
                                    tvStoryCategory.text =
                                        category.title
                                }
                            }
                        }
                    }
                }
                llCategory.visibility =
                    View.VISIBLE
                tvNowPlayingDuration.text =
                    (story.duration * 1000L)
                        .timestampToMSS(
                            requireContext()
                        )
                tvStoryDescription.apply {
                    visibility =
                        View.VISIBLE
                    text =
                        story.description
                }
                binding.btnShare.setOnClickListener {
                    showPaywallOrProceedWithNormalProcess {
                        shareStory(
                            requireContext(),
                            story.id
                        )
                    }
                }
                binding.btnFeedback.setOnClickListener {
                    writeEmailToCustomerSupport(
                        requireContext()
                    )
                }
                binding.btnOptions.setOnClickListener {
                    showStoryOptions(
                        story
                    )
                }
                mapCard.visibility =
                    View.VISIBLE
            } else {
                sBTrack.isEnabled =
                    false
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
                llCategory.visibility =
                    View.GONE
                tvNowPlayingDuration.text =
                    requireContext().resources.getResourceName(
                        R.string.duration_unknown
                    )
                tvStoryDescription.visibility =
                    View.GONE
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

    private fun getUSStateCode(
        address: Address
    ): String {
        var fullAddress =
            ""
        for (i in 0..address.maxAddressLineIndex) if (address.getAddressLine(
                i
            ) != null
        ) fullAddress =
            "$fullAddress " + address.getAddressLine(
                i
            )

        var stateCode: String? =
            null
        val pattern =
            Pattern.compile(
                " [A-Z]{2} "
            )
        val helper =
            fullAddress.uppercase()
                .substring(
                    0,
                    fullAddress.uppercase()
                        .indexOf(
                            "USA"
                        )
                )
        val matcher =
            pattern.matcher(
                helper
            )
        while (matcher.find()) {
            stateCode =
                matcher.group()
                    .trim()
        }
        return stateCode
            ?: ""
    }

    private fun showStoryOptions(
        story: Story
    ) {
        val inflater =
            requireContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE
            ) as LayoutInflater
        val view =
            inflater.inflate(
                R.layout.list_popup_window,
                binding.root,
                false
            )
        val recyclerView =
            view.findViewById<RecyclerView>(
                R.id.rvWindowPopupList
            )
        val popup =
            PopupWindow(
                view,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        val options: MutableList<StoryOption> =
            arrayListOf(
                if (story.isDownloaded == true) StoryOption.REMOVE_DOWNLOAD else StoryOption.DOWNLOAD,
                StoryOption.DIRECTIONS
            )
        val storyOptionsAdapter =
            StoryOptionsAdapter(
                story,
                options,
                object :
                    OptionClickListener<Story> {
                    override fun onItemClick(
                        option: StoryOption,
                        story: Story
                    ) {
                        popup.dismiss()
                        onOptionClicked(
                            option,
                            story
                        )
                    }
                }
            )
        recyclerView.adapter =
            storyOptionsAdapter
        popup.isOutsideTouchable =
            true
        popup.isFocusable =
            true
        popup.showAsDropDown(
            binding.btnOptions,
            -200,
            50
        )
    }

    private fun onOptionClicked(
        option: StoryOption,
        story: Story
    ) {
        showPaywallOrProceedWithNormalProcess {
            when (option) {
                StoryOption.DOWNLOAD -> lifecycleScope.launch {
                    try {
                        val downloadedStory =
                            DownloadedStory.fromStory(
                                requireContext(),
                                story
                            )
                        lifecycleScope.launch {
                            storyViewModel.downloadStory(
                                downloadedStory!!
                            )
                            showFeedbackSnackBar(
                                "Story Saved To My Device"
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "PlayerFragment",
                            "exception: ",
                            e
                        )
                        showFeedbackSnackBar(
                            "Failed Downloading Story"
                        )
                    }
                }
                StoryOption.REMOVE_DOWNLOAD -> {
                    storyViewModel.removeDownloadedStory(
                        story.id
                    )
                    showFeedbackSnackBar(
                        "Story Removed From My Device"
                    )
                }
                StoryOption.DIRECTIONS -> openLocationInMapsApp(
                    requireActivity(),
                    story.lat,
                    story.lon
                )
                else -> Log.d(
                    "PlayerFragment",
                    "optionSelectedNotAdded"
                )
            }
        }
    }

    private fun showFeedbackSnackBar(
        feedback: String
    ) {
        if (isAdded && activity != null) {
            cancelJob()
            snackBarView.alpha =
                1F
            snackBarView.findViewById<TextView>(
                R.id.tvFeedback
            ).text =
                feedback
            activityLayout.addView(
                snackBarView
            )
            feedbackJob =
                lifecycleScope.launch {
                    delay(
                        2000
                    )
                    snackBarView.animate()
                        .alpha(
                            0F
                        )
                        .withEndAction {
                            activityLayout.removeView(
                                snackBarView
                            )
                        }
                }
        }
    }

    private fun showPaywallOrProceedWithNormalProcess(
        normalProcess: () -> Unit
    ) {
        if (prefRepository.remainingStories <= 0) {
            (requireActivity() as BottomNavigation).showPayWall()
        } else {
            normalProcess.invoke()
        }
    }

    private fun cancelJob() {
        if (activityLayout.contains(
                snackBarView
            )
        ) {
            activityLayout.removeView(
                snackBarView
            )
        }
        feedbackJob?.cancel()
    }
}

private const val STORY_ID_ARG =
    "com.autio.android_app.ui.view.usecases.home.fragment.PlayerFragment.STORY_ID"
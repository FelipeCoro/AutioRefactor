package com.autio.android_app.ui.stories.fragments

import android.content.res.Resources
import android.graphics.Paint
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.contains
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.autio.android_app.R
import com.autio.android_app.data.api.model.StoryOption
import com.autio.android_app.data.api.model.modelLegacy.NowPlayingMetadata
import com.autio.android_app.databinding.FragmentPlayerBinding
import com.autio.android_app.extensions.getAddress
import com.autio.android_app.extensions.timestampToMSS
import com.autio.android_app.ui.stories.BottomNavigation
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.ui.stories.view_model.BottomNavigationViewModel
import com.autio.android_app.ui.stories.view_model.PlayerFragmentViewModel
import com.autio.android_app.ui.stories.view_model.StoryViewModel
import com.autio.android_app.ui.stories.view_states.PlayerViewState
import com.autio.android_app.ui.stories.view_states.StoryViewState
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.regex.Pattern

@AndroidEntryPoint
class PlayerFragment : Fragment(), OnMapReadyCallback, FragmentManager.OnBackStackChangedListener {


    private val bottomNavigationViewModel: BottomNavigationViewModel by activityViewModels()
    private val playerFragmentViewModel: PlayerFragmentViewModel by viewModels()
    private val storyViewModel: StoryViewModel by viewModels()

    private lateinit var binding: FragmentPlayerBinding

    private lateinit var activityLayout: ConstraintLayout


    private var map: GoogleMap? = null

    private lateinit var snackBarView: View
    private var feedbackJob: Job? = null

    private var storyId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // listen to backstack changes
        requireActivity().supportFragmentManager.addOnBackStackChangedListener(this)
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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayerBinding.inflate(
            inflater, container, false
        )

        snackBarView = layoutInflater.inflate(
            R.layout.feedback_snackbar, binding.root as ViewGroup, false
        )

        lifecycleScope.launch {
            val mapFragment = childFragmentManager.findFragmentById(
                R.id.player_map
            ) as SupportMapFragment?
            mapFragment?.getMapAsync(
                this@PlayerFragment
            )
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(
            view, savedInstanceState
        )

        val context = activity ?: return

        bindObservers()

        activityLayout = requireActivity().findViewById(
            R.id.activity_layout
        )


        val story = playerFragmentViewModel.currentStory

        story.value?.let {
            storyId = it.id
        }


        storyViewModel.isStoryLiked(storyId)


        binding.btnHeart.setOnClickListener {
            storyViewModel.getStoriesByIds(listOf(storyId))
        }


        binding.btnBookmark.setOnClickListener {
            storyViewModel.toggleStoryBookmarked(storyId)
        }



        playerFragmentViewModel.currentStory.observe(viewLifecycleOwner) { mediaItem ->
            updateUI(view, mediaItem)
            if (mediaItem != null) {
                storyId = mediaItem.id
                storyViewModel.isStoryBookmarked(mediaItem.id)
            }
        }
        playerFragmentViewModel.speedButtonRes.observe(viewLifecycleOwner) { res ->
            binding.btnChangeSpeed.setImageResource(res)
        }
        playerFragmentViewModel.mediaButtonRes.observe(viewLifecycleOwner) { res ->
            binding.btnPlay.setImageResource(res)
        }
        playerFragmentViewModel.mediaPosition.observe(viewLifecycleOwner) { pos ->
            binding.tvNowPlayingSeek.text = NowPlayingMetadata.timestampToMSS(context, pos)
            binding.sBTrack.progress = (pos / 1000).toInt()
        }

        binding.btnChangeSpeed.setOnClickListener {
            playerFragmentViewModel.changePlaybackSpeed()
        }

        binding.btnRewind.setOnClickListener {
            playerFragmentViewModel.handleRewindClick()
        }

        binding.btnPlay.setOnClickListener {
            playerFragmentViewModel.currentStory.value?.let {
                bottomNavigationViewModel.playMediaId(
                    it.id
                )
            }
        }

        binding.btnNext.setOnClickListener {
            bottomNavigationViewModel.skipToNextStory()
        }

        binding.tvNowPlayingDuration.text = NowPlayingMetadata.timestampToMSS(
            context, 0L
        )
        binding.tvNowPlayingSeek.text = NowPlayingMetadata.timestampToMSS(
            context, 0L
        )
    }


    private fun bindObservers() {
        storyViewModel.storyViewState.observe(viewLifecycleOwner, ::handleStoryViewState)
        playerFragmentViewModel.playerViewState.observe(viewLifecycleOwner, ::handlePlayerViewState)
    }

    private fun handlePlayerViewState(playerViewState: PlayerViewState?) {
        playerViewState?.let { viewState ->
            when (viewState) {
                is PlayerViewState.OnNotPremiumUser -> handleNotPremiumUser()
                is PlayerViewState.OnShareStoriesSuccess -> handleShareStory()
                is PlayerViewState.OnChangeProgressSuccess -> handleProgressChange(viewState.progress)
                is PlayerViewState.OnHandleRewindClickSuccess -> handleRewindClickSuccess()
            }
        }
    }

    private fun handleShareStory() {
        shareStory(requireContext())
    }

    private fun handleRewindClickSuccess() {
        bottomNavigationViewModel.rewindFifteenSeconds()
    }

    private fun handleNotPremiumUser() {
        bottomNavigationActivity?.showPayWall()
    }

    private fun handleStoryViewState(viewState: StoryViewState?) {
        when (viewState) {
            is StoryViewState.StoryLikesCount -> handleStoryLikesCount(viewState.storyLikesCount)
            is StoryViewState.FetchedStoriesByIds -> handleFetchedStory(viewState.stories.first())
            is StoryViewState.ToggleStoryBookmark -> toggleStoryBookmarked(
                viewState.isBookmarked,
                viewState.storyId
            )
            is StoryViewState.StoryLiked -> handleLIsLikedState(viewState.likeCount)
            is StoryViewState.LikedRemoved -> handleLIsNotLikedState(viewState.likeCount)
            is StoryViewState.IsStoryLiked -> isStoryLiked(viewState.isLiked)
            is StoryViewState.StoryIsBookmarked -> isStoryBookmarked(viewState.status)
            is StoryViewState.OnNotPremiumUser -> handleNotPremiumUser()
            is StoryViewState.StoryDownloaded -> showFeedbackSnackBar("Story Downloaded")//TODO(Use common method later)
            else -> {} //TODO(Ideally have error handling for each error)
        }
    }

    private fun isStoryLiked(isLiked: Boolean) {
        if (!isLiked) {
            binding.btnHeart.setImageResource(R.drawable.ic_heart)
            storyViewModel.storyLikesCount(storyId)
        } else
            binding.btnHeart.setImageResource(R.drawable.ic_heart_filled)
        storyViewModel.storyLikesCount(storyId)
    }

    private fun handleFetchedStory(story: Story) {
        if (story.isLiked == true) {
            storyViewModel.removeLikeFromStory(story.id)
            binding.btnHeart.setImageResource(R.drawable.ic_heart)

        } else {
            storyViewModel.giveLikeToStory(story.id)
            binding.btnHeart.setImageResource(R.drawable.ic_heart_filled)
        }
    }

    private fun isStoryBookmarked(isBookmarked: Boolean) {
        if (!isBookmarked) {
            binding.btnBookmark.setImageResource(R.drawable.ic_player_bookmark)

        } else
            binding.btnBookmark.setImageResource(R.drawable.ic_player_bookmark_filled)

    }

    private fun toggleStoryBookmarked(isBookmarked: Boolean, storyId: Int) {
        if (isBookmarked) {
            binding.btnBookmark.setImageResource(R.drawable.ic_player_bookmark)
            storyViewModel.removeBookmarkFromStory(storyId)
            showFeedbackSnackBar("Removed Bookmark")
        } else {
            binding.btnBookmark.setImageResource(R.drawable.ic_player_bookmark_filled)
            storyViewModel.bookmarkStory(storyId)
            showFeedbackSnackBar("Added Bookmark")
        }
    }

    private fun handleLIsLikedState(likeCount: Int) {
        showFeedbackSnackBar("Added To Favorites")
        binding.tvNumberOfLikes.text = likeCount.toString()
    }

    private fun handleLIsNotLikedState(likeCount: Int) {
        showFeedbackSnackBar("Removed From Favorites")
        binding.tvNumberOfLikes.text = likeCount.toString()
    }

    private fun handleStoryLikesCount(storyLikesInt: Int) {
        binding.tvNumberOfLikes.text = storyLikesInt.toString()
    }

    private fun updateUI(view: View, story: Story?) = with(
        binding
    ) {
        if (story != null) {
            val bundle = Bundle()
            bundle.putInt(
                STORY_ID_ARG, story.id
            )
            if (story.imageUrl.isNotEmpty()) {
                val uri = Uri.parse(
                    story.imageUrl
                )
                Glide.with(view).load(uri).into(ivStoryImage)
            }
            sBTrack.isEnabled = true
            sBTrack.max = story.duration
            sBTrack.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
                        playerFragmentViewModel.onProgressChanged(progress)
                    }
                }
            })

            storyViewModel.isStoryLiked(storyId)
            tvStoryTitle.text = story.title
            tvStoryAuthor.apply {
                text = story.author
                paintFlags = Paint.UNDERLINE_TEXT_FLAG
                visibility = View.VISIBLE
                setOnClickListener {
                    findNavController().navigate(
                        R.id.action_player_to_author_details, bundle
                    )
                }
            }
            tvStoryNarrator.apply {
                text = story.narrator
                paintFlags = Paint.UNDERLINE_TEXT_FLAG
                visibility = View.VISIBLE
                setOnClickListener {
                    findNavController().navigate(
                        R.id.action_player_to_narrator_details, bundle
                    )
                }
            }
            lifecycleScope.launch {
                withContext(
                    Dispatchers.IO
                ) {
                    Geocoder(
                        requireContext(), Locale.US
                    ).getAddress(
                        story.lat, story.lng
                    ) { address ->
                        lifecycleScope.launch {
                            withContext(
                                Dispatchers.Main
                            ) {
                                if (address != null) {
                                    val stateCode = getUSStateCode(
                                        address
                                    )
                                    binding.tvStoryLocation.text = resources.getString(
                                        R.string.address, address.subAdminArea, stateCode
                                    )
                                    binding.tvStoryLocation.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                }
                val category = story.category
                Constants.categoryIcons[category.uppercase()]?.let {
                    ivCategoryIcon.setImageResource(
                        it
                    )
                    tvStoryCategory.text = category
                }
            }
            llCategory.visibility = View.VISIBLE
            tvNowPlayingDuration.text = (story.duration * 1000L).timestampToMSS(
                requireContext()
            )
            tvStoryDescription.apply {
                visibility = View.VISIBLE
                text = story.description
            }
            btnShare.setOnClickListener {
                playerFragmentViewModel.shareStory()
            }
            btnFeedback.setOnClickListener {
                writeEmailToCustomerSupport(
                    requireContext()
                )
            }
            btnOptions.setOnClickListener {
                if (story.isDownloaded == false) {
                    storyViewModel.downloadStory(story)
                } else (context?.let {
                    showToast(it, "Story has already been downloaded")
                })


                //        showStoryOptions(  //TODO(THIS IS FOR WHEN DIRECTIONS IS IMPLEMENTED)
                //            requireContext(), binding.root as ViewGroup, it, story, arrayListOf(
                //                if (story.isDownloaded == true)
                //                    StoryOption.REMOVE_DOWNLOAD else StoryOption.DOWNLOAD,
                //             //   StoryOption.DIRECTIONS
                //            ), onOptionClick = ::optionClicked
                //        )
            }
            mapCard.visibility = View.VISIBLE
        } else {
            sBTrack.setOnSeekBarChangeListener(null)
            ivStoryImage.setImageResource(0)
            tvStoryTitle.text = requireContext().resources.getResourceName(
                R.string.no_story_loaded
            )
            tvStoryAuthor.visibility = View.GONE
            tvStoryNarrator.visibility = View.GONE
            llCategory.visibility = View.GONE
            tvNowPlayingDuration.text = requireContext().resources.getResourceName(
                R.string.duration_unknown
            )
            tvStoryDescription.visibility = View.GONE
            mapCard.visibility = View.GONE
        }
    }

    private fun handleProgressChange(progress: Int) {
        bottomNavigationViewModel.setPlaybackPosition(progress)
    }

    override fun onMapReady(
        map: GoogleMap
    ) {
        map.uiSettings.isScrollGesturesEnabled = false
        this.map = map

        setMapLayout()

        playerFragmentViewModel.currentStory.value?.let {
            addStoryMarker(it)
        }
    }

    private fun setMapLayout() {
        try {
            map?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), R.raw.map_style
                )
            )
        } catch (e: Resources.NotFoundException) {
            return
        }
    }

    private fun addStoryMarker(
        story: Story, customIcon: BitmapDescriptor? = null
    ) {
        val latLng = LatLng(
            story.lat, story.lng
        )
        val marker = MarkerOptions().apply {
            position(latLng)
            title(story.title)
            if (customIcon != null) {
                icon(customIcon)
            }
        }
        this.map?.apply {
            addMarker(marker)
            moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(story.lat, story.lng), 15f
                )
            )
        }
    }

    private fun getUSStateCode(
        address: Address
    ): String {
        var fullAddress = ""
        for (i in 0..address.maxAddressLineIndex) if (address.getAddressLine(
                i
            ) != null
        ) fullAddress = "$fullAddress " + address.getAddressLine(
            i
        )

        var stateCode: String? = null
        val pattern = Pattern.compile(
            " [A-Z]{2} "
        )
        val helper = fullAddress.uppercase().substring(
            0, fullAddress.uppercase().indexOf(
                "USA"
            )
        )
        val matcher = pattern.matcher(
            helper
        )
        while (matcher.find()) {
            stateCode = matcher.group().trim()
        }
        return stateCode ?: ""
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

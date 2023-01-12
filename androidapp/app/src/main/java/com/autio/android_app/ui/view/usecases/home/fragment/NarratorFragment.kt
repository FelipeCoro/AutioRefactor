package com.autio.android_app.ui.view.usecases.home.fragment

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.contains
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.autio.android_app.R
import com.autio.android_app.data.model.StoryOption
import com.autio.android_app.data.model.story.DownloadedStory
import com.autio.android_app.data.model.story.Story
import com.autio.android_app.data.repository.ApiService
import com.autio.android_app.data.repository.FirebaseStoryRepository
import com.autio.android_app.data.repository.PrefRepository
import com.autio.android_app.databinding.FragmentNarratorBinding
import com.autio.android_app.ui.view.usecases.home.BottomNavigation
import com.autio.android_app.ui.view.usecases.home.adapter.StoryAdapter
import com.autio.android_app.ui.viewmodel.BottomNavigationViewModel
import com.autio.android_app.ui.viewmodel.StoryViewModel
import com.autio.android_app.util.InjectorUtils
import com.autio.android_app.util.openLocationInMapsApp
import com.autio.android_app.util.openUrl
import com.autio.android_app.util.shareStory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NarratorFragment :
    Fragment() {
    private val prefRepository by lazy {
        PrefRepository(
            requireContext()
        )
    }

    private val apiService =
        ApiService()

    private val bottomNavigationViewModel by activityViewModels<BottomNavigationViewModel>()
    private val storyViewModel by viewModels<StoryViewModel> {
        InjectorUtils.provideStoryViewModel(
            requireContext()
        )
    }

    private lateinit var binding: FragmentNarratorBinding

    private lateinit var activityLayout: ConstraintLayout

    private lateinit var storyAdapter: StoryAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var snackBarView: View
    private var feedbackJob: Job? =
        null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )
        (requireActivity() as BottomNavigation).showUpButton()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentNarratorBinding.inflate(
                inflater,
                container,
                false
            )

        binding.btnBack.setOnClickListener {
            findNavController().navigate(
                R.id.action_narrator_details_to_player
            )
        }

        snackBarView =
            layoutInflater.inflate(
                R.layout.feedback_snackbar,
                binding.root,
                false
            )

        recyclerView =
            binding.rvNarratorStories
        storyAdapter =
            StoryAdapter(
                onStoryPlay = { id ->
                    showPaywallOrProceedWithNormalProcess {
                        bottomNavigationViewModel.playMediaId(
                            id
                        )
                    }
                },
                onOptionClick = ::onOptionClicked
            )
        recyclerView.adapter =
            storyAdapter
        recyclerView.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        val storyId =
            arguments?.getInt(
                STORY_ID_ARG
            )

        activityLayout =
            requireActivity().findViewById(
                R.id.activityRoot
            )

        storyId?.let {
            lifecycleScope.launch {
                apiService.getNarratorOfStory(
                    prefRepository.userId,
                    "Bearer " + prefRepository.userApiToken,
                    it
                ) { narrator ->
                    if (narrator != null) {
                        if (narrator.imageUrl != null) {
                            Glide.with(
                                this@NarratorFragment
                            )
                                .load(
                                    narrator.imageUrl
                                )
                                .transition(
                                    DrawableTransitionOptions.withCrossFade(
                                        100
                                    )
                                )
                                .into(
                                    binding.ivNarratorPic
                                )
                        }
                        binding.tvNarratorName.apply {
                            visibility =
                                View.VISIBLE
                            text =
                                narrator.name
                        }
                        binding.tvNarratorBio.apply {
                            visibility =
                                View.VISIBLE
                            text =
                                narrator.biography
                        }
                        if (narrator.url != null) {
                            binding.btnVisitNarratorLink.apply {
                                setOnClickListener {
                                    openUrl(
                                        requireContext(),
                                        narrator.url
                                    )
                                }
                                visibility =
                                    View.VISIBLE
                            }
                        }
                        apiService.getStoriesByContributor(
                            prefRepository.userId,
                            prefRepository.userApiToken,
                            narrator.id,
                            1
                        ) { contributorApiResponse ->
                            if (contributorApiResponse != null) {
                                for (story in contributorApiResponse.data) {
                                    storyViewModel.cacheRecordOfStory(
                                        story.id,
                                        story.narrationUrl
                                            ?: ""
                                    )
                                }
                                storyViewModel.getStoriesByIds(
                                    contributorApiResponse.data.map {
                                        it.id
                                    }
                                        .toTypedArray()
                                )
                                    .observe(
                                        viewLifecycleOwner
                                    ) { stories ->
                                        if (stories.isNotEmpty()) {
                                            binding.tvPublishedStoriesSubtitle.visibility =
                                                View.VISIBLE
                                        }
                                        storyAdapter.submitList(
                                            stories.toList()
                                        )
                                    }
                            }
                        }
                    }
                }
            }
        }

        return binding.root
    }

    private fun onOptionClicked(
        option: StoryOption,
        story: Story
    ) {
        showPaywallOrProceedWithNormalProcess {
            when (option) {
                StoryOption.BOOKMARK -> {
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
                StoryOption.REMOVE_BOOKMARK -> {
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
                }
                StoryOption.LIKE -> {
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
                StoryOption.REMOVE_LIKE -> {
                    FirebaseStoryRepository.removeLikeFromStory(
                        prefRepository.firebaseKey,
                        story.id,
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
                }
                StoryOption.DOWNLOAD -> lifecycleScope.launch {
                    try {
                        val downloadedStory =
                            DownloadedStory.fromStory(
                                requireContext(),
                                story
                            )!!
                        storyViewModel.downloadStory(
                            downloadedStory
                        )
                        showFeedbackSnackBar(
                            "Story Saved To My Device"
                        )
                    } catch (e: Exception) {
                        Log.e(
                            "BookmarksFragment",
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
                StoryOption.SHARE -> {
                    shareStory(
                        requireContext(),
                        story.id
                    )
                }
                else -> Log.d(
                    "AuthorFragment",
                    "no option available"
                )
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
            TransitionManager.beginDelayedTransition(
                activityLayout,
                Slide(
                    Gravity.TOP
                )
            )
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
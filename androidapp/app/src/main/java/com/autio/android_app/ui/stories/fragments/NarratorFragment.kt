package com.autio.android_app.ui.stories.fragments

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
import com.autio.android_app.data.api.ApiClient
import com.autio.android_app.data.api.model.StoryOption
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.FragmentNarratorBinding
import com.autio.android_app.domain.mappers.toDto
import com.autio.android_app.ui.stories.adapter.StoryAdapter
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.ui.stories.view_model.BottomNavigationViewModel
import com.autio.android_app.ui.stories.view_model.StoryViewModel
import com.autio.android_app.ui.stories.view_states.StoryViewState
import com.autio.android_app.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NarratorFragment : Fragment() {

    @Inject
    lateinit var prefRepository: PrefRepository

    //TODO(Move service calls)
    @Inject
    lateinit var apiClient: ApiClient

    private val bottomNavigationViewModel: BottomNavigationViewModel by activityViewModels()
    private val storyViewModel: StoryViewModel by viewModels()
    private lateinit var binding: FragmentNarratorBinding
    private lateinit var activityLayout: ConstraintLayout
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var snackBarView: View
    private var feedbackJob: Job? = null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )
        (requireActivity() as com.autio.android_app.ui.stories.BottomNavigation).showUpButton()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        bindObservers()
        binding = FragmentNarratorBinding.inflate(
            inflater, container, false
        )

        binding.btnBack.setOnClickListener {
            findNavController().navigate(
                R.id.action_narrator_details_to_player
            )
        }

        snackBarView = layoutInflater.inflate(
            R.layout.feedback_snackbar, binding.root as ViewGroup, false
        )

        recyclerView = binding.rvNarratorStories
        storyAdapter = StoryAdapter(
            bottomNavigationViewModel.playingStory, onStoryPlay = { id ->
                showPaywallOrProceedWithNormalProcess(
                    prefRepository,
                    requireActivity(), true
                ) {
                    bottomNavigationViewModel.playMediaId(
                        id
                    )
                }
            }, onOptionClick = ::optionClicked
        )
        recyclerView.adapter = storyAdapter
        recyclerView.layoutManager = LinearLayoutManager(
            requireContext()
        )

        val storyId = arguments?.getInt(
            STORY_ID_ARG
        )

        activityLayout = requireActivity().findViewById(
            R.id.activityRoot
        )

        storyId?.let {
            lifecycleScope.launch {
                val narratorResponse = apiClient.getNarratorOfStory(
                    prefRepository.userId, prefRepository.userApiToken, it
                )
                if (narratorResponse.isSuccessful) {
                    val narrator = narratorResponse.body()!!
                    if (narrator.imageUrl != null) {
                        Glide.with(
                            this@NarratorFragment
                        ).load(
                            narrator.imageUrl
                        ).transition(
                            DrawableTransitionOptions.withCrossFade(
                                100
                            )
                        ).into(
                            binding.ivNarratorPic
                        )
                    }
                    binding.tvNarratorName.apply {
                        visibility = View.VISIBLE
                        text = narrator.name
                    }
                    binding.tvNarratorBio.apply {
                        visibility = View.VISIBLE
                        text = narrator.biography
                    }
                    if (narrator.url != null) {
                        binding.btnVisitNarratorLink.apply {
                            setOnClickListener {
                                openUrl(
                                    requireContext(), narrator.url
                                )
                            }
                            visibility = View.VISIBLE
                        }
                    }
                    val contributorApiResponse = apiClient.getStoriesByContributor(
                        prefRepository.userId, prefRepository.userApiToken, narrator.id, 1
                    )
                    if (contributorApiResponse.isSuccessful) {
                        for (story in contributorApiResponse.body()!!.data) {
                            storyViewModel.cacheRecordOfStory(
                                story.id, story.narrationUrl ?: ""
                            )
                        }
                        storyViewModel.getStoriesByIds(contributorApiResponse.body()!!.data.map {
                            it.id
                        })  //TODO(This should change with livedata??)
                        /* .observe(
                             viewLifecycleOwner
                         ) { stories ->
                             if (stories.isNotEmpty()) {
                                 binding.tvPublishedStoriesSubtitle.visibility =
                                     View.VISIBLE
                             }
                             storyAdapter.submitList(
                                 stories.toList()
                             )
                         }*/
                    }
                }
            }
        }
        return binding.root
    }

    fun bindObservers() {
        storyViewModel.storyViewState.observe(viewLifecycleOwner, ::handleViewState)
    }

    private fun handleViewState(viewState: StoryViewState?) {
        when (viewState) {
            is StoryViewState.AddedBookmark -> showFeedbackSnackBar("Added To Bookmarks")
            is StoryViewState.RemovedBookmark -> showFeedbackSnackBar("Removed From Bookmarks")
            is StoryViewState.StoryLiked -> showFeedbackSnackBar("Added To Favorites")
            is StoryViewState.LikedRemoved -> showFeedbackSnackBar("Removed From Favorites")
            is StoryViewState.StoryDownloaded -> showFeedbackSnackBar("Story Saved To My Device")
            is StoryViewState.StoryRemoved -> showFeedbackSnackBar("Story Removed From My Device")
            else -> showFeedbackSnackBar("Connection Failure") //TODO(Ideally have error handling for each error)
        }
    }

    private fun optionClicked(
        option: StoryOption, story: Story
    ) {
        activity?.let { verifiedActivity ->
            context?.let { verifiedContext ->
                onOptionClicked(
                    option, story, storyViewModel, prefRepository, verifiedActivity, verifiedContext
                )
            }
        }
    }

    private fun showFeedbackSnackBar(
        feedback: String
    ) {
        if (isAdded && activity != null) {
            cancelJob()
            snackBarView.alpha = 1F
            snackBarView.findViewById<TextView>(
                R.id.tvFeedback
            ).text = feedback
            TransitionManager.beginDelayedTransition(
                activityLayout, Slide(
                    Gravity.TOP
                )
            )
            activityLayout.addView(
                snackBarView
            )
            feedbackJob = lifecycleScope.launch {
                delay(
                    2000
                )
                snackBarView.animate().alpha(
                    0F
                ).withEndAction {
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

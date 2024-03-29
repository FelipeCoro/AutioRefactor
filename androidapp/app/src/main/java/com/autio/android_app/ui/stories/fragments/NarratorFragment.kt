package com.autio.android_app.ui.stories.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.contains
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.api.model.StoryOption
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.FragmentNarratorBinding
import com.autio.android_app.ui.stories.BottomNavigation
import com.autio.android_app.ui.stories.adapter.StoryAdapter
import com.autio.android_app.ui.stories.models.Contributor
import com.autio.android_app.ui.stories.models.Narrator
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.ui.stories.view_model.BottomNavigationViewModel
import com.autio.android_app.ui.stories.view_model.NarratorViewModel
import com.autio.android_app.ui.stories.view_model.StoryViewModel
import com.autio.android_app.ui.stories.view_states.BottomNavigationViewState
import com.autio.android_app.ui.stories.view_states.NarratorViewState
import com.autio.android_app.ui.stories.view_states.StoryViewState
import com.autio.android_app.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import javax.inject.Inject

@AndroidEntryPoint
class NarratorFragment : Fragment() {

    @Inject
    lateinit var prefRepository: PrefRepository

    private val bottomNavigationViewModel: BottomNavigationViewModel by activityViewModels()
    private val storyViewModel: StoryViewModel by viewModels()
    private val narratorViewModel: NarratorViewModel by viewModels()
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
        (requireActivity() as BottomNavigation).showUpButton()
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
                bottomNavigationViewModel.shouldPlayMedia(id)
            }, onOptionClick = ::optionClicked, lifecycleOwner = viewLifecycleOwner
        )
        recyclerView.adapter = storyAdapter
        recyclerView.layoutManager = LinearLayoutManager(
            requireContext()
        )

        val storyId = arguments?.getInt(
            STORY_ID_ARG
        )

        activityLayout = requireActivity().findViewById(
            R.id.activity_layout
        )

        if (storyId != null) {
            narratorViewModel.getNarratorOfStory(storyId)

        }


        return binding.root
    }


    private fun bindObservers() {
        storyViewModel.storyViewState.observe(viewLifecycleOwner, ::handleStoryViewState)
        narratorViewModel.narratorViewState.observe(viewLifecycleOwner, ::handleNarratorViewState)
        bottomNavigationViewModel.bottomNavigationViewState.observe(viewLifecycleOwner, ::handleBottomNavViewState)
    }

    private fun handleStoryViewState(viewState: StoryViewState?) {
        when (viewState) {
            is StoryViewState.FetchedStoriesByIds -> handleStoryViewStateSuccess(viewState.stories)
            is StoryViewState.AddedBookmark -> showFeedbackSnackBar("Added To Bookmarks")
            is StoryViewState.RemovedBookmark -> showFeedbackSnackBar("Removed From Bookmarks")
            is StoryViewState.StoryLiked -> showFeedbackSnackBar("Added To Favorites")
            is StoryViewState.LikedRemoved -> showFeedbackSnackBar("Removed From Favorites")
            is StoryViewState.StoryDownloaded -> showFeedbackSnackBar("Story Saved To My Device")
            is StoryViewState.StoryRemoved -> showFeedbackSnackBar("Story Removed From My Device")
            else -> {}//TODO(Ideally have error handling for each error)
        }
    }

    private fun handleBottomNavViewState(viewState: BottomNavigationViewState?) {
        when (viewState) {
            is BottomNavigationViewState.OnPlayMediaSuccess -> handlePLayMediaSuccess(viewState.id)
            else -> {}//TODO(Ideally have error handling for each error)
        }
    }

    private fun handlePLayMediaSuccess(id:Int){
        bottomNavigationViewModel.playMediaId(id)
    }

    private fun handleNarratorViewState(viewState: NarratorViewState?) {
        when (viewState) {
            is NarratorViewState.FetchedNarrator -> handleNarratorViewStateSuccess(viewState.narrator)
            is NarratorViewState.FetchedStoriesByContributor -> handleContributorViewStateSuccess(
                viewState.contributor
            )
            else -> showFeedbackSnackBar("Connection Failure") //TODO(Ideally have error handling for each error)
        }
    }

    private fun handleNarratorViewStateSuccess(narrator: Narrator?) {
        narrator?.let {
            if (narrator.imageUrl != null) {

                Glide.with(
                    this@NarratorFragment
                ).load(narrator.imageUrl).transition(
                    DrawableTransitionOptions.withCrossFade(100)
                ).into(
                    binding.ivNarratorPic
                )
                binding.tvNarratorName.apply {
                    visibility = View.VISIBLE
                    text = narrator.name
                }
                binding.tvNarratorBio.apply {
                    visibility = View.VISIBLE
                    text = narrator.biography

                }
            }
            if (narrator.url != null) {
                binding.btnVisitNarratorLink.apply {
                    setOnClickListener {
                        openUrl(
                            requireContext(), narrator.url //TODO(INTENT FAILING)
                        )
                    }
                    visibility = View.VISIBLE
                }

                narratorViewModel.getStoriesByContributor(
                    narrator,
                    1
                )
            }
        }
    }

    private fun handleContributorViewStateSuccess(contributor: Contributor) {

        for (story in contributor.data) {
            storyViewModel.cacheRecordOfStory(
                story.id, story.narrationUrl ?: ""
            )
        }
        storyViewModel.getStoriesByIds(contributor.data.map { it.id })

    }


    private fun handleStoryViewStateSuccess(stories: List<Story>) {

        if (stories.isNotEmpty()) {
            binding.tvPublishedStoriesSubtitle.visibility =
                View.VISIBLE
        }
        storyAdapter.submitList(
            stories
        )
    }

    private fun optionClicked(
        option: StoryOption, story: Story
    ) {
        activity?.let { verifiedActivity ->
            context?.let { verifiedContext ->
                onOptionClicked(
                    option,
                    story,
                    storyViewModel,
                    verifiedActivity,
                    verifiedContext
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

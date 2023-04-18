package com.autio.android_app.ui.stories.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.contains
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.api.ApiClient
import com.autio.android_app.data.api.model.PlaylistOption
import com.autio.android_app.data.api.model.StoryOption
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.FragmentPlaylistBinding
import com.autio.android_app.ui.stories.adapter.DownloadedStoryAdapter
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.ui.stories.view_model.BottomNavigationViewModel
import com.autio.android_app.ui.stories.view_model.StoryViewModel
import com.autio.android_app.ui.stories.view_states.StoryViewState
import com.autio.android_app.util.navController
import com.autio.android_app.util.onOptionClicked
import com.autio.android_app.util.showFeedbackSnackBar
import com.autio.android_app.util.showPlaylistOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    @Inject
    lateinit var prefRepository: PrefRepository

    //TODO(Move service calls)
    @Inject
    lateinit var apiClient: ApiClient

    private val bottomNavigationViewModel: BottomNavigationViewModel by activityViewModels()
    private val storyViewModel: StoryViewModel by viewModels()
    private lateinit var binding: FragmentPlaylistBinding
    private lateinit var activityLayout: ConstraintLayout
    private lateinit var storyAdapter: DownloadedStoryAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var snackBarView: View
    private var feedbackJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaylistBinding.inflate(
            inflater, container, false
        )

        snackBarView = layoutInflater.inflate(
            R.layout.feedback_snackbar, binding.root as ViewGroup, false
        )

        binding.tvToolbarTitle.text = resources.getString(
            R.string.my_stories_history
        )
        binding.btnBack.setOnClickListener {
            findNavController().navigate(
                R.id.action_history_playlist_to_my_stories
            )
        }

        recyclerView = binding.rvStories
        storyAdapter = DownloadedStoryAdapter(
            { id -> bottomNavigationViewModel.playMediaId(id) }, ::optionClicked
        )
        return binding.root
    }

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        bindObservers()

        activityLayout = requireActivity().findViewById(
            R.id.activity_layout
        )

        storyViewModel.getHistory()
    }

    private fun bindObservers() {
        storyViewModel.storyViewState.observe(viewLifecycleOwner, ::handleViewState)
    }

    private fun handleViewState(viewState: StoryViewState?) {
        when (viewState) {
            is StoryViewState.AddedBookmark -> showFeedbackSnackBar("Added To Bookmarks")
            is StoryViewState.RemovedBookmark -> showFeedbackSnackBar("Removed From Bookmarks")
            is StoryViewState.StoryLiked -> showFeedbackSnackBar("Added To Favorites")
            is StoryViewState.LikedRemoved -> showFeedbackSnackBar("Removed From Favorites")
            is StoryViewState.StoryDownloaded -> showFeedbackSnackBar("Story Saved To My Device")
            is StoryViewState.FetchedStoriesHistory -> showStoryHistory(viewState.stories)
            is StoryViewState.StoryRemoved -> showFeedbackSnackBar("Story Removed From My Device")
            is StoryViewState.FetchedStoriesHistoryFailed -> historyFetchedError()
            else -> showFeedbackSnackBar("Connection Failure") //TODO(Ideally have error handling for each error)
        }
    }

    private fun showStoryHistory(stories: List<Story>) {
        recyclerView.adapter = storyAdapter
//            binding.tvToolbarSubtitle.text =
//                resources.getQuantityString(
//                    R.plurals.toolbar_stories_subtitle,
//                    stories.size,
//                    stories.size
//                )
        binding.pbLoadingStories.visibility = View.GONE
        binding.btnPlaylistOptions.setOnClickListener { view ->
            showPlaylistOptions(
                requireContext(),
                binding.root as ViewGroup,
                view,
                listOf(//PlaylistOption.DOWNLOAD, this is how you add more options to that menu
                    PlaylistOption.CLEAR_HISTORY).map {
                    it.also { option ->
                        option.disabled = stories.isEmpty()
                    }
                }, onOptionClicked = ::onPlaylistOptionClicked
            )
        }
        if (stories.isEmpty()) {
            binding.ivNoContentIcon.setImageResource(R.drawable.ic_history)
            binding.tvNoContentMessage.text = resources.getText(R.string.empty_history_message)
            binding.rlStories.visibility = View.GONE
            binding.llNoContent.visibility = View.VISIBLE
        } else {
            storyAdapter.submitList(stories)
            binding.llNoContent.visibility = View.GONE
            binding.rlStories.visibility = View.VISIBLE
        }
    }


    private fun optionClicked(option: StoryOption, story: Story) {
        when (option) {
            StoryOption.DELETE -> {
                storyViewModel.removeStoryFromHistory(story.id)
                binding.pbLoadingProcess.visibility = View.GONE
                showFeedbackSnackBar("Removed Story")
                navController.navigate(R.id.action_history_playlist_to_my_stories)
            }
            else -> {
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
        }
    }

    private fun historyFetchedError(){
        binding.ivNoContentIcon.setImageResource(R.drawable.ic_history)
        binding.tvNoContentMessage.text = resources.getText(R.string.empty_history_message)
        binding.rlStories.visibility = View.GONE
        binding.llNoContent.visibility = View.VISIBLE
    }

    private fun onPlaylistOptionClicked(option: PlaylistOption) {
        binding.pbLoadingProcess.visibility = View.VISIBLE
        when (option) {
            PlaylistOption.CLEAR_HISTORY -> {
                storyViewModel.clearStoryHistory()
                binding.pbLoadingProcess.visibility = View.GONE
                showFeedbackSnackBar("Cleared History")
                navController.navigate(R.id.action_history_playlist_to_my_stories)

            }
            else -> Log.d(
                "HistoryFragment", "option not available for this playlist"
            )
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

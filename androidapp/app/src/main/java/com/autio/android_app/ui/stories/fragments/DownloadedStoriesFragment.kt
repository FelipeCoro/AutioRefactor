package com.autio.android_app.ui.stories.fragments

import android.os.Bundle
import android.util.Log
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
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.api.model.StoryOption
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.FragmentPlaylistBinding
import com.autio.android_app.ui.stories.adapter.DownloadedStoryAdapter
import com.autio.android_app.ui.stories.view_model.BottomNavigationViewModel
import com.autio.android_app.ui.stories.view_model.StoryViewModel
import com.autio.android_app.ui.stories.view_states.StoryViewState
import com.autio.android_app.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DownloadedStoriesFragment : Fragment() {

    @Inject
    lateinit var prefRepository: PrefRepository

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
            R.string.my_stories_downloaded_stories
        )
        binding.btnBack.setOnClickListener {
            findNavController().navigate(
                R.id.action_downloaded_playlist_to_my_stories
            )
        }

        recyclerView = binding.rvStories
        storyAdapter = DownloadedStoryAdapter(
            onStoryPlay = { id ->
                bottomNavigationViewModel.playMediaId(
                    id
                )
            }, ::onOptionClicked
        )

        return binding.root
    }

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view, savedInstanceState
        )

        bindObservers()

        activityLayout = requireActivity().findViewById(
            R.id.activityRoot
        )

        storyViewModel.downloadedStories.observe(viewLifecycleOwner) { stories ->
            recyclerView.adapter = storyAdapter
//                binding.tvToolbarSubtitle.text =
//                    resources.getQuantityString(
//                        R.plurals.toolbar_stories_subtitle,
//                        stories.size,
//                        stories.size
//                    )
            binding.pbLoadingStories.visibility = View.GONE
            binding.btnPlaylistOptions.setOnClickListener { view ->
                showPlaylistOptions(
                    requireContext(), binding.root as ViewGroup, view, listOf(
                        com.autio.android_app.data.api.model.PlaylistOption.REMOVE
                    ).map {
                        it.also { option ->
                            option.disabled = stories.isEmpty()
                        }
                    }, onOptionClicked = ::onPlaylistOptionClicked
                )
            }
            if (stories.isEmpty()) {
                binding.ivNoContentIcon.setImageResource(R.drawable.ic_download)
                binding.tvNoContentMessage.text = resources.getText(
                    R.string.empty_downloads_message
                )
                binding.rlStories.visibility = View.GONE
                binding.llNoContent.visibility = View.VISIBLE
            } else {
                storyAdapter.submitList(stories)
                binding.llNoContent.visibility = View.GONE
                binding.rlStories.visibility = View.VISIBLE
            }
        }
    }

    fun bindObservers() {
        storyViewModel.storyViewState.observe(viewLifecycleOwner, ::handleViewState)
    }

    private fun handleViewState(viewState: StoryViewState?) {
        when (viewState) {
            is StoryViewState.AddedBookmark -> addedBookmark()
            is StoryViewState.RemovedBookmark -> removeBookmark()
            is StoryViewState.StoryLiked -> storyLiked()
            is StoryViewState.LikedRemoved -> likedRemoved()
            is StoryViewState.StoryRemoved -> removedFromDownload()
            else -> viewStateError() //TODO(Ideally have error handling for each error, ideally would be not to have so many viewstates)
        }
    }

    private fun addedBookmark() {
        showFeedbackSnackBar("Added To Bookmarks")
    }

    private fun removeBookmark() {
        showFeedbackSnackBar("Removed From Bookmarks")
    }

    private fun storyLiked() {
        showFeedbackSnackBar("Added To Favorites")
    }

    private fun likedRemoved() {
        showFeedbackSnackBar("Removed From Favorites")
    }

    private fun removedFromDownload() {
        showFeedbackSnackBar("Story Removed From My Device")
    }

    private fun viewStateError() {
        //TODO(Macro error handling for viewstate error)
        showFeedbackSnackBar("Connection Failure")
    }

    private fun onOptionClicked(
        option: StoryOption, story: DownloadedStoryEntity
    ) {
        when (option) {
            StoryOption.BOOKMARK -> {
                storyViewModel.bookmarkStory(
                    prefRepository.userId, prefRepository.userApiToken, story.id
                )
            }
            StoryOption.REMOVE_BOOKMARK -> {
                storyViewModel.removeBookmarkFromStory(
                    prefRepository.userId, prefRepository.userApiToken, story.id
                )
            }
            StoryOption.LIKE -> {
                storyViewModel.giveLikeToStory(
                    prefRepository.userId, prefRepository.userApiToken, story.id
                )
            }
            StoryOption.REMOVE_LIKE -> {
                storyViewModel.removeLikeFromStory(
                    prefRepository.userId, prefRepository.userApiToken, story.id
                )
            }
            StoryOption.DELETE, StoryOption.REMOVE_DOWNLOAD -> {
                storyViewModel.removeDownloadedStory(
                    story.id
                )
                showFeedbackSnackBar("Story Removed From My Device")
            }
            StoryOption.DIRECTIONS -> openLocationInMapsApp(
                requireActivity(), story.lat, story.lon
            )
            StoryOption.SHARE -> {
                shareStory(requireContext(), story.id)
            }
            else -> Log.d(
                "HistoryFragment", "no action defined for this option"
            )
        }
    }

    private fun onPlaylistOptionClicked(
        option: com.autio.android_app.data.api.model.PlaylistOption
    ) {
        showPaywallOrProceedWithNormalProcess(
            requireActivity(), isActionExclusiveForSignedInUser = true
        ) {
            binding.pbLoadingProcess.visibility = View.VISIBLE
            when (option) {
                com.autio.android_app.data.api.model.PlaylistOption.REMOVE -> {
                    storyViewModel.removeAllDownloads()
                    binding.pbLoadingProcess.visibility = View.GONE
                    showFeedbackSnackBar("Removed Downloads")
                }
                else -> Log.d(
                    "DownloadedStoriesFragment", "option not available for this playlist"
                )
            }
        }
    }

    private fun showFeedbackSnackBar(feedback: String) {
        cancelJob()
        snackBarView.alpha = 1F
        snackBarView.findViewById<TextView>(R.id.tvFeedback).text = feedback
        activityLayout.addView(snackBarView)
        feedbackJob = lifecycleScope.launch {
            delay(2000)
            snackBarView.animate().alpha(0F).withEndAction {
                activityLayout.removeView(snackBarView)
            }
        }
    }

    private fun cancelJob() {
        if (activityLayout.contains(snackBarView)) {
            activityLayout.removeView(snackBarView)
        }
        feedbackJob?.cancel()
    }
}

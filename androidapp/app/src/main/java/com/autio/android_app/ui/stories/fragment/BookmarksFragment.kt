package com.autio.android_app.ui.stories.fragment

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.contains
import androidx.databinding.DataBindingUtil
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
import com.autio.android_app.data.api.model.PlaylistOption
import com.autio.android_app.data.api.model.StoryOption
import com.autio.android_app.data.repository.legacy.FirebaseStoryRepository
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.FragmentPlaylistBinding
import com.autio.android_app.ui.stories.adapter.StoryAdapter
import com.autio.android_app.ui.stories.view_model.BottomNavigationViewModel
import com.autio.android_app.ui.stories.view_model.StoryViewModel
import com.autio.android_app.util.*
import dagger.hilt.EntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@EntryPoint
class BookmarksFragment : Fragment() {
    private val bottomNavigationViewModel: BottomNavigationViewModel by activityViewModels()
    private val storyViewModel: StoryViewModel by viewModels()

    @Inject
    private lateinit var prefRepository: PrefRepository
    private lateinit var binding: FragmentPlaylistBinding
    private lateinit var activityLayout: ConstraintLayout
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var snackBarView: View
    private var feedbackJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_playlist, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindListeners()
        bindObservables()
        initView()

        snackBarView = layoutInflater.inflate(
            R.layout.feedback_snackbar, binding.root as ViewGroup, false
        )

        recyclerView = binding.rvStories

        storyAdapter = StoryAdapter(
            bottomNavigationViewModel.playingStory, onStoryPlay = { id ->
                showPaywallOrProceedWithNormalProcess(requireActivity(), true) {
                    bottomNavigationViewModel.playMediaId(id)
                }
            }, onOptionClick = ::onStoryOptionClicked
        )
        recyclerView.adapter = storyAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        activityLayout = requireActivity().findViewById(R.id.activityRoot)
    }

    private fun initView() {
        binding.tvToolbarTitle.text = resources.getString(R.string.my_stories_bookmarks)

    }

    private fun bindObservables() {
        storyViewModel.bookmarkedStories.observe(viewLifecycleOwner) { stories ->
//            val totalTime =
//                stories.sumOf { it.duration } / 60
//            binding.tvToolbarSubtitle.text =
//                resources.getQuantityString(
//                    R.plurals.toolbar_stories_with_time_subtitle,
//                    stories.size,
//                    stories.size,
//                    totalTime
//                )
            binding.pbLoadingStories.visibility = View.GONE
            if (stories.isEmpty()) {
                binding.ivNoContentIcon.setImageResource(
                    R.drawable.ic_player_bookmark
                )
                binding.tvNoContentMessage.text = resources.getText(
                    R.string.empty_bookmarks_message
                )
                binding.rlStories.visibility = View.GONE
                binding.llNoContent.visibility = View.VISIBLE
            } else {
                binding.btnPlaylistOptions.setOnClickListener {
                    showPlaylistOptions(
                        requireContext(),
                        binding.root as ViewGroup,
                        it,
                        listOf(PlaylistOption.DOWNLOAD, PlaylistOption.REMOVE),
                        onOptionClicked = ::onPlaylistOptionClicked
                    )
                }
                val storiesWithoutRecords = stories.filter { it.recordUrl.isEmpty() }
                if (storiesWithoutRecords.isNotEmpty()) {
                    ApiService.getStoriesByIds(prefRepository.userId,
                        prefRepository.userApiToken,
                        storiesWithoutRecords.map { it.originalId }) { storiesFromAPI ->
                        if (storiesFromAPI != null) {
                            for (story in storiesFromAPI) {
                                storyViewModel.cacheRecordOfStory(
                                    story.id, story.recordUrl
                                )
                            }
                        }
                    }
                }
                storyAdapter.submitList(stories)
                binding.llNoContent.visibility = View.GONE
                binding.rlStories.visibility = View.VISIBLE
            }
        }
    }

    private fun bindListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_bookmarks_playlist_to_my_stories)
        }
    }

    private fun onPlaylistOptionClicked(option: PlaylistOption) {
        showPaywallOrProceedWithNormalProcess(requireActivity(), true) {
            binding.pbLoadingProcess.visibility = View.VISIBLE
            when (option) {
                PlaylistOption.DOWNLOAD -> {}
                PlaylistOption.REMOVE -> {
                    FirebaseStoryRepository.removeAllBookmarks(prefRepository.firebaseKey,
                        onSuccessListener = {
                            storyViewModel.removeAllBookmarks()
                            binding.pbLoadingProcess.visibility = View.GONE
                            showFeedbackSnackBar("Removed All Bookmarks")
                        },
                        onFailureListener = {
                            binding.pbLoadingProcess.visibility = View.GONE
                            showFeedbackSnackBar("Connection Failure")
                        })
                }
                else -> Log.d("BookmarksFragment", "option not available for this playlist")
            }
        }
    }

    private fun onStoryOptionClicked(
        option: StoryOption, story: Story
    ) {
        showPaywallOrProceedWithNormalProcess(
            requireActivity(), isActionExclusiveForSignedInUser = true
        ) {
            when (option) {
                StoryOption.DELETE, StoryOption.REMOVE_BOOKMARK -> {
                    FirebaseStoryRepository.removeBookmarkFromStory(
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
                        })
//                    ApiService.removeBookmarkFromStory(
//                        prefRepository.userId,
//                        prefRepository.userApiToken,
//                        story.originalId
//                    ) {
//                        if (it?.removed == true) {
//                            storyViewModel.removeBookmarkFromStory(
//                                story.id
//                            )
//                            showFeedbackSnackBar(
//                                "Removed From Bookmarks"
//                            )
//                        } else {
//                            showFeedbackSnackBar(
//                                "Connection Failure"
//                            )
//                        }
//                    }
                }
                StoryOption.LIKE -> {
                    FirebaseStoryRepository.giveLikeToStory(story.id,
                        prefRepository.firebaseKey,
                        onSuccessListener = {
                            storyViewModel.setLikeToStory(story.id)
                            showFeedbackSnackBar("Added To Favorites")
                        },
                        onFailureListener = {
                            showFeedbackSnackBar("Connection Failure")
                        })
//                    ApiService.likeStory(
//                        prefRepository.userId,
//                        prefRepository.userApiToken,
//                        story.originalId
//                    ) {
//                        if (it == true) {
//                            storyViewModel.setLikeToStory(
//                                story.id
//                            )
//                            showFeedbackSnackBar(
//                                "Added To Favorites"
//                            )
//                        } else {
//                            showFeedbackSnackBar(
//                                "Connection Failure"
//                            )
//                        }
//                    }
                }
                StoryOption.REMOVE_LIKE -> {
                    FirebaseStoryRepository.removeLikeFromStory(story.id,
                        prefRepository.firebaseKey,
                        onSuccessListener = {
                            storyViewModel.removeLikeFromStory(story.id)
                            showFeedbackSnackBar("Removed From Favorites")
                        },
                        onFailureListener = { showFeedbackSnackBar("Connection Failure") })
                }
                StoryOption.DOWNLOAD -> lifecycleScope.launch {
                    try {
                        val downloadedStory = DownloadedStory.fromStory(
                            requireContext(), story
                        )
                        storyViewModel.downloadStory(downloadedStory!!)
                        showFeedbackSnackBar("Story Saved To My Device")
                    } catch (e: Exception) {
                        Log.e("BookmarksFragment", "exception: ", e)
                        showFeedbackSnackBar("Failed Downloading Story")
                    }
                }
                StoryOption.REMOVE_DOWNLOAD -> {
                    storyViewModel.removeDownloadedStory(story.id)
                    showFeedbackSnackBar("Story Removed From My Device")
                }
                StoryOption.DIRECTIONS -> openLocationInMapsApp(
                    requireActivity(), story.lat, story.lon
                )
                StoryOption.SHARE -> {
                    shareStory(requireContext(), story.id)
                }
                else -> Log.d("BookmarksFragment", "no action defined for this option")
            }
        }
    }

    private fun showFeedbackSnackBar(feedback: String) {
        if (isAdded && activity != null) {
            cancelJob()
            snackBarView.alpha = 1F
            snackBarView.findViewById<TextView>(R.id.tvFeedback).text = feedback
            TransitionManager.beginDelayedTransition(activityLayout, Slide(Gravity.TOP))
            activityLayout.addView(snackBarView)
            feedbackJob = lifecycleScope.launch {
                delay(2000)
                snackBarView.animate().alpha(0F).withEndAction {
                    activityLayout.removeView(snackBarView)
                }
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

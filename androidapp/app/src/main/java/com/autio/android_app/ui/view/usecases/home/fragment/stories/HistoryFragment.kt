package com.autio.android_app.ui.view.usecases.home.fragment.stories

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.model.StoryOption
import com.autio.android_app.data.model.story.DownloadedStory
import com.autio.android_app.data.model.story.Story
import com.autio.android_app.data.repository.ApiService
import com.autio.android_app.data.repository.FirebaseStoryRepository
import com.autio.android_app.data.repository.PrefRepository
import com.autio.android_app.databinding.FragmentPlaylistBinding
import com.autio.android_app.ui.view.usecases.home.BottomNavigation
import com.autio.android_app.ui.view.usecases.home.adapter.StoryAdapter
import com.autio.android_app.ui.viewmodel.BottomNavigationViewModel
import com.autio.android_app.ui.viewmodel.StoryViewModel
import com.autio.android_app.util.InjectorUtils
import com.autio.android_app.util.openLocationInMapsApp
import com.autio.android_app.util.shareStory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HistoryFragment :
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

    private lateinit var binding: FragmentPlaylistBinding

    private lateinit var activityLayout: ConstraintLayout

    private lateinit var storyAdapter: StoryAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var snackBarView: View
    private var feedbackJob: Job? =
        null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentPlaylistBinding.inflate(
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

        binding.tvToolbarTitle.text =
            resources.getString(
                R.string.my_stories_history
            )

        binding.btnBack.setOnClickListener {
            findNavController().navigate(
                R.id.action_history_playlist_to_my_stories
            )
        }

        recyclerView =
            binding.rvStories
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

        activityLayout =
            requireActivity().findViewById(
                R.id.activityRoot
            )

        storyViewModel.storiesHistory.observe(
            viewLifecycleOwner
        ) { stories ->
            binding.btnClearPlaylist.visibility =
                View.GONE
            recyclerView.adapter =
                storyAdapter
            binding.tvToolbarSubtitle.text =
                resources.getQuantityString(
                    R.plurals.toolbar_stories_subtitle,
                    stories.size,
                    stories.size
                )
            binding.pbLoadingStories.visibility =
                View.GONE
            if (stories.isEmpty()) {
                binding.ivNoContentIcon.setImageResource(
                    R.drawable.ic_history
                )
                binding.tvNoContentMessage.text =
                    resources.getText(
                        R.string.empty_history_message
                    )
                binding.rlStories.visibility =
                    View.GONE
                binding.llNoContent.visibility =
                    View.VISIBLE
            } else {
                val storiesWithoutRecords =
                    stories.filter { it.recordUrl.isEmpty() }
                if (storiesWithoutRecords.isNotEmpty()) {
                    apiService.getStoriesByIds(
                        prefRepository.userId,
                        prefRepository.userApiToken,
                        storiesWithoutRecords.map { it.originalId }
                    ) { storiesFromAPI ->
                        if (storiesFromAPI != null) {

                            for (story in storiesFromAPI) {
                                storyViewModel.cacheRecordOfStory(
                                    story.id,
                                    story.recordUrl
                                )
                            }
                        }
                    }
                }
                binding.btnClearPlaylist.apply {
                    visibility =
                        View.VISIBLE
                    setOnClickListener {
                        FirebaseStoryRepository.removeWholeUserHistory(
                            prefRepository.firebaseKey,
                            onSuccessListener = {
                                storyViewModel.clearStoryHistory()
                                showFeedbackSnackBar(
                                    "Cleared History"
                                )
                            }
                        )
                    }
                }
                storyAdapter.submitList(
                    stories
                )
                binding.llNoContent.visibility =
                    View.GONE
                binding.rlStories.visibility =
                    View.VISIBLE
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
                StoryOption.DELETE -> {
                    FirebaseStoryRepository.removeStoryFromUserHistory(
                        prefRepository.firebaseKey,
                        story.id,
                        onSuccessListener = {
                            storyViewModel.removeStoryFromHistory(
                                story.id
                            )
                            showFeedbackSnackBar(
                                "Removed From History"
                            )
                        },
                        onFailureListener = {
                            showFeedbackSnackBar(
                                "Connection Failure"
                            )
                        }
                    )
                }
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
                            )
                        storyViewModel.downloadStory(
                            downloadedStory!!
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
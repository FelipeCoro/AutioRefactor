
package com.autio.android_app.ui.stories.fragment

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
import com.autio.android_app.data.api.ApiClient
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.database.entities.MapPoint
import com.autio.android_app.data.repository.legacy.FirebaseStoryRepository
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.FragmentPlaylistBinding
import com.autio.android_app.ui.stories.adapter.StoryAdapter
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.ui.stories.view_model.BottomNavigationViewModel
import com.autio.android_app.ui.stories.view_model.StoryViewModel
import com.autio.android_app.util.*
import dagger.hilt.EntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@EntryPoint
class FavoritesFragment : Fragment() {

    @Inject
    lateinit var prefRepository: PrefRepository

    //TODO(Move service calls)
    @Inject
    lateinit var apiClient: ApiClient

    private val bottomNavigationViewModel: BottomNavigationViewModel by activityViewModels()
    private val storyViewModel: StoryViewModel by viewModels()

    private lateinit var binding: FragmentPlaylistBinding
    private lateinit var activityLayout: ConstraintLayout

    private lateinit var storyAdapter: StoryAdapter
    private lateinit var recyclerView: RecyclerView

    private var stories: List<MapPoint>? = null

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

        binding.tvToolbarTitle.text =
            resources.getString(
                R.string.my_stories_favorites
            )
        binding.btnBack.setOnClickListener {
            findNavController().navigate(
                R.id.action_favorites_playlist_to_my_stories
            )
        }

        snackBarView =
            layoutInflater.inflate(
                R.layout.feedback_snackbar,
                binding.root as ViewGroup,
                false
            )

        recyclerView =
            binding.rvStories
        storyAdapter =
            StoryAdapter(
                bottomNavigationViewModel.playingStory,
                onStoryPlay = { id ->
                    showPaywallOrProceedWithNormalProcess(
                        requireActivity(),
                        isActionExclusiveForSignedInUser = true
                    ) {
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

        storyViewModel.favoriteStories.observe(
            viewLifecycleOwner
        ) { stories ->
            this.stories =
                stories
            recyclerView.adapter =
                storyAdapter
//            val totalTime =
//                stories.sumOf { it.duration } / 60
//            binding.tvToolbarSubtitle.text =
//                resources.getQuantityString(
//                    R.plurals.toolbar_stories_with_time_subtitle,
//                    stories.size,
//                    stories.size,
//                    totalTime
//                )
            binding.pbLoadingStories.visibility =
                View.GONE
            binding.btnPlaylistOptions.setOnClickListener { view ->
                showPlaylistOptions(
                    requireContext(),
                    binding.root as ViewGroup,
                    view,
                    listOf(
                        com.autio.android_app.data.api.model.PlaylistOption.DOWNLOAD,
                        com.autio.android_app.data.api.model.PlaylistOption.REMOVE
                    ).map {
                        it.also { option ->
                            option.disabled =
                                stories.isEmpty()
                        }
                    },
                    onOptionClicked = ::onPlaylistOptionClicked
                )
            }
            if (stories.isEmpty()) {
                binding.ivNoContentIcon.setImageResource(
                    R.drawable.ic_heart
                )
                binding.tvNoContentMessage.text =
                    resources.getText(
                        R.string.empty_favorites_message
                    )
                binding.rlStories.visibility =
                    View.GONE
                binding.llNoContent.visibility =
                    View.VISIBLE
            } else {
                val storiesWithoutRecords =
                    stories.filter { it.recordUrl.isEmpty() }
                if (storiesWithoutRecords.isNotEmpty()) {
                    lifecycleScope.launch{
                   val storiesFromAPI = apiClient.getStoriesByIds(
                        prefRepository.userId,
                        prefRepository.userApiToken,
                        storiesWithoutRecords.map { it.originalId }
                    )
                        if (storiesFromAPI.isSuccessful) {
                            for (story in storiesFromAPI) {
                                storyViewModel.cacheRecordOfStory(
                                    story.id,
                                    story.recordUrl
                                )
                            }
                        }
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

    private fun onPlaylistOptionClicked(
        option: com.autio.android_app.data.api.model.PlaylistOption
    ) {
        showPaywallOrProceedWithNormalProcess(
            requireActivity(),
            isActionExclusiveForSignedInUser = true
        ) {
            binding.pbLoadingProcess.visibility =
                View.VISIBLE
            when (option) {
                com.autio.android_app.data.api.model.PlaylistOption.DOWNLOAD -> {

                }
                com.autio.android_app.data.api.model.PlaylistOption.REMOVE -> {
                    FirebaseStoryRepository.removeAllLikes(
                        prefRepository.firebaseKey,
                        stories!!.map { it.id },
                        onSuccessListener = {
                            storyViewModel.removeAllBookmarks()
                            binding.pbLoadingProcess.visibility =
                                View.GONE
                            showFeedbackSnackBar(
                                "Removed All Bookmarks"
                            )
                        },
                        onFailureListener = {
                            binding.pbLoadingProcess.visibility =
                                View.GONE
                            showFeedbackSnackBar(
                                "Connection Failure"
                            )
                        }
                    )
                }
                else -> Log.d(
                    "FavoritesFragment",
                    "option not available for this playlist"
                )
            }
        }
    }

    private fun onOptionClicked(
        option: com.autio.android_app.data.api.model.StoryOption,
        story: Story
    ) {
        showPaywallOrProceedWithNormalProcess(
            requireActivity(),
            isActionExclusiveForSignedInUser = true
        ) {
            when (option) {
                com.autio.android_app.data.api.model.StoryOption.BOOKMARK -> {
                    // TODO: change Firebase code with commented code once stable
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
//                    ApiService.bookmarkStory(
//                        prefRepository.userId,
//                        prefRepository.userApiToken,
//                        story.originalId
//                    ) {
//                        if (it != null) {
//                            storyViewModel.bookmarkStory(
//                                story.id
//                            )
//                            showFeedbackSnackBar(
//                                "Added To Bookmarks"
//                            )
//                        } else {
//                            showFeedbackSnackBar(
//                                "Connection Failure"
//                            )
//                        }
//                    }
                }
                com.autio.android_app.data.api.model.StoryOption.REMOVE_BOOKMARK -> {
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
                        }
                    )
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
                com.autio.android_app.data.api.model.StoryOption.DELETE, com.autio.android_app.data.api.model.StoryOption.REMOVE_LIKE -> {
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
                com.autio.android_app.data.api.model.StoryOption.DOWNLOAD -> lifecycleScope.launch {
                    try {
                        val downloadedStory =
                            DownloadedStoryEntity.fromStory(
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
                com.autio.android_app.data.api.model.StoryOption.REMOVE_DOWNLOAD -> {
                    storyViewModel.removeDownloadedStory(
                        story.id
                    )
                    showFeedbackSnackBar(
                        "Story Removed From My Device"
                    )
                }
                com.autio.android_app.data.api.model.StoryOption.DIRECTIONS -> openLocationInMapsApp(
                    requireActivity(),
                    story.lat,
                    story.lon
                )
                com.autio.android_app.data.api.model.StoryOption.SHARE -> {
                    shareStory(
                        requireContext(),
                        story.id
                    )
                }
                else -> Log.d(
                    "FavoritesFragment",
                    "no action defined for this option"
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

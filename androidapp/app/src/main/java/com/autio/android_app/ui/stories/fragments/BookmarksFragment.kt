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
import com.autio.android_app.data.api.ApiClient
import com.autio.android_app.data.api.model.PlaylistOption
import com.autio.android_app.data.api.model.StoryOption
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.FragmentPlaylistBinding
import com.autio.android_app.domain.mappers.toDto
import com.autio.android_app.domain.mappers.toModel
import com.autio.android_app.ui.stories.adapter.StoryAdapter
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.ui.stories.view_model.BottomNavigationViewModel
import com.autio.android_app.ui.stories.view_model.StoryViewModel
import com.autio.android_app.ui.stories.view_states.StoryViewState
import com.autio.android_app.util.openLocationInMapsApp
import com.autio.android_app.util.shareStory
import com.autio.android_app.util.showPaywallOrProceedWithNormalProcess
import com.autio.android_app.util.showPlaylistOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BookmarksFragment : Fragment() {

    private val bottomNavigationViewModel: BottomNavigationViewModel by activityViewModels()
    private val storyViewModel: StoryViewModel by viewModels()
    private lateinit var binding: FragmentPlaylistBinding
    private lateinit var activityLayout: ConstraintLayout
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var snackBarView: View
    private var feedbackJob: Job? = null

    //TODO(Remove rep calls)
    @Inject
    lateinit var prefRepository: PrefRepository

    //TODO(Move service calls)
    @Inject
    lateinit var apiClient: ApiClient

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
                    lifecycleScope.launch {
                        val ids =
                            storiesWithoutRecords.map { it.id }
                        val result = apiClient.getStoriesByIds(
                            prefRepository.userId,
                            prefRepository.userApiToken,
                            ids
                        )
                        if (result.isSuccessful) {
                            val storiesFromAPI = result.body()
                            if (storiesFromAPI != null) {
                                for (story in storiesFromAPI) {
                                    storyViewModel.cacheRecordOfStory(story.id, story.recordUrl)
                                }
                            }
                        }
                    }
                }
            }
            storyAdapter.submitList(stories.map { it.toModel() })
            binding.llNoContent.visibility = View.GONE
            binding.rlStories.visibility = View.VISIBLE
        }

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
                   // FirebaseStoryRepository.removeAllBookmarks(prefRepository.firebaseKey,
                   //   onSuccessListener = {
                   //       //         storyViewModel.removeAllBookmarks() //TODO(We need a backend method for online removal)
                   //       binding.pbLoadingProcess.visibility = View.GONE
                   //       showFeedbackSnackBar("Removed All Bookmarks")
                   //   },
                   //   onFailureListener = {
                   //       binding.pbLoadingProcess.visibility = View.GONE
                   //       showFeedbackSnackBar("Connection Failure")
                   //   })
                }
                else -> Log.d("BookmarksFragment", "option not available for this playlist")
            }
        }
    }

    //TODO(This is all replicated code with authorFragment need to extract this into a util funtion later)
    private fun onStoryOptionClicked(
        option: StoryOption, story: Story
    ) {
        showPaywallOrProceedWithNormalProcess(
            requireActivity(), isActionExclusiveForSignedInUser = true
        ) {
            //  TODO(I dont see add bookmark option, need to double check if needed latter)
            when (option) {
                StoryOption.DELETE, StoryOption.REMOVE_BOOKMARK -> {

                    storyViewModel.bookmarkStory(
                        prefRepository.userId,
                        prefRepository.userApiToken,
                        story.id
                    )

                }
                StoryOption.LIKE -> {
                    storyViewModel.giveLikeToStory(
                        prefRepository.userId,
                        prefRepository.userApiToken,
                        story.id
                    )
                }
                StoryOption.REMOVE_LIKE -> {
                    storyViewModel.removeLikeFromStory(
                        prefRepository.userId,
                        prefRepository.userApiToken,
                        story.id
                    )
                }
                StoryOption.DOWNLOAD -> lifecycleScope.launch {
                    try {
                        val downloadedStory =
                            DownloadedStoryEntity.fromStory(requireContext(), story.toDto())
                        storyViewModel.downloadStory(downloadedStory!!)
                        showFeedbackSnackBar("Story Saved To My Device")
                    } catch (e: Exception) {
                        Log.e("BookmarksFragment", "exception: ", e)
                        showFeedbackSnackBar("Failed Downloading Story")
                    }
                }
                StoryOption.REMOVE_DOWNLOAD -> {
                    storyViewModel.removeDownloadedStory(
                        story.id
                    ) }
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

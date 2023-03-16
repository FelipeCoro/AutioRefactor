package com.autio.android_app.ui.stories.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.autio.android_app.R
import com.autio.android_app.data.api.ApiClient
import com.autio.android_app.data.api.model.PlaylistOption
import com.autio.android_app.data.api.model.StoryOption
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.FragmentPlaylistBinding
import com.autio.android_app.ui.stories.adapter.StoryAdapter
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.ui.stories.view_model.BottomNavigationViewModel
import com.autio.android_app.ui.stories.view_model.StoryViewModel
import com.autio.android_app.ui.stories.view_states.StoryViewState
import com.autio.android_app.util.onOptionClicked
import com.autio.android_app.util.showFeedbackSnackBar
import com.autio.android_app.util.showPaywallOrProceedWithNormalProcess
import com.autio.android_app.util.showPlaylistOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
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
                showPaywallOrProceedWithNormalProcess(prefRepository, requireActivity(), true) {
                    bottomNavigationViewModel.playMediaId(id)
                }
            }, onOptionClick = ::optionClicked, lifecycleOwner = viewLifecycleOwner
        )
        recyclerView.adapter = storyAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        activityLayout = requireActivity().findViewById(R.id.activity_layout)
    }

    private fun bindListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_bookmarks_playlist_to_my_stories)
        }
    }

    private fun initView() {
        binding.tvToolbarTitle.text = resources.getString(R.string.my_stories_bookmarks)

    }

    private fun bindObservables() {
        storyViewModel.storyViewState.observe(viewLifecycleOwner, ::handleViewState)
    }

    private fun handleViewState(viewState: StoryViewState?) {
        when (viewState) {
            is StoryViewState.AddedBookmark -> showFeedbackSnackBar("Added To Bookmarks")
            is StoryViewState.RemovedBookmark -> showFeedbackSnackBar("Removed From Bookmarks")
            is StoryViewState.StoryLiked -> showFeedbackSnackBar("Added To Favorites")
            is StoryViewState.LikedRemoved -> showFeedbackSnackBar("Removed From Favorites")
            is StoryViewState.StoryDownloaded -> showFeedbackSnackBar("Story Saved To My Device")
            is StoryViewState.FetchedBookmarkedStories -> showAllBookmarkedStories(viewState.stories)
            is StoryViewState.StoryRemoved -> showFeedbackSnackBar("Story Removed From My Device")
            else -> showFeedbackSnackBar("Connection Failure") //TODO(Ideally have error handling for each error)
        }
    }


    private fun showAllBookmarkedStories(stories: List<Story>) {
        recyclerView.adapter = storyAdapter
//                stories.sumOf { it.duration } / 60
//            binding.tvToolbarSubtitle.text =
//                resources.getQuantityString(
//                    R.plurals.toolbar_stories_with_time_subtitle,
//                    stories.size,
//                    stories.size,
//                    totalTime
//                )
        binding.pbLoadingStories.visibility = View.GONE
        binding.btnPlaylistOptions.setOnClickListener {
            showPlaylistOptions(
                requireContext(),
                binding.root as ViewGroup,
                it,
                listOf(PlaylistOption.DOWNLOAD, PlaylistOption.REMOVE),
                onOptionClicked = ::onPlaylistOptionClicked
            )
        }
        if (stories.isEmpty()) {
            binding.ivNoContentIcon.setImageResource(R.drawable.ic_player_bookmark)
            binding.tvNoContentMessage.text = resources.getText(R.string.empty_bookmarks_message)
            binding.rlStories.visibility = View.GONE
            binding.llNoContent.visibility = View.VISIBLE
        } else {

            storyAdapter.submitList(stories)
            binding.llNoContent.visibility = View.GONE
            binding.rlStories.visibility = View.VISIBLE


            //          val storiesWithoutRecords = stories.filter { it.recordUrl.isEmpty() }
            //          if (storiesWithoutRecords.isNotEmpty()) {
            //              lifecycleScope.launch {
            //                  val ids =
            //                      storiesWithoutRecords.map { it.id }
            //                  val result = apiClient.getStoriesByIds(
            //                      prefRepository.userId,
            //                      prefRepository.userApiToken,
            //                      ids
            //                  )
            //                  if (result.isSuccessful) {
            //                      val storiesFromAPI = result.body()!!//TODO(REVIEW THIS QUICK FIX)
            //                      for (story in storiesFromAPI) {
            //                          storyViewModel.cacheRecordOfStory(story.id, story.recordUrl)
            //                      }
            //                  }
            //              }
            //          }
            //      }

        }


    }


    private fun onPlaylistOptionClicked(option: PlaylistOption) {
        showPaywallOrProceedWithNormalProcess(prefRepository, requireActivity(), true) {
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

    private fun cancelJob() {
        if (activityLayout.contains(snackBarView)) {
            activityLayout.removeView(snackBarView)
        }
        feedbackJob?.cancel()
    }
}

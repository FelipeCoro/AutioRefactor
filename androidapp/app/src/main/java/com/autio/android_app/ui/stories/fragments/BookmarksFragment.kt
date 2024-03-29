package com.autio.android_app.ui.stories.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.api.model.PlaylistOption
import com.autio.android_app.data.api.model.StoryOption
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

@AndroidEntryPoint
class BookmarksFragment : Fragment() {

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
        storyAdapter = DownloadedStoryAdapter(
            { id -> bottomNavigationViewModel.playMediaId(id) }, ::optionClicked
        )
        storyViewModel.getBookmarkedStoriesByIds()

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
            is StoryViewState.FetchedBookmarkedStories -> showAllBookmarkedStories(viewState.stories)
            else -> {}
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
        binding.btnPlaylistOptions.setOnClickListener { view ->
            showPlaylistOptions(
                requireContext(),
                binding.root as ViewGroup,
                view,
                listOf(PlaylistOption.REMOVE).map {
                    it.also { option ->
                        option.disabled = stories.isEmpty()
                    }
                }, onOptionClicked = ::onPlaylistOptionClicked
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


    private fun optionClicked(option: StoryOption, story: Story) {
        when (option) {
            StoryOption.DELETE -> {
                storyViewModel.removeBookmarkFromStory(story.id)
                binding.pbLoadingProcess.visibility = View.GONE
                showFeedbackSnackBar("Removed Bookmark")
                navController.navigate(R.id.action_bookmarks_playlist_to_my_stories)
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


        private fun onPlaylistOptionClicked(option: PlaylistOption) {

            binding.pbLoadingProcess.visibility = View.VISIBLE
            when (option) {
                PlaylistOption.REMOVE -> {
                    storyViewModel.removeAllBookmarks()
                    binding.pbLoadingProcess.visibility = View.GONE
                    showFeedbackSnackBar("Removed Bookmarks")
                    navController.navigate(R.id.action_bookmarks_playlist_to_my_stories)
                }
                else -> Log.d("BookmarksFragment", "option not available for this playlist")
            }
        }
    }

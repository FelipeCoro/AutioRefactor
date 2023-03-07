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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.R
import com.autio.android_app.data.api.ApiClient
import com.autio.android_app.data.api.model.PlaylistOption
import com.autio.android_app.data.api.model.StoryOption
import com.autio.android_app.data.database.entities.DownloadedStoryEntity
import com.autio.android_app.data.database.entities.MapPointEntity
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.FragmentPlaylistBinding
import com.autio.android_app.domain.mappers.toDto
import com.autio.android_app.domain.mappers.toModel
import com.autio.android_app.ui.stories.adapter.StoryAdapter
import com.autio.android_app.ui.stories.models.Story
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

    private var stories: List<MapPointEntity>? = null

    private lateinit var snackBarView: View
    private var feedbackJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaylistBinding.inflate(
            inflater, container, false
        )
        bindObservers()

        binding.tvToolbarTitle.text = resources.getString(
            R.string.my_stories_favorites
        )
        binding.btnBack.setOnClickListener {
            findNavController().navigate(
                R.id.action_favorites_playlist_to_my_stories
            )
        }

        snackBarView = layoutInflater.inflate(
            R.layout.feedback_snackbar, binding.root as ViewGroup, false
        )

        recyclerView = binding.rvStories
        storyAdapter = StoryAdapter(
            bottomNavigationViewModel.playingStory, onStoryPlay = { id ->
                showPaywallOrProceedWithNormalProcess(
                    requireActivity(), isActionExclusiveForSignedInUser = true
                ) {
                    bottomNavigationViewModel.playMediaId(
                        id
                    )
                }
            }, onOptionClick = ::onOptionClicked
        )
        recyclerView.adapter = storyAdapter
        recyclerView.layoutManager = LinearLayoutManager(
            requireContext()
        )

        activityLayout = requireActivity().findViewById(
            R.id.activityRoot
        )

        storyViewModel.favoriteStories.observe(
            viewLifecycleOwner
        ) { stories ->
            this.stories = stories
            recyclerView.adapter = storyAdapter
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
            binding.btnPlaylistOptions.setOnClickListener { view ->
                showPlaylistOptions(
                    requireContext(), binding.root as ViewGroup, view, listOf(
                        com.autio.android_app.data.api.model.PlaylistOption.DOWNLOAD,
                        com.autio.android_app.data.api.model.PlaylistOption.REMOVE
                    ).map {
                        it.also { option ->
                            option.disabled = stories.isEmpty()
                        }
                    }, onOptionClicked = ::onPlaylistOptionClicked
                )
            }
            if (stories.isEmpty()) {
                binding.ivNoContentIcon.setImageResource(
                    R.drawable.ic_heart
                )
                binding.tvNoContentMessage.text = resources.getText(
                    R.string.empty_favorites_message
                )
                binding.rlStories.visibility = View.GONE
                binding.llNoContent.visibility = View.VISIBLE
            } else {
                val storiesWithoutRecords = stories.filter { it.recordUrl.isEmpty() }
                if (storiesWithoutRecords.isNotEmpty()) {
                    lifecycleScope.launch {
                        val storiesFromAPI = apiClient.getStoriesByIds(prefRepository.userId,
                            prefRepository.userApiToken,
                            //TODO DTO has a val "original id" we need to see if mapPointEntity might need it
                            storiesWithoutRecords.map { it.toModel().toDto().id })
                        if (storiesFromAPI.isSuccessful) {
                            for (story in storiesFromAPI.body()!!) { //TODO(need to extract list from result)
                                storyViewModel.cacheRecordOfStory(
                                    story.id, story.recordUrl
                                )
                            }
                        }
                    }
                }
                storyAdapter.submitList(stories.map { it.toModel() })
                binding.llNoContent.visibility = View.GONE
                binding.rlStories.visibility = View.VISIBLE
            }
        }
        return binding.root
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

    private fun onPlaylistOptionClicked(
        option: PlaylistOption
    ) {
        showPaywallOrProceedWithNormalProcess(
            requireActivity(), isActionExclusiveForSignedInUser = true
        ) {
            binding.pbLoadingProcess.visibility = View.VISIBLE
            when (option) {
                PlaylistOption.DOWNLOAD -> {
                    //TODO(Add Method)

                }
                PlaylistOption.REMOVE -> {
                    //TODO(LOCAL METHOD)
                  //  FirebaseStoryRepository.removeAllLikes(prefRepository.firebaseKey,
              //    stories!!.map { it.id },
              //    onSuccessListener = {
              //        storyViewModel.removeAllBookmarks() TODO(Need online BACKEND method)
              //        binding.pbLoadingProcess.visibility = View.GONE
              //        showFeedbackSnackBar(
              //            "Removed All Bookmarks"
              //        )
              //    },
              //    onFailureListener = {
              //        binding.pbLoadingProcess.visibility = View.GONE
              //        showFeedbackSnackBar(
              //            "Connection Failure"
              //        )
              //    })
                }
                else -> Log.d(
                    "FavoritesFragment", "option not available for this playlist"
                )
            }
        }
    }

    private fun onOptionClicked(
        option: StoryOption, story: Story
    ) {
        showPaywallOrProceedWithNormalProcess(
            requireActivity(), isActionExclusiveForSignedInUser = true
        ) { //TODO(No LIKE option?)
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
                StoryOption.REMOVE_LIKE -> {
                    storyViewModel.removeLikeFromStory(
                        prefRepository.userId, prefRepository.userApiToken, story.id
                    )
                }
                StoryOption.DOWNLOAD -> lifecycleScope.launch {
                    try {
                        val downloadedStory = DownloadedStoryEntity.fromStory(
                            requireContext(), story.toDto() //TODO(temp fix)
                        )
                        storyViewModel.downloadStory(
                            downloadedStory!!
                        )
                        showFeedbackSnackBar(
                            "Story Saved To My Device"
                        )
                    } catch (e: Exception) {
                        Log.e(
                            "BookmarksFragment", "exception: ", e
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
                }
                StoryOption.DIRECTIONS -> openLocationInMapsApp(
                    requireActivity(), story.lat, story.lon
                )
                StoryOption.SHARE -> {
                    shareStory(
                        requireContext(), story.id
                    )
                }
                else -> Log.d(
                    "FavoritesFragment", "no action defined for this option"
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

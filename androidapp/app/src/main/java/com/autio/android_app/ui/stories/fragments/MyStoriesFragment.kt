package com.autio.android_app.ui.stories.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.autio.android_app.R
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.FragmentMyStoriesBinding
import com.autio.android_app.ui.stories.models.User
import com.autio.android_app.ui.stories.view_model.MyStoriesViewModel
import com.autio.android_app.ui.stories.view_states.MyStoriesViewState
import com.autio.android_app.util.bottomNavigationActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyStoriesFragment : Fragment() {

    @Inject
    lateinit var prefRepository: PrefRepository
    private lateinit var binding: FragmentMyStoriesBinding
    private val viewModel: MyStoriesViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_stories, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindObservables()
        bindListeners()
        viewModel.initView()
    }

    private fun bindObservables() {
        viewModel.viewState.observe(viewLifecycleOwner, ::handleViewState)
    }

    private fun handleViewState(myStoriesViewState: MyStoriesViewState?) {
        myStoriesViewState?.let { viewState ->
            when (viewState) {
                is MyStoriesViewState.OnGetUser -> handleGetUser(viewState.user)
                is MyStoriesViewState.OnGetUserFailed -> handleGetUserFailed()
            }
        }
    }

    private fun handleGetUser(user: User) {

        if (user.isGuest && !user.isPremiumUser) {
            binding.signInLayout.root.isGone = false
            binding.myStoriesList.root.isGone = true
        }

        if (!user.isGuest && !user.isPremiumUser) {
            binding.signInLayout.root.isGone = false
            binding.myStoriesList.root.isGone = true
            binding.signInLayout.btnSignIn.visibility = View.GONE
            binding.signInLayout.btnSignup.visibility = View.GONE
            binding.signInLayout.btnChoosePlanStories.visibility = View.VISIBLE
        }

        if (user.isPremiumUser) {
            binding.myStoriesList.root.isGone = false
            binding.signInLayout.root.isGone = true
        }
    }

    private fun handleGetUserFailed() {
        binding.signInLayout.root.isVisible = false
        binding.myStoriesList.root.isVisible = false
    }

    private fun bindListeners() {
        val navController = findNavController()
        binding.myStoriesList.bookmarks.setOnClickListener {
            navController.navigate(R.id.action_my_stories_to_bookmarks_playlist)
        }
        binding.myStoriesList.favorites.setOnClickListener {
            navController.navigate(R.id.action_my_stories_to_favorites_playlist)
        }
        binding.myStoriesList.history.setOnClickListener {
            navController.navigate(R.id.action_my_stories_to_history_playlist)
        }
        binding.myStoriesList.downloadedFavorites.setOnClickListener {
            navController.navigate(R.id.action_my_stories_to_downloaded_playlist)
        }
        binding.signInLayout.btnSignIn.setOnClickListener { goToSignIn() }
        binding.signInLayout.btnSignup.setOnClickListener { goToSignUp() }
        binding.signInLayout.btnChoosePlanStories.setOnClickListener { goToPlans() }
    }

    private fun goToSignIn() {
        findNavController().navigate(R.id.action_my_stories_to_authentication_nav)
        bottomNavigationActivity?.finish()
    }

    private fun goToSignUp() {
        findNavController().navigate(R.id.action_my_stories_to_authentication_nav)
        bottomNavigationActivity?.finish()
    }

    private fun goToPlans() {
        bottomNavigationActivity?.showPayWall()
      //  bottomNavigationActivity?.finish()
    }
}

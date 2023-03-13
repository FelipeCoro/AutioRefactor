package com.autio.android_app.ui.stories.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import com.autio.android_app.R
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.FragmentMyStoriesBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyStoriesFragment : Fragment() {

    @Inject
    lateinit var prefRepository: PrefRepository
    private lateinit var binding: FragmentMyStoriesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyStoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindListeners()
        initView()
    }

    private fun initView() {
        val isUser = false //isUserGuest()

        binding.lySignIn.isVisible = isUser
        binding.lyStoriesList.isGone = isUser

        if (!isUser) {
            val navController = findNavController()
            binding.llBookmarksPlaylist.setOnClickListener {
                navController.navigate(R.id.action_my_stories_to_bookmarks_playlist)
            }
            binding.llFavoritesPlaylist.setOnClickListener {
                navController.navigate(R.id.action_my_stories_to_favorites_playlist)
            }
            binding.llHistoryPlaylist.setOnClickListener {
                navController.navigate(R.id.action_my_stories_to_history_playlist)
            }
            binding.llDownloadedPlaylist.setOnClickListener {
                navController.navigate(R.id.action_my_stories_to_downloaded_playlist)
            }
        }
    }

    private fun bindListeners() {
        binding.btnSignIn.setOnClickListener { goToSignIn() }
        binding.btnSignup.setOnClickListener { goToSignUp() }
    }

    private fun goToSignIn() {
        val request =
            NavDeepLinkRequest.Builder
                .fromUri("android-app://navigation.autio.app/sign-in".toUri())
                .build()
        val nav = findNavController()
        nav.navigate(request)
    }

    private fun goToSignUp() {
        val request =
            NavDeepLinkRequest.Builder
                .fromUri("android-app://navigation.autio.app/login".toUri())
                .build()
        val nav = findNavController()
        nav.navigate(request)
    }

    private fun isUserGuest(): Boolean = prefRepository.isUserGuest
}

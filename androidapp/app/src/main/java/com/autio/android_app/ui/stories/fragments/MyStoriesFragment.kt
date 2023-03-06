package com.autio.android_app.ui.stories.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.autio.android_app.R
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.FragmentMyStoriesBinding
import com.autio.android_app.ui.login.SignInActivity
import com.autio.android_app.ui.login.SignUpActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyStoriesFragment : Fragment() {

    @Inject
    lateinit var prefRepository: PrefRepository

    private var _binding: FragmentMyStoriesBinding? =
        null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentMyStoriesBinding.inflate(
                inflater,
                container,
                false
            )

        createView()
        intentFunctions()
        return binding.root
    }

    private fun createView() {
        if (isUserGuest()) {
            binding.lySignIn.visibility =
                View.VISIBLE
            binding.lyStoriesList.visibility =
                View.GONE
        } else {
            binding.lySignIn.visibility =
                View.GONE
            binding.lyStoriesList.visibility =
                View.VISIBLE
            binding.llBookmarksPlaylist.setOnClickListener {
                findNavController().navigate(
                    R.id.action_my_stories_to_bookmarks_playlist
                )
            }
            binding.llFavoritesPlaylist.setOnClickListener {
                findNavController().navigate(
                    R.id.action_my_stories_to_favorites_playlist
                )
            }
            binding.llHistoryPlaylist.setOnClickListener {
                findNavController().navigate(
                    R.id.action_my_stories_to_history_playlist
                )
            }
            binding.llDownloadedPlaylist.setOnClickListener {
                findNavController().navigate(
                    R.id.action_my_stories_to_downloaded_playlist
                )
            }
        }
    }

    private fun intentFunctions() {
        binding.btnSignIn.setOnClickListener {
            goToSignIn()
        }
        binding.btnSignup.setOnClickListener {
            goToSignUp()
        }
    }

    private fun goToSignIn() {
        val signInIntent =
            Intent(
                activity,
                SignInActivity::class.java
            )
        startActivity(
            signInIntent
        )
    }

    private fun goToSignUp() {
        val signUpIntent =
            Intent(
                activity,
                SignUpActivity::class.java
            )
        startActivity(
            signUpIntent
        )
    }

    private fun isUserGuest(): Boolean =
        prefRepository.isUserGuest
}

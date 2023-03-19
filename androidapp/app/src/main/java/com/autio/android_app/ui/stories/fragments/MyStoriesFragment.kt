package com.autio.android_app.ui.stories.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.net.toUri
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import com.autio.android_app.R
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.FragmentMyStoriesBinding
import com.autio.android_app.ui.stories.BottomNavigation
import com.autio.android_app.ui.subscribe.view_model.PurchaseViewModel
import com.autio.android_app.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyStoriesFragment : Fragment() {

    @Inject
    lateinit var prefRepository: PrefRepository
    private lateinit var binding: FragmentMyStoriesBinding
    private val purchaseViewModel: PurchaseViewModel by viewModels()
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
       val isUser = isUserGuest()//TODO(tHIS SHOULD ACTUALLY BE isUserSubcribed [From RevenueCAT])
      //  val isUser = purchaseViewModel.customerInfo.value?.entitlements?.get(Constants.REVENUE_CAT_ENTITLEMENT)?.isActive == true

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
        findNavController().navigate(R.id.action_my_stories_to_authentication_nav)
        (activity as BottomNavigation).finish()
    }

    private fun goToSignUp() {
        findNavController().navigate(R.id.action_my_stories_to_authentication_nav)
        (activity as BottomNavigation).finish()
    }
}

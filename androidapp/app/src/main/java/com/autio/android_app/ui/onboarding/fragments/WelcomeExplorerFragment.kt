package com.autio.android_app.ui.onboarding.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.autio.android_app.R
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.databinding.FragmentWelcomeExplorerBinding
import com.autio.android_app.ui.stories.BottomNavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WelcomeExplorerFragment : Fragment() {

    @Inject
    lateinit var prefRepository: PrefRepository
    private lateinit var binding: FragmentWelcomeExplorerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentWelcomeExplorerBinding.inflate(
            inflater, container, false
        )

        binding.buttonLetsGo.setOnClickListener {
            findNavController().navigate(R.id.action_welcomeExplorerFragment_to_viewPagerFragment)
        }
        initView()

        return binding.root
    }

    private fun initView() {
        if (isOnBoardingFinished()) {
            if (isUserLoggedIn()) {
                startActivity(Intent(activity, BottomNavigation::class.java))
                activity?.finish()
            } else findNavController().navigate(R.id.action_welcomeExplorerFragment_to_authentication_nav)
        }
        startAnimation()
    }

    private fun startAnimation() {
        binding.tvWelcome.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).duration = 2000
        }
        binding.ivDivider.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(2000).withEndAction {
                binding.tvAppDescription.apply {
                    alpha = 0f
                    visibility = View.VISIBLE
                    animate().alpha(1f).setDuration(2000).withEndAction {
                        binding.buttonLetsGo.apply {
                            alpha = 0f
                            visibility = View.VISIBLE
                            animate().alpha(1f).duration = 2000
                        }
                    }
                }
            }
        }
    }

    private fun isOnBoardingFinished(): Boolean {
        val sharedPreferences =
            activity?.getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return sharedPreferences?.getBoolean("Finished", false) ?: false
    }

    private fun isUserLoggedIn() =
        prefRepository.userApiToken.isEmpty() //TODO(Shouldn't this be if its NOT empty?)
}
package com.autio.android_app.ui.view.usecases.onboarding.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.autio.android_app.R
import com.autio.android_app.databinding.FragmentWelcomeExplorerBinding

class WelcomeExplorerFragment :
    Fragment() {

    private var _binding: FragmentWelcomeExplorerBinding? =
        null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentWelcomeExplorerBinding.inflate(
                inflater,
                container,
                false
            )

        binding.buttonLetsGo.setOnClickListener {
            findNavController().navigate(
                R.id.action_welcomeExplorerFragment_to_viewPagerFragment
            )
        }

        startAnimation()

        return binding.root
    }

    private fun startAnimation() {
        binding.tvWelcome.apply {
            alpha =
                0f
            visibility =
                View.VISIBLE
            animate().alpha(
                1f
            )
                .duration =
                2000
        }
        binding.ivDivider.apply {
            alpha =
                0f
            x =
                binding.root.x
            visibility =
                View.VISIBLE
            animate().alpha(
                1f
            )
                .setDuration(
                    2000
                )
                .withEndAction {
                    binding.tvAppDescription.apply {
                        alpha =
                            0f
                        visibility =
                            View.VISIBLE
                        animate().alpha(
                            1f
                        )
                            .setDuration(
                                2000
                            )
                            .withEndAction {
                                binding.buttonLetsGo.apply {
                                    alpha =
                                        0f
                                    visibility =
                                        View.VISIBLE
                                    animate().alpha(
                                        1f
                                    )
                                        .duration =
                                        2000
                                }
                            }
                    }
                }
        }
    }
}
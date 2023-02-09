package com.autio.android_app.ui.view.usecases.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.autio.android_app.databinding.FragmentViewPagerBinding
import com.autio.android_app.ui.view.usecases.onboarding.screens.BackgroundLocationFragment
import com.autio.android_app.ui.view.usecases.onboarding.screens.InAppLocationFragment
import com.autio.android_app.ui.view.usecases.onboarding.screens.NotificationsFragment

class ViewPagerFragment :
    Fragment() {

    private var _binding: FragmentViewPagerBinding? =
        null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentViewPagerBinding.inflate(
                inflater,
                container,
                false
            )

        val adapter =
            ViewPagerAdapter(
                arrayOf(
                    NotificationsFragment(),
                    InAppLocationFragment(),
                    BackgroundLocationFragment()
                ),
                requireActivity().supportFragmentManager,
                lifecycle
            )

        binding.viewPager.adapter =
            adapter
        binding.viewPager.isUserInputEnabled =
            false

        return binding.root
    }

}
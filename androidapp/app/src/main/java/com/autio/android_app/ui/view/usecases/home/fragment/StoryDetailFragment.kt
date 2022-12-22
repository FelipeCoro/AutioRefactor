package com.autio.android_app.ui.view.usecases.home.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.autio.android_app.ui.viewmodel.BottomNavigationViewModel
import com.autio.android_app.ui.viewmodel.StoryDetailFragmentViewModel
import com.autio.android_app.util.InjectorUtils

class StoryDetailFragment : Fragment() {
    private val bottomNavigationViewModel by activityViewModels<BottomNavigationViewModel> {
        InjectorUtils.provideBottomNavigationViewModel(requireContext())
    }
    private val storyDetailViewModel by viewModels<StoryDetailFragmentViewModel> {
        InjectorUtils.provideBottomNavigationViewModel(requireContext())
    }

//    lateinit var binding:
    companion object {
        fun newInstance() = StoryDetailFragment()
    }
}
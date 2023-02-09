package com.autio.android_app.ui.view.usecases.home.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.autio.android_app.ui.viewmodel.BottomNavigationViewModel

class StoryDetailFragment :
    Fragment() {
    private val bottomNavigationViewModel by activityViewModels<BottomNavigationViewModel>()
//    private val storyDetailViewModel by viewModels<StoryDetailFragmentViewModel>()

    //    lateinit var binding:
    companion object {
        fun newInstance() =
            StoryDetailFragment()
    }
}
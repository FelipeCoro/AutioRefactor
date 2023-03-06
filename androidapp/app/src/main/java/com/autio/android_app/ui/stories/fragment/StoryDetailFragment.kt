package com.autio.android_app.ui.stories.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.autio.android_app.ui.stories.models.Story
import com.autio.android_app.ui.stories.view_model.BottomNavigationViewModel
import com.autio.android_app.ui.stories.view_model.StoryDetailFragmentViewModel
import com.autio.android_app.util.showError
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoryDetailFragment : Fragment() {

    private val viewmodel: StoryDetailFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val storyParam = savedInstanceState?.let {
            it.getParcelable(STORY) as Story?
        }
        viewmodel.initView(storyParam)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private val bottomNavigationViewModel: BottomNavigationViewModel by activityViewModels()
//    private val storyDetailViewModel by viewModels<StoryDetailFragmentViewModel>()

    //    lateinit var binding:
    companion object {
        const val STORY = "story-detail-param"
        fun newInstance(story: Story): StoryDetailFragment {
            return StoryDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(STORY, story)
                }
            }
        }
    }
}

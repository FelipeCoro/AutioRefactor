package com.autio.android_app.ui.view.usecases.home.fragment.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.adapter.StoryAdapter
import com.autio.android_app.data.model.story.Story
import com.autio.android_app.databinding.FragmentMapPlaylistBinding
import com.autio.android_app.ui.viewmodel.BottomNavigationViewModel
import com.autio.android_app.ui.viewmodel.StoryViewModel
import com.autio.android_app.util.InjectorUtils

class MapPlaylistFragment :
    Fragment() {
    private val bottomNavigationViewModel by activityViewModels<BottomNavigationViewModel> {
        InjectorUtils.provideBottomNavigationViewModel(
            requireContext()
        )
    }

    private var _binding: FragmentMapPlaylistBinding? =
        null
    private val binding get() = _binding!!

    private lateinit var stories: List<Story>
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var storyViewModel: StoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentMapPlaylistBinding.inflate(
                inflater,
                container,
                false
            )
        stories =
            ArrayList()
        recyclerView =
            binding.mapPlaylist
        storyViewModel =
            ViewModelProvider(
                this
            )[StoryViewModel::class.java]
        storyAdapter =
            StoryAdapter(
                stories
            )
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        bottomNavigationViewModel.storiesInScreen.observe(viewLifecycleOwner) { stories ->
            recyclerView.adapter =
                storyAdapter
            storyAdapter.getAllData(
                stories.values.toList()
            )
        }
    }
}
package com.autio.android_app.ui.view.usecases.home.fragment.map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.autio.android_app.adapter.StoryAdapter
import com.autio.android_app.data.database.repository.StoryRepository
import com.autio.android_app.data.model.story.Story
import com.autio.android_app.databinding.FragmentMapPlaylistBinding
import com.autio.android_app.ui.viewmodel.StoryViewModel

class MapPlaylistFragment :
    Fragment() {
    private var _binding: FragmentMapPlaylistBinding? =
        null
    private val binding get() = _binding!!

    private lateinit var stories: List<Story>
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var storyRepository: StoryRepository
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
        storyRepository =
            StoryRepository(
                application = requireActivity().application
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
        storyViewModel.getAllStories()
            .observe(
                viewLifecycleOwner
            ) { t ->
                recyclerView.adapter =
                    storyAdapter
                if (t != null) {
                    storyAdapter.getAllData(
                        t
                    )
                }
            }
        return binding.root
    }
}
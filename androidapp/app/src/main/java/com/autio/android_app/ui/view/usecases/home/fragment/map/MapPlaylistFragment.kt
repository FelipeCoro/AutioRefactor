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
import com.autio.android_app.data.database.StoryDataBase
import com.autio.android_app.data.database.repository.StoryRepository
import com.autio.android_app.data.model.story.StoryDto
import com.autio.android_app.data.model.story.StoryResponse
import com.autio.android_app.data.repository.ApiService
import com.autio.android_app.data.repository.PrefRepository
import com.autio.android_app.databinding.FragmentMapPlaylistBinding
import com.autio.android_app.ui.viewmodel.StoryViewModel
import kotlinx.coroutines.runBlocking

class MapPlaylistFragment :
    Fragment() {
    private var _binding: FragmentMapPlaylistBinding? =
        null
    private val binding get() = _binding!!
    private val prefRepository by lazy {
        PrefRepository(
            requireContext()
        )
    }

    private lateinit var stories: List<StoryResponse>
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var storyRepository: StoryRepository
    private lateinit var storyViewModel: StoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(
            "STORIES",
            "Stories being fetched..."
        )
        _binding =
            FragmentMapPlaylistBinding.inflate(
                inflater,
                container,
                false
            )
        val database =
            container?.context?.let {
                StoryDataBase.getInstance(
                    it
                )
            }
        if (database != null) {
            storyRepository =
                StoryRepository(
                    database.storyDao()
                )
        }
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
        requestStories()
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

    private fun requestStories() {
        // TODO (Marshysaurus): Remove dummy ids
        val dummyIds =
            StoryDto(
                listOf(
                    "1",
                    "2",
                    "3"
                )
            )
        ApiService().getStoriesByIds(
            getUserId(),
            getApiToken(),
            dummyIds
        ) {
            if (it != null) {
                Log.d(
                    "STORIES",
                    "Stories fetched: $it"
                )
                runBlocking {
                    for (s in it) {
                        storyRepository.addPointer(
                            s
                        )
                    }
                }
            }
        }
    }

    private fun getUserId(): Int =
        prefRepository.userId

    private fun getApiToken(): String =
        "Bearer " + prefRepository.userApiToken
}
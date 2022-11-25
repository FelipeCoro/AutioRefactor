package com.autio.android_app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.autio.android_app.data.database.StoryDataBase
import com.autio.android_app.data.database.repository.StoryRepository
import com.autio.android_app.data.model.story.StoryResponse

class StoryViewModel(
    application: Application
) :
    AndroidViewModel(
        application
    ) {
    private val database =
        StoryDataBase.getInstance(
            application
        )
    private val storyRepository =
        StoryRepository(
            database
                .storyDao()
        )
    private val getAllStories =
        storyRepository.getAllStories()

    fun insert(
        story: StoryResponse
    ) {
        storyRepository.addPointer(
            story
        )
    }

    fun getAllStories(): LiveData<List<StoryResponse>> =
        getAllStories
}
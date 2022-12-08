package com.autio.android_app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.autio.android_app.data.database.repository.StoryRepository
import com.autio.android_app.data.model.story.Story

class StoryViewModel(
    application: Application
) :
    AndroidViewModel(
        application
    ) {
    private val storyRepository =
        StoryRepository(
            application
        )

    fun addStories(
        stories: List<Story>
    ) {
        storyRepository.addPointers(
            stories
        )
    }

    fun getAllStories(): LiveData<List<Story>> = storyRepository.getAllStories()

    fun getStoriesByIds(ids: Array<String>): LiveData<Array<Story>> = storyRepository.getStoriesByIds(ids)
}
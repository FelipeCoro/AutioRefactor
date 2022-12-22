package com.autio.android_app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.autio.android_app.data.database.repository.StoryRepository
import com.autio.android_app.data.model.story.Story

class StoryViewModel(
    application: Application
) :
    AndroidViewModel(
        application
    ) {

    private val storyRepository = StoryRepository(application)

    fun addStories(
        stories: List<Story>
    ) =
        storyRepository.addPointers(
            stories
        )

    fun getLiveStories(): LiveData<List<Story>> =
        storyRepository.getLiveStories()

    suspend fun getStoryById(
        ids: String
    ): Story? =
        storyRepository.getStoryById(
            ids
        )
}
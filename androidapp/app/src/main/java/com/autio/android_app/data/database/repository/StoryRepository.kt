package com.autio.android_app.data.database.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.autio.android_app.data.database.StoryDataBase
import com.autio.android_app.data.model.story.Story
import java.util.concurrent.Executors
import javax.inject.Inject

class StoryRepository @Inject constructor(
    application: Application
) {
    private val database =
        StoryDataBase.getInstance(
            application
        )
    private val storyDao = database.storyDao()

    private val executor = Executors.newSingleThreadExecutor()

    fun getLiveStories(): LiveData<List<Story>> {
        return storyDao.readLiveStories()
    }

    suspend fun getStories(): List<Story> {
        return storyDao.readStories()
    }

    suspend fun getStoryById(id: String) = storyDao.getStoryById(id)

    fun getStoriesByIds(ids: Array<String>): LiveData<Array<Story>> {
        return storyDao.readStoriesWithIds(ids)
    }

    fun getLastFetchedStory(): Story {
        return storyDao.readLastFetchedStory()
    }

    fun addPointers(
        stories: List<Story>
    ) {
        executor.execute {
            storyDao.addStories(
                stories
            )
        }
    }
}
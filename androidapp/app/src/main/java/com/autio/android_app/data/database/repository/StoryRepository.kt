package com.autio.android_app.data.database.repository

import android.app.Application
import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.LiveData
import com.autio.android_app.data.database.StoryDataBase
import com.autio.android_app.data.database.dao.StoryDao
import com.autio.android_app.data.model.story.Story
import javax.inject.Inject

class StoryRepository @Inject constructor(
    application: Application
) {
    private val database =
        StoryDataBase.getInstance(
            application
        )
    private val storyDao =
        database.storyDao()

    fun getAllStories(): LiveData<List<Story>> {
        return storyDao.readAllStories()
    }

    fun getStoriesByIds(ids: Array<String>): LiveData<Array<Story>> {
        return storyDao.readStoriesWithIds(ids)
    }

    fun getLastFetchedStory(): Story {
        return storyDao.readLastFetchedStory()
    }

    fun addPointers(
        story: List<Story>
    ) {
        InsertAsyncTask(
            storyDao
        ).execute(
            story
        )
    }

    private class InsertAsyncTask(
        storyDao: StoryDao
    ) : AsyncTask<List<Story>, Unit, Unit>() {
        private var localStoryDao: StoryDao

        init {
            localStoryDao =
                storyDao
        }

        @Deprecated(
            "Deprecated in Java"
        )
        override fun doInBackground(
            vararg stories: List<Story>
        ) {
            localStoryDao.addStories(
                stories[0]
            )
        }
    }
}
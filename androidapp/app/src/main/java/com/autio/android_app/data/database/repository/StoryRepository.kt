package com.autio.android_app.data.database.repository

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import com.autio.android_app.data.database.dao.StoryDao
import com.autio.android_app.data.model.story.StoryResponse
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class StoryRepository @Inject constructor(
    private val storyDao: StoryDao
) {
    fun getAllStories(): LiveData<List<StoryResponse>> =
        storyDao.readAllData()

    fun addPointer(
        storyResponse: StoryResponse
    ) {
        InsertAsyncTask(
            storyDao
        ).execute(
            storyResponse
        )
    }

    private class InsertAsyncTask(
        val storyDao: StoryDao
    ) : AsyncTask<StoryResponse, Unit, Unit>() {
        @Deprecated(
            "Deprecated in Java"
        )
        override fun doInBackground(
            vararg stories: StoryResponse
        ) {
            runBlocking {
                storyDao.addPointer(
                    stories[0]
                )
            }
        }

    }
}
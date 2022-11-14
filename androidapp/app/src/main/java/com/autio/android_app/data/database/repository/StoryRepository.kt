package com.autio.android_app.data.database.repository

import androidx.lifecycle.LiveData
import com.autio.android_app.data.database.dao.StoryDao
import com.autio.android_app.data.database.entities.StoryEntitie

class StoryRepository(
    private val storyDao: StoryDao
) {

    val readAllData: LiveData<List<StoryEntitie>> =
        storyDao.readAllData()

    suspend fun addPointer(
        storyEntitie: StoryEntitie
    ) {
        storyDao.addPointer(
            storyEntitie
        )
    }
}
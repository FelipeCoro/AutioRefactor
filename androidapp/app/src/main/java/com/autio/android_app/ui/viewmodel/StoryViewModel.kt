package com.autio.android_app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.autio.android_app.data.database.StoryDataBase
import com.autio.android_app.data.database.entities.StoryEntitie
import com.autio.android_app.data.database.repository.StoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StoryViewModel(application: Application):AndroidViewModel(application) {

    private val readAllData: LiveData<List<StoryEntitie>>
    private val repository: StoryRepository

    init {
        val storyDao = StoryDataBase.getDatabase(application).storyDao()
        repository = StoryRepository(storyDao)
        readAllData = repository.readAllData
    }

    fun addPointer(storyEntity: StoryEntitie){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addPointer(storyEntity)
        }
    }
}
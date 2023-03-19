package com.autio.android_app.ui.stories.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.ui.di.coroutines.IoDispatcher
import com.autio.android_app.ui.stories.models.Narrator
import com.autio.android_app.ui.stories.view_states.NarratorViewState
import com.autio.android_app.ui.stories.view_states.StoryViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NarratorViewModel @Inject constructor(
    private val autioRepository: AutioRepository,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _narratorViewState = MutableLiveData<NarratorViewState>()
    val narratorViewState: LiveData<NarratorViewState> = _narratorViewState

    private fun setViewState(newState: NarratorViewState) {
        _narratorViewState.postValue(newState)
    }

    fun getNarratorOfStory(storyId: Int) {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                autioRepository.getNarratorOfStory(storyId)
            }.onSuccess { result ->
                val narrator = result.getOrNull()
                if (narrator != null) {
                    setViewState(NarratorViewState.FetchedNarrator(narrator))
                }
            }.onFailure {
                setViewState(NarratorViewState.FetchedNarratorFailed)
            }

        }
    }

    fun getStoriesByContributor(narrator: Narrator, page: Int) {
        viewModelScope.launch(coroutineDispatcher) {
            runCatching {
                autioRepository.getStoriesByContributor(narrator.id, 1)
            }.onSuccess { result ->
                val storiesByContributor = result.getOrNull()
                if (storiesByContributor != null) {
                    setViewState(NarratorViewState.FetchedStoriesByContributor(storiesByContributor))
                }
            }.onFailure {
                setViewState(NarratorViewState.FetchedStoriesByContributorFailed)
            }

        }
    }
}


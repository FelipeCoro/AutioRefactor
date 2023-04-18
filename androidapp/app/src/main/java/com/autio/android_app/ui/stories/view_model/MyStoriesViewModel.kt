package com.autio.android_app.ui.stories.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.ui.di.coroutines.IoDispatcher
import com.autio.android_app.ui.stories.view_states.MyStoriesViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyStoriesViewModel @Inject constructor(
    private val autioRepository: AutioRepository,
    @IoDispatcher
    private val coroutineDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _viewState = MutableLiveData<MyStoriesViewState>()
    val viewState: LiveData<MyStoriesViewState> = _viewState

    private fun setViewState(newState: MyStoriesViewState) {
        _viewState.postValue(newState)
    }

    fun initView() {
        viewModelScope.launch(coroutineDispatcher) {
            kotlin.runCatching {
                autioRepository.getUserAccount()
            }.onSuccess { user ->
                if (user == null) {
                    setViewState(MyStoriesViewState.OnGetUserFailed)
                } else setViewState(MyStoriesViewState.OnGetUser(user))
            }.onFailure {
                setViewState(MyStoriesViewState.OnGetUserFailed)
            }
        }
    }


}
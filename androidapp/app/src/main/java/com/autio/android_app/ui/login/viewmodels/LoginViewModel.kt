package com.autio.android_app.ui.login.viewmodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.ui.di.coroutines.IoDispatcher
import com.autio.android_app.ui.stories.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val autioRepository: AutioRepository,
    private val prefRepository: PrefRepository,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _viewState = MutableLiveData<LoginViewState>()
    val viewState: LiveData<LoginViewState> = _viewState


    fun loginGuest() {

        viewModelScope.launch(coroutineDispatcher) {
            val result = autioRepository.loginAsGuest()
            if (result.isSuccess) { //TODO(Double check this)
                result.getOrNull()?.let { saveGuestInfo(it) }
                result.getOrNull()?.let { handleSuccessViewState(it) }
            } else
                handleFailureViewState(result.exceptionOrNull() as Exception)

        }
    }

    private fun saveGuestInfo(guestResponse: User) {
        with(prefRepository) {
            isUserGuest = true
            userId = guestResponse.id
            userApiToken = guestResponse.apiToken
            remainingStories = 5
        }
    }

    private fun handleSuccessViewState(data: User) {
        setViewState(LoginViewState.SuccessViewState(data))

    }

    private fun handleFailureViewState(exception: Exception) {
        setViewState(LoginViewState.ErrorViewState(exception))
    }

    private fun setViewState(loginViewState: LoginViewState) {
        _viewState.postValue(loginViewState)
    }

}

sealed interface LoginViewState {
    data class ErrorViewState(val exception: Exception) : LoginViewState
    data class SuccessViewState(val data: User) : LoginViewState
}




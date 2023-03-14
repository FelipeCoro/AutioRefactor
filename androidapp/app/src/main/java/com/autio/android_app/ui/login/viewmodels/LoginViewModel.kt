package com.autio.android_app.ui.login.viewmodels


import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.ui.di.coroutines.IoDispatcher
import com.autio.android_app.ui.login.viewstates.LoginViewState
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

    val isLoading = ObservableBoolean(false)
    private val _viewState = MutableLiveData<LoginViewState>()
    val viewState: LiveData<LoginViewState> = _viewState

    fun loginGuest() {
        isLoading.set(true)
        viewModelScope.launch(coroutineDispatcher) {
            kotlin.runCatching {
                val result = autioRepository.loginAsGuest()
                result.getOrNull()
            }.onSuccess { user ->
                user?.let {
                    saveGuestInfo(user)
                    setViewState(LoginViewState.GuestLoginSuccess(user))
                } ?: setViewState(LoginViewState.LoginError)
            }
                .onFailure { setViewState(LoginViewState.LoginError) }

        }
    }

    private fun saveGuestInfo(guestResponse: User) {
        prefRepository.userApiToken = guestResponse.apiToken
        with(prefRepository) {
            isUserGuest = true
            userId = guestResponse.id
            userApiToken = guestResponse.apiToken
            remainingStories = 5
        }
    }

    private fun setViewState(loginViewState: LoginViewState) {
        _viewState.postValue(loginViewState)
        isLoading.set(false)
    }

}




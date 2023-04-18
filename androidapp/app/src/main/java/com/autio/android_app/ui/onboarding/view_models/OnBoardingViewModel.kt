package com.autio.android_app.ui.onboarding.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.ui.di.coroutines.IoDispatcher
import com.autio.android_app.ui.onboarding.view_states.OnBoardingViewState
import com.autio.android_app.ui.onboarding.view_states.OnBoardingViewState.NavigateToAllowNotifications
import com.autio.android_app.ui.onboarding.view_states.OnBoardingViewState.NavigateToBackgroundLocation
import com.autio.android_app.ui.onboarding.view_states.OnBoardingViewState.NavigateToHome
import com.autio.android_app.ui.onboarding.view_states.OnBoardingViewState.NavigateToInAppLocationPermission
import com.autio.android_app.ui.onboarding.view_states.OnBoardingViewState.NavigateToLogin
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val autioRepository: AutioRepository,
    @IoDispatcher
    private val coroutineDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _viewState = MutableLiveData<OnBoardingViewState>()
    val viewState: LiveData<OnBoardingViewState> = _viewState

    private fun setViewState(newViewState: OnBoardingViewState) {
        _viewState.postValue(newViewState)

    }

    fun initView(
        isOnBoardingFinished: Boolean,
        notifyPermission: Boolean,
        inAppPermission: Boolean,
        backgroundPermission: Boolean,
    ) {
        viewModelScope.launch(coroutineDispatcher) {
            if (isOnBoardingFinished) {
                handleFinishedOnBoarding()
            } else {
                when {
                    !notifyPermission -> setViewState(NavigateToAllowNotifications)
                    !inAppPermission -> setViewState(NavigateToInAppLocationPermission)
                    !backgroundPermission -> setViewState(NavigateToBackgroundLocation)
                }
            }
        }
    }

    private suspend fun handleFinishedOnBoarding() {
        val isUserLoggedIn = autioRepository.isUserLoggedIn()
        if (isUserLoggedIn) {
            setViewState(NavigateToHome)
        } else {
            setViewState(NavigateToLogin)
        }
    }

}
package com.autio.android_app.ui.onboarding.view_states

sealed interface OnBoardingViewState {
    object NavigateToAllowNotifications : OnBoardingViewState
    object NavigateToInAppLocationPermission : OnBoardingViewState
    object NavigateToBackgroundLocation : OnBoardingViewState
    object NavigateToLogin : OnBoardingViewState
    object NavigateToHome : OnBoardingViewState
}
package com.autio.android_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autio.android_app.data.repository.ApiService
import com.autio.android_app.data.repository.datasource.local.AutioLocalDataSource
import com.autio.android_app.data.repository.prefs.PrefRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel
class AccountFragmentViewModel(
    private val autioLocalDataSourceImpl: AutioLocalDataSource,
    private val prefRepository: PrefRepository,
) : ViewModel() {


    fun fetchUserData() {
        ApiService.getProfileData(
            prefRepository.userId,
            prefRepository.userApiToken
        ) { profile ->
            if (profile?.categories != null) {
                autioLocalDataSourceImpl.addUserCategories(
                    profile.categories.toTypedArray()
                )
            }
        }
    }

    fun updateProfile(
        name: String,
        email: String,
        categories: ArrayList<Category>,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val infoUser =
            com.autio.android_app.data.api.model.account.ProfileDto(
                email,
                name,
                categories
            )
        ApiService.updateProfile(
            prefRepository.userId,
            prefRepository.userApiToken,
            infoUser
        ) {
            if (it != null) {
                prefRepository.userName =
                    name
                prefRepository.userEmail =
                    email
                onSuccess.invoke()
            } else {
                onFailure.invoke()
            }
        }
    }

    fun saveCategoriesOrder(
        categories: List<Category>,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val infoUser =
            com.autio.android_app.data.api.model.account.ProfileDto(
                prefRepository.userEmail,
                prefRepository.userName,
                categories
            )
        ApiService.updateProfile(
            prefRepository.userId,
            prefRepository.userApiToken,
            infoUser
        ) {
            if (it != null) {
                viewModelScope.launch(
                    Dispatchers.IO
                ) {
                    autioLocalDataSourceImpl.updateCategories(
                        it.categories.toTypedArray()
                    )
                }
                onSuccess.invoke()
            } else {
                onFailure.invoke()
            }
        }
    }
}

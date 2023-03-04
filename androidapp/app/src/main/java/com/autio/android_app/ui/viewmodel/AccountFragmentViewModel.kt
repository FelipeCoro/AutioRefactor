package com.autio.android_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autio.android_app.data.api.ApiClient
import com.autio.android_app.data.repository.datasource.local.AutioLocalDataSource
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.ui.stories.models.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel
class AccountFragmentViewModel(
    private val autioLocalDataSourceImpl: AutioLocalDataSource,
    private val prefRepository: PrefRepository,
) : ViewModel() {


    fun fetchUserData() {
        ApiClient (
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
        ApiClient.updateProfile(
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
        ApiClient.updateProfile(
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

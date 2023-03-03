package com.autio.android_app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autio.android_app.data.repository.datasource.local.AutioLocalDataSourceImpl
import com.autio.android_app.data.entities.story.Category
import com.autio.android_app.data.repository.ApiService
import com.autio.android_app.data.repository.legacy.PrefRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccountFragmentViewModel(
    private val app: Application,
    private val autioLocalDataSourceImpl: AutioLocalDataSourceImpl
) : AndroidViewModel(
    app
) {
    private val prefRepository by lazy {
        PrefRepository(
            app
        )
    }

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

    class Factory(
        private val app: Application,
        private val autioLocalDataSourceImpl: AutioLocalDataSourceImpl
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress(
            "unchecked_cast"
        )
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            return AccountFragmentViewModel(
                app,
                autioLocalDataSourceImpl
            ) as T
        }
    }
}

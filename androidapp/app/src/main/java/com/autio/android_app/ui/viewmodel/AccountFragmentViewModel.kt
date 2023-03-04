package com.autio.android_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.autio.android_app.data.api.model.account.ProfileDto
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.ui.stories.models.Category
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class AccountFragmentViewModel(
    private val autioRepository: AutioRepository,
    private val prefRepository: PrefRepository,
) : ViewModel() {


    suspend fun fetchUserData() {
        autioRepository.fetchUserData()
    }

    suspend fun updateProfile(
        name: String,
        email: String,
        categories: ArrayList<Category>,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        val infoUser = ProfileDto(email, name, categories)
        autioRepository.updateProfile(infoUser, onSuccess, onFailure)
    }

    suspend fun saveCategoriesOrder(
        categories: List<Category>,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        val infoUser = ProfileDto(prefRepository.userEmail, prefRepository.userName, categories)
        autioRepository.updateCategoriesOrder(infoUser, onSuccess, onFailure)
    }
}

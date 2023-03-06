package com.autio.android_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autio.android_app.data.api.model.account.ProfileDto
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.domain.mappers.toEntity
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.ui.stories.models.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountFragmentViewModel @Inject constructor(
    private val autioRepository: AutioRepository,
    private val prefRepository: PrefRepository,
) : ViewModel() {


    fun fetchUserData() {
        viewModelScope.launch {
            autioRepository.fetchUserData()
        }
    }

    fun updateProfile(
        name: String,
        email: String,
        categories: List<Category>,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val infoUser = ProfileDto(email, name, categories.map { it.toEntity() })
            autioRepository.updateProfile(infoUser, onSuccess, onFailure)
        }
    }

    fun saveCategoriesOrder(
        categories: List<Category>,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val infoUser = ProfileDto(prefRepository.userEmail, prefRepository.userName, categories.map{it.toEntity()})
            autioRepository.updateCategoriesOrder(infoUser, onSuccess, onFailure)
        }
    }
}

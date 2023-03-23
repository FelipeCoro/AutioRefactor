package com.autio.android_app.ui.account.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autio.android_app.data.api.model.account.ProfileDto
import com.autio.android_app.domain.mappers.toMapPointEntity
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.ui.account.view_states.AccountViewState
import com.autio.android_app.ui.di.coroutines.IoDispatcher
import com.autio.android_app.ui.stories.models.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountFragmentViewModel @Inject constructor(
    private val autioRepository: AutioRepository,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _viewState = MutableLiveData<AccountViewState>()
    val viewState: LiveData<AccountViewState> = _viewState


    private fun setViewState(newState: AccountViewState) {
        _viewState.postValue(newState)
    }

    fun fetchUserData() {
        viewModelScope.launch(coroutineDispatcher) {

          val data =  autioRepository.fetchUserData()
            val result = data.getOrNull()
            if(result!=null)
            setViewState(AccountViewState.OnUserDataFetched(result))
        }
    }

    fun updateProfile(
        name: String,
        email: String,
        categories: List<Category>,
        onSuccess: () -> Unit = {},
        onFailure: () -> Unit = {}
    ) {
        viewModelScope.launch(coroutineDispatcher) {
            val infoUser = ProfileDto(email, name, categories.map { it.toMapPointEntity() })
            autioRepository.updateProfile(infoUser, onSuccess, onFailure)
        }
    }

    fun saveCategoriesOrder(
        categories: List<Category>, onSuccess: () -> Unit = {}, onFailure: () -> Unit = {}
    ) {
        viewModelScope.launch(coroutineDispatcher) {
          //  val infoUser = ProfileDto( categories.map { it.toMapPointEntity() }) //TODO(This is not used right now but should be changed to UserDao impl. later on)
         //   autioRepository.updateCategoriesOrder(infoUser, onSuccess, onFailure)
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch(coroutineDispatcher) {
            if (validatePasswordSuccess(currentPassword, newPassword, confirmPassword)) {
                val result =
                    autioRepository.changePassword(currentPassword, newPassword, confirmPassword)
                if (result.isSuccess) {
                    setViewState(AccountViewState.OnSuccessPasswordChanged)
                } else {
                    setViewState(AccountViewState.OnFailedPasswordChanged)
                }
            }
        }
    }

    fun deleteAccount(){
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.deleteAccount()
        }
    }

    private fun validatePasswordSuccess(
        currentPassword: String, newPassword: String, confirmPassword: String
    ): Boolean {
        return newPassword == confirmPassword && currentPassword != newPassword
        //TODO(Validate policies for password, this should include minlength, complexity, etc...)
    }
}

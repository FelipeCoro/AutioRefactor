package com.autio.android_app.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.autio.android_app.data.repository.revenue.RevenueCatRepository
import com.autio.android_app.domain.repository.AutioRepository
import com.autio.android_app.ui.di.coroutines.IoDispatcher
import com.autio.android_app.ui.stories.models.AccountRequest
import com.autio.android_app.ui.stories.models.LoginRequest
import com.autio.android_app.ui.stories.models.User
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.models.StoreTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val autioRepository: AutioRepository,
    private val revenueCatRepository: RevenueCatRepository,
    private val prefRepository: PrefRepository,
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _viewState = MutableLiveData<PurchaseViewState>()
    val viewState: LiveData<PurchaseViewState> = _viewState
    val customerInfo = revenueCatRepository.customerInfo

    fun getUserInfo() {
        revenueCatRepository.getUserInfo()
    }

    fun login(loginRequest: LoginRequest) {
        viewModelScope.launch(coroutineDispatcher) {

            val result = autioRepository.login(loginRequest)

            if (result.isSuccess) { //TODO(Double check this)
                result.getOrNull()?.let {
                    saveUserInfo(it)
                    handleSuccessViewState(it)
                    revenueCatRepository.login(result.getOrNull()?.id.toString())
                } ?: handleFailureViewState(result.exceptionOrNull() as Exception)
            } else
                handleFailureViewState(result.exceptionOrNull() as Exception)
        }
    }

    fun createAccount(accountRequest: AccountRequest) {
        viewModelScope.launch(coroutineDispatcher) {
            val result = autioRepository.createAccount(accountRequest)
            if (result.isSuccess) { //TODO(Double check this)
                result.getOrNull()?.let { user ->
                    saveUserInfo(user)
                    handleSuccessViewState(user)
                    revenueCatRepository.login(user.id.toString())
                }
            } else
                handleFailureViewState(result.exceptionOrNull() as Exception)
        }
    }

    private fun handleSuccessViewState(data: User) {
        setViewState(PurchaseViewState.SuccessViewState(data))
    }

    private fun handleFailureViewState(exception: Exception) {
        setViewState(PurchaseViewState.ErrorViewState(exception))
    }

    private fun setViewState(purchaseViewState: PurchaseViewState) {
        _viewState.postValue(purchaseViewState)
    }

    /**
     * Saves user's data in the shared preferences
     */
    private fun saveUserInfo(loginResponse: User) {
        prefRepository.isUserGuest = false
        prefRepository.userId = loginResponse.id
        prefRepository.userApiToken = loginResponse.apiToken
        prefRepository.userName = loginResponse.name
        prefRepository.userEmail = loginResponse.email
        prefRepository.firebaseKey = loginResponse.firebaseKey
        prefRepository.remainingStories = -1
    }

    fun logOut() {
        revenueCatRepository.logOut()
    }

    fun getOfferings(
        onError: (PurchasesError) -> Unit = {},
        onSuccess: (Offerings) -> Unit
    ) {
        revenueCatRepository.getOfferings(
            onError, onSuccess
        )
    }

    fun purchasePackage(
        activity: Activity,
        packageToPurchase: Package,
        onError: ((PurchasesError, Boolean) -> Unit) = { _, _ -> },
        onSuccess: ((StoreTransaction, CustomerInfo) -> Unit) = { _, _ -> }
    ) {
        revenueCatRepository.purchasePackage(
            activity, packageToPurchase, onError, onSuccess
        )
    }

    fun restorePurchase(
        onError: ((PurchasesError) -> Unit) = { },
        onSuccess: ((CustomerInfo) -> Unit) = { }
    ) {
        revenueCatRepository.restorePurchase(
            onError, onSuccess
        )
    }
}

sealed interface PurchaseViewState {
    data class ErrorViewState(val exception: Exception) : PurchaseViewState
    data class SuccessViewState(val data: User) : PurchaseViewState
}

//    class ProductDetails internal constructor(
//        product: String,
//        applicationRepository: CoreApplicationRepository
//    ) {
//        val title =
//            applicationRepository.getProductTitle(
//                product
//            )
//                .asLiveData()
//        val description =
//            applicationRepository.getProductDescription(
//                product
//            )
//                .asLiveData()
//        val price =
//            applicationRepository.getProductPrice(
//                product
//            )
//                .asLiveData()
//        val iconDrawableId = skuToResourceIdMap[sku]!!
//    }
//
//    fun getProductDetails(
//        productId: String
//    ): ProductDetails {
//        return ProductDetails(
//            productId,
//            applicationRepository
//        )
//    }
//
//    fun canBuySku(
//        sku: String
//    ): LiveData<Boolean> {
//        return applicationRepository.canPurchase(
//            sku
//        )
//            .asLiveData()
//    }
//
//    fun isPurchased(
//        sku: String
//    ): LiveData<Boolean> {
//        return applicationRepository.isPurchased(
//            sku
//        )
//            .asLiveData()
//    }
//
//    private fun getPurchasedDate(
//        sku: String
//    ): LiveData<Long> {
//        return applicationRepository.getPurchaseDate(
//            sku
//        )
//            .asLiveData()
//    }
//
//    /**
//     * Starts a billing flow for purchasing Google Play Products.
//     * @param activity
//     * @return whether or not we were able to start the flow
//     */
//    fun buySubscription(
//        activity: Activity,
//        sku: String
//    ) {
//        applicationRepository.purchasePlan(
//            activity,
//            sku
//        )
//    }
//
//    val billingFlowInProcess: LiveData<Boolean>
//        get() = applicationRepository.billingFlowInProcess.asLiveData()
//
//    fun sendMessage(message: Int) {
//        viewModelScope.launch {
//            applicationRepository.sendMessage(message)
//        }
//    }
//
//    val isUserPremium =
//        MediatorLiveData<Boolean>().apply {
//            addSource(
//                isPurchased(
//                    TRAVELER_TRIP_SUBSCRIPTION
//                )
//            ) {
//                val data1 =
//                    it
//                val data2 =
//                    getPurchasedDate(
//                        SINGLE_TRIP_PRODUCT
//                    ).value
//                val data3 =
//                    getPurchasedDate(
//                        ADVENTURER_TRIP_PRODUCT
//                    ).value
//                value =
//                    combineData(
//                        data1,
//                        data2,
//                        data3
//                    )
//            }
//            addSource(
//                getPurchasedDate(
//                    SINGLE_TRIP_PRODUCT
//                )
//            ) {
//                val data1 =
//                    isPurchased(
//                        TRAVELER_TRIP_SUBSCRIPTION
//                    ).value
//                val data2 =
//                    it
//                val data3 =
//                    getPurchasedDate(
//                        ADVENTURER_TRIP_PRODUCT
//                    ).value
//                value =
//                    combineData(
//                        data1,
//                        data2,
//                        data3
//                    )
//            }
//            addSource(
//                getPurchasedDate(
//                    ADVENTURER_TRIP_PRODUCT
//                )
//            ) {
//                val data1 =
//                    isPurchased(
//                        TRAVELER_TRIP_SUBSCRIPTION
//                    ).value
//                val data2 =
//                    getPurchasedDate(
//                        SINGLE_TRIP_PRODUCT
//                    ).value
//                val data3 =
//                    it
//                value =
//                    combineData(
//                        data1,
//                        data2,
//                        data3
//                    )
//            }
//        }
//
//    private fun combineData(
//        data1: Boolean?,
//        data2: Long?,
//        data3: Long?
//    ): Boolean {
//        val currentTime =
//            System.currentTimeMillis()
//        return data1 == true
//                || currentTime - (data2
//            ?: 0) <= SINGLE_TRIP_DURATION
//                || currentTime - (data3
//            ?: 0) <= ADVENTURER_TRIP_DURATION
//    }

/*    class Factory(
        private val revenueCatRepository: RevenueCatRepository
    ) :
        ViewModelProvider.NewInstanceFactory() {

        @Suppress(
            "unchecked_cast"
        )
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T {
            if (modelClass.isAssignableFrom(
                    PurchaseViewModel::class.java
                )
            ) {
                return PurchaseViewModel(
                    revenueCatRepository
                ) as T
            }
            throw IllegalArgumentException(
                "Unknown ViewModel class"
            )
        }
    }
}

private val TAG =
    PurchaseViewModel::class.java.simpleName*/




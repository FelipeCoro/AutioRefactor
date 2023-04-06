package com.autio.android_app.ui.subscribe.view_model

import android.app.Activity
import android.view.View
import androidx.databinding.ObservableBoolean
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
import com.autio.android_app.ui.subscribe.view_states.PurchaseViewState
import com.autio.android_app.util.Constants
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
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher
) : ViewModel() {

    val isLoading = ObservableBoolean(false)
    private val _viewState = MutableLiveData<PurchaseViewState>()
    val viewState: LiveData<PurchaseViewState> = _viewState
    val customerInfo = revenueCatRepository.customerInfo

    fun updateUserInfo(isPremium: Boolean) {
        viewModelScope.launch(coroutineDispatcher) { //TODO(URGENT. THIS MUST GO WITH API CALL)
            autioRepository.updateSubStatus(isPremium)
        }
    }

    fun getUserInfo() {
        viewModelScope.launch(coroutineDispatcher) {
            val reveCatInfo = revenueCatRepository.getUserInfo()
            val result = autioRepository.getUserAccount()
            if (result != null) {
                setViewState(PurchaseViewState.FetchedUserSuccess(result))
            }
            else {
                setViewState(PurchaseViewState.UserNotLoggedIn)
            }
        }
    }

    fun getUserStories() {
        viewModelScope.launch(coroutineDispatcher) {
            val result = autioRepository.getUserAccount()
            if (result != null) {
                setViewState(PurchaseViewState.FetchedUserStoriesSuccess(result))
            }
        }

    }

    fun login(loginRequest: LoginRequest) {
        isLoading.set(true)
        viewModelScope.launch(coroutineDispatcher) {
            val result = autioRepository.login(loginRequest)
            if (result.isSuccess) {
                result.getOrNull()?.let { user ->
                    revenueCatRepository.login(user.id.toString())//TODO(URGENT.UPDATE ISPREMIUM THROUGH API SERVICE)
                    setViewState(PurchaseViewState.OnLoginSuccess(user))
                } ?: setViewState(PurchaseViewState.OnLoginFailed)
            } else setViewState(PurchaseViewState.OnLoginFailed)
        }
    }

    fun createAccount(accountRequest: AccountRequest) {
        isLoading.set(true)

        viewModelScope.launch(coroutineDispatcher) {
            val result = autioRepository.createAccount(accountRequest)
            if (result.isSuccess) {
                result.getOrNull()?.let { user ->
                    revenueCatRepository.login(user.id.toString())
                    setViewState(PurchaseViewState.OnCreatedAccountSuccess(user))
                } ?: setViewState(PurchaseViewState.OnCreatedAccountFailed)
            } else setViewState(PurchaseViewState.OnCreatedAccountFailed)
        }
    }


    private fun setViewState(purchaseViewState: PurchaseViewState) {
        _viewState.postValue(purchaseViewState)
        isLoading.set(false)
    }

    fun logOut() {
        viewModelScope.launch(coroutineDispatcher) {
            revenueCatRepository.logOut()
            autioRepository.clearUserData()
            setViewState(PurchaseViewState.OnSuccessLogOut)
        }
    }

    fun getOfferings() {
        isLoading.set(true)
        viewModelScope.launch(coroutineDispatcher) {
            revenueCatRepository.getOfferings(
                ::handleGetOfferingsFailed, ::handleSuccessOfferingFetched
            )
        }
    }

    private fun handleSuccessOfferingFetched(offerings: Offerings) {
        setViewState(PurchaseViewState.OnOfferingsFetched(offerings))
    }

    private fun handleGetOfferingsFailed(error: PurchasesError) {
        setViewState(PurchaseViewState.OnFetchedOfferingsFailed)
    }

    fun purchasePackage(activity: Activity, packageToPurchase: Package) {
        viewModelScope.launch(coroutineDispatcher) {
            val result = autioRepository.getUserAccount()
            if (result != null) {
                if (!result.isGuest) {
                    revenueCatRepository.purchasePackage(
                        activity,
                        packageToPurchase, ::handlePurchaseError, ::handleSuccessTransaction
                    )
                } else {
                    setViewState(PurchaseViewState.UserNotLoggedIn)
                }
            }
        }
    }

    private fun handleSuccessTransaction(
        storeTransaction: StoreTransaction, customerInfo: CustomerInfo
    ) {
        viewModelScope.launch(coroutineDispatcher) {
            autioRepository.updateSubStatus(true)

            setViewState(PurchaseViewState.SuccessfulPurchase) //TODO URGENT. API CALL WILL HAPPEN HERE)
        }
    }

    private fun handlePurchaseError(error: PurchasesError, userCancelled: Boolean) {

        if (userCancelled)
            setViewState(PurchaseViewState.CancelledPurchase)
        else setViewState(PurchaseViewState.PurchaseError(error.message))
    }

    fun restorePurchase(
        onError: ((PurchasesError) -> Unit) = {}, onSuccess: ((CustomerInfo) -> Unit) = {}
    ) {
        revenueCatRepository.restorePurchase(onError, onSuccess)
    }
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
    PurchaseViewModel::class.java.simpleName
    */

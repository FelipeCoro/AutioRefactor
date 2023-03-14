package com.autio.android_app.ui.subscribe.view_model

import android.app.Activity
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
    val isLoading = ObservableBoolean(false)
    private val _viewState = MutableLiveData<PurchaseViewState>()
    val viewState: LiveData<PurchaseViewState> = _viewState
    val customerInfo = revenueCatRepository.customerInfo

    fun getUserInfo() {
        revenueCatRepository.getUserInfo()
    }

    fun login(loginRequest: LoginRequest) {
        isLoading.set(true)
        viewModelScope.launch(coroutineDispatcher) {
            val result = autioRepository.login(loginRequest)
            if (result.isSuccess) {
                result.getOrNull()?.let { user ->
                    setViewState(PurchaseViewState.OnLoginSuccess(user))
                    revenueCatRepository.login(user.id.toString())
                    revenueCatRepository.getUserInfo()
                    saveUserInfo(user)
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
                    saveUserInfo(user)
                    setViewState(PurchaseViewState.OnCreatedAccountSuccess(user))
                } ?: setViewState(PurchaseViewState.OnCreatedAccountFailed)
            } else setViewState(PurchaseViewState.OnCreatedAccountFailed)
        }
    }


    private fun setViewState(purchaseViewState: PurchaseViewState) {
        _viewState.postValue(purchaseViewState)
        isLoading.set(false)
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
        prefRepository.remainingStories = -1
        if (customerInfo.value?.activeSubscriptions?.isNotEmpty() == true){//TODO(SHOULD BE SOMETHING LIKE THIS BUT IS NOT WORKING)
        prefRepository.userSubIsActive = true}
    }

    fun logOut() {
        revenueCatRepository.logOut()
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
            revenueCatRepository.purchasePackage(
                activity,
                packageToPurchase, ::handlePurchaseError, ::handleSuccessTransaction
            )
        }
    }

    private fun handleSuccessTransaction(
        storeTransaction: StoreTransaction, customerInfo: CustomerInfo
    ) {
        TODO("handleSuccessTransaction Purchase")
    }

    private fun handlePurchaseError(error: PurchasesError, isTrue: Boolean) {
        TODO("handlePurchaseError Purchase")
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

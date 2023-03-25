package com.autio.android_app.ui.subscribe.view_states

import com.autio.android_app.ui.stories.models.User
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Purchases

sealed interface PurchaseViewState {
    data class OnCreatedAccountSuccess(val data: User) : PurchaseViewState
    data class OnLoginSuccess(val data: User) : PurchaseViewState
    data class OnOfferingsFetched(val offerings: Offerings) : PurchaseViewState
    data class FetchedUserSuccess(val data: User) : PurchaseViewState
    data class FetchedUserStoriesSuccess(val user: User) : PurchaseViewState
    object OnCreatedAccountFailed : PurchaseViewState
    object OnLoginFailed : PurchaseViewState
    object OnFetchedOfferingsFailed : PurchaseViewState
    object FetchedUserFailed : PurchaseViewState
    object UserNotLoggedIn : PurchaseViewState
    object SuccessfulPurchase : PurchaseViewState
    object CancelledPurchase : PurchaseViewState

    object OnSuccessLogOut : PurchaseViewState
    data class PurchaseError(val error: String) : PurchaseViewState

}

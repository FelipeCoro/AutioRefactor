package com.autio.android_app.ui.subscribe.view_states

import com.autio.android_app.ui.stories.models.User
import com.revenuecat.purchases.Offerings

sealed interface PurchaseViewState {
    data class OnCreatedAccountSuccess(val data: User) : PurchaseViewState
    data class OnLoginSuccess(val data: User) : PurchaseViewState
    data class OnOfferingsFetched(val offerings: Offerings) : PurchaseViewState

    object OnCreatedAccountFailed : PurchaseViewState
    object OnLoginFailed : PurchaseViewState
    object OnFetchedOfferingsFailed : PurchaseViewState

}

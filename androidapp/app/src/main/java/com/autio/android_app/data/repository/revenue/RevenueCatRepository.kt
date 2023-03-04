package com.autio.android_app.data.repository.revenue

import android.app.Activity
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.revenuecat.purchases.*
import com.revenuecat.purchases.models.StoreTransaction

interface RevenueCatRepository {
    val customerInfo: LiveData<CustomerInfo>

    /**
     * Identifies the user inside the RevenueCat local code so the requests
     * are made to the correct user in the RevenueCat backend service
     * @param userId is the user's identifier (generally obtained from the backend)
     * @param onError an optional error callback
     * @param onSuccess an optional successful callback
     */
    fun login(
        userId: String,
        name: String? = null,
        email: String? = null,
        onError: ((PurchasesError) -> Unit)? = null,
        onSuccess: ((CustomerInfo, Boolean) -> Unit)? = null
    )

    /**
     * Log out from RevenueCat
     * This step is optional since the RevenueCat's login
     * method will replace the user's id, but it doesn't
     * hurt to keep this line of code
     */
    fun logOut()
    fun getOfferings(
        onError: (PurchasesError) -> Unit = { },
        onSuccess: (Offerings) -> Unit
    )

    fun purchasePackage(
        activity: Activity,
        packageToPurchase: Package,
        onError: (PurchasesError, Boolean) -> Unit = { _, _ -> },
        onSuccess: (StoreTransaction, CustomerInfo) -> Unit = { _, _ -> }
    )

    fun restorePurchase(
        onError: (PurchasesError) -> Unit = { },
        onSuccess: (CustomerInfo) -> Unit = { }
    )

    /**
     * The latest CustomerInfo from RevenueCat.
     */
    fun getUserInfo()
    fun updateUserInfo(
        customerInfo: CustomerInfo
    )

    /** Error occurred during a RC process and a custom display is
     * shown (either showing a SnackBar, a popup, etc).
     * An icon (or legend, as RC refers) is displayed along to
     * understand which platform is presenting the issue
     * 😿 -> RC
     * 🤖 -> Google
     * 📦 -> Amazon
     * For better understanding on Android-specific errors, follow next link:
     * https://www.revenuecat.com/docs/errors#android-errors
     */
    fun displayError(
        error: PurchasesError
    )
}

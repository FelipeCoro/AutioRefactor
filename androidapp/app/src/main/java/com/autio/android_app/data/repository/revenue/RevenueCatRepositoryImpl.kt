package com.autio.android_app.data.repository.revenue

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.PurchasesErrorCode
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.logInWith
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.purchasePackageWith
import com.revenuecat.purchases.restorePurchasesWith
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class RevenueCatRepositoryImpl  @Inject constructor(
    @ApplicationContext private val context: Context,
) : RevenueCatRepository {
    private val _customerInfo = MutableLiveData<CustomerInfo>().apply {
        value = null
    }
    override val customerInfo: LiveData<CustomerInfo> = _customerInfo

    /**
     * Identifies the user inside the RevenueCat local code so the requests
     * are made to the correct user in the RevenueCat backend service
     * @param userId is the user's identifier (generally obtained from the backend)
     * @param onError an optional error callback
     * @param onSuccess an optional successful callback
     */
    override fun login(
        userId: String,
        name: String?,
        email: String?,
        onError: ((PurchasesError) -> Unit)?,
        onSuccess: ((CustomerInfo, Boolean) -> Unit)?
    ) {
        with(Purchases.sharedInstance) {
            logInWith(userId, { error ->
                onError?.invoke(error)
                displayError(error)
            }) { customerInfo, created ->

                // Set user's data in RevenueCat
                setDisplayName(name)
                setEmail(email)

                // Update status of this user for subscription details
                updateUserInfo(
                    customerInfo
                )

                // created variable checks whether the user is new to RC
                onSuccess?.invoke(
                    customerInfo, created
                )
            }
        }
    }

    /**
     * Log out from RevenueCat
     * This step is optional since the RevenueCat's login
     * method will replace the user's id, but it doesn't
     * hurt to keep this line of code
     */
    override fun logOut() {
        Purchases.sharedInstance.logOut()
    }

    override fun getOfferings(
        onError: ((PurchasesError) -> Unit), onSuccess: (Offerings) -> Unit
    ) {
        Purchases.sharedInstance.getOfferingsWith(
            onError = onError, onSuccess = onSuccess
        )
    }

    override fun purchasePackage(
        activity: Activity,
        packageToPurchase: Package,
        onError: ((PurchasesError, Boolean) -> Unit),
        onSuccess: ((StoreTransaction, CustomerInfo) -> Unit)
    ) {
        Purchases.sharedInstance.purchasePackageWith(
            activity,
            packageToPurchase,
            onError = onError,
            onSuccess = { transaction, customerInfo ->
                updateUserInfo(customerInfo)
                onSuccess.invoke(
                    transaction, customerInfo
                )
            })
    }

    override fun restorePurchase(
        onError: ((PurchasesError) -> Unit), onSuccess: ((CustomerInfo) -> Unit)
    ) {
        Purchases.sharedInstance.restorePurchasesWith(
            onError
        ) { customerInfo ->
            updateUserInfo(
                customerInfo
            )
            onSuccess.invoke(
                customerInfo
            )
        }
    }

    /**
     * The latest CustomerInfo from RevenueCat.
     */
    override fun getUserInfo() {
        Purchases.sharedInstance.getCustomerInfoWith {
            _customerInfo.postValue(it)
        }
    }

    override fun updateUserInfo(
        customerInfo: CustomerInfo
    ) {
        _customerInfo.postValue(
            customerInfo
        )
    }

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
    override fun displayError(
        error: PurchasesError
    ) {
        with(
            error
        ) {
            print(
                "Error: $code"
            )
            print(
                "Message: $message"
            )
            print(
                "Underlying Error: $underlyingErrorMessage"
            )
            when (code) {
                PurchasesErrorCode.PurchaseNotAllowedError -> {
                    print(
                        "Purchases not allowed on this device."
                    )
                }
                PurchasesErrorCode.PurchaseInvalidError -> {
                    print(
                        "Purchase invalid, check payment source."
                    )
                }
                else -> {}
            }
        }
    }

    init {
        Purchases.debugLogsEnabled = true
        Purchases.configure(
            PurchasesConfiguration.Builder(
                context, "goog_nHYcykYaWBQiHNHuZEzjVkdxLaS"
            ).observerMode(
                false
            ).build()
        )
    }
}

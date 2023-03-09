package com.autio.android_app.util

import android.app.Activity
import android.util.Log
import com.autio.android_app.data.repository.prefs.PrefRepository
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import javax.inject.Inject


class UtilsClass(val prefRepository: PrefRepository) {

    fun showPaywallOrProceedWithNormalProcess(
        activity: Activity,
        isActionExclusiveForSignedInUser: Boolean = false,
        normalProcess: () -> Unit
    ) {
        Purchases.sharedInstance.getCustomerInfoWith {
            if (it.entitlements[Constants.REVENUE_CAT_ENTITLEMENT]?.isActive == true) {
                normalProcess.invoke()
            } else {
                try {
                    val isUserGuest = prefRepository.isUserGuest
                    val remainingStories = prefRepository.remainingStories
                    if ((isActionExclusiveForSignedInUser && isUserGuest) || remainingStories <= 0) {
                        showPaywall(activity)
                    } else {
                        normalProcess.invoke()
                    }
                } catch (exception: java.lang.ClassCastException) {
                    Log.e(
                        "CastException",
                        "Activity is not a subtype of BottomNavigation"
                    )
                }
            }
        }
    }

}

package com.autio.android_app.util

import android.app.Activity
import android.util.Log
import com.autio.android_app.domain.repository.AutioRepository
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import javax.inject.Inject

class ShowPaywallOrProceedWithNormalProcess(
    val activity: Activity,
    private val isActionExclusiveForSignedInUser: Boolean = false,
    private val normalProcess: () -> Unit
) {
    @Inject
    lateinit var autioRepository: AutioRepository



   fun showPaywall() {
       lifecycleScope.launch {
            val user = autioRepository.getUserAccount()
            Purchases.sharedInstance.getCustomerInfoWith {
                if (it.entitlements[Constants.REVENUE_CAT_ENTITLEMENT]?.isActive == true) {
                    normalProcess.invoke()
                } else {
                    try {
                        if (user != null) {
                            if ((isActionExclusiveForSignedInUser && user.isGuest) || (user.remainingStories <= 0)) {
                                showPaywall(activity)
                            } else {
                                normalProcess.invoke()
                            }
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
}

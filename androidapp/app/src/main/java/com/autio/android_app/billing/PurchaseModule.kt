package com.autio.android_app.billing

import android.content.Context
import com.autio.android_app.BuildConfig
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object PurchaseModule {

    //TODO(move this key to sec)
    const val googlePlayPublicKey = "goog_nHYcykYaWBQiHNHuZEzjVkdxLaS"

    @Provides
    fun getRevenueCatPurchasesHandler(
        @ApplicationContext context: Context,
    ): Purchases {
        if (BuildConfig.DEBUG) {
            Purchases.debugLogsEnabled = true
        }

        Purchases.configure(
            PurchasesConfiguration.Builder(context, googlePlayPublicKey)
                .observerMode(false).build()
        )
        return Purchases.sharedInstance
    }

}
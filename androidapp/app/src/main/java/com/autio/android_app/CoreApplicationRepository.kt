package com.autio.android_app

/**
 * The repository uses data from the Billing data source and the game state model together to give
 * a unified version of the state of the game to the ViewModel. It works closely with the
 * BillingDataSource to implement consumable items, premium items, etc.
 */
//class CoreApplicationRepository(
//    private val billingDataSource: BillingDataSource,
//    private val defaultScope: CoroutineScope
//) {
/**
 * Sets up the event that we can use to send messages up to the UI to be used in Snackbars.
 * This collects new purchase events from the BillingDataSource, transforming the known SKU
 * strings into useful String messages, and emitting the messages into the game messages flow.
 */
//    private fun postMessagesFromBillingFlow() {
//        defaultScope.launch {
//            try {
//                billingDataSource.getNewPurchases()
//                    .collect { skuList ->
//                        for (sku in skuList) {
//                            when (sku) {
//                                SINGLE_TRIP_PRODUCT -> {}
//                                ADVENTURER_TRIP_PRODUCT -> {}
//                                TRAVELER_TRIP_SUBSCRIPTION -> {
//                                    // this makes sure that upgrades/downgrades to subscriptions are
//                                    // reflected correctly in our user interface
//                                    billingDataSource.refreshPurchases()
//
//                                }
//                            }
//                        }
//                    }
//            } catch (e: Throwable) {
//                Log.d(
//                    TAG,
//                    "Collection complete"
//                )
//            }
//            Log.d(
//                TAG,
//                "Collection Coroutine Scope Exited"
//            )
//        }
//    }

/**
 * Call to purchase both In-App Products and Subscriptions.
 * Automatic support for upgrading/downgrading subscription.
 * @param activity
 * @param productId
 */
//    fun purchasePlan(
//        activity: Activity,
//        productId: String
//    ) {
//        var oldProductId: String? =
//            null
//        if (productId != TRAVELER_TRIP_SUBSCRIPTION) {
//            oldProductId =
//                TRAVELER_TRIP_SUBSCRIPTION
//        }
//        if (oldProductId == null) {
//            billingDataSource.launchBillingFlow(
//                activity,
//                productId
//            )
//        } else {
//            billingDataSource.launchBillingFlow(
//                activity,
//                productId,
//                oldProductId
//            )
//        }
//    }

/**
 * Return Flow that indicates whether the sku is currently purchased.
 *
 * @param sku the SKU to get and observe the value for
 * @return Flow that returns true if the sku is purchased.
 */
//    fun isPurchased(
//        sku: String
//    ): Flow<Boolean> {
//        return billingDataSource.isPurchased(
//            sku
//        )
//    }

/**
 * We can buy a plan if billing data source allows us to purchase,
 * which means that the item isn't already
 * purchased.
 *
 * @param sku the SKU to get and observe the value for
 * @return Flow<Boolean> that returns true if the sku can be purchased
 */
//    fun canPurchase(
//        sku: String
//    ): Flow<Boolean> {
//        return billingDataSource.canPurchase(
//            sku
//        )
//    }

//    fun getPurchaseDate(
//        sku: String
//    ): Flow<Long> {
//        return billingDataSource.getPurchaseDate(
//            sku
//        )
//    }

//    suspend fun refreshPurchases() {
//        billingDataSource.refreshPurchases()
//    }

//    val billingLifecycleObserver: LifecycleObserver
//        get() = billingDataSource

//    fun getProductTitle(
//        product: String
//    ): Flow<String> {
//        return billingDataSource.getProductTitle(
//            product
//        )
//    }

//    fun getProductPrice(
//        product: String
//    ): Flow<String> {
//        return billingDataSource.getProductPrice(
//            product
//        )
//    }

//    fun getProductDescription(
//        product: String
//    ): Flow<String> {
//        return billingDataSource.getProductDescription(
//            product
//        )
//    }

//    val billingFlowInProcess: Flow<Boolean>
//        get() = billingDataSource.getBillingFlowInProcess()

//    fun debugConsumeSingleTrip() {
//        CoroutineScope(
//            Dispatchers.Main
//        ).launch {
//            billingDataSource.consumeInAppPurchase(
//                SINGLE_TRIP_PRODUCT
//            )
//        }
//    }

//    companion object {
//        val TAG =
//            CoreApplicationRepository::class.simpleName
//        val IN_APP_SKUS =
//            arrayOf(
//                SINGLE_TRIP_PRODUCT,
//                ADVENTURER_TRIP_PRODUCT
//            )
//        val SUBS_SKUS =
//            arrayOf(
//                TRAVELER_TRIP_SUBSCRIPTION
//            )
//    }

//    init {
//        postMessagesFromBillingFlow()

// Since both are tied to application lifecycle, we can launch this scope to collect
// consumed purchases from the billing data source while the app process is alive.
//        defaultScope.launch {
//            billingDataSource.getConsumedPurchases()
//                .collect {
//                    Log.d(
//                        TAG,
//                        "consumedPurchases: $it"
//                    )
//                for( sku in it ) {
//                    if (sku == SKU_ADVENTURER) {
//                        gameStateModel.incrementGas(GAS_TANK_MAX)
//                    }
//                }
//                }
//        }
//    }
//}
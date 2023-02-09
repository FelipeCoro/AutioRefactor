package com.autio.android_app.billing
//
//import android.app.Activity
//import android.app.Application
//import android.os.Handler
//import android.os.Looper
//import android.os.SystemClock
//import android.util.Log
//import androidx.lifecycle.DefaultLifecycleObserver
//import androidx.lifecycle.LifecycleOwner
//import com.android.billingclient.api.*
//import com.android.billingclient.api.BillingClient.ProductType
//import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
//import com.android.billingclient.api.QueryProductDetailsParams.Product
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.*
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.util.*
//import kotlin.math.min
//
//private const val RECONNECT_TIMER_START_MILLISECONDS =
//    1L * 1000L
//private const val RECONNECT_TIMER_MAX_TIME_MILLISECONDS =
//    1000L * 60L * 15L // 15 minutes
//private const val SKU_DETAILS_REQUERY_TIME =
//    1000L * 60L * 60L * 4L // 4 hours
//
///**
// * Source code for Google Play Billing processes
// *
// * @param application Android application class.
// * @param knownInAppProducts SKUs of in-app purchases the source should know about
// * @param knownSubscriptionProducts SKUs of subscriptions the source should know about
// */
//class BillingDataSource private constructor(
//    application: Application,
//    private val defaultScope: CoroutineScope,
//    knownInAppProducts: Array<String>?,
//    knownSubscriptionProducts: Array<String>?,
//    autoConsumeSKUs: Array<String>?
//) : DefaultLifecycleObserver,
//    PurchasesUpdatedListener,
//    BillingClientStateListener {
//    // Billing client, connection, cached data
//    private val billingClient: BillingClient
//
//    // known SKUs (used to query sku data and validate responses)
//    private val knownInAppProducts: List<String>?
//    private val knownSubsProducts: List<String>?
//
//    // SKUs to auto-consume
//    private val knownAutoConsumeSKUs: MutableSet<String>
//
//    // how long before the data source tries to reconnect to Google play
//    private var reconnectMilliseconds =
//        RECONNECT_TIMER_START_MILLISECONDS
//
//    // when was the last successful SkuDetailsResponse?
//    private var skuDetailsResponseTime =
//        -SKU_DETAILS_REQUERY_TIME
//
//    private enum class ProductState {
//        PRODUCT_STATE_NON_PURCHASED,
//        PRODUCT_STATE_PENDING,
//        PRODUCT_STATE_PURCHASED,
//        PRODUCT_STATE_PURCHASED_AND_ACKNOWLEDGED
//    }
//
//    // Flows that are mostly maintained so they can be transformed into observables.
//    private val productStateMap: MutableMap<String, MutableStateFlow<ProductState>> =
//        HashMap()
//    private val productDetailsMap: MutableMap<String, MutableStateFlow<ProductDetails?>> =
//        HashMap()
//    private val productDateMap: MutableMap<String, MutableStateFlow<Long>> =
//        HashMap()
//
//    // Observables that are used to communicate state.
//    private val purchaseConsumptionInProcess: MutableSet<Purchase> =
//        HashSet()
//    private val newPurchaseFlow =
//        MutableSharedFlow<List<String>>(
//            extraBufferCapacity = 1
//        )
//    private val purchaseConsumedFlow =
//        MutableSharedFlow<List<String>>()
//    private val billingFlowInProcess =
//        MutableStateFlow(
//            false
//        )
//
//    override fun onBillingSetupFinished(
//        billingResult: BillingResult
//    ) {
//        val responseCode =
//            billingResult.responseCode
//        val debugMessage =
//            billingResult.debugMessage
//        Log.d(
//            TAG,
//            "onBillingSetupFinished: $responseCode $debugMessage"
//        )
//        when (responseCode) {
//            BillingClient.BillingResponseCode.OK -> {
//                // The billing client is ready. You can query purchases here.
//                // This doesn't mean that your app is set up correctly in the console -- it just
//                // means that you have a connection to the Billing service.
//                reconnectMilliseconds =
//                    RECONNECT_TIMER_START_MILLISECONDS
//                defaultScope.launch {
//                    queryProductsDetailsAsync()
//                    refreshPurchases()
//                }
//            }
//            else -> retryBillingServiceConnectionWithExponentialBackoff()
//        }
//    }
//
//    /**
//     * This is a pretty unusual occurrence. It happens primarily if the Google Play Store
//     * self-upgrades or is force closed.
//     */
//    override fun onBillingServiceDisconnected() {
//        retryBillingServiceConnectionWithExponentialBackoff()
//    }
//
//    /**
//     * Retries the billing service connection with exponential backoff, maxing out at the time
//     * specified by RECONNECT_TIMER_MAX_TIME_MILLISECONDS.
//     */
//    private fun retryBillingServiceConnectionWithExponentialBackoff() {
//        handler.postDelayed(
//            {
//                billingClient.startConnection(
//                    this@BillingDataSource
//                )
//            },
//            reconnectMilliseconds
//        )
//        reconnectMilliseconds =
//            min(
//                reconnectMilliseconds * 2,
//                RECONNECT_TIMER_MAX_TIME_MILLISECONDS
//            )
//    }
//
//    /**
//     * Called by initializeFlows to create the various Flow objects we're planning to emit.
//     * @param skuList a List<String> of products representing purchases and subscriptions.
//    </String> */
//    private fun addSkuFlows(
//        skuList: List<String>?
//    ) {
//        for (product in skuList!!) {
//            val productState =
//                MutableStateFlow(
//                    ProductState.PRODUCT_STATE_NON_PURCHASED
//                )
//            val details =
//                MutableStateFlow<ProductDetails?>(
//                    null
//                )
//            val date =
//                MutableStateFlow(
//                    0L
//                )
//            details.subscriptionCount.map { count -> count > 0 } // map count into active/inactive flag
//                .distinctUntilChanged() // only react to true<->false changes
//                .onEach { isActive -> // configure an action
//                    if (isActive && (SystemClock.elapsedRealtime() - skuDetailsResponseTime > SKU_DETAILS_REQUERY_TIME)) {
//                        skuDetailsResponseTime =
//                            SystemClock.elapsedRealtime()
//                        Log.v(
//                            TAG,
//                            "Products not fresh, re-querying"
//                        )
//                        queryProductsDetailsAsync()
//                    }
//                }
//                .launchIn(
//                    defaultScope
//                ) // launch it
//            productStateMap[product] =
//                productState
//            productDetailsMap[product] =
//                details
//            productDateMap[product] =
//                date
//        }
//    }
//
//    /**
//     * Creates a Flow object for every known SKU so the state and SKU details can be observed
//     * in other layers. The repository is responsible for mapping this data in ways that are more
//     * useful for the application.
//     */
//    private fun initializeFlows() {
//        addSkuFlows(
//            knownInAppProducts
//        )
//        addSkuFlows(
//            knownSubsProducts
//        )
//    }
//
//    fun getNewPurchases() =
//        newPurchaseFlow.asSharedFlow()
//
//    /**
//     * This is a flow that is used to observe consumed purchases.
//     * @return Flow that contains skus of the consumed purchases.
//     */
//    fun getConsumedPurchases() =
//        purchaseConsumedFlow.asSharedFlow()
//
//    /**
//     * Returns whether or not the user has purchased a SKU. It does this by returning
//     * a Flow that returns true if the SKU is in the PURCHASED state and
//     * the Purchase has been acknowledged.
//     * @return a Flow that observes the SKUs purchase state
//     */
//    fun isPurchased(
//        sku: String
//    ): Flow<Boolean> {
//        val productStateFlow =
//            productStateMap[sku]!!
//        return productStateFlow.map { productState -> productState == ProductState.PRODUCT_STATE_PURCHASED_AND_ACKNOWLEDGED }
//    }
//
//    /**
//     * Returns whether or not the user can purchase a SKU. It does this by returning
//     * a Flow combine transformation that returns true if the SKU is in the UNSPECIFIED state, as
//     * well as if we have skuDetails for the SKU. (SKUs cannot be purchased without valid
//     * SkuDetails.)
//     * @return a Flow that observes the SKUs purchase state
//     */
//    fun canPurchase(
//        sku: String
//    ): Flow<Boolean> {
//        val productDetailsFlow =
//            productDetailsMap[sku]!!
//        val productStateFlow =
//            productStateMap[sku]!!
//
//        return productStateFlow.combine(
//            productDetailsFlow
//        ) { skuState, skuDetails ->
//            skuState == ProductState.PRODUCT_STATE_NON_PURCHASED && skuDetails != null
//        }
//    }
//
//    fun getPurchaseDate(
//        sku: String
//    ): Flow<Long> {
//        return productDateMap[sku]!!
//    }
//
//    // There's lots of information in SkuDetails, but our app only needs a few things, since our
//    // goods never go on sale, have introductory pricing, etc. You can add to this for your app,
//    // or create your own class to pass the information across.
//    /**
//     * The title of our SKU from SkuDetails.
//     * @param product to get the title from
//     * @return title of the requested SKU as an observable Flow<String>
//    </String> */
//    fun getProductTitle(
//        product: String
//    ): Flow<String> {
//        val productDetailsFlow =
//            productDetailsMap[product]!!
//        return productDetailsFlow.mapNotNull { productDetails ->
//            productDetails?.title
//        }
//    }
//
//    fun getProductPrice(
//        sku: String
//    ): Flow<String> {
//        val productDetailsFlow =
//            productDetailsMap[sku]!!
//        return productDetailsFlow.mapNotNull { productDetails ->
//            when (productDetails?.productType) {
//                ProductType.INAPP -> productDetails.oneTimePurchaseOfferDetails?.formattedPrice
//                ProductType.SUBS -> productDetails.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
//                else -> null
//            }
//        }
//    }
//
//    fun getProductDescription(
//        product: String
//    ): Flow<String> {
//        val productDetailsFlow =
//            productDetailsMap[product]!!
//        return productDetailsFlow.mapNotNull { productDetails ->
//            productDetails?.description
//        }
//    }
//
//    /**
//     * Receives the result from [.querySkuDetailsAsync]}.
//     *
//     * Store the SkuDetails and post them in the [.skuDetailsMap]. This allows other
//     * parts of the app to use the [SkuDetails] to show SKU information and make purchases.
//     */
//    private fun onProductDetailsResponse(
//        billingResult: BillingResult,
//        productDetailsList: List<ProductDetails>?
//    ) {
//        val responseCode =
//            billingResult.responseCode
//        val debugMessage =
//            billingResult.debugMessage
//        when (responseCode) {
//            BillingClient.BillingResponseCode.OK -> {
//                Log.i(
//                    TAG,
//                    "onSkuDetailsResponse: $debugMessage"
//                )
//                if (productDetailsList == null || productDetailsList.isEmpty()) {
//                    Log.e(
//                        TAG,
//                        "onSkuDetailsResponse: " +
//                                "Found null or empty SkuDetails. " +
//                                "Check to see if the SKUs you requested are correctly published " +
//                                "in the Google Play Console."
//                    )
//                } else {
//                    for (productDetails in productDetailsList) {
//                        val id =
//                            productDetails.productId
//                        val detailsMutableFlow =
//                            productDetailsMap[id]
//                        detailsMutableFlow?.tryEmit(
//                            productDetails
//                        )
//                            ?: Log.e(
//                                TAG,
//                                "Unknown sku: $id"
//                            )
//                    }
//                }
//            }
//            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
//            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
//            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
//            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
//            BillingClient.BillingResponseCode.DEVELOPER_ERROR,
//            BillingClient.BillingResponseCode.ERROR ->
//                Log.e(
//                    TAG,
//                    "onSkuDetailsResponse: $responseCode $debugMessage"
//                )
//            BillingClient.BillingResponseCode.USER_CANCELED ->
//                Log.i(
//                    TAG,
//                    "onSkuDetailsResponse: $responseCode $debugMessage"
//                )
//            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
//            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
//            BillingClient.BillingResponseCode.ITEM_NOT_OWNED ->
//                Log.wtf(
//                    TAG,
//                    "onSkuDetailsResponse: $responseCode $debugMessage"
//                )
//            else -> Log.wtf(
//                TAG,
//                "onSkuDetailsResponse: $responseCode $debugMessage"
//            )
//        }
//        skuDetailsResponseTime =
//            if (responseCode == BillingClient.BillingResponseCode.OK) {
//                SystemClock.elapsedRealtime()
//            } else {
//                -SKU_DETAILS_REQUERY_TIME
//            }
//    }
//
//    /**
//     * Calls the billing client functions to query sku details for both the in-app and subscription
//     * SKUs. SKU details are useful for displaying item names and price lists to the user, and are
//     * required to make a purchase.
//     */
//    private suspend fun queryProductsDetailsAsync() {
//        if (!knownInAppProducts.isNullOrEmpty()) {
//            val params =
//                QueryProductDetailsParams.newBuilder()
//            val inAppProductList: MutableList<Product> =
//                arrayListOf()
//
//            for (product in knownInAppProducts) {
//                inAppProductList.add(
//                    Product.newBuilder()
//                        .setProductId(
//                            product
//                        )
//                        .setProductType(
//                            ProductType.INAPP
//                        )
//                        .build()
//                )
//            }
//
//            params.setProductList(
//                inAppProductList
//            )
//            val productDetailsResult =
//                withContext(
//                    Dispatchers.IO
//                ) {
//                    billingClient.queryProductDetails(
//                        params.build()
//                    )
//                }
//            onProductDetailsResponse(
//                productDetailsResult.billingResult,
//                productDetailsResult.productDetailsList
//            )
//        }
//        if (!knownSubsProducts.isNullOrEmpty()) {
//            val params =
//                QueryProductDetailsParams.newBuilder()
//            val subsProductList: MutableList<Product> =
//                arrayListOf()
//
//            for (product in knownSubsProducts) {
//                subsProductList.add(
//                    Product.newBuilder()
//                        .setProductId(
//                            product
//                        )
//                        .setProductType(
//                            ProductType.SUBS
//                        )
//                        .build()
//                )
//            }
//
//            params
//                .setProductList(
//                    subsProductList
//                )
//
//            val subsDetailsResult =
//                withContext(
//                    Dispatchers.IO
//                ) {
//                    billingClient.queryProductDetails(
//                        params.build()
//                    )
//                }
//            onProductDetailsResponse(
//                subsDetailsResult.billingResult,
//                subsDetailsResult.productDetailsList
//            )
//        }
//    }
//
//    /**
//     * GPBLv3 now queries purchases synchronously, simplifying this flow. This only gets active
//     * purchases.
//     */
//    suspend fun refreshPurchases() {
//        Log.d(
//            TAG,
//            "Refreshing purchases."
//        )
//        var purchasesResult =
//            billingClient.queryPurchasesAsync(
//                QueryPurchasesParams.newBuilder()
//                    .setProductType(
//                        ProductType.INAPP
//                    )
//                    .build()
//            )
//        var billingResult =
//            purchasesResult.billingResult
//        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
//            Log.e(
//                TAG,
//                "Problem getting purchases: " + billingResult.debugMessage
//            )
//        } else {
//            processPurchaseList(
//                purchasesResult.purchasesList,
//                knownInAppProducts
//            )
//        }
//        purchasesResult =
//            billingClient.queryPurchasesAsync(
//                QueryPurchasesParams.newBuilder()
//                    .setProductType(
//                        ProductType.SUBS
//                    )
//                    .build()
//            )
//        billingResult =
//            purchasesResult.billingResult
//        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
//            Log.e(
//                TAG,
//                "Problem getting subscriptions: " + billingResult.debugMessage
//            )
//        } else {
//            processPurchaseList(
//                purchasesResult.purchasesList,
//                knownSubsProducts
//            )
//        }
//        Log.d(
//            TAG,
//            "Refreshing purchases finished."
//        )
//    }
//
//    /**
//     * Used internally to get purchases from a requested set of SKUs. This is particularly
//     * important when changing subscriptions, as [onPurchasesUpdated] won't update the purchase state
//     * of a subscription that has been upgraded from.
//     *
//     * @param products products to get purchase information for
//     * @param params sku type, in-app or subscription, to get purchase information for.
//     * @return purchases
//     */
//    private suspend fun getPurchases(
//        products: Array<String>,
//        params: QueryPurchasesParams
//    ): List<Purchase> {
//        val purchasesResult =
//            billingClient.queryPurchasesAsync(
//                params
//            )
//        val br =
//            purchasesResult.billingResult
//        val returnPurchasesList: MutableList<Purchase> =
//            LinkedList()
//        if (br.responseCode != BillingClient.BillingResponseCode.OK) {
//            Log.e(
//                TAG,
//                "Problem getting purchases: " + br.debugMessage
//            )
//        } else {
//            val purchasesList =
//                purchasesResult.purchasesList
//            for (purchase in purchasesList) {
//                for (product in products) {
//                    for (productId in purchase.products) {
//                        if (productId == product) {
//                            returnPurchasesList.add(
//                                purchase
//                            )
//                        }
//                    }
//                }
//            }
//        }
//        return returnPurchasesList
//    }
//
//    /**
//     * Consumes an in-app purchase. Interested listeners can watch the purchaseConsumed LiveEvent.
//     * To make things easy, you can send in a list of SKUs that are auto-consumed by the
//     * BillingDataSource.
//     */
//    suspend fun consumeInAppPurchase(
//        sku: String
//    ) {
//        val pr =
//            billingClient.queryPurchasesAsync(
//                QueryPurchasesParams.newBuilder()
//                    .setProductType(
//                        ProductType.INAPP
//                    )
//                    .build()
//            )
//        val br =
//            pr.billingResult
//        val purchasesList =
//            pr.purchasesList
//        if (br.responseCode != BillingClient.BillingResponseCode.OK) {
//            Log.e(
//                TAG,
//                "Problem getting purchases: " + br.debugMessage
//            )
//        } else {
//            for (purchase in purchasesList) {
//                // for right now any bundle of SKUs must all be consumable
//                for (purchaseSku in purchase.products) {
//                    if (purchaseSku == sku) {
//                        consumePurchase(
//                            purchase
//                        )
//                        return
//                    }
//                }
//            }
//        }
//        Log.e(
//            TAG,
//            "Unable to consume SKU: $sku Sku not found."
//        )
//    }
//
//    /**
//     * Calling this means that we have the most up-to-date information for a Sku in a purchase
//     * object. This uses the purchase state (Pending, Unspecified, Purchased) along with the
//     * acknowledged state.
//     * @param purchase an up-to-date object to set the state for the Sku
//     */
//    private fun setSkuStateFromPurchase(
//        purchase: Purchase
//    ) {
//        for (purchaseSku in purchase.products) {
//            val skuStateFlow =
//                productStateMap[purchaseSku]
//            if (null == skuStateFlow) {
//                Log.e(
//                    TAG,
//                    "Unknown SKU " + purchaseSku + ". Check to make " +
//                            "sure SKU matches SKUS in the Play developer console."
//                )
//            } else {
//                when (purchase.purchaseState) {
//                    Purchase.PurchaseState.PENDING -> skuStateFlow.tryEmit(
//                        ProductState.PRODUCT_STATE_PENDING
//                    )
//                    Purchase.PurchaseState.UNSPECIFIED_STATE -> skuStateFlow.tryEmit(
//                        ProductState.PRODUCT_STATE_NON_PURCHASED
//                    )
//                    Purchase.PurchaseState.PURCHASED -> if (purchase.isAcknowledged) {
//                        skuStateFlow.tryEmit(
//                            ProductState.PRODUCT_STATE_PURCHASED_AND_ACKNOWLEDGED
//                        )
//                    } else {
//                        skuStateFlow.tryEmit(
//                            ProductState.PRODUCT_STATE_PURCHASED
//                        )
//                    }
//                    else -> Log.e(
//                        TAG,
//                        "Purchase in unknown state: " + purchase.purchaseState
//                    )
//                }
//            }
//        }
//    }
//
//    private fun setDateFromPurchase(
//        purchase: Purchase
//    ) {
//        for (purchaseSku in purchase.products) {
//            val skuStateFlow =
//                productDateMap[purchaseSku]
//            if (null == skuStateFlow) {
//                Log.e(
//                    TAG,
//                    "Unknown SKU " + purchaseSku + ". Check to make " +
//                            "sure SKU matches SKUS in the Play developer console."
//                )
//            } else {
//                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
//                    skuStateFlow.tryEmit(
//                        purchase.purchaseTime
//                    )
//                }
//            }
//        }
//
//    }
//
//    /**
//     * Since we (mostly) are getting sku states when we actually make a purchase or update
//     * purchases, we keep some internal state when we do things like acknowledge or consume.
//     * @param sku product ID to change the state of
//     * @param newProductState the new state of the sku.
//     */
//    private fun setSkuState(
//        sku: String,
//        newProductState: ProductState
//    ) {
//        val skuStateFlow =
//            productStateMap[sku]
//        skuStateFlow?.tryEmit(
//            newProductState
//        )
//            ?: Log.e(
//                TAG,
//                "Unknown SKU " + sku + ". Check to make " +
//                        "sure SKU matches SKUS in the Play developer console."
//            )
//    }
//
//    /**
//     * Goes through each purchase and makes sure that the purchase state is processed and the state
//     * is available through Flows. Verifies signature and acknowledges purchases. PURCHASED isn't
//     * returned until the purchase is acknowledged.
//     *
//     * https://developer.android.com/google/play/billing/billing_library_releases_notes#2_0_acknowledge
//     *
//     * Developers can choose to acknowledge purchases from a server using the
//     * Google Play Developer API. The server has direct access to the user database,
//     * so using the Google Play Developer API for acknowledgement might be more reliable.
//     *
//     * If the purchase token is not acknowledged within 3 days,
//     * then Google Play will automatically refund and revoke the purchase.
//     * This behavior helps ensure that users are not charged unless the user has successfully
//     * received access to the content.
//     * This eliminates a category of issues where users complain to developers
//     * that they paid for something that the app is not giving to them.
//     *
//     * If a skusToUpdate list is passed-into this method, any purchases not in the list of
//     * purchases will have their state set to NON-PURCHASED.
//     *
//     * @param purchases the List of purchases to process.
//     * @param productsToUpdate a list of skus that we want to update the state from --- this allows us
//     * to set the state of non-returned SKUs to NON-PURCHASED.
//     */
//    private fun processPurchaseList(
//        purchases: List<Purchase>?,
//        productsToUpdate: List<String>?
//    ) {
//        val updatedSkus =
//            HashSet<String>()
//        if (null != purchases) {
//            for (purchase in purchases) {
//                for (sku in purchase.products) {
//                    val skuStateFlow =
//                        productStateMap[sku]
//                    if (null == skuStateFlow) {
//                        Log.e(
//                            TAG,
//                            "Unknown SKU " + sku + ". Check to make " +
//                                    "sure SKU matches SKUS in the Play developer console."
//                        )
//                        continue
//                    }
//                    updatedSkus.add(
//                        sku
//                    )
//                }
//                // Global check to make sure all purchases are signed correctly.
//                // This check is best performed on your server.
//                val purchaseState =
//                    purchase.purchaseState
//                if (purchaseState == Purchase.PurchaseState.PURCHASED) {
//                    if (!isSignatureValid(
//                            purchase
//                        )
//                    ) {
//                        Log.e(
//                            TAG,
//                            "Invalid signature. Check to make sure your " +
//                                    "public key is correct."
//                        )
//                        continue
//                    }
//                    // only set the purchased state after we've validated the signature.
//                    setSkuStateFromPurchase(
//                        purchase
//                    )
//                    setDateFromPurchase(
//                        purchase
//                    )
//                    var isConsumable =
//                        false
//                    defaultScope.launch {
//                        for (sku in purchase.products) {
//                            if (knownAutoConsumeSKUs.contains(
//                                    sku
//                                )
//                            ) {
//                                isConsumable =
//                                    true
//                            } else {
//                                if (isConsumable) {
//                                    Log.e(
//                                        TAG,
//                                        "Purchase cannot contain a mixture of consumable" +
//                                                "and non-consumable items: " + purchase.products.toString()
//                                    )
//                                    isConsumable =
//                                        false
//                                    break
//                                }
//                            }
//                        }
//                        if (isConsumable) {
//                            consumePurchase(
//                                purchase
//                            )
//                            newPurchaseFlow.tryEmit(
//                                purchase.products
//                            )
//                        } else if (!purchase.isAcknowledged) {
//                            // acknowledge everything --- new purchases are ones not yet acknowledged
//                            val billingResult =
//                                billingClient.acknowledgePurchase(
//                                    AcknowledgePurchaseParams.newBuilder()
//                                        .setPurchaseToken(
//                                            purchase.purchaseToken
//                                        )
//                                        .build()
//                                )
//                            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
//                                Log.e(
//                                    TAG,
//                                    "Error acknowledging purchase: ${purchase.products}"
//                                )
//                            } else {
//                                // purchase acknowledged
//                                for (sku in purchase.products) {
//                                    setSkuState(
//                                        sku,
//                                        ProductState.PRODUCT_STATE_PURCHASED_AND_ACKNOWLEDGED
//                                    )
//                                }
//                            }
//                            newPurchaseFlow.tryEmit(
//                                purchase.products
//                            )
//                        }
//                    }
//                } else {
//                    // make sure the state is set
//                    setSkuStateFromPurchase(
//                        purchase
//                    )
//                    setDateFromPurchase(
//                        purchase
//                    )
//                }
//            }
//        } else {
//            Log.d(
//                TAG,
//                "Empty purchase list."
//            )
//        }
//        // Clear purchase state of anything that didn't come with this purchase list if this is
//        // part of a refresh.
//        if (null != productsToUpdate) {
//            for (sku in productsToUpdate) {
//                if (!updatedSkus.contains(
//                        sku
//                    )
//                ) {
//                    setSkuState(
//                        sku,
//                        ProductState.PRODUCT_STATE_NON_PURCHASED
//                    )
//                }
//            }
//        }
//    }
//
//    /**
//     * Internal call only. Assumes that all signature checks have been completed and the purchase
//     * is ready to be consumed. If the sku is already being consumed, does nothing.
//     * @param purchase purchase to consume
//     */
//    private suspend fun consumePurchase(
//        purchase: Purchase
//    ) {
//        // weak check to make sure we're not already consuming the sku
//        if (purchaseConsumptionInProcess.contains(
//                purchase
//            )
//        ) {
//            // already consuming
//            return
//        }
//        purchaseConsumptionInProcess.add(
//            purchase
//        )
//        val consumePurchaseResult =
//            billingClient.consumePurchase(
//                ConsumeParams.newBuilder()
//                    .setPurchaseToken(
//                        purchase.purchaseToken
//                    )
//                    .build()
//            )
//
//        purchaseConsumptionInProcess.remove(
//            purchase
//        )
//        if (consumePurchaseResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
//            Log.d(
//                TAG,
//                "Consumption successful. Emitting sku."
//            )
//            defaultScope.launch {
//                purchaseConsumedFlow.emit(
//                    purchase.products
//                )
//            }
//            // Since we've consumed the purchase
//            for (sku in purchase.products) {
//                setSkuState(
//                    sku,
//                    ProductState.PRODUCT_STATE_NON_PURCHASED
//                )
//            }
//        } else {
//            Log.e(
//                TAG,
//                "Error while consuming: ${consumePurchaseResult.billingResult.debugMessage}"
//            )
//        }
//    }
//
//    /**
//     * Launch the billing flow. This will launch an external Activity for a result, so it requires
//     * an Activity reference. For subscriptions, it supports upgrading from one SKU type to another
//     * by passing in SKUs to be upgraded.
//     *
//     * @param activity active activity to launch our billing flow from
//     * @param sku SKU (Product ID) to be purchased
//     * @param upgradeProductsVarargs SKUs that the subscription can be upgraded from
//     * @return true if launch is successful
//     */
//    fun launchBillingFlow(
//        activity: Activity?,
//        sku: String,
//        vararg upgradeProductsVarargs: String
//    ) {
//        if (!billingClient.isReady) {
//            Log.e(
//                TAG,
//                "launchBillingFlow: BillingClient is not ready"
//            )
//        }
//        val productDetails =
//            productDetailsMap[sku]?.value
//        if (productDetails != null) {
//            val productDetailsParams =
//                ProductDetailsParams.newBuilder()
//                    .setProductDetails(
//                        productDetails
//                    )
//            if (productDetails.productType == ProductType.SUBS) {
//                productDetailsParams.setOfferToken(
//                    productDetails.subscriptionOfferDetails!!.first().offerToken
//                )
//            }
//            val billingFlowParamsBuilder =
//                BillingFlowParams.newBuilder()
//            billingFlowParamsBuilder.setProductDetailsParamsList(
//                arrayListOf(
//                    productDetailsParams.build()
//                )
//            )
//            val upgradeSkus =
//                arrayOf(
//                    *upgradeProductsVarargs
//                )
//            defaultScope.launch {
//                val heldSubscriptions =
//                    getPurchases(
//                        upgradeSkus,
//                        QueryPurchasesParams.newBuilder()
//                            .setProductType(
//                                ProductType.SUBS
//                            )
//                            .build()
//                    )
//                when (heldSubscriptions.size) {
//                    1 -> {
//                        val purchase =
//                            heldSubscriptions[0]
//                        billingFlowParamsBuilder.setSubscriptionUpdateParams(
//                            BillingFlowParams.SubscriptionUpdateParams.newBuilder()
//                                .setOldPurchaseToken(
//                                    purchase.purchaseToken
//                                )
//                                .build()
//                        )
//                    }
//                    0 -> {}
//                    else -> Log.e(
//                        TAG,
//                        heldSubscriptions.size.toString() +
//                                " subscriptions subscribed to. Upgrade not possible."
//                    )
//                }
//                val br =
//                    billingClient.launchBillingFlow(
//                        activity!!,
//                        billingFlowParamsBuilder.build()
//                    )
//                if (br.responseCode == BillingClient.BillingResponseCode.OK) {
//                    billingFlowInProcess.emit(
//                        true
//                    )
//                } else {
//                    Log.e(
//                        TAG,
//                        "Billing failed: + " + br.debugMessage
//                    )
//                }
//            }
//        } else {
//            Log.e(
//                TAG,
//                "SkuDetails not found for: $sku"
//            )
//        }
//    }
//
//    /**
//     * Returns a Flow that reports if a billing flow is in process, meaning that
//     * launchBillingFlow has returned BillingResponseCode.OK and onPurchasesUpdated hasn't yet
//     * been called.
//     * @return Flow that indicates the known state of the billing flow.
//     */
//    fun getBillingFlowInProcess(): Flow<Boolean> {
//        return billingFlowInProcess.asStateFlow()
//    }
//
//    /**
//     * Called by the BillingLibrary when new purchases are detected; typically in response to a
//     * launchBillingFlow.
//     * @param billingResult result of the purchase flow.
//     * @param list of new purchases.
//     */
//    override fun onPurchasesUpdated(
//        billingResult: BillingResult,
//        list: List<Purchase>?
//    ) {
//        when (billingResult.responseCode) {
//            BillingClient.BillingResponseCode.OK -> if (null != list) {
//                processPurchaseList(
//                    list,
//                    null
//                )
//                return
//            } else Log.d(
//                TAG,
//                "Null Purchase List Returned from OK response!"
//            )
//            BillingClient.BillingResponseCode.USER_CANCELED -> Log.i(
//                TAG,
//                "onPurchasesUpdated: User canceled the purchase"
//            )
//            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> Log.i(
//                TAG,
//                "onPurchasesUpdated: The user already owns this item"
//            )
//            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> Log.e(
//                TAG,
//                "onPurchasesUpdated: Developer error means that Google Play " +
//                        "does not recognize the configuration. If you are just getting started, " +
//                        "make sure you have configured the application correctly in the " +
//                        "Google Play Console. The SKU product ID must match and the APK you " +
//                        "are using must be signed with release keys."
//            )
//            else -> Log.d(
//                TAG,
//                "BillingResult [" + billingResult.responseCode + "]: " + billingResult.debugMessage
//            )
//        }
//        defaultScope.launch {
//            billingFlowInProcess.emit(
//                false
//            )
//        }
//    }
//
//    /**
//     * Ideally your implementation will comprise a secure server, rendering this check
//     * unnecessary. @see [Security]
//     */
//    private fun isSignatureValid(
//        purchase: Purchase
//    ): Boolean {
//        return Security.verifyPurchase(
//            purchase.originalJson,
//            purchase.signature
//        )
//    }
//
//    /**
//     * It's recommended to re-query purchases during onResume.
//     */
//    override fun onResume(
//        owner: LifecycleOwner
//    ) {
//        super.onResume(
//            owner
//        )
//        // this just avoids an extra purchase refresh after we finish a billing flow
//        if (!billingFlowInProcess.value) {
//            if (billingClient.isReady) {
//                defaultScope.launch {
//                    refreshPurchases()
//                }
//            }
//        }
//    }
//
//    companion object {
//        private val TAG =
//            BillingDataSource::class.java.simpleName
//
//        @Volatile
//        private var sInstance: BillingDataSource? =
//            null
//        private val handler =
//            Handler(
//                Looper.getMainLooper()
//            )
//
//        // Standard boilerplate double check locking pattern for thread-safe singletons.
//        @JvmStatic
//        fun getInstance(
//            application: Application,
//            defaultScope: CoroutineScope,
//            knownInAppSKUs: Array<String>?,
//            knownSubscriptionSKUs: Array<String>?,
//            autoConsumeSKUs: Array<String>?
//        ) =
//            sInstance
//                ?: synchronized(
//                    this
//                ) {
//                    sInstance
//                        ?: BillingDataSource(
//                            application,
//                            defaultScope,
//                            knownInAppSKUs,
//                            knownSubscriptionSKUs,
//                            autoConsumeSKUs
//                        )
//                            .also {
//                                sInstance =
//                                    it
//                            }
//                }
//    }
//
//    /**
//     * Our initializer. Since we are a singleton, this is only used internally.
//     */
//    init {
//        this.knownInAppProducts =
//            if (knownInAppProducts == null) {
//                ArrayList()
//            } else {
//                listOf(
//                    *knownInAppProducts
//                )
//            }
//        this.knownSubsProducts =
//            if (knownSubscriptionProducts == null) {
//                ArrayList()
//            } else {
//                listOf(
//                    *knownSubscriptionProducts
//                )
//            }
//        knownAutoConsumeSKUs =
//            HashSet()
//        if (autoConsumeSKUs != null) {
//            knownAutoConsumeSKUs.addAll(
//                listOf(
//                    *autoConsumeSKUs
//                )
//            )
//        }
//        initializeFlows()
//        billingClient =
//            BillingClient.newBuilder(
//                application
//            )
//                .setListener(
//                    this
//                )
//                .enablePendingPurchases()
//                .build()
//        billingClient.startConnection(
//            this
//        )
//    }
//}
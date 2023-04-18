package com.autio.android_app.data.api.model.account

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class AndroidReceiptDto(
    @SerializedName("receipt")
    val receipt: Receipt
)

@Serializable
data class Receipt(
    val isAutoRenewing: Boolean?,
    val marketplace: String? = null,
    val orderId: String?,
    val originalJson: OriginalJson,
    val presentedOfferingIdentifier: String?,
    val purchaseState: String?,
    val purchaseTime: Long,
    val purchaseToken: String,
    val purchaseType: String,
    val signature: String?,
    val skus: List<String>,
    val storeUserID: String? = null,
    val type: String
)

@Serializable
data class OriginalJson(
    val acknowledged: Boolean,
    val obfuscatedAccountId: String,
    val orderId: String,
    val packageName: String,
    val productId: String,
    val purchaseState: Int,
    val purchaseTime: Long,
    val purchaseToken: String,
    val quantity: Int
)

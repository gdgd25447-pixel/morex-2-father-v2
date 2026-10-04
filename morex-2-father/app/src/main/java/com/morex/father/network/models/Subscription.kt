package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

/** subscriptions: id, plan (basic|family|gold), status, started_at, expires_at */
data class Subscription(
    @SerializedName("id") val id: String? = null,
    @SerializedName("plan") val plan: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("started_at") val startedAt: String? = null,
    @SerializedName("expires_at") val expiresAt: String? = null
)

/** طلب اشتراك معلّق (subscription_requests) */
data class PendingRequest(
    @SerializedName("id") val id: String? = null,
    @SerializedName("plan") val plan: String? = null,
    @SerializedName("amount_usd") val amountUsd: Double? = null,
    @SerializedName("amount_yer") val amountYer: Double? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

/** GET /parent/subscription */
data class SubscriptionResponse(
    @SerializedName("subscription") val subscription: Subscription? = null,
    @SerializedName("pending_request") val pendingRequest: PendingRequest? = null
)

/** سعر باقة من الإعداد plan_pricing: { basic: { usd, yer }, ... } */
data class PlanPrice(
    @SerializedName("usd") val usd: Double = 0.0,
    @SerializedName("yer") val yer: Double = 0.0
)

data class ExchangeRate(
    @SerializedName("usd_to_yer") val usdToYer: Double = 0.0
)

/** payment_wallets: wallet_key, wallet_name, wallet_number, account_name */
data class Wallet(
    @SerializedName("wallet_key") val key: String = "",
    @SerializedName("wallet_name") val name: String? = null,
    @SerializedName("wallet_number") val number: String? = null,
    @SerializedName("account_name") val accountName: String? = null
)

/** GET /parent/subscription/plans */
data class PlansResponse(
    @SerializedName("pricing") val pricing: Map<String, PlanPrice> = emptyMap(),
    @SerializedName("rate") val rate: ExchangeRate? = null,
    @SerializedName("wallets") val wallets: List<Wallet> = emptyList()
)

/** باقة جاهزة للعرض، تُبنى محلياً من PlansResponse.pricing. */
data class Plan(
    val id: String,
    val priceUsd: Double,
    val priceYer: Double
)

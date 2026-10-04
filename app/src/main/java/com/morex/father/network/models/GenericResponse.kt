package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

/** ردود عامة: السيرفر يعيد { ok: true } عند النجاح و{ error: "CODE" } عند الفشل. */
data class GenericResponse(
    @SerializedName("ok") val ok: Boolean = false,
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: String? = null
) {
    val isOk: Boolean get() = ok || success
}

/** POST /auth/otp/verify → { ok, token, is_new_registration, parent } */
data class AuthResponse(
    @SerializedName("ok") val ok: Boolean = false,
    @SerializedName("token") val token: String? = null,
    @SerializedName("is_new_registration") val isNewRegistration: Boolean = false,
    @SerializedName("parent") val parent: Parent? = null,
    @SerializedName("error") val error: String? = null
)

/** POST /auth/refresh → { ok, token } */
data class RefreshResponse(
    @SerializedName("ok") val ok: Boolean = false,
    @SerializedName("token") val token: String? = null
)

/** POST /auth/pin/verify */
data class PinVerifyResponse(
    @SerializedName("ok") val ok: Boolean = false,
    @SerializedName("valid") val valid: Boolean = false,
    @SerializedName("attempts_left") val attemptsLeft: Int? = null,
    @SerializedName("retry_after_sec") val retryAfterSec: Int? = null,
    @SerializedName("error") val error: String? = null
)

/** GET /parent/block-status → { blocked: false } أو تفاصيل الحظر */
data class BlockStatus(
    @SerializedName("blocked") val blocked: Boolean = false,
    @SerializedName("reason") val reason: String? = null,
    @SerializedName("can_review") val canReview: Boolean = true
)

/** GET /parent/credits */
data class CreditsResponse(
    @SerializedName("balance") val balance: Long = 0,
    @SerializedName("currency") val currency: String = "YER",
    @SerializedName("total_earned") val totalEarned: Long = 0
)

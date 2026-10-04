package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

data class OtpSendResponse(
    @SerializedName("ok") val ok: Boolean = false,
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("channel") val channel: String? = null,
    @SerializedName("telegram_required") val telegramRequired: Boolean = false,
    @SerializedName("telegram_url") val telegramUrl: String? = null,
    @SerializedName("expires_in") val expiresIn: Int = 300,
    @SerializedName("error") val error: String? = null
)

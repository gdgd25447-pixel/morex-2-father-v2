package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

/** POST /parent/children/:id/pairing-code */
data class ChildPairingResponse(
    @SerializedName("code") val code: String? = null,
    @SerializedName("pairing_code") val pairingCode: String? = null,
    @SerializedName("expires_at") val expiresAt: String? = null,
    @SerializedName("qr_data") val qrData: String? = null,
    @SerializedName("error") val error: String? = null
)

/** GET /parent/children/:id/pairing-status */
data class PairingStatusResponse(
    @SerializedName("ok") val ok: Boolean = false,
    @SerializedName("paired") val paired: Boolean = false,
    @SerializedName("code_status") val codeStatus: String = "none", // none | active | expired | used
    @SerializedName("expires_at") val expiresAt: String? = null
)

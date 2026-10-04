package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

data class ReviewRequest(
    @SerializedName("id") val id: String? = null,
    @SerializedName("phone") val phone: String?,
    @SerializedName("device_id") val deviceId: String?,
    @SerializedName("block_type") val blockType: String?,
    @SerializedName("reason") val reason: String?,
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: String = "pending", // pending | approved | rejected
    @SerializedName("admin_response") val adminResponse: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("reviewed_at") val reviewedAt: String? = null
)

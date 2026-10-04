package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

/** keyword_rules: id, child_id, keyword, severity (warning|danger), is_active */
data class KeywordRule(
    @SerializedName("id") val id: String? = null,
    @SerializedName("child_id") val childId: String? = null,
    @SerializedName("keyword") val keyword: String = "",
    @SerializedName("severity") val type: String = "warning",
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("created_at") val createdAt: String? = null
)

/** POST /web/:childId/keywords تعيد { keyword: {...} } */
data class KeywordEnvelope(
    @SerializedName("keyword") val keyword: KeywordRule? = null
)

/** app_approval_requests: id, child_id, package_name, app_name, status (pending|approved|denied) */
data class AppApprovalRequest(
    @SerializedName("id") val id: String? = null,
    @SerializedName("child_id") val childId: String? = null,
    @SerializedName("package_name") val packageName: String = "",
    @SerializedName("app_name") val appName: String? = null,
    @SerializedName("status") val status: String = "pending",
    @SerializedName("responded_at") val respondedAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

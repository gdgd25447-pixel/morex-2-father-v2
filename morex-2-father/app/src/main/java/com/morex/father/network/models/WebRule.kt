package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

/** web_rules: id, child_id, domain, list_type (black|white) */
data class WebRule(
    @SerializedName("id") val id: String? = null,
    @SerializedName("child_id") val childId: String? = null,
    @SerializedName("domain") val domain: String = "",
    @SerializedName("list_type") val type: String = "black", // black | white
    @SerializedName("created_at") val createdAt: String? = null
) {
    val isBlack: Boolean get() = type == "black"
}

/** web_history: id, device_id, url, domain, title, is_blocked, visited_at */
data class WebHistory(
    @SerializedName("id") val id: String? = null,
    @SerializedName("device_id") val deviceId: String? = null,
    @SerializedName("url") val url: String = "",
    @SerializedName("domain") val domain: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("is_blocked") val isBlocked: Boolean = false,
    @SerializedName("visited_at") val visitedAt: String? = null
)

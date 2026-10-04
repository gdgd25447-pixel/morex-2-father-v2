package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

/** app_rules: id, child_id, package_name, app_name, category, icon_url, is_blocked, daily_limit_min, is_approved */
data class AppRule(
    @SerializedName("id") val id: String? = null,
    @SerializedName("child_id") val childId: String? = null,
    @SerializedName("package_name") val packageName: String = "",
    @SerializedName("app_name") val appName: String? = null,
    @SerializedName("icon_url") val appIcon: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("is_blocked") val isBlocked: Boolean = false,
    @SerializedName("is_approved") val isWhitelisted: Boolean = false,
    @SerializedName("daily_limit_min") val dailyLimitMinutes: Int? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

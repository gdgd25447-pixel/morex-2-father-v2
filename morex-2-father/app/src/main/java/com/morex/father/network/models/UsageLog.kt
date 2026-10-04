package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

/** GET /apps/usage/:childId تعيد { date, usage: [...] } */
data class AppUsageResponse(
    @SerializedName("date") val date: String? = null,
    @SerializedName("usage") val usage: List<UsageLog> = emptyList()
)

/** صف من usage_logs مع قاعدة التطبيق: package_name, app_name, duration_sec, is_blocked, daily_limit_min */
data class UsageLog(
    @SerializedName("package_name") val packageName: String = "",
    @SerializedName("app_name") val appName: String? = null,
    @SerializedName("duration_sec") val durationSec: Int = 0,
    @SerializedName("is_blocked") val isBlocked: Boolean? = null,
    @SerializedName("daily_limit_min") val dailyLimitMin: Int? = null
)

/** call_logs: id, device_id, phone_number, contact_name, call_type, duration_sec (أو duration)، called_at */
data class CallLog(
    @SerializedName("id") val id: String? = null,
    @SerializedName("phone_number") val number: String? = null,
    @SerializedName("contact_name") val contactName: String? = null,
    @SerializedName("call_type") val type: String? = null, // incoming | outgoing | missed
    @SerializedName(value = "duration_sec", alternate = ["duration"]) val durationSeconds: Int = 0,
    @SerializedName("called_at") val calledAt: String? = null
)

/** sms_logs: id, device_id, phone_number, contact_name, direction, body, has_keyword, sent_at */
data class SmsLog(
    @SerializedName("id") val id: String? = null,
    @SerializedName("phone_number") val number: String? = null,
    @SerializedName("contact_name") val contactName: String? = null,
    @SerializedName("direction") val type: String? = null,
    @SerializedName("body") val body: String? = null,
    @SerializedName("has_keyword") val isFlagged: Boolean = false,
    @SerializedName("sent_at") val sentAt: String? = null
)

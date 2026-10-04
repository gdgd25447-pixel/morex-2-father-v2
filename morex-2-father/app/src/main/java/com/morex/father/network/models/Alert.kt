package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

/**
 * تنبيه من السيرفر. الجدول alerts: id, device_id, child_id, type (SOS, LOW_BATTERY, ...), severity, payload(JSON), is_read.
 * لا يوجد title/message في السيرفر: النص يُبنى محلياً من type + payload عبر utils/AlertText.
 */
data class Alert(
    @SerializedName("id") val id: String,
    @SerializedName("child_id") val childId: String? = null,
    @SerializedName("child_name") val childName: String? = null,
    @SerializedName("device_id") val deviceId: String? = null,
    @SerializedName("device_model") val deviceModel: String? = null,
    @SerializedName("type") val type: String = "",
    @SerializedName("severity") val severity: String = "info",
    @SerializedName("payload") val payload: Map<String, Any?>? = null,
    @SerializedName("is_read") val isRead: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null
) {
    val isSos: Boolean get() = type.equals("SOS", ignoreCase = true)
    val isResolved: Boolean get() = payload?.get("resolved")?.toString() == "true"

    /** إحداثيات التنبيه إن وُجدت في الحمولة (SOS مثلاً). */
    val lat: Double? get() = (payload?.get("lat") as? Number)?.toDouble() ?: payload?.get("lat")?.toString()?.toDoubleOrNull()
    val lng: Double? get() = (payload?.get("lng") as? Number)?.toDouble() ?: payload?.get("lng")?.toString()?.toDoubleOrNull()
}

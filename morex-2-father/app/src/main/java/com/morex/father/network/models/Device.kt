package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

data class Device(
    @SerializedName("id") val id: String,
    @SerializedName("child_id") val childId: String?,
    @SerializedName("child_name") val childName: String?,
    @SerializedName("parent_id") val parentId: String?,
    @SerializedName("model") val model: String?,
    @SerializedName("manufacturer") val manufacturer: String?,
    @SerializedName("android_version") val androidVersion: String?,
    @SerializedName("app_version") val appVersion: String?,
    @SerializedName("device_fingerprint") val deviceFingerprint: String?,
    @SerializedName("fcm_token") val fcmToken: String?,
    @SerializedName("is_online") val isOnline: Boolean = false,
    @SerializedName("is_paired") val isPaired: Boolean = false,
    @SerializedName("is_blocked") val isBlocked: Boolean = false,
    @SerializedName("battery_level") val batteryLevel: Int = 0,
    @SerializedName("network_type") val networkType: String?,
    @SerializedName("last_seen") val lastSeen: String?,
    @SerializedName("paired_at") val pairedAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

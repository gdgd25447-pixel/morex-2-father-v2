package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

data class Child(
    @SerializedName("id") val id: String? = null,
    @SerializedName("parent_id") val parentId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("birth_date") val birthDate: String? = null,
    @SerializedName("birth_year") val birthYear: Int? = null,
    @SerializedName("gender") val gender: String? = "male",
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("grade") val grade: String? = null,
    @SerializedName("school") val school: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,

    // ⭐ Pairing
    @SerializedName("pairing_code") val pairingCode: String? = null,
    @SerializedName("pairing_code_expires_at") val pairingCodeExpiresAt: String? = null,
    @SerializedName("is_paired") val isPaired: Boolean = false,
    @SerializedName("paired_at") val pairedAt: String? = null,
    @SerializedName("is_active") val isActive: Boolean = true,

    // Status
    @SerializedName("is_online") val isOnline: Boolean = false,
    @SerializedName("last_seen") val lastSeen: String? = null,
    @SerializedName("last_seen_at") val lastSeenAt: String? = null,
    @SerializedName("battery_level") val batteryLevel: Int = 0,
    @SerializedName("network_type") val networkType: String? = null,
    @SerializedName("status") val status: String? = "safe",

    // Location
    @SerializedName("last_location") val lastLocation: LocationData? = null,
    @SerializedName("location_updated_at") val locationUpdatedAt: String? = null,

    // Devices
    @SerializedName("devices_count") val devicesCount: Int = 0,
    @SerializedName("paired") val paired: Boolean = false,
    @SerializedName("device_id") val deviceId: String? = null,
    @SerializedName("model") val model: String? = null,

    // ⭐ Nested child — للتوافق مع الاستجابة الجديدة
    @SerializedName("child") val child: Child? = null,

    // ⭐ Flags
    @SerializedName("ok") val ok: Boolean? = null,
    @SerializedName("success") val success: Boolean? = null
)

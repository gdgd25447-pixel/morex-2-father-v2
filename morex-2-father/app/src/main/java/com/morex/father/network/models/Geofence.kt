package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

/** geofences: id, child_id, name, type (safe|danger), lat, lng, radius_m, notify_on_enter, notify_on_exit, is_active */
data class Geofence(
    @SerializedName("id") val id: String? = null,
    @SerializedName("child_id") val childId: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String = "safe", // safe | danger
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lng") val longitude: Double,
    @SerializedName("radius_m") val radius: Int = 300,
    @SerializedName("notify_on_enter") val notifyOnEnter: Boolean = true,
    @SerializedName("notify_on_exit") val notifyOnExit: Boolean = true,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("created_at") val createdAt: String? = null
)

/** POST/PUT geofences تعيد { geofence: {...} } */
data class GeofenceEnvelope(
    @SerializedName("geofence") val geofence: Geofence? = null
)

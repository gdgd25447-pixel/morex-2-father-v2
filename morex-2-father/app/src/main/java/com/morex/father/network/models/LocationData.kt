package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

/**
 * Response from GET /api/parent/location/live
 */
data class LiveLocationsResponse(
    @SerializedName("devices")
    val devices: List<LiveDeviceLocation> = emptyList()
)

data class LiveDeviceLocation(
    @SerializedName("device_id")
    val deviceId: String,
    @SerializedName("child_id")
    val childId: String,
    @SerializedName("child_name")
    val childName: String? = null,
    @SerializedName("is_online")
    val isOnline: Boolean = false,
    @SerializedName("last_seen_at")
    val lastSeenAt: String? = null,
    @SerializedName("lat")
    val latitude: Double? = null,
    @SerializedName("lng")
    val longitude: Double? = null,
    @SerializedName("speed")
    val speed: Float? = null,
    @SerializedName("accuracy")
    val accuracy: Float? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("battery_level")
    val batteryLevel: Int? = null,
    @SerializedName("is_charging")
    val isCharging: Boolean? = null,
    @SerializedName("network_type")
    val networkType: String? = null
)

/**
 * Response from GET /api/parent/location/history/:deviceId
 */
data class LocationHistoryPointsResponse(
    @SerializedName("points")
    val points: List<LocationHistoryPoint> = emptyList()
)

data class LocationHistoryPoint(
    @SerializedName("lat")
    val latitude: Double,
    @SerializedName("lng")
    val longitude: Double,
    @SerializedName("speed")
    val speed: Float = 0f,
    @SerializedName("accuracy")
    val accuracy: Float = 0f,
    @SerializedName("is_mock")
    val isMock: Boolean = false,
    @SerializedName("recorded_at")
    val recordedAt: String? = null
)

/**
 * Response from GET /api/parent/location/summary/:deviceId
 */
data class LocationSummaryResponse(
    @SerializedName("summary")
    val summary: LocationSummary? = null
)

data class LocationSummary(
    @SerializedName("points_today")
    val pointsToday: Int = 0,
    @SerializedName("max_speed")
    val maxSpeed: Float? = null,
    @SerializedName("avg_speed")
    val averageSpeed: Float? = null,
    @SerializedName("first_seen")
    val firstSeen: String? = null,
    @SerializedName("last_seen")
    val lastSeen: String? = null
)

data class LocationData(
    @SerializedName("id") val id: String? = null,
    @SerializedName("child_id") val childId: String? = null,
    @SerializedName("device_id") val deviceId: String? = null,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("accuracy") val accuracy: Float = 0f,
    @SerializedName("speed") val speed: Float = 0f,
    @SerializedName("altitude") val altitude: Float = 0f,
    @SerializedName("bearing") val bearing: Float = 0f,
    @SerializedName("address") val address: String? = null,
    @SerializedName("provider") val provider: String? = "gps",
    @SerializedName("battery_level") val batteryLevel: Int = 0,
    @SerializedName("network_type") val networkType: String? = null,
    @SerializedName("timestamp") val timestamp: Long = 0L,
    @SerializedName("created_at") val createdAt: String? = null
)

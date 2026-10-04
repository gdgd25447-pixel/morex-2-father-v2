package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

data class Report(
    @SerializedName("id") val id: String? = null,
    @SerializedName("child_id") val childId: String,
    @SerializedName("period") val period: String, // daily | weekly | monthly
    @SerializedName("start_date") val startDate: String,
    @SerializedName("end_date") val endDate: String,
    @SerializedName("total_screen_time_minutes") val totalScreenTimeMinutes: Int = 0,
    @SerializedName("total_distance_km") val totalDistanceKm: Float = 0f,
    @SerializedName("total_alerts") val totalAlerts: Int = 0,
    @SerializedName("apps_usage") val appsUsage: List<AppUsageItem> = emptyList(),
    @SerializedName("daily_screen_time") val dailyScreenTime: List<DailyScreenTime> = emptyList(),
    @SerializedName("top_locations") val topLocations: List<TopLocation> = emptyList(),
    @SerializedName("key_events") val keyEvents: List<KeyEvent> = emptyList()
)

data class AppUsageItem(
    @SerializedName("app_name") val appName: String,
    @SerializedName("package_name") val packageName: String?,
    @SerializedName("minutes") val minutes: Int,
    @SerializedName("percentage") val percentage: Float,
    @SerializedName("color") val color: String? = null
)

data class DailyScreenTime(
    @SerializedName("day") val day: String,
    @SerializedName("date") val date: String,
    @SerializedName("minutes") val minutes: Int
)

data class TopLocation(
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String?,
    @SerializedName("visit_count") val visitCount: Int = 0
)

data class KeyEvent(
    @SerializedName("type") val type: String,
    @SerializedName("title") val title: String,
    @SerializedName("day") val day: String?,
    @SerializedName("timestamp") val timestamp: Long = 0L
)

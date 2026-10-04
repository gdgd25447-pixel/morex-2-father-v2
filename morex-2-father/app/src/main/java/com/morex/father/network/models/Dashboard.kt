package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

data class DashboardData(
    @SerializedName("parent_name") val parentName: String?,
    @SerializedName("parent_avatar") val parentAvatar: String?,
    @SerializedName("children") val children: List<Child> = emptyList(),
    @SerializedName("unread_alerts") val unreadAlerts: Int = 0,
    @SerializedName("today_alerts") val todayAlerts: Int = 0,
    @SerializedName("today_screen_time_minutes") val todayScreenTimeMinutes: Int = 0,
    @SerializedName("quran_pages_today") val quranPagesToday: Int = 0,
    @SerializedName("next_prayer") val nextPrayer: NextPrayer? = null,
    @SerializedName("active_sos") val activeSos: Boolean = false,
    @SerializedName("subscription") val subscription: Subscription? = null
)

data class NextPrayer(
    @SerializedName("name") val name: String, // Fajr | Dhuhr | Asr | Maghrib | Isha
    @SerializedName("name_ar") val nameAr: String,
    @SerializedName("time") val time: String, // HH:mm
    @SerializedName("minutes_remaining") val minutesRemaining: Int = 0
)

data class PrayerTimes(
    @SerializedName("date") val date: String,
    @SerializedName("fajr") val fajr: String,
    @SerializedName("sunrise") val sunrise: String? = null,
    @SerializedName("dhuhr") val dhuhr: String,
    @SerializedName("asr") val asr: String,
    @SerializedName("maghrib") val maghrib: String,
    @SerializedName("isha") val isha: String
)

data class QuranProgress(
    @SerializedName("child_id") val childId: String,
    @SerializedName("pages_today") val pagesToday: Int = 0,
    @SerializedName("daily_goal") val dailyGoal: Int = 5,
    @SerializedName("pages_this_week") val pagesThisWeek: Int = 0,
    @SerializedName("total_pages") val totalPages: Int = 0,
    @SerializedName("last_surah") val lastSurah: String? = null,
    @SerializedName("last_ayah") val lastAyah: Int? = null
)

data class DeviceStatus(
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("child_id") val childId: String,
    @SerializedName("is_online") val isOnline: Boolean = false,
    @SerializedName("battery_level") val batteryLevel: Int = 0,
    @SerializedName("is_charging") val isCharging: Boolean = false,
    @SerializedName("network_type") val networkType: String? = null,
    @SerializedName("storage_free") val storageFree: Long = 0L,
    @SerializedName("ram_free") val ramFree: Long = 0L,
    @SerializedName("is_rooted") val isRooted: Boolean = false,
    @SerializedName("screen_on") val screenOn: Boolean = false,
    @SerializedName("last_seen") val lastSeen: String? = null,
    @SerializedName("timestamp") val timestamp: Long = 0L
)

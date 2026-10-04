package com.morex.father.utils

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.text.format.DateUtils
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Helpers {

    // ═══ Toast ═══
    fun toast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun toastLong(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    // ═══ Internet Check ═══
    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // ═══ Time Ago ═══
    /** يحوّل تاريخ ISO-8601 القادم من السيرفر (مثل 2026-10-03T12:00:00.000Z) إلى ميلي ثانية. 0 إن فشل. */
    fun parseIso(iso: String?): Long {
        if (iso.isNullOrBlank()) return 0L
        val patterns = arrayOf("yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ssX", "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd")
        for (p in patterns) {
            try {
                val f = java.text.SimpleDateFormat(p, java.util.Locale.US)
                if (!p.endsWith("X")) f.timeZone = java.util.TimeZone.getTimeZone("UTC")
                return f.parse(iso)?.time ?: continue
            } catch (_: Exception) { }
        }
        return 0L
    }

    fun timeAgo(context: Context, timestamp: Long): String {
        return DateUtils.getRelativeTimeSpanString(
            timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }

    // ═══ Format Dates ═══
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("ar"))
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale("ar"))
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale("ar"))
        return sdf.format(Date(timestamp))
    }

    fun formatFullDate(): String {
        val sdf = SimpleDateFormat("EEEE، dd MMMM yyyy", Locale("ar"))
        return sdf.format(Date())
    }

    // ═══ Battery Icon ═══
    fun getBatteryLevel(): Int {
        return 0
    }

    fun batteryColor(level: Int): Int {
        return when {
            level >= 60 -> 0xFF10B981.toInt()  // Green
            level >= 30 -> 0xFFF59E0B.toInt()  // Yellow
            else -> 0xFFEF4444.toInt()          // Red
        }
    }

    // ═══ Distance ═══
    fun formatDistance(meters: Float): String {
        return if (meters < 1000) {
            "${meters.toInt()} م"
        } else {
            String.format(Locale.US, "%.1f كم", meters / 1000f)
        }
    }

    // ═══ Speed ═══
    fun formatSpeed(kmh: Float): String {
        return String.format(Locale("ar"), "%.1f كم/س", kmh)
    }

    // ═══ File Size ═══
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024))
            else -> String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }

    // ═══ Phone Formatting ═══
    fun formatPhone(phone: String): String {
        val cleaned = phone.replace("+", "").replace(" ", "").replace("-", "")
        return "+$cleaned"
    }

    fun maskPhone(phone: String): String {
        if (phone.length < 6) return phone
        return phone.substring(0, phone.length - 4) + "••••"
    }

    // ═══ Greeting ═══
    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "صباح الخير"
            in 12..16 -> "مساء الخير"
            in 17..20 -> "مساء الخير"
            else -> "طاب مساؤك"
        }
    }

    // ═══ Language Helpers ═══
    fun isRTL(context: Context): Boolean {
        return context.resources.configuration.layoutDirection ==
                android.view.View.LAYOUT_DIRECTION_RTL
    }

    // ═══ Validation ═══
    fun isValidPhone(phone: String): Boolean {
        val cleaned = phone.replace("+", "").replace(" ", "")
        return cleaned.length in 8..15 && cleaned.all { it.isDigit() }
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // ═══ Truncate ═══
    fun truncate(text: String, maxLength: Int): String {
        return if (text.length <= maxLength) text
        else text.substring(0, maxLength - 3) + "..."
    }

    // ═══ Current Timezone ═══
    fun getTimezone(): String = TimeZone.getDefault().id

    // ═══ Day Name (Arabic) ═══
    fun getDayNameArabic(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "الأحد"
            Calendar.MONDAY -> "الاثنين"
            Calendar.TUESDAY -> "الثلاثاء"
            Calendar.WEDNESDAY -> "الأربعاء"
            Calendar.THURSDAY -> "الخميس"
            Calendar.FRIDAY -> "الجمعة"
            Calendar.SATURDAY -> "السبت"
            else -> ""
        }
    }

    // ═══ Screen Size ═══
    fun isTablet(context: Context): Boolean {
        return (context.resources.configuration.screenLayout
                and Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE
    }
}

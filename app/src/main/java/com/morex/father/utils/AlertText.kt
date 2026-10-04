package com.morex.father.utils

import android.content.Context
import com.morex.father.R
import com.morex.father.network.models.Alert

/**
 * السيرفر يرسل type (SOS, LOW_BATTERY, ...) و payload فقط، فنبني العنوان والنص محلياً.
 * الحقول المقروءة من payload هي التي ينتجها السيرفر: fence_name, speed_kmh, battery_level, keyword, message.
 */
object AlertText {

    fun title(ctx: Context, alert: Alert): String {
        val res = when (alert.type.uppercase()) {
            "SOS" -> R.string.alert_title_sos
            "GEOFENCE_ENTER" -> R.string.alert_title_geofence_enter
            "GEOFENCE_EXIT" -> R.string.alert_title_geofence_exit
            "OVERSPEED" -> R.string.alert_title_overspeed
            "LOW_BATTERY" -> R.string.alert_title_low_battery
            "MOCK_LOCATION" -> R.string.alert_title_mock_location
            "ROOT_DETECTED" -> R.string.alert_title_root_detected
            "UNINSTALL_ATTEMPT" -> R.string.alert_title_uninstall_attempt
            "KEYWORD_HIT" -> R.string.alert_title_keyword_hit
            "UNKNOWN_APK" -> R.string.alert_title_unknown_apk
            "PERMISSION_REVOKED" -> R.string.alert_title_permission_revoked
            "AIRPLANE_ON" -> R.string.alert_title_airplane_on
            "DEVELOPER_MODE_ON" -> R.string.alert_title_developer_mode_on
            else -> R.string.alert_title_generic
        }
        return ctx.getString(res)
    }

    /** نص ثانوي: اسم الطفل + تفصيل حسب النوع. */
    fun message(ctx: Context, alert: Alert): String {
        val p = alert.payload
        val detail: String? = when (alert.type.uppercase()) {
            "GEOFENCE_ENTER", "GEOFENCE_EXIT" ->
                p?.get("fence_name")?.toString()?.let { ctx.getString(R.string.alert_msg_place, it) }
            "OVERSPEED" ->
                (p?.get("speed_kmh") as? Number)?.let { ctx.getString(R.string.alert_msg_speed, it.toInt()) }
            "LOW_BATTERY" ->
                (p?.get("battery_level") as? Number)?.let { ctx.getString(R.string.alert_msg_battery, it.toInt()) }
            "KEYWORD_HIT" ->
                p?.get("keyword")?.toString()?.let { ctx.getString(R.string.alert_msg_keyword, it) }
            "SOS" ->
                p?.get("message")?.toString() ?: ctx.getString(R.string.alert_msg_sos_default)
            else -> p?.get("message")?.toString()
        }
        val child = alert.childName
        return listOfNotNull(child, detail).joinToString(" · ")
    }
}

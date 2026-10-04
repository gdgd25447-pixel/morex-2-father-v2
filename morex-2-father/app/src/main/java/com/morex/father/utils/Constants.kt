package com.morex.father.utils

object Constants {

    // ═══ Server ═══
    const val BASE_URL = "https://morex-1-server.onrender.com"
    const val API_BASE = "$BASE_URL/api"
    const val SOCKET_URL = BASE_URL

    // ═══ Timeouts ═══
    const val CONNECT_TIMEOUT = 60L
    const val READ_TIMEOUT = 60L
    const val WRITE_TIMEOUT = 60L

    // ═══ SharedPreferences ═══
    const val PREFS_NAME = "morex_father_prefs"

    // ═══ Prefs Keys ═══
    const val KEY_TOKEN = "auth_token"
    const val KEY_PARENT_ID = "parent_id"
    const val KEY_PARENT_NAME = "parent_name"
    const val KEY_PARENT_PHONE = "parent_phone"
    const val KEY_PARENT_EMAIL = "parent_email"
    const val KEY_PARENT_AVATAR = "parent_avatar"
    const val KEY_SUBSCRIPTION = "subscription_plan"
    const val KEY_SUBSCRIPTION_END = "subscription_end"
    const val KEY_LANGUAGE = "app_language"
    const val KEY_DARK_MODE = "dark_mode"
    const val KEY_ONBOARDING_DONE = "onboarding_done"
    const val KEY_PIN_CODE = "pin_code"
    const val KEY_PIN_ENABLED = "pin_enabled"
    const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    const val KEY_FCM_TOKEN = "fcm_token"
    const val KEY_LAST_SYNC = "last_sync"
    const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val KEY_SELECTED_CHILD_ID = "selected_child_id"

    // ═══ Intent Keys ═══
    const val EXTRA_CHILD_ID = "extra_child_id"
    const val EXTRA_CHILD_NAME = "extra_child_name"
    const val EXTRA_ALERT_ID = "extra_alert_id"
    const val EXTRA_DEVICE_ID = "extra_device_id"
    const val EXTRA_PHONE = "extra_phone"
    const val EXTRA_LANGUAGE = "extra_language"

    // ═══ Notification Channels ═══
    const val CHANNEL_DEFAULT = "morex_default_channel"
    const val CHANNEL_ALERTS = "morex_alerts_channel"
    const val CHANNEL_SOS = "morex_sos_channel"
    const val CHANNEL_LOCATION = "morex_location_channel"
    const val CHANNEL_PRAYER = "morex_prayer_channel"
    const val CHANNEL_QURAN = "morex_quran_channel"
    const val CHANNEL_SUPPORT = "morex_support_channel"

    // ═══ Notification IDs ═══
    const val NOTIF_ALERT = 1001
    const val NOTIF_SOS = 1002
    const val NOTIF_LOCATION = 1003
    const val NOTIF_PRAYER = 1004
    const val NOTIF_QURAN = 1005
    const val NOTIF_CHAT = 1006
    const val NOTIF_GENERIC = 1100

    // ═══ Request Codes ═══
    const val REQ_LOCATION_PERMISSION = 2001
    const val REQ_CAMERA_PERMISSION = 2002
    const val REQ_NOTIFICATION_PERMISSION = 2003
    const val REQ_BIOMETRIC = 2004
    const val REQ_PICK_IMAGE = 2005
    const val REQ_CAPTURE_IMAGE = 2006
    const val REQ_CALL_PHONE = 2007

    // ═══ OTP ═══
    const val OTP_LENGTH = 6
    const val OTP_TIMEOUT_SECONDS = 45
    const val RESEND_COOLDOWN_SECONDS = 45

    // ═══ PIN ═══
    const val PIN_LENGTH = 4

    // ═══ Socket Events ═══
    // أسماء أحداث Socket الفعلية معرّفة في network/SocketManager.Events (ما يرسله السيرفر فقط).
    const val SOCKET_EVENT_ALERT = "alert:new"
    const val SOCKET_EVENT_LOCATION = "location:update"

    // ═══ Default Values ═══
    const val DEFAULT_LANGUAGE = "ar"
    const val DEFAULT_COUNTRY_CODE = "+967"
    const val DEFAULT_CURRENCY = "YER"

    // ═══ Limits ═══
    const val MAX_CHILDREN_FREE = 3
    const val MAX_CHILDREN_FAMILY = 6
    const val MAX_CHILDREN_GOLD = 12

    // ═══ Trial ═══
    const val TRIAL_DAYS = 3
}

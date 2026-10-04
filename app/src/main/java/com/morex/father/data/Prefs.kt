package com.morex.father.data

import android.content.Context
import android.content.SharedPreferences
import com.morex.father.utils.Constants

object Prefs {

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    // ═══ Auth ═══
    // التوكن في التخزين المشفّر. القيمة القديمة (نص صريح) تُنقل مرة واحدة ثم تُمسح.
    fun saveToken(context: Context, token: String) {
        EncryptedPrefs.saveToken(context, token)
        prefs(context).edit().remove(Constants.KEY_TOKEN).apply()
    }
    fun getToken(context: Context): String? {
        EncryptedPrefs.getToken(context)?.let { return it }
        val legacy = prefs(context).getString(Constants.KEY_TOKEN, null)
        if (!legacy.isNullOrBlank()) {
            EncryptedPrefs.saveToken(context, legacy)
            prefs(context).edit().remove(Constants.KEY_TOKEN).apply()
        }
        return legacy
    }
    fun isLoggedIn(context: Context): Boolean = !getToken(context).isNullOrBlank()

    // ═══ Parent ═══
    fun saveParent(
        context: Context,
        id: String,
        name: String,
        phone: String,
        email: String?,
        avatar: String?
    ) {
        prefs(context).edit().apply {
            putString(Constants.KEY_PARENT_ID, id)
            putString(Constants.KEY_PARENT_NAME, name)
            putString(Constants.KEY_PARENT_PHONE, phone)
            putString(Constants.KEY_PARENT_EMAIL, email)
            putString(Constants.KEY_PARENT_AVATAR, avatar)
            apply()
        }
    }

    fun getParentId(context: Context): String? =
        prefs(context).getString(Constants.KEY_PARENT_ID, null)
    fun getParentName(context: Context): String? =
        prefs(context).getString(Constants.KEY_PARENT_NAME, null)
    fun getParentPhone(context: Context): String? =
        prefs(context).getString(Constants.KEY_PARENT_PHONE, null)
    fun getParentEmail(context: Context): String? =
        prefs(context).getString(Constants.KEY_PARENT_EMAIL, null)
    fun getParentAvatar(context: Context): String? =
        prefs(context).getString(Constants.KEY_PARENT_AVATAR, null)

    // ═══ Subscription ═══
    fun saveSubscription(context: Context, plan: String, endDate: String) {
        prefs(context).edit().apply {
            putString(Constants.KEY_SUBSCRIPTION, plan)
            putString(Constants.KEY_SUBSCRIPTION_END, endDate)
            apply()
        }
    }
    fun getSubscriptionPlan(context: Context): String? =
        prefs(context).getString(Constants.KEY_SUBSCRIPTION, null)
    fun getSubscriptionEnd(context: Context): String? =
        prefs(context).getString(Constants.KEY_SUBSCRIPTION_END, null)

    // ═══ Language ═══
    fun setLanguage(context: Context, lang: String) {
        prefs(context).edit().putString(Constants.KEY_LANGUAGE, lang).apply()
    }
    fun getLanguage(context: Context): String =
        prefs(context).getString(Constants.KEY_LANGUAGE, Constants.DEFAULT_LANGUAGE) ?: Constants.DEFAULT_LANGUAGE

    // ═══ Dark Mode ═══
    fun setDarkMode(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(Constants.KEY_DARK_MODE, enabled).apply()
    }
    fun isDarkMode(context: Context): Boolean =
        prefs(context).getBoolean(Constants.KEY_DARK_MODE, false)

    // ═══ Onboarding ═══
    fun setOnboardingDone(context: Context, done: Boolean) {
        prefs(context).edit().putBoolean(Constants.KEY_ONBOARDING_DONE, done).apply()
    }
    fun isOnboardingDone(context: Context): Boolean =
        prefs(context).getBoolean(Constants.KEY_ONBOARDING_DONE, false)

    // ═══ PIN ═══
    // الـ PIN يُخزَّن كـ bcrypt hash في التخزين المشفّر (لا نص صريح).
    fun setPin(context: Context, pin: String) {
        EncryptedPrefs.savePinHash(context, com.morex.father.utils.PinHasher.hash(pin))
        prefs(context).edit().apply {
            remove(Constants.KEY_PIN_CODE) // قيمة قديمة بنص صريح إن وُجدت
            putBoolean(Constants.KEY_PIN_ENABLED, true)
            apply()
        }
    }

    /** يتحقق من الـ PIN. يرحّل تلقائياً أي PIN قديم مخزَّن بنص صريح إلى hash. */
    fun verifyPin(context: Context, pin: String): Boolean {
        val hash = EncryptedPrefs.getPinHash(context)
        if (hash != null) return com.morex.father.utils.PinHasher.verify(pin, hash)
        val legacy = prefs(context).getString(Constants.KEY_PIN_CODE, null) ?: return false
        if (legacy != pin) return false
        setPin(context, pin)
        return true
    }
    fun isPinEnabled(context: Context): Boolean =
        prefs(context).getBoolean(Constants.KEY_PIN_ENABLED, false)
    fun clearPin(context: Context) {
        EncryptedPrefs.clearPin(context)
        prefs(context).edit().apply {
            remove(Constants.KEY_PIN_CODE)
            putBoolean(Constants.KEY_PIN_ENABLED, false)
            apply()
        }
    }

    // ═══ Biometric ═══
    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(Constants.KEY_BIOMETRIC_ENABLED, enabled).apply()
    }
    fun isBiometricEnabled(context: Context): Boolean =
        prefs(context).getBoolean(Constants.KEY_BIOMETRIC_ENABLED, false)

    // ═══ FCM Token ═══
    fun setFcmToken(context: Context, token: String) {
        prefs(context).edit().putString(Constants.KEY_FCM_TOKEN, token).apply()
    }
    fun getFcmToken(context: Context): String? =
        prefs(context).getString(Constants.KEY_FCM_TOKEN, null)

    // ═══ Selected Child ═══
    fun setSelectedChildId(context: Context, id: String?) {
        prefs(context).edit().putString(Constants.KEY_SELECTED_CHILD_ID, id).apply()
    }
    fun getSelectedChildId(context: Context): String? =
        prefs(context).getString(Constants.KEY_SELECTED_CHILD_ID, null)

    // ═══ Notifications ═══
    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(Constants.KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }
    fun areNotificationsEnabled(context: Context): Boolean =
        prefs(context).getBoolean(Constants.KEY_NOTIFICATIONS_ENABLED, true)

    // ═══ Last Sync ═══
    fun setLastSync(context: Context, timestamp: Long) {
        prefs(context).edit().putLong(Constants.KEY_LAST_SYNC, timestamp).apply()
    }
    fun getLastSync(context: Context): Long =
        prefs(context).getLong(Constants.KEY_LAST_SYNC, 0L)

    // ═══ Clear All ═══
    fun clearAuth(context: Context) {
        EncryptedPrefs.clearAuth(context)
        prefs(context).edit().apply {
            remove(Constants.KEY_TOKEN)
            remove(Constants.KEY_PARENT_ID)
            remove(Constants.KEY_PARENT_NAME)
            remove(Constants.KEY_PARENT_PHONE)
            remove(Constants.KEY_PARENT_EMAIL)
            remove(Constants.KEY_PARENT_AVATAR)
            remove(Constants.KEY_FCM_TOKEN)
            remove(Constants.KEY_SELECTED_CHILD_ID)
            apply()
        }
    }

    fun clearAll(context: Context) {
        EncryptedPrefs.clearAll(context)
        prefs(context).edit().clear().apply()
    }
}

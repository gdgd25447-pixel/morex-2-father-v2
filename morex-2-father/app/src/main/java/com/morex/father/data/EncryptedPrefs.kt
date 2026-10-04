package com.morex.father.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.morex.father.utils.Constants
import java.io.File

/**
 * تخزين مشفّر للبيانات الحساسة (التوكن، هاش الـ PIN).
 * AndroidX Security Crypto: المفاتيح AES256-SIV، القيم AES256-GCM، والمفتاح الرئيسي في Android Keystore.
 */
object EncryptedPrefs {
    private const val ENCRYPTED_PREFS = "morex_encrypted_prefs"
    private const val KEY_PIN_HASH = "pin_hash"

    @Volatile
    private var cached: SharedPreferences? = null

    private fun create(context: Context): SharedPreferences {
        val app = context.applicationContext
        val masterKey = MasterKey.Builder(app)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            app,
            ENCRYPTED_PREFS,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun prefs(context: Context): SharedPreferences {
        cached?.let { return it }
        synchronized(this) {
            cached?.let { return it }
            val p = try {
                create(context)
            } catch (e: Exception) {
                // مفتاح Keystore تالف (يحدث بعد استعادة نسخة احتياطية أو تغيير قفل الشاشة):
                // نحذف الملف التالف ونعيد المحاولة مرة واحدة. المستخدم سيسجّل الدخول من جديد.
                val app = context.applicationContext
                File(app.applicationInfo.dataDir, "shared_prefs/$ENCRYPTED_PREFS.xml").delete()
                create(context)
            }
            cached = p
            return p
        }
    }

    fun saveToken(context: Context, token: String) {
        prefs(context).edit().putString(Constants.KEY_TOKEN, token).apply()
    }

    fun getToken(context: Context): String? =
        prefs(context).getString(Constants.KEY_TOKEN, null)

    fun savePinHash(context: Context, hash: String) {
        prefs(context).edit().putString(KEY_PIN_HASH, hash).apply()
    }

    fun getPinHash(context: Context): String? =
        prefs(context).getString(KEY_PIN_HASH, null)

    fun clearPin(context: Context) {
        prefs(context).edit().remove(KEY_PIN_HASH).apply()
    }

    fun clearAuth(context: Context) {
        prefs(context).edit().apply {
            remove(Constants.KEY_TOKEN)
            remove(Constants.KEY_PARENT_ID)
            remove(Constants.KEY_FCM_TOKEN)
            apply()
        }
    }

    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }
}

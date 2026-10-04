package com.morex.father

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.morex.father.data.Prefs
import com.morex.father.network.ApiClient
import com.morex.father.network.FileLogger

class FatherApp : Application() {

    companion object {
        private const val TAG = "MOREX"
    }

    override fun onCreate() {
        super.onCreate()

        // ① FileLogger أولاً
        try {
            FileLogger.init(this)
            FileLogger.write("🟢 FatherApp.onCreate START")
            Log.d(TAG, "📝 Log file: ${FileLogger.path()}")
        } catch (t: Throwable) {
            Log.e(TAG, "❌ FileLogger FAILED", t)
        }

        Log.d(TAG, "🔵 FatherApp.onCreate START")

        // ② Firebase
        try {
            com.google.firebase.FirebaseApp.initializeApp(this)
            Log.d(TAG, "✅ Firebase OK")
            FileLogger.write("✅ Firebase OK")
        } catch (t: Throwable) {
            Log.e(TAG, "❌ Firebase FAILED", t)
            FileLogger.write("❌ Firebase FAILED: ${t.message}")
        }

        // ③ Language
        try {
            val lang = Prefs.getLanguage(this) ?: "ar"
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(lang)
            )
            Log.d(TAG, "✅ Locale OK: $lang")
            FileLogger.write("✅ Locale OK: $lang")
        } catch (t: Throwable) {
            Log.e(TAG, "❌ Locale FAILED", t)
            FileLogger.write("❌ Locale FAILED: ${t.message}")
        }

        // ④ ApiClient
        try {
            ApiClient.init(this)
            Log.d(TAG, "✅ ApiClient OK")
            FileLogger.write("✅ ApiClient OK")
        } catch (t: Throwable) {
            Log.e(TAG, "❌ ApiClient FAILED", t)
            FileLogger.write("❌ ApiClient FAILED: ${t.message}")
        }

        // ⑤ SocketManager
        try {
            if (Prefs.isLoggedIn(this)) {
                com.morex.father.network.SocketManager.connect(this)
                Log.d(TAG, "✅ Socket connecting...")
                FileLogger.write("✅ Socket connecting...")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "❌ Socket FAILED", t)
            FileLogger.write("❌ Socket FAILED: ${t.message}")
        }

        Log.d(TAG, "🟢 FatherApp.onCreate DONE")
        FileLogger.write("🟢 FatherApp.onCreate DONE")
    }
}

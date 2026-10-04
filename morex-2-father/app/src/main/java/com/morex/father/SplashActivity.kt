package com.morex.father

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.morex.father.auth.LoginActivity
import com.morex.father.auth.PinLockActivity
import com.morex.father.data.Prefs
import com.morex.father.onboarding.LanguageActivity
import com.morex.father.utils.SecurityUtils
import androidx.appcompat.app.AlertDialog

class SplashActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SplashActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "🔵 SplashActivity.onCreate")

        setContentView(R.layout.activity_splash)

        if (isEnvironmentBlocked()) {
            AlertDialog.Builder(this)
                .setTitle(R.string.security_blocked_title)
                .setMessage(R.string.security_blocked_message)
                .setCancelable(false)
                .setPositiveButton(R.string.security_blocked_close) { _, _ -> finishAffinity() }
                .show()
            return
        }

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                navigateNext()
            } catch (t: Throwable) {
                Log.e(TAG, "❌ Navigation error", t)
                try {
                    startActivity(Intent(this, LoginActivity::class.java))
                } catch (t2: Throwable) {
                    Log.e(TAG, "❌ Fallback failed", t2)
                }
                finish()
            }
        }, 2000L)
    }

    /** يُطبَّق في نسخة release فقط: جذر، مصحح أخطاء، أو توقيع لا يطابق BuildConfig.EXPECTED_SIGNATURE_SHA256 (إن ضُبط). */
    private fun isEnvironmentBlocked(): Boolean {
        if (BuildConfig.DEBUG) return false
        val report = SecurityUtils.performSecurityCheck(this)
        if (report.rooted || report.debugger || report.tampered) return true
        val expected = BuildConfig.EXPECTED_SIGNATURE_SHA256
        return expected.isNotBlank() && !SecurityUtils.verifySignature(this, expected)
    }

    private fun navigateNext() {
        val intent = when {
            !Prefs.isLoggedIn(this) && !Prefs.isOnboardingDone(this) ->
                Intent(this, LanguageActivity::class.java)
            !Prefs.isLoggedIn(this) ->
                Intent(this, LoginActivity::class.java)
            Prefs.isPinEnabled(this) ->
                Intent(this, PinLockActivity::class.java)
            else ->
                Intent(this, MainActivity::class.java)
        }
        Log.d(TAG, "➡️ Navigating to: ${intent.component?.className}")
        startActivity(intent)
        finish()
    }
}

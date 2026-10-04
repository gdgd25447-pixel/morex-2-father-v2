package com.morex.father.children

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.morex.father.R
import com.morex.father.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PairingCodeActivity : AppCompatActivity() {

    private var childId: String = ""
    private var currentCode: String? = null
    private var pollingJob: Job? = null
    private var isPaired = false

    companion object {
        private const val TAG = "PairingCode"
        private const val POLL_INTERVAL_MS = 3000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pairing_code)

        childId = intent.getStringExtra("child_id") ?: ""

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }

        findViewById<View>(R.id.btnCopy)?.setOnClickListener { copyCode() }

        findViewById<View>(R.id.btnRefresh)?.setOnClickListener {
            stopPolling()
            isPaired = false
            loadPairingCode()
        }

        loadPairingCode()
    }

    override fun onResume() {
        super.onResume()
        // أعد الفحص عند العودة للشاشة
        if (!isPaired && currentCode != null) {
            startPolling()
        }
    }

    override fun onPause() {
        super.onPause()
        stopPolling()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPolling()
    }

    // ═══════════════════════════════════════════
    // توليد الكود
    // ═══════════════════════════════════════════

    private fun loadPairingCode() {
        if (childId.isEmpty()) {
            Toast.makeText(this, "معرّف الطفل مفقود", Toast.LENGTH_SHORT).show()
            return
        }

        val progress = findViewById<ProgressBar>(R.id.progressBar)
        val codeText = findViewById<TextView>(R.id.codeText)
        val expiryText = findViewById<TextView>(R.id.expiryText)

        progress?.visibility = View.VISIBLE
        codeText?.text = "———"
        expiryText?.text = "جارٍ التوليد..."

        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().generatePairingCode(childId)
                }
                progress?.visibility = View.GONE

                if (resp.isSuccessful && resp.body() != null) {
                    val body = resp.body()!!
                    val code: String? = body.code

                    if (code.isNullOrEmpty()) {
                        Toast.makeText(
                            this@PairingCodeActivity,
                            "لم يُرجع السيرفر كوداً",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }

                    currentCode = code
                    codeText?.text = code

                    body.expiresAt?.let { exp ->
                        val formatted = formatDate(exp)
                        expiryText?.text = "صالح حتى: $formatted"
                    }

                    Log.i(TAG, "✅ Code generated: $code")

                    // ابدأ الفحص الدوري فوراً
                    startPolling()

                } else {
                    Toast.makeText(
                        this@PairingCodeActivity,
                        "فشل التوليد (HTTP ${resp.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                progress?.visibility = View.GONE
                Toast.makeText(
                    this@PairingCodeActivity,
                    "خطأ: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "Load failed", e)
            }
        }
    }

    // ═══════════════════════════════════════════
    // الفحص الدوري — كل 3 ثوانٍ
    // ═══════════════════════════════════════════

    private fun startPolling() {
        stopPolling()
        Log.i(TAG, "▶ Polling started (interval ${POLL_INTERVAL_MS}ms)")
        pollingJob = lifecycleScope.launch {
            while (isActive && !isPaired) {
                delay(POLL_INTERVAL_MS)
                if (isPaired) break
                checkPairingStatus()
            }
        }
    }

    private fun stopPolling() {
        if (pollingJob?.isActive == true) {
            pollingJob?.cancel()
            Log.i(TAG, "⏹ Polling stopped")
        }
        pollingJob = null
    }

    private suspend fun checkPairingStatus() {
        try {
            val resp = withContext(Dispatchers.IO) {
                ApiClient.get().getChildren()
            }
            if (!resp.isSuccessful || resp.body() == null) return

            val list = resp.body() ?: emptyList()
            val me = list.firstOrNull { it.id == childId } ?: return

            Log.d(TAG, "poll: child=$childId is_paired=${me.isPaired} device=${me.deviceId}")

            if (me.isPaired && !me.deviceId.isNullOrEmpty()) {
                isPaired = true
                onPairedSuccess(me.deviceId!!)
            }
        } catch (e: Exception) {
            Log.w(TAG, "poll error: ${e.message}")
        }
    }

    // ═══════════════════════════════════════════
    // عند نجاح الاقتران
    // ═══════════════════════════════════════════

    private fun onPairedSuccess(deviceId: String) {
        stopPolling()
        Log.i(TAG, "✅✅✅ PAIRED SUCCESSFULLY! device=$deviceId")

        Toast.makeText(this, "✅ تم الربط بنجاح", Toast.LENGTH_LONG).show()

        // حدّث النص
        findViewById<TextView>(R.id.codeText)?.text = "✅"
        findViewById<TextView>(R.id.expiryText)?.text = "تم الربط — جارٍ الانتقال..."

        // انتقل لصفحة التفاصيل بعد ثانية
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, ChildDetailActivity::class.java)
            intent.putExtra("child_id", childId)
            startActivity(intent)
            finish()
        }, 1200)
    }

    // ═══════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════

    private fun formatDate(iso: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(iso) ?: return iso

            val out = SimpleDateFormat("hh:mm a", Locale("ar"))
            out.timeZone = TimeZone.getDefault()
            out.format(date)
        } catch (e: Exception) {
            iso
        }
    }

    private fun copyCode() {
        val code = currentCode ?: return
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("Morex Pair Code", code))
        Toast.makeText(this, "✅ تم النسخ: $code", Toast.LENGTH_SHORT).show()
    }
}

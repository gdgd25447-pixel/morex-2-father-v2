package com.morex.father.media

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.morex.father.R
import com.morex.father.databinding.ActivityRemoteActionBinding
import com.morex.father.network.ApiClient
import com.morex.father.utils.DeviceResolver
import com.morex.father.utils.Helpers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * طلب لقطة شاشة. السيرفر يرسل الأمر SCREENSHOT للجهاز، والابن يرفعها ثم تظهر في media_captures.
 * بعد الطلب نستطلع كل 3 ثوانٍ (حتى دقيقة) عن لقطة أحدث من وقت الطلب.
 * الموافقة تتم على جهاز الابن؛ هنا نطلب تأكيد الأب فقط.
 */
class ScreenshotActivity : AppCompatActivity() {

    private lateinit var b: ActivityRemoteActionBinding
    private var childId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRemoteActionBinding.inflate(layoutInflater)
        setContentView(b.root)

        childId = intent.getStringExtra("child_id").orEmpty()
        b.tvTitle.text = getString(R.string.screenshot_title)
        b.tvNotice.text = getString(R.string.screenshot_notice)
        b.btnPrimary.text = getString(R.string.screenshot_request)
        b.ivBack.setOnClickListener { finish() }
        b.btnPrimary.setOnClickListener { confirm() }
    }

    private fun confirm() {
        AlertDialog.Builder(this)
            .setMessage(R.string.screenshot_confirm)
            .setPositiveButton(R.string.common_continue) { _, _ -> request() }
            .setNegativeButton(R.string.common_cancel, null)
            .show()
    }

    private fun request() {
        b.btnPrimary.isEnabled = false
        b.progressBar.visibility = View.VISIBLE
        b.tvStatus.text = getString(R.string.remote_sending)
        lifecycleScope.launch {
            val deviceId = DeviceResolver.deviceIdOf(childId)
            if (deviceId == null) { fail(getString(R.string.device_not_paired)); return@launch }
            val requestedAt = System.currentTimeMillis() - 5_000 // هامش لفرق الساعة
            try {
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().requestScreenshot(mapOf("device_id" to deviceId))
                }
                if (!resp.isSuccessful) { fail(getString(R.string.common_loading_failed)); return@launch }
            } catch (_: Exception) { fail(getString(R.string.common_network_error)); return@launch }

            b.tvStatus.text = getString(R.string.screenshot_waiting)
            repeat(20) {
                delay(3_000)
                val url = latestScreenshotAfter(requestedAt)
                if (url != null) {
                    b.progressBar.visibility = View.GONE
                    b.btnPrimary.isEnabled = true
                    b.tvStatus.text = getString(R.string.screenshot_done)
                    b.ivPreview.visibility = View.VISIBLE
                    Glide.with(this@ScreenshotActivity).load(url).into(b.ivPreview)
                    return@launch
                }
            }
            fail(getString(R.string.screenshot_timeout))
        }
    }

    private suspend fun latestScreenshotAfter(afterMs: Long): String? = withContext(Dispatchers.IO) {
        try {
            ApiClient.get().getMediaCaptures(childId).body()
                ?.filter { it.type == "screenshot" && !it.url.isNullOrEmpty() }
                ?.firstOrNull { Helpers.parseIso(it.createdAt) >= afterMs }
                ?.url
        } catch (_: Exception) { null }
    }

    private fun fail(msg: String) {
        b.progressBar.visibility = View.GONE
        b.btnPrimary.isEnabled = true
        b.tvStatus.text = msg
    }
}

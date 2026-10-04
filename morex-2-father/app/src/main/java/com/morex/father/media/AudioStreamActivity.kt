package com.morex.father.media

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.morex.father.R
import com.morex.father.databinding.ActivityRemoteActionBinding
import com.morex.father.network.ApiClient
import com.morex.father.utils.DeviceResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * طلب تسجيل صوتي محيطي. الطلب يمرّ عبر POST /parent/media/mic (AUDIO_STREAM_START، consent_required=true).
 * الابن يُسأل عن الموافقة على جهازه في كل مرة؛ بدونها لا يبدأ التسجيل.
 * التسجيلات الجاهزة (media_type = audio) تظهر في زر "فتح آخر تسجيل".
 */
class AudioStreamActivity : AppCompatActivity() {

    private lateinit var b: ActivityRemoteActionBinding
    private var childId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRemoteActionBinding.inflate(layoutInflater)
        setContentView(b.root)

        childId = intent.getStringExtra("child_id").orEmpty()
        b.tvTitle.text = getString(R.string.audio_title)
        b.tvNotice.text = getString(R.string.audio_notice)
        b.rgDuration.visibility = View.VISIBLE
        b.btnPrimary.text = getString(R.string.audio_start)
        b.btnSecondary.visibility = View.VISIBLE
        b.btnSecondary.text = getString(R.string.audio_open_latest)

        b.ivBack.setOnClickListener { finish() }
        b.btnPrimary.setOnClickListener { confirm() }
        b.btnSecondary.setOnClickListener { openLatest() }
    }

    private fun duration(): Int = when (b.rgDuration.checkedRadioButtonId) {
        R.id.rb15 -> 15
        R.id.rb60 -> 60
        else -> 30
    }

    private fun confirm() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.audio_confirm, duration()))
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
            if (deviceId == null) { done(getString(R.string.device_not_paired)); return@launch }
            try {
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().requestMicRecord(
                        mapOf<String, Any>("device_id" to deviceId, "duration_sec" to duration())
                    )
                }
                done(getString(if (resp.isSuccessful) R.string.audio_sent else R.string.common_loading_failed))
            } catch (_: Exception) {
                done(getString(R.string.common_network_error))
            }
        }
    }

    private fun done(msg: String) {
        b.progressBar.visibility = View.GONE
        b.btnPrimary.isEnabled = true
        b.tvStatus.text = msg
    }

    private fun openLatest() {
        b.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val url = withContext(Dispatchers.IO) {
                try {
                    ApiClient.get().getMediaCaptures(childId).body()
                        ?.firstOrNull { it.type == "audio" && !it.url.isNullOrEmpty() }?.url
                } catch (_: Exception) { null }
            }
            b.progressBar.visibility = View.GONE
            if (url == null) {
                b.tvStatus.text = getString(R.string.audio_none)
            } else {
                try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                catch (_: Exception) { b.tvStatus.text = getString(R.string.audio_none) }
            }
        }
    }
}

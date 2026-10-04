package com.morex.father.control

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

/** قفل الشاشة وفتحها فوراً: LOCK_NOW / UNLOCK عبر POST /parent/commands. */
class LockScreenActivity : AppCompatActivity() {

    private lateinit var b: ActivityRemoteActionBinding
    private var childId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRemoteActionBinding.inflate(layoutInflater)
        setContentView(b.root)

        childId = intent.getStringExtra("child_id").orEmpty()
        b.tvTitle.text = getString(R.string.lock_title)
        b.tvNotice.text = getString(R.string.lock_notice)
        b.btnPrimary.text = getString(R.string.lock_now)
        b.btnSecondary.visibility = View.VISIBLE
        b.btnSecondary.text = getString(R.string.lock_unlock)

        b.ivBack.setOnClickListener { finish() }
        b.btnPrimary.setOnClickListener { confirm("LOCK_NOW", R.string.lock_confirm) }
        b.btnSecondary.setOnClickListener { send("UNLOCK") }
    }

    private fun confirm(type: String, msg: Int) {
        AlertDialog.Builder(this)
            .setMessage(msg)
            .setPositiveButton(R.string.common_continue) { _, _ -> send(type) }
            .setNegativeButton(R.string.common_cancel, null)
            .show()
    }

    private fun send(type: String) {
        setBusy(true)
        lifecycleScope.launch {
            val deviceId = DeviceResolver.deviceIdOf(childId)
            if (deviceId == null) { finishWith(getString(R.string.device_not_paired)); return@launch }
            try {
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().sendCommand(mapOf("device_id" to deviceId, "type" to type, "payload" to emptyMap<String, Any>()))
                }
                finishWith(getString(if (resp.isSuccessful) R.string.remote_sent else R.string.common_loading_failed))
            } catch (_: Exception) {
                finishWith(getString(R.string.common_network_error))
            }
        }
    }

    private fun setBusy(on: Boolean) {
        b.progressBar.visibility = if (on) View.VISIBLE else View.GONE
        b.btnPrimary.isEnabled = !on
        b.btnSecondary.isEnabled = !on
        if (on) b.tvStatus.text = getString(R.string.remote_sending)
    }

    private fun finishWith(msg: String) {
        setBusy(false)
        b.tvStatus.text = msg
    }
}

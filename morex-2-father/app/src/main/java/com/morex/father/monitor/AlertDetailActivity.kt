package com.morex.father.monitor

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.morex.father.R
import com.morex.father.databinding.ActivityAlertDetailBinding
import com.morex.father.network.ApiClient
import com.morex.father.network.models.Alert
import com.morex.father.utils.AlertText
import com.morex.father.utils.Helpers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * تفاصيل تنبيه. السيرفر لا يوفّر GET /alerts/:id، لذلك يصل التنبيه كاملاً من القائمة عبر EXTRA_ALERT_JSON.
 */
class AlertDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ALERT_JSON = "alert_json"
        const val EXTRA_ALERT_ID = "alert_id"
    }

    private lateinit var binding: ActivityAlertDetailBinding
    private var alert: Alert? = null
    private var alertId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alertId = intent.getStringExtra(EXTRA_ALERT_ID) ?: ""
        alert = intent.getStringExtra(EXTRA_ALERT_JSON)?.let {
            try { Gson().fromJson(it, Alert::class.java) } catch (_: Exception) { null }
        }
        if (alertId.isEmpty()) alertId = alert?.id ?: ""

        binding.ivBack.setOnClickListener { finish() }
        binding.btnResolve.setOnClickListener { resolve() }
        binding.btnDelete.setOnClickListener { delete() }

        alert?.let { a ->
            binding.tvAlertTitle.text = AlertText.title(this, a)
            binding.tvAlertMessage.text = AlertText.message(this, a)
            binding.tvAlertTime.text = Helpers.formatDateTime(Helpers.parseIso(a.createdAt))
            // تنبيه SOS محلول لا يحتاج زر الحل
            if (a.isSos && a.isResolved) binding.btnResolve.visibility = View.GONE
        }
    }

    private fun resolve() {
        if (alertId.isEmpty()) return
        val isSos = alert?.isSos == true
        binding.btnResolve.isEnabled = false
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    if (isSos) ApiClient.get().resolveSos(alertId) else ApiClient.get().markAlertRead(alertId)
                }
                if (resp.isSuccessful) {
                    Toast.makeText(this@AlertDetailActivity, R.string.alert_detail_resolved, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    binding.btnResolve.isEnabled = true
                    Toast.makeText(this@AlertDetailActivity, R.string.common_loading_failed, Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                binding.btnResolve.isEnabled = true
                Toast.makeText(this@AlertDetailActivity, R.string.common_network_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun delete() {
        if (alertId.isEmpty()) return
        binding.btnDelete.isEnabled = false
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) { ApiClient.get().deleteAlert(alertId) }
                if (resp.isSuccessful) {
                    Toast.makeText(this@AlertDetailActivity, R.string.alert_detail_deleted, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    binding.btnDelete.isEnabled = true
                    Toast.makeText(this@AlertDetailActivity, R.string.common_loading_failed, Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                binding.btnDelete.isEnabled = true
                Toast.makeText(this@AlertDetailActivity, R.string.common_network_error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

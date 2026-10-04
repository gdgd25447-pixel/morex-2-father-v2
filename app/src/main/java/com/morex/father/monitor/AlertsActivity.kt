package com.morex.father.monitor

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.morex.father.databinding.ActivityAlertsBinding
import com.morex.father.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlertsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlertsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }
        binding.rvAlerts.layoutManager = LinearLayoutManager(this)

        binding.tvMarkAllRead.setOnClickListener { markAllRead() }

        loadAlerts()
    }

    private fun loadAlerts() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) { ApiClient.get().getAlerts() }
                binding.progressBar.visibility = View.GONE
                if (resp.isSuccessful) {
                    val alerts = resp.body() ?: emptyList()
                    binding.rvAlerts.adapter = AlertsAdapter(alerts) { alert ->
                        val i = Intent(this@AlertsActivity, AlertDetailActivity::class.java)
                        i.putExtra(AlertDetailActivity.EXTRA_ALERT_ID, alert.id)
                        i.putExtra(AlertDetailActivity.EXTRA_ALERT_JSON, com.google.gson.Gson().toJson(alert))
                        startActivity(i)
                    }
                    binding.emptyView.visibility =
                        if (alerts.isEmpty()) View.VISIBLE else View.GONE
                }
            } catch (_: Exception) {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun markAllRead() {
        lifecycleScope.launch {
            try { withContext(Dispatchers.IO) { ApiClient.get().markAllAlertsRead() }
                loadAlerts()
            } catch (_: Exception) {}
        }
    }
}

package com.morex.father.monitor

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.morex.father.databinding.ActivitySosBinding
import com.morex.father.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SosActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }
        binding.rvSos.layoutManager = LinearLayoutManager(this)

        loadActiveSos()

        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                com.morex.father.network.SocketManager.events.collect { e ->
                    val isSos = e.data?.optString("type").equals("SOS", ignoreCase = true)
                    if (e.name == com.morex.father.network.SocketManager.Events.ALERT_NEW && isSos) loadActiveSos()
                }
            }
        }
    }

    private fun loadActiveSos() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) { ApiClient.get().getActiveSos() }
                binding.progressBar.visibility = View.GONE
                if (resp.isSuccessful) {
                    val alerts = resp.body() ?: emptyList()
                    if (alerts.isEmpty()) {
                        binding.tvStatus.text = "لا يوجد تنبيه طارئ"
                        binding.ivShield.setImageResource(com.morex.father.R.drawable.ic_shield_check)
                        binding.rvSos.visibility = View.GONE
                    } else {
                        binding.tvStatus.text = "تنبيه طارئ!"
                        binding.ivShield.setImageResource(com.morex.father.R.drawable.ic_nav_alerts)
                        binding.rvSos.visibility = View.VISIBLE
                        binding.rvSos.adapter = AlertsAdapter(alerts) {}
                    }
                }
            } catch (_: Exception) {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}

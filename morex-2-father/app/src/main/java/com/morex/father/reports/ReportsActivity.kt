package com.morex.father.reports

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.morex.father.databinding.ActivityReportsBinding
import com.morex.father.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }
        loadReport()
    }

    private fun loadReport() {
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) { ApiClient.get().getAlerts(limit = 50) }
                if (resp.isSuccessful) {
                    val count = resp.body()?.size ?: 0
                    binding.tvAlertsCount.text = count.toString()
                }
            } catch (_: Exception) {}
        }
    }
}

package com.morex.father.alerts

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.repeatOnLifecycle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.morex.father.databinding.FragmentAlertsBinding
import com.morex.father.location.SpeedAlertsActivity
import com.morex.father.monitor.AlertDetailActivity
import com.morex.father.monitor.AlertsAdapter
import com.morex.father.monitor.SosActivity
import com.morex.father.network.ApiClient
import com.morex.father.web.KeywordAlertsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlertsFragment : Fragment() {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentAlertsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvAlerts.layoutManager = LinearLayoutManager(requireContext())

        // ═══ أزرار الإجراءات السريعة ═══
        binding.btnSos.setOnClickListener {
            startActivity(Intent(requireContext(), SosActivity::class.java))
        }
        binding.btnSpeedAlerts.setOnClickListener {
            startActivity(Intent(requireContext(), SpeedAlertsActivity::class.java))
        }
        binding.btnKeywordAlerts.setOnClickListener {
            startActivity(Intent(requireContext(), KeywordAlertsActivity::class.java))
        }

        load()

        // تحديث لحظي عند وصول تنبيه جديد
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                com.morex.father.network.SocketManager.events.collect { e ->
                    if (e.name == com.morex.father.network.SocketManager.Events.ALERT_NEW) load()
                }
            }
        }
    }

    private fun load() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) { ApiClient.get().getAlerts() }
                binding.progressBar.visibility = View.GONE
                if (resp.isSuccessful) {
                    val alerts = resp.body() ?: emptyList()
                    binding.rvAlerts.adapter = AlertsAdapter(alerts) { alert ->
                        val i = Intent(requireContext(), AlertDetailActivity::class.java)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

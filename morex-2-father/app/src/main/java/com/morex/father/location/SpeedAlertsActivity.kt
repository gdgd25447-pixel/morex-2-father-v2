package com.morex.father.location

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.morex.father.R
import com.morex.father.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class SpeedAlertsActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progress: View
    private val adapter = SpeedAdapter()

    companion object {
        private const val TAG = "SpeedAlerts"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speed_alerts)

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }

        rv = findViewById(R.id.rvSpeed)
        emptyView = findViewById(R.id.emptyView)
        progress = findViewById(R.id.progressBar)

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        loadAlerts()
    }

    private fun loadAlerts() {
        progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().getAlerts(limit = 100)
                }
                progress.visibility = View.GONE

                if (!resp.isSuccessful || resp.body() == null) {
                    emptyView.visibility = View.VISIBLE
                    return@launch
                }

                val items = (resp.body() ?: emptyList())
                    .filter { it.type.equals("OVERSPEED", ignoreCase = true) }
                    .map { a ->
                        SpeedItem(
                            speed = (a.payload?.get("speed_kmh") as? Number)?.toDouble() ?: 0.0,
                            limit = (a.payload?.get("limit_kmh") as? Number)?.toDouble() ?: 0.0,
                            time = a.createdAt.orEmpty()
                        )
                    }

                adapter.submit(items)
                emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE

            } catch (e: Exception) {
                progress.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
                Log.e(TAG, "Load failed", e)
            }
        }
    }

    data class SpeedItem(val speed: Double, val limit: Double, val time: String)

    private class SpeedAdapter : RecyclerView.Adapter<SpeedAdapter.VH>() {
        private val items = mutableListOf<SpeedItem>()

        fun submit(list: List<SpeedItem>) {
            items.clear(); items.addAll(list); notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_speed_alert, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
        override fun getItemCount(): Int = items.size

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvSpeed: TextView = v.findViewById(R.id.tvSpeed)
            val tvLimit: TextView = v.findViewById(R.id.tvLimit)
            val tvTime: TextView = v.findViewById(R.id.tvTime)

            fun bind(item: SpeedItem) {
                tvSpeed.text = "🚗 ${"%.0f".format(item.speed)} كم/س"
                tvLimit.text = if (item.limit > 0) "الحد: ${"%.0f".format(item.limit)} كم/س" else ""
                tvTime.text = item.time
            }
        }
    }
}

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
import com.morex.father.utils.Helpers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class LocationHistoryActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progress: View
    private val adapter = HistoryAdapter()
    private var childId: String = ""

    companion object {
        private const val TAG = "LocationHistory"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_history)

        childId = intent.getStringExtra("child_id") ?: ""

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }

        rv = findViewById(R.id.rvHistory)
        emptyView = findViewById(R.id.emptyView)
        progress = findViewById(R.id.progressBar)

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        loadHistory()
    }

    private fun loadHistory() {
        if (childId.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            return
        }

        progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // ⭐ استدعاء بمعامل واحد — بدون days
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().getLocationHistory(childId)
                }
                progress.visibility = View.GONE

                if (!resp.isSuccessful || resp.body() == null) {
                    Log.e(TAG, "HTTP ${resp.code()}")
                    emptyView.visibility = View.VISIBLE
                    return@launch
                }

                val items = (resp.body()?.points ?: emptyList()).map { p ->
                    HistoryItem(
                        lat = p.latitude,
                        lng = p.longitude,
                        speed = p.speed.toDouble(),
                        accuracy = p.accuracy.toDouble(),
                        time = Helpers.formatDateTime(Helpers.parseIso(p.recordedAt))
                    )
                }.asReversed() // الأحدث أولاً

                adapter.submit(items)
                emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                Log.i(TAG, "Loaded ${items.size} points")

            } catch (e: Exception) {
                progress.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
                Log.e(TAG, "Load failed", e)
            }
        }
    }

    data class HistoryItem(
        val lat: Double,
        val lng: Double,
        val speed: Double,
        val accuracy: Double,
        val time: String
    )

    private class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.VH>() {

        private val items = mutableListOf<HistoryItem>()

        fun submit(list: List<HistoryItem>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_location_history, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvTime: TextView = v.findViewById(R.id.tvTime)
            val tvCoords: TextView = v.findViewById(R.id.tvCoords)
            val tvSpeed: TextView = v.findViewById(R.id.tvSpeed)

            fun bind(item: HistoryItem) {
                tvTime.text = item.time
                tvCoords.text = "📍 ${item.lat.toString().take(8)}, ${item.lng.toString().take(8)}"
                tvSpeed.text = if (item.speed > 0.1) {
                    "🚗 ${"%.1f".format(item.speed * 3.6)} كم/س"
                } else "🛑 متوقف"
            }
        }
    }
}

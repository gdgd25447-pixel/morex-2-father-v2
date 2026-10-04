package com.morex.father.geofence

import android.content.Intent
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.morex.father.R
import com.morex.father.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class GeofenceListActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progress: View
    private val adapter = GeofenceAdapter()
    private var childId: String = ""

    companion object {
        private const val TAG = "GeofenceList"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geofence_list)

        childId = intent.getStringExtra("child_id") ?: ""

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }
        findViewById<FloatingActionButton>(R.id.fabAdd)?.setOnClickListener {
            val i = Intent(this, GeofenceEditActivity::class.java)
            i.putExtra("child_id", childId)
            startActivity(i)
        }

        rv = findViewById(R.id.rvGeofences)
        emptyView = findViewById(R.id.emptyView)
        progress = findViewById(R.id.progressBar)

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        loadGeofences()
    }

    override fun onResume() {
        super.onResume()
        loadGeofences()
    }

    private fun loadGeofences() {
        if (childId.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            return
        }

        progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().getGeofences(childId)
                }
                progress.visibility = View.GONE

                if (!resp.isSuccessful || resp.body() == null) {
                    emptyView.visibility = View.VISIBLE
                    return@launch
                }

                val gson = com.google.gson.Gson()
                val items = (resp.body() ?: emptyList()).map { g ->
                    GeofenceItem(
                        id = g.id.orEmpty(),
                        name = g.name,
                        lat = g.latitude,
                        lng = g.longitude,
                        radius = g.radius,
                        enabled = g.isActive,
                        json = gson.toJson(g)
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

    data class GeofenceItem(
        val id: String,
        val name: String,
        val lat: Double,
        val lng: Double,
        val radius: Int,
        val enabled: Boolean,
        val json: String = ""
    )

    private inner class GeofenceAdapter : RecyclerView.Adapter<GeofenceAdapter.VH>() {
        private val items = mutableListOf<GeofenceItem>()

        fun submit(list: List<GeofenceItem>) {
            items.clear(); items.addAll(list); notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_geofence, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
        override fun getItemCount(): Int = items.size

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvName: TextView = v.findViewById(R.id.tvName)
            val tvCoords: TextView = v.findViewById(R.id.tvCoords)
            val tvStatus: TextView = v.findViewById(R.id.tvStatus)

            fun bind(item: GeofenceItem) {
                tvName.text = item.name
                tvCoords.text = "📍 ${"%.4f".format(item.lat)}, ${"%.4f".format(item.lng)} · ${item.radius}م"
                tvStatus.text = if (item.enabled) "🟢 مفعّل" else "⚪ متوقف"

                itemView.setOnClickListener {
                    val i = Intent(this@GeofenceListActivity, GeofenceEditActivity::class.java)
                    i.putExtra("child_id", childId)
                    i.putExtra("geofence_id", item.id)
                    i.putExtra("geofence_json", item.json)
                    startActivity(i)
                }
            }
        }
    }
}

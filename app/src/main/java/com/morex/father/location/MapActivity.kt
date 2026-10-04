package com.morex.father.location

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.gson.Gson
import com.morex.father.R
import com.morex.father.network.ApiClient
import com.morex.father.utils.Helpers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class MapActivity : AppCompatActivity() {

    private var childId: String = ""
    private var lat: Double = 0.0
    private var lng: Double = 0.0

    companion object {
        private const val TAG = "Map"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        childId = intent.getStringExtra("child_id") ?: ""

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }
        findViewById<View>(R.id.btnOpenMaps)?.setOnClickListener { openInExternalMap() }

        loadLocation()

        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                com.morex.father.network.SocketManager.events.collect { e ->
                    if (e.name != com.morex.father.network.SocketManager.Events.LOCATION_UPDATE) return@collect
                    val d = e.data ?: return@collect
                    val sameChild = d.optString("child_id") == childId
                    if (sameChild && d.has("lat") && d.has("lng")) {
                        lat = d.optDouble("lat")
                        lng = d.optDouble("lng")
                        findViewById<TextView>(R.id.tvLocation)?.text = "📍 ${"%.5f".format(lat)}, ${"%.5f".format(lng)}"
                    }
                }
            }
        }
    }

    private fun loadLocation() {
        val progress = findViewById<View>(R.id.progressBar)
        val tvLocation = findViewById<TextView>(R.id.tvLocation)
        val tvUpdated = findViewById<TextView>(R.id.tvUpdated)

        if (childId.isEmpty()) {
            tvLocation?.text = "لم يتم تحديد الطفل"
            return
        }

        progress?.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // ⭐ نستخدم getLiveLocations() — موجودة في ApiService
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().getLiveLocations()
                }
                progress?.visibility = View.GONE

                if (!resp.isSuccessful || resp.body() == null) {
                    tvLocation?.text = "تعذّر الاتصال بالخادم (${resp.code()})"
                    return@launch
                }

                val devices = resp.body()?.devices ?: emptyList()
                if (devices.isEmpty()) {
                    tvLocation?.text = getString(R.string.map_no_data)
                    tvUpdated?.text = getString(R.string.map_no_device_sent)
                    return@launch
                }

                // الجهاز المطابق للطفل فقط (لا نعرض موقع طفل آخر احتياطاً)
                val matched = devices.firstOrNull { it.childId == childId }
                if (matched == null) {
                    tvLocation?.text = getString(R.string.map_child_not_paired)
                    tvUpdated?.text = ""
                    return@launch
                }

                val mLat = matched.latitude
                val mLng = matched.longitude
                if (mLat == null || mLng == null || (mLat == 0.0 && mLng == 0.0)) {
                    tvLocation?.text = getString(R.string.map_location_unavailable)
                    tvUpdated?.text = getString(R.string.map_no_valid_location)
                    return@launch
                }
                lat = mLat
                lng = mLng
                tvLocation?.text = "📍 ${"%.5f".format(lat)}, ${"%.5f".format(lng)}"

                val lines = mutableListOf<String>()
                val updated = matched.updatedAt ?: matched.lastSeenAt
                if (!updated.isNullOrEmpty()) {
                    lines.add(getString(R.string.map_last_update, Helpers.formatDateTime(Helpers.parseIso(updated))))
                }
                matched.accuracy?.takeIf { it > 0 }?.let { lines.add(getString(R.string.map_accuracy_m, it.toInt())) }
                // السيرفر يخزّن السرعة بالمتر/ثانية
                matched.speed?.takeIf { it > 0.5f }?.let { lines.add(getString(R.string.map_speed_kmh, it * 3.6f)) }
                tvUpdated?.text = lines.joinToString("\n")

                Log.i(TAG, "location ok")

            } catch (e: Exception) {
                progress?.visibility = View.GONE
                Log.e(TAG, "Load failed", e)
                tvLocation?.text = "خطأ: ${e.message}"
            }
        }
    }

    private fun openInExternalMap() {
        if (lat == 0.0 && lng == 0.0) {
            Toast.makeText(this, "لا يوجد موقع لفتحه", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        } catch (e: Exception) {
            val uri = Uri.parse("https://maps.google.com/?q=$lat,$lng")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }
}

package com.morex.father.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.morex.father.R
import com.morex.father.network.ApiClient
import com.morex.father.network.models.Child
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DevicesActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var emptyView: TextView
    private val adapter = DevicesAdapter()

    companion object {
        private const val TAG = "DevicesActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices)

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }

        rv = findViewById(R.id.rvDevices)
        emptyView = findViewById(R.id.emptyView)

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        loadDevices()
    }

    private fun loadDevices() {
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().getChildren()
                }
                if (!resp.isSuccessful || resp.body() == null) {
                    Log.e(TAG, "Failed: ${resp.code()}")
                    return@launch
                }

                val list = resp.body() ?: emptyList()
                val paired = list.filter { it.isPaired && !it.deviceId.isNullOrEmpty() }

                adapter.submit(paired)
                emptyView.visibility = if (paired.isEmpty()) View.VISIBLE else View.GONE
                Log.i(TAG, "Loaded ${paired.size} devices from ${list.size} children")

            } catch (e: Exception) {
                Log.e(TAG, "Load failed", e)
            }
        }
    }

    // ═══════════════════════════════════════════
    // Adapter داخلي
    // ═══════════════════════════════════════════

    private class DevicesAdapter : RecyclerView.Adapter<DevicesAdapter.VH>() {

        private val items = mutableListOf<Child>()

        fun submit(list: List<Child>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_device, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val ivAvatar: ImageView = v.findViewById(R.id.ivAvatar)
            val tvChild: TextView = v.findViewById(R.id.tvChild)
            val tvModel: TextView = v.findViewById(R.id.tvModel)
            val tvStatus: TextView = v.findViewById(R.id.tvStatus)
            val tvBattery: TextView = v.findViewById(R.id.tvBattery)

            fun bind(c: Child) {
                tvChild.text = c.name ?: "بدون اسم"
                tvModel.text = c.model ?: "جهاز غير معروف"
                tvStatus.text = if (c.isOnline) "🟢 متصل" else "⚪ غير متصل"
                tvBattery.text = "${c.batteryLevel}%"
            }
        }
    }
}

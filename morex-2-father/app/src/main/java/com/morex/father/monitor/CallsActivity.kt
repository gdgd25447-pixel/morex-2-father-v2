package com.morex.father.monitor

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

class CallsActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progress: View
    private val adapter = CallsAdapter()
    private var childId: String = ""

    companion object {
        private const val TAG = "Calls"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calls)

        childId = intent.getStringExtra("child_id") ?: ""

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }

        rv = findViewById(R.id.rvCalls)
        emptyView = findViewById(R.id.emptyView)
        progress = findViewById(R.id.progressBar)

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        loadCalls()
    }

    private fun loadCalls() {
        if (childId.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            return
        }

        progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().getCallLogs(childId)
                }
                progress.visibility = View.GONE

                if (!resp.isSuccessful || resp.body() == null) {
                    emptyView.visibility = View.VISIBLE
                    return@launch
                }

                val unknown = getString(R.string.contact_unknown)
                val items = (resp.body() ?: emptyList()).map { c ->
                    CallItem(
                        name = c.contactName ?: unknown,
                        number = c.number.orEmpty(),
                        type = c.type ?: "incoming",
                        duration = c.durationSeconds,
                        time = Helpers.formatDateTime(Helpers.parseIso(c.calledAt))
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

    data class CallItem(
        val name: String,
        val number: String,
        val type: String,
        val duration: Int,
        val time: String
    )

    private class CallsAdapter : RecyclerView.Adapter<CallsAdapter.VH>() {

        private val items = mutableListOf<CallItem>()

        fun submit(list: List<CallItem>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_call, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvName: TextView = v.findViewById(R.id.tvName)
            val tvNumber: TextView = v.findViewById(R.id.tvNumber)
            val tvType: TextView = v.findViewById(R.id.tvType)
            val tvTime: TextView = v.findViewById(R.id.tvTime)

            fun bind(item: CallItem) {
                tvName.text = item.name
                tvNumber.text = item.number
                tvTime.text = item.time

                val (icon, color) = when (item.type.lowercase()) {
                    "incoming", "in" -> "📥 واردة" to 0xFF2E7D32
                    "outgoing", "out" -> "📤 صادرة" to 0xFF1565C0
                    "missed" -> "❌ فائتة" to 0xFFC62828
                    else -> "📞 مكالمة" to 0xFF666666
                }
                val mins = item.duration / 60
                val secs = item.duration % 60
                tvType.text = if (item.duration > 0) "$icon · ${mins}:${"%02d".format(secs)}" else icon
                tvType.setTextColor(color.toInt())
            }
        }
    }
}

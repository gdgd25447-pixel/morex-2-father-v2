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

class SmsActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progress: View
    private val adapter = SmsAdapter()
    private var childId: String = ""

    companion object {
        private const val TAG = "Sms"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms)

        childId = intent.getStringExtra("child_id") ?: ""

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }

        rv = findViewById(R.id.rvSms)
        emptyView = findViewById(R.id.emptyView)
        progress = findViewById(R.id.progressBar)

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        loadSms()
    }

    private fun loadSms() {
        if (childId.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            return
        }

        progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().getSmsLogs(childId)
                }
                progress.visibility = View.GONE

                if (!resp.isSuccessful || resp.body() == null) {
                    emptyView.visibility = View.VISIBLE
                    return@launch
                }

                val unknown = getString(R.string.contact_unknown)
                val items = (resp.body() ?: emptyList()).map { m ->
                    SmsItem(
                        name = m.contactName ?: unknown,
                        number = m.number.orEmpty(),
                        body = m.body.orEmpty(),
                        type = m.type ?: "in",
                        time = Helpers.formatDateTime(Helpers.parseIso(m.sentAt))
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

    data class SmsItem(
        val name: String,
        val number: String,
        val body: String,
        val type: String,
        val time: String
    )

    private class SmsAdapter : RecyclerView.Adapter<SmsAdapter.VH>() {

        private val items = mutableListOf<SmsItem>()

        fun submit(list: List<SmsItem>) {
            items.clear()
            items.addAll(list)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sms, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvName: TextView = v.findViewById(R.id.tvName)
            val tvNumber: TextView = v.findViewById(R.id.tvNumber)
            val tvBody: TextView = v.findViewById(R.id.tvBody)
            val tvTime: TextView = v.findViewById(R.id.tvTime)

            fun bind(item: SmsItem) {
                tvName.text = item.name
                tvNumber.text = item.number
                tvBody.text = item.body
                tvTime.text = item.time
            }
        }
    }
}

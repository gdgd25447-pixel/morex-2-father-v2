package com.morex.father.geofence

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.morex.father.R
import com.morex.father.databinding.ActivitySimpleListBinding
import com.morex.father.network.ApiClient
import com.morex.father.network.models.Alert
import com.morex.father.utils.AlertText
import com.morex.father.utils.Helpers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** تنبيهات دخول/خروج السياج (GEOFENCE_ENTER / GEOFENCE_EXIT) من GET /parent/alerts. */
class GeofenceAlertsActivity : AppCompatActivity() {

    private lateinit var b: ActivitySimpleListBinding
    private var childId: String? = null
    private val adapter = Adapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySimpleListBinding.inflate(layoutInflater)
        setContentView(b.root)

        childId = intent.getStringExtra("child_id")?.takeIf { it.isNotEmpty() }
        b.tvTitle.text = getString(R.string.geofence_alerts_title)
        b.ivBack.setOnClickListener { finish() }
        b.rvItems.layoutManager = LinearLayoutManager(this)
        b.rvItems.adapter = adapter
        b.swipeRefresh.setOnRefreshListener { load(fromSwipe = true) }
        b.btnRetry.setOnClickListener { load() }
        load()
    }

    private fun load(fromSwipe: Boolean = false) {
        b.errorView.visibility = View.GONE
        b.emptyView.visibility = View.GONE
        if (!fromSwipe) b.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) { ApiClient.get().getAlerts(limit = 100, childId = childId) }
                b.progressBar.visibility = View.GONE
                b.swipeRefresh.isRefreshing = false
                if (!resp.isSuccessful) { b.errorView.visibility = View.VISIBLE; return@launch }
                val items = (resp.body() ?: emptyList()).filter {
                    it.type.equals("GEOFENCE_ENTER", true) || it.type.equals("GEOFENCE_EXIT", true)
                }
                adapter.submit(items)
                b.emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            } catch (_: Exception) {
                b.progressBar.visibility = View.GONE
                b.swipeRefresh.isRefreshing = false
                b.errorView.visibility = View.VISIBLE
            }
        }
    }

    private class Adapter : RecyclerView.Adapter<Adapter.VH>() {
        private val items = mutableListOf<Alert>()
        fun submit(list: List<Alert>) { items.clear(); items.addAll(list); notifyDataSetChanged() }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_alert, parent, false))
        override fun onBindViewHolder(h: VH, position: Int) = h.bind(items[position])
        override fun getItemCount() = items.size

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            private val title: TextView = v.findViewById(R.id.tvTitle)
            private val message: TextView = v.findViewById(R.id.tvMessage)
            private val time: TextView = v.findViewById(R.id.tvTime)
            fun bind(a: Alert) {
                val ctx = itemView.context
                title.text = AlertText.title(ctx, a)
                message.text = AlertText.message(ctx, a)
                time.text = Helpers.timeAgo(ctx, Helpers.parseIso(a.createdAt))
            }
        }
    }
}

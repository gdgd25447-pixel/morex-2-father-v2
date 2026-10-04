package com.morex.father.web

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

class WebHistoryActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progress: View
    private val adapter = WebAdapter()
    private var childId: String = ""

    companion object {
        private const val TAG = "WebHistory"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_history)

        childId = intent.getStringExtra("child_id") ?: ""

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }

        rv = findViewById(R.id.rvWeb)
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
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().getWebHistory(childId)
                }
                progress.visibility = View.GONE

                if (!resp.isSuccessful || resp.body() == null) {
                    emptyView.visibility = View.VISIBLE
                    return@launch
                }

                val gson = com.google.gson.Gson()
                val json = gson.toJson(resp.body())
                val arr = JSONArray(json)

                val items = mutableListOf<WebItem>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    items.add(WebItem(
                        url = o.optString("url", o.optString("domain", "")),
                        title = o.optString("title", "بدون عنوان"),
                        visits = o.optInt("visit_count", o.optInt("visits", 1)),
                        time = o.optString("visited_at", o.optString("created_at", ""))
                    ))
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

    data class WebItem(val url: String, val title: String, val visits: Int, val time: String)

    private class WebAdapter : RecyclerView.Adapter<WebAdapter.VH>() {
        private val items = mutableListOf<WebItem>()

        fun submit(list: List<WebItem>) {
            items.clear(); items.addAll(list); notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_web, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
        override fun getItemCount(): Int = items.size

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvTitle: TextView = v.findViewById(R.id.tvTitle)
            val tvUrl: TextView = v.findViewById(R.id.tvUrl)
            val tvVisits: TextView = v.findViewById(R.id.tvVisits)

            fun bind(item: WebItem) {
                tvTitle.text = item.title
                tvUrl.text = "🌐 ${item.url}"
                tvVisits.text = "🔁 ${item.visits} زيارة · ${item.time}"
            }
        }
    }
}

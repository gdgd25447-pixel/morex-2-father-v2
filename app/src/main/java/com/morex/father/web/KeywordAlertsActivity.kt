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

class KeywordAlertsActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progress: View
    private val adapter = KeywordAdapter()

    companion object {
        private const val TAG = "KeywordAlerts"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keyword_alerts)

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }

        rv = findViewById(R.id.rvKeywords)
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
                    .filter { it.type.equals("KEYWORD_HIT", ignoreCase = true) }
                    .map { a ->
                        KeywordItem(
                            keyword = a.payload?.get("keyword")?.toString().orEmpty(),
                            source = a.payload?.get("app_name")?.toString()
                                ?: a.payload?.get("source")?.toString()
                                ?: a.childName.orEmpty(),
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

    data class KeywordItem(val keyword: String, val source: String, val time: String)

    private class KeywordAdapter : RecyclerView.Adapter<KeywordAdapter.VH>() {
        private val items = mutableListOf<KeywordItem>()

        fun submit(list: List<KeywordItem>) {
            items.clear(); items.addAll(list); notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_keyword_alert, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
        override fun getItemCount(): Int = items.size

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvKeyword: TextView = v.findViewById(R.id.tvKeyword)
            val tvSource: TextView = v.findViewById(R.id.tvSource)
            val tvTime: TextView = v.findViewById(R.id.tvTime)

            fun bind(item: KeywordItem) {
                tvKeyword.text = "🔍 ${item.keyword}"
                tvSource.text = "من: ${item.source}"
                tvTime.text = item.time
            }
        }
    }
}

package com.morex.father.web

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.morex.father.R
import com.morex.father.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class WebFilterActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progress: View
    private var childId: String = ""
    private val items = mutableListOf<WebRuleItem>()
    private lateinit var adapter: WebAdapter

    companion object {
        private const val TAG = "WebFilter"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_filter)

        childId = intent.getStringExtra("child_id") ?: ""

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }
        findViewById<View>(R.id.fabAdd)?.setOnClickListener { showAddDialog() }

        rv = findViewById(R.id.rvRules)
        emptyView = findViewById(R.id.emptyView)
        progress = findViewById(R.id.progressBar)

        rv.layoutManager = LinearLayoutManager(this)
        adapter = WebAdapter(items) { rule -> deleteRule(rule) }
        rv.adapter = adapter

        loadRules()
    }

    private fun loadRules() {
        if (childId.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            return
        }

        progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().getWebRules(childId)
                }
                progress.visibility = View.GONE

                if (!resp.isSuccessful || resp.body() == null) {
                    emptyView.visibility = View.VISIBLE
                    return@launch
                }

                items.clear()
                (resp.body() ?: emptyList()).forEach { r ->
                    items.add(WebRuleItem(id = r.id.orEmpty(), domain = r.domain, type = r.type))
                }

                adapter.notifyDataSetChanged()
                emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE

            } catch (e: Exception) {
                progress.visibility = View.GONE
                Log.e(TAG, "Load failed", e)
                emptyView.visibility = View.VISIBLE
            }
        }
    }

    private fun showAddDialog() {
        val input = EditText(this).apply {
            hint = "مثال: facebook.com"
            setPadding(40, 40, 40, 40)
        }

        AlertDialog.Builder(this)
            .setTitle("إضافة نطاق محظور")
            .setView(input)
            .setPositiveButton("إضافة") { _, _ ->
                val domain = input.text.toString().trim()
                    .replace("https://", "")
                    .replace("http://", "")
                    .replace("www.", "")
                    .split("/")[0]
                if (domain.isNotEmpty()) addRule(domain)
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun addRule(domain: String) {
        lifecycleScope.launch {
            try {
                val body = mapOf(
                    "domain" to domain,
                    "list_type" to "black"
                )
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().addWebRule(childId, body)
                }
                if (resp.isSuccessful) {
                    Toast.makeText(this@WebFilterActivity, "✅ تم حظر $domain", Toast.LENGTH_SHORT).show()
                    loadRules()
                } else {
                    Toast.makeText(this@WebFilterActivity, "فشل (${resp.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Add failed", e)
                Toast.makeText(this@WebFilterActivity, "خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteRule(rule: WebRuleItem) {
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().deleteWebRule(rule.id)
                }
                if (resp.isSuccessful) {
                    items.remove(rule)
                    adapter.notifyDataSetChanged()
                    emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                    Toast.makeText(this@WebFilterActivity, "🗑️ تم الحذف", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Delete failed", e)
            }
        }
    }

    data class WebRuleItem(val id: String, val domain: String, val type: String)

    private class WebAdapter(
        val items: List<WebRuleItem>,
        val onDelete: (WebRuleItem) -> Unit
    ) : RecyclerView.Adapter<WebAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_web_rule, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) =
            holder.bind(items[position])

        override fun getItemCount(): Int = items.size

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvDomain: TextView = v.findViewById(R.id.tvDomain)
            val tvType: TextView = v.findViewById(R.id.tvType)
            val ivDelete: ImageView? = v.findViewById(R.id.ivDelete)

            fun bind(item: WebRuleItem) {
                tvDomain.text = "🌐 ${item.domain}"
                tvType.text = if (item.type == "black") itemView.context.getString(R.string.web_rule_blocked) else itemView.context.getString(R.string.web_rule_allowed)
                ivDelete?.setOnClickListener { onDelete(item) }
                itemView.setOnLongClickListener {
                    onDelete(item)
                    true
                }
            }
        }
    }
}

package com.morex.father.apps

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
import com.google.gson.Gson
import com.morex.father.R
import com.morex.father.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AppUsageActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progress: View
    private var childId: String = ""

    companion object {
        private const val TAG = "AppUsage"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_usage)

        childId = intent.getStringExtra("child_id") ?: ""

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }

        rv = findViewById(R.id.rvUsage)
        emptyView = findViewById(R.id.emptyView)
        progress = findViewById(R.id.progressBar)

        rv.layoutManager = LinearLayoutManager(this)

        loadUsage()
    }

    private fun loadUsage() {
        if (childId.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            return
        }

        progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().getAppUsage(childId)
                }
                progress.visibility = View.GONE

                if (!resp.isSuccessful || resp.body() == null) {
                    Log.e(TAG, "HTTP ${resp.code()}")
                    emptyView.visibility = View.VISIBLE
                    return@launch
                }

                val usage = resp.body()?.usage ?: emptyList()
                if (usage.isEmpty()) {
                    emptyView.visibility = View.VISIBLE
                    return@launch
                }

                val defaultName = getString(com.morex.father.R.string.app_default_name)
                val items = usage.map { u ->
                    UsageItem(
                        pkg = u.packageName,
                        name = u.appName ?: defaultName,
                        seconds = u.durationSec,
                        isBlocked = u.isBlocked ?: false
                    )
                }

                rv.adapter = UsageAdapter(items)
                emptyView.visibility = View.GONE

            } catch (e: Exception) {
                progress.visibility = View.GONE
                Log.e(TAG, "Load failed", e)
                emptyView.visibility = View.VISIBLE
            }
        }
    }

    data class UsageItem(
        val pkg: String,
        val name: String,
        val seconds: Int,
        val isBlocked: Boolean
    )

    private class UsageAdapter(val items: List<UsageItem>) :
        RecyclerView.Adapter<UsageAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app_usage, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) =
            holder.bind(items[position])

        override fun getItemCount(): Int = items.size

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvName: TextView = v.findViewById(R.id.tvName)
            val tvPackage: TextView = v.findViewById(R.id.tvPackage)
            val tvMinutes: TextView = v.findViewById(R.id.tvMinutes)

            fun bind(item: UsageItem) {
                tvName.text = if (item.isBlocked) "🚫 ${item.name}" else item.name
                tvPackage.text = item.pkg
                val h = item.seconds / 3600
                val m = (item.seconds % 3600) / 60
                tvMinutes.text = when {
                    h > 0 -> "${h}س ${m}د"
                    m > 0 -> "${m} دقيقة"
                    else -> "${item.seconds} ثانية"
                }
            }
        }
    }
}

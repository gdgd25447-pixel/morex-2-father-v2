package com.morex.father.apps

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
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

class AppBlockerActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progress: View
    private val adapter = BlockerAdapter()
    private var childId: String = ""

    companion object {
        private const val TAG = "AppBlocker"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_blocker)

        childId = intent.getStringExtra("child_id") ?: ""

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }

        rv = findViewById(R.id.rvApps)
        emptyView = findViewById(R.id.emptyView)
        progress = findViewById(R.id.progressBar)

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        loadApps()
    }

    private fun loadApps() {
        if (childId.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            return
        }

        progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().getAppRules(childId)
                }
                progress.visibility = View.GONE

                if (!resp.isSuccessful || resp.body() == null) {
                    emptyView.visibility = View.VISIBLE
                    return@launch
                }

                val defaultName = getString(com.morex.father.R.string.app_default_name)
                val items = (resp.body() ?: emptyList()).map { r ->
                    AppItem(
                        packageName = r.packageName,
                        appName = r.appName ?: defaultName,
                        isBlocked = r.isBlocked
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

    private fun toggleBlock(item: AppItem, blocked: Boolean) {
        lifecycleScope.launch {
            try {
                val body = mapOf("package_name" to item.packageName)
                val endpoint = if (blocked) "block" else "allow"
                val resp = withContext(Dispatchers.IO) {
                    if (blocked) ApiClient.get().blockApp(childId, body)
                    else ApiClient.get().allowApp(childId, body)
                }

                if (resp.isSuccessful) {
                    Toast.makeText(
                        this@AppBlockerActivity,
                        if (blocked) "🚫 تم حظر ${item.appName}" else "✅ تم السماح بـ ${item.appName}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this@AppBlockerActivity, "فشل (${resp.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Toggle failed", e)
                Toast.makeText(this@AppBlockerActivity, "خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    data class AppItem(
        val packageName: String,
        val appName: String,
        val isBlocked: Boolean
    )

    private inner class BlockerAdapter : RecyclerView.Adapter<BlockerAdapter.VH>() {
        private val items = mutableListOf<AppItem>()

        fun submit(list: List<AppItem>) {
            items.clear(); items.addAll(list); notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app_blocker, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
        override fun getItemCount(): Int = items.size

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvName: TextView = v.findViewById(R.id.tvName)
            val tvPackage: TextView = v.findViewById(R.id.tvPackage)
            val switch: Switch = v.findViewById(R.id.switchBlock)

            fun bind(item: AppItem) {
                tvName.text = item.appName
                tvPackage.text = item.packageName
                switch.setOnCheckedChangeListener(null)
                switch.isChecked = item.isBlocked
                switch.setOnCheckedChangeListener { _, isChecked ->
                    toggleBlock(item, isChecked)
                }
            }
        }
    }
}

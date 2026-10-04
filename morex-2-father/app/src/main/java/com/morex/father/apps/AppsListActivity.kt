package com.morex.father.apps

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.morex.father.R
import com.morex.father.network.ApiClient
import com.morex.father.network.models.AppRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppsListActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progress: View
    private var childId: String = ""

    companion object { private const val TAG = "AppsList" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps_list)

        childId = intent.getStringExtra("child_id") ?: ""
        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }

        rv = findViewById(R.id.rvApps)
        emptyView = findViewById(R.id.emptyView)
        progress = findViewById(R.id.progressBar)
        rv.layoutManager = LinearLayoutManager(this)

        loadApps()
    }

    private fun loadApps() {
        if (childId.isEmpty()) { emptyView.visibility = View.VISIBLE; return }
        progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) { ApiClient.get().getAppRules(childId) }
                progress.visibility = View.GONE

                if (!resp.isSuccessful || resp.body() == null) {
                    emptyView.visibility = View.VISIBLE
                    return@launch
                }

                val items = resp.body()!!
                rv.adapter = AppsAdapter(items) { rule, blocked ->
                    toggleBlock(rule.packageName ?: "", blocked)
                }
                emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                progress.visibility = View.GONE
                Log.e(TAG, "Load failed", e)
                emptyView.visibility = View.VISIBLE
            }
        }
    }

    private fun toggleBlock(pkg: String, blocked: Boolean) {
        if (pkg.isEmpty()) return
        lifecycleScope.launch {
            try {
                val body = mapOf("child_id" to childId, "package_name" to pkg)
                val resp = withContext(Dispatchers.IO) {
                    if (blocked) ApiClient.get().blockApp(childId, body)
                    else ApiClient.get().allowApp(childId, body)
                }
                if (resp.isSuccessful) {
                    Toast.makeText(this@AppsListActivity, "✅ تم", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@AppsListActivity, "فشل (${resp.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Toggle failed", e)
            }
        }
    }

    private class AppsAdapter(
        val items: List<AppRule>,
        val onToggle: (AppRule, Boolean) -> Unit
    ) : RecyclerView.Adapter<AppsAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app_rule, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
        override fun getItemCount(): Int = items.size

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvName: TextView = v.findViewById(R.id.tvName)
            val tvPackage: TextView = v.findViewById(R.id.tvPackage)
            val switch: Switch? = v.findViewById(R.id.switchBlock)

            fun bind(rule: AppRule) {
                tvName.text = rule.appName ?: rule.packageName ?: "تطبيق"
                tvPackage.text = rule.packageName ?: ""

                switch?.setOnCheckedChangeListener(null)
                switch?.isChecked = rule.isBlocked
                switch?.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
                    onToggle(rule, b)
                }
            }
        }
    }
}

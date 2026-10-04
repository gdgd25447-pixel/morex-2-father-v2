package com.morex.father.control

import android.os.Bundle
import android.util.Log
import android.view.View
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
import org.json.JSONObject

class ScheduleActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progress: View
    private var childId: String = ""
    private val items = mutableListOf<ScheduleAdapter.ScheduleItem>()
    private lateinit var adapter: ScheduleAdapter

    companion object {
        private const val TAG = "Schedule"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        childId = intent.getStringExtra("child_id") ?: ""

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }

        rv = findViewById(R.id.rvSchedules)
        emptyView = findViewById(R.id.emptyView)
        progress = findViewById(R.id.progressBar)

        rv.layoutManager = LinearLayoutManager(this)

        adapter = ScheduleAdapter(items) { item, isActive ->
            Log.i(TAG, "Schedule ${item.name} → active=$isActive")
            Toast.makeText(
                this,
                if (isActive) "✅ تم تفعيل ${item.name}" else "⏸️ تم تعطيل ${item.name}",
                Toast.LENGTH_SHORT
            ).show()
        }
        rv.adapter = adapter

        loadSchedules()
    }

    private fun loadSchedules() {
        if (childId.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            return
        }

        progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().getSchedules(childId)
                }
                progress.visibility = View.GONE

                if (!resp.isSuccessful || resp.body() == null) {
                    emptyView.visibility = View.VISIBLE
                    return@launch
                }

                items.clear()
                (resp.body() ?: emptyList()).forEach { s ->
                    items.add(ScheduleAdapter.ScheduleItem(
                        id = s.id.orEmpty(),
                        name = s.name ?: getString(com.morex.father.R.string.schedule_default_name),
                        startTime = s.startHm,
                        endTime = s.endHm,
                        action = s.action,
                        isActive = s.isActive
                    ))
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
}

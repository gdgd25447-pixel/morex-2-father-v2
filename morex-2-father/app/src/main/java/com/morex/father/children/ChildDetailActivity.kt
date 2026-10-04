package com.morex.father.children

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.morex.father.R
import com.morex.father.apps.AppUsageActivity
import com.morex.father.apps.AppsListActivity
import com.morex.father.commands.CommandsActivity
import com.morex.father.control.DeviceControlActivity
import com.morex.father.control.ScheduleActivity
import com.morex.father.geofence.GeofenceListActivity
import com.morex.father.location.LocationHistoryActivity
import com.morex.father.location.MapActivity
import com.morex.father.location.SpeedAlertsActivity
import com.morex.father.media.MediaGalleryActivity
import com.morex.father.monitor.CallsActivity
import com.morex.father.monitor.SmsActivity
import com.morex.father.monitor.SosActivity
import com.morex.father.network.ApiClient
import com.morex.father.settings.DevicesActivity
import com.morex.father.web.WebFilterActivity
import com.morex.father.web.WebHistoryActivity
import com.morex.father.web.KeywordAlertsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ChildDetailActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var tvName: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvBattery: TextView
    private lateinit var ivAvatar: ImageView
    private lateinit var progressBar: View
    private var childId: String = ""

    companion object {
        private const val TAG = "ChildDetail"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_detail)

        childId = intent.getStringExtra("child_id") ?: ""

        tvName = findViewById(R.id.tvName)
        tvStatus = findViewById(R.id.tvStatus)
        tvBattery = findViewById(R.id.tvBattery)
        ivAvatar = findViewById(R.id.ivAvatar)
        progressBar = findViewById(R.id.progressBar)
        rv = findViewById(R.id.rvActions)

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }

        rv.layoutManager = GridLayoutManager(this, 3)
        setupActions()
        loadChild()
    }

    private fun setupActions() {
        val actions = listOf(
            Action("الموقع", "🗺️") { go(MapActivity::class.java) },
            Action("التحكم", "⚙️") { go(DeviceControlActivity::class.java) },
            Action("المكالمات", "📞") { go(CallsActivity::class.java) },
            Action("الرسائل", "💬") { go(SmsActivity::class.java) },
            Action("سجل الموقع", "📍") { go(LocationHistoryActivity::class.java) },
            Action("السياج", "🛡️") { go(GeofenceListActivity::class.java) },
            Action("استخدام التطبيقات", "📊") { go(AppUsageActivity::class.java) },
            Action("حظر التطبيقات", "🚫") { go(AppsListActivity::class.java) },
            Action("سجل التصفح", "🌐") { go(WebHistoryActivity::class.java) },
            Action("فلترة الويب", "🔒") { go(WebFilterActivity::class.java) },
            Action("تنبيهات الكلمات", "🔍") { go(KeywordAlertsActivity::class.java) },
            Action("تنبيهات السرعة", "🚗") { go(SpeedAlertsActivity::class.java) },
            Action("الوسائط", "🖼️") { go(MediaGalleryActivity::class.java) },
            Action("الجدولة", "⏰") { go(ScheduleActivity::class.java) },
            Action("الأوامر", "📜") { go(CommandsActivity::class.java) },
            Action("الأجهزة", "📱") { go(DevicesActivity::class.java) },
            Action("SOS", "🆘") { go(SosActivity::class.java) }
        )

        rv.adapter = ActionsAdapter(actions)
    }

    private fun go(cls: Class<*>) {
        val i = Intent(this, cls)
        i.putExtra("child_id", childId)
        startActivity(i)
    }

    private fun loadChild() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) { ApiClient.get().getChildren() }
                progressBar.visibility = View.GONE
                if (!resp.isSuccessful || resp.body() == null) return@launch

                val c = (resp.body() ?: emptyList()).firstOrNull { it.id == childId } ?: return@launch
                tvName.text = c.name ?: getString(R.string.child_no_name)
                tvStatus.text = getString(if (c.isOnline) R.string.status_online else R.string.status_offline)
                tvBattery.text = getString(R.string.battery_percent, c.batteryLevel)
                val avatar = c.avatarUrl ?: c.avatar
                if (!avatar.isNullOrEmpty()) {
                    Glide.with(this@ChildDetailActivity).load(avatar).circleCrop().into(ivAvatar)
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Log.e(TAG, "Load failed", e)
            }
        }
    }

    data class Action(val title: String, val emoji: String, val onClick: () -> Unit)

    private class ActionsAdapter(val items: List<Action>) :
        RecyclerView.Adapter<ActionsAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_child_detail_action, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
        override fun getItemCount(): Int = items.size

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvEmoji: TextView = v.findViewById(R.id.tvEmoji)
            val tvTitle: TextView = v.findViewById(R.id.tvTitle)

            fun bind(item: Action) {
                tvTitle.text = item.title
                tvEmoji.text = item.emoji
                itemView.setOnClickListener { item.onClick() }
            }
        }
    }
}

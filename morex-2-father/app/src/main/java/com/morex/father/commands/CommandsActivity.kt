package com.morex.father.commands

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class CommandsActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var progress: View
    private var childId: String = ""
    private var deviceId: String? = null

    companion object {
        private const val TAG = "Commands"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_commands)

        childId = intent.getStringExtra("child_id") ?: ""
        deviceId = intent.getStringExtra("device_id")

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }

        rv = findViewById(R.id.rvCommands)
        progress = findViewById(R.id.progressBar)
        rv.layoutManager = LinearLayoutManager(this)

        val commands = listOf(
            Cmd("🔔", "رنين"),
            Cmd("📳", "اهتزاز"),
            Cmd("🔒", "قفل الشاشة"),
            Cmd("💬", "رسالة نصية"),
            Cmd("🔄", "مزامنة الآن"),
            Cmd("🧹", "مسح الكاش")
        )

        rv.adapter = CmdAdapter(commands) { title -> sendCommandByTitle(title) }

        loadDeviceIdIfNeeded()
    }

    private fun loadDeviceIdIfNeeded() {
        if (!deviceId.isNullOrEmpty()) {
            Log.i(TAG, "device_id from intent: $deviceId")
            return
        }
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) { ApiClient.get().getChildren() }
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body() ?: emptyList()
                    val me = list.firstOrNull { it.id == childId }
                    deviceId = me?.deviceId
                    Log.i(TAG, "device_id loaded: $deviceId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadDeviceId failed", e)
            }
        }
    }

    private fun sendCommandByTitle(title: String) {
        val type = when (title) {
            "رنين" -> "RING"
            "اهتزاز" -> "VIBRATE"
            "قفل الشاشة" -> "LOCK_NOW"
            "رسالة نصية" -> "NOTIFY_TEXT"
            "مزامنة الآن" -> "SYNC_NOW"
            "مسح الكاش" -> "CLEAR_CACHE"
            else -> return
        }
        sendCommand(type)
    }

    private fun sendCommand(type: String) {
        if (deviceId.isNullOrEmpty()) {
            Toast.makeText(this, "⚠️ الجهاز غير مربوط", Toast.LENGTH_SHORT).show()
            loadDeviceIdIfNeeded()
            return
        }

        progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val payload = if (type == "NOTIFY_TEXT") mapOf("text" to "رسالة من الوالد")
                              else emptyMap<String, Any>()

                val body = mapOf(
                    "device_id" to deviceId!!,
                    "type" to type,
                    "payload" to payload
                )

                val resp = withContext(Dispatchers.IO) { ApiClient.get().sendCommand(body) }
                progress.visibility = View.GONE

                if (resp.isSuccessful) {
                    Toast.makeText(this@CommandsActivity, "✅ تم الإرسال", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@CommandsActivity, "فشل (${resp.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                progress.visibility = View.GONE
                Toast.makeText(this@CommandsActivity, "خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Send failed", e)
            }
        }
    }

    data class Cmd(val emoji: String, val title: String)

    class CmdAdapter(val items: List<Cmd>, val onClick: (String) -> Unit) :
        RecyclerView.Adapter<CmdAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_command, parent, false)
            return VH(v, onClick)
        }

        override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
        override fun getItemCount(): Int = items.size

        class VH(v: View, val onClick: (String) -> Unit) : RecyclerView.ViewHolder(v) {
            val tvEmoji: TextView = v.findViewById(R.id.tvEmoji)
            val tvTitle: TextView = v.findViewById(R.id.tvTitle)

            fun bind(cmd: Cmd) {
                tvEmoji.text = cmd.emoji
                tvTitle.text = cmd.title
                itemView.setOnClickListener { onClick(cmd.title) }
            }
        }
    }
}

package com.morex.father.control

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.morex.father.R
import com.morex.father.apps.AppsListActivity
import com.morex.father.databinding.ActivityDeviceControlBinding
import com.morex.father.network.ApiClient
import com.morex.father.web.WebFilterActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeviceControlActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceControlBinding
    private var childId: String = ""
    private var deviceId: String? = null

    companion object {
        private const val TAG = "DeviceControl"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceControlBinding.inflate(layoutInflater)
        setContentView(binding.root)

        childId = intent.getStringExtra("child_id") ?: ""

        binding.ivBack.setOnClickListener { finish() }

        setupCards()

        binding.cardLock.root.setOnClickListener {
            startActivity(Intent(this, LockScreenActivity::class.java).putExtra("child_id", childId))
        }
        binding.cardSchedule.root.setOnClickListener {
            startActivity(Intent(this, ScheduleActivity::class.java).putExtra("child_id", childId))
        }
        binding.cardApps.root.setOnClickListener {
            startActivity(Intent(this, AppsListActivity::class.java).putExtra("child_id", childId))
        }
        binding.cardWeb.root.setOnClickListener {
            startActivity(Intent(this, WebFilterActivity::class.java).putExtra("child_id", childId))
        }
        binding.cardScreenshot.root.setOnClickListener {
            startActivity(Intent(this, com.morex.father.media.ScreenshotActivity::class.java).putExtra("child_id", childId))
        }
        binding.cardMic.root.setOnClickListener {
            startActivity(Intent(this, com.morex.father.media.AudioStreamActivity::class.java).putExtra("child_id", childId))
        }

        binding.btnRing.setOnClickListener { sendCommand("RING") }
        binding.btnVibrate.setOnClickListener { sendCommand("VIBRATE") }
        binding.btnMessage.setOnClickListener { sendCommand("NOTIFY_TEXT") }

        loadDeviceId()
    }

    private fun setupCards() {
        binding.cardLock.tvTitle.text = "قفل الشاشة"
        binding.cardLock.tvSubtitle.text = "أقفل الجهاز الآن"
        binding.cardLock.ivIcon.setImageResource(R.drawable.ic_lock_secure)

        binding.cardSchedule.tvTitle.text = "الجدولة"
        binding.cardSchedule.tvSubtitle.text = "أوقات النوم والدراسة"
        binding.cardSchedule.ivIcon.setImageResource(R.drawable.ic_calendar)

        binding.cardApps.tvTitle.text = "التطبيقات"
        binding.cardApps.tvSubtitle.text = "حظر وتنظيم"
        binding.cardApps.ivIcon.setImageResource(R.drawable.ic_nav_children)

        binding.cardWeb.tvTitle.text = "الويب"
        binding.cardWeb.tvSubtitle.text = "فلترة المحتوى"
        binding.cardWeb.ivIcon.setImageResource(R.drawable.ic_globe)

        binding.cardScreenshot.tvTitle.text = "لقطة شاشة"
        binding.cardScreenshot.tvSubtitle.text = "التقط الشاشة الآن"
        binding.cardScreenshot.ivIcon.setImageResource(R.drawable.ic_camera)

        binding.cardMic.tvTitle.text = "الاستماع"
        binding.cardMic.tvSubtitle.text = "بث الصوت المحيط"
        binding.cardMic.ivIcon.setImageResource(R.drawable.ic_person)
    }

    /**
     * ⭐ الإصلاح الحاسم:
     * نستخدم getChildren() (يحتوي device_id) وليس getChild() (لا يحتويه)
     */
    private fun loadDeviceId() {
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().getChildren()
                }
                if (resp.isSuccessful && resp.body() != null) {
                    val list = resp.body() ?: emptyList()
                    val me = list.firstOrNull { it.id == childId }
                    deviceId = me?.deviceId

                    Log.i(TAG, "childId=$childId | deviceId=$deviceId | " +
                            "found=${me != null} | list_size=${list.size}")

                    if (deviceId.isNullOrEmpty()) {
                        Log.w(TAG, "⚠️ Device not paired for this child")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadDeviceId failed", e)
            }
        }
    }

    private fun sendCommand(type: String) {
        if (deviceId.isNullOrEmpty()) {
            Toast.makeText(this, "⚠️ الجهاز غير مربوط", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "device_id empty — re-fetching...")
            // حاول إعادة الجلب
            loadDeviceId()
            return
        }

        Log.i(TAG, "→ Sending $type to device=$deviceId")
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val payload = if (type == "NOTIFY_TEXT") {
                    mapOf("text" to "رسالة من الوالد")
                } else emptyMap<String, Any>()

                val body = mapOf(
                    "device_id" to deviceId!!,
                    "type" to type,
                    "payload" to payload
                )

                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().sendCommand(body)
                }
                binding.progressBar.visibility = View.GONE

                if (resp.isSuccessful) {
                    Toast.makeText(this@DeviceControlActivity, "✅ تم الإرسال", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@DeviceControlActivity, "فشل (${resp.code()})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@DeviceControlActivity, "خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Send failed", e)
            }
        }
    }
}

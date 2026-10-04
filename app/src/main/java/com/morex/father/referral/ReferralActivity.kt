package com.morex.father.referral

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.morex.father.databinding.ActivityReferralBinding
import com.morex.father.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReferralActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReferralBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReferralBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }
        binding.btnCopy.setOnClickListener { copyCode() }
        binding.btnShare.setOnClickListener { shareCode() }

        loadReferralCode()
        loadStats()
    }

    private fun loadReferralCode() {
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) { ApiClient.get().getReferralCode() }
                if (resp.isSuccessful) {
                    binding.tvCode.text = resp.body()?.code ?: "—"
                }
            } catch (_: Exception) {}
        }
    }

    private fun loadStats() {
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) { ApiClient.get().getReferralStats() }
                if (resp.isSuccessful && resp.body() != null) {
                    val s = resp.body()?.stats ?: return@launch
                    // pending_count يشمل pending+completed+eligible؛ المجموع = pending + paid + cancelled
                    binding.tvCompleted.text = (s.completed + s.paid).toString()
                    binding.tvPending.text = (s.pending - s.completed).coerceAtLeast(0).toString()
                    binding.tvBalance.text = getString(com.morex.father.R.string.referral_earned_yer, s.totalEarned.toLong())
                    binding.tvTotal.text = (s.pending + s.paid + s.cancelled).toString()
                }
            } catch (_: Exception) {}
        }
    }

    private fun copyCode() {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("referral", binding.tvCode.text))
        Toast.makeText(this, "تم نسخ الكود", Toast.LENGTH_SHORT).show()
    }

    private fun shareCode() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "انضم إلى MOREX بكود: ${binding.tvCode.text}")
        }
        startActivity(Intent.createChooser(intent, "مشاركة"))
    }
}

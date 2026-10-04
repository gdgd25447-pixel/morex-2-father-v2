package com.morex.father.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.morex.father.databinding.ActivityLoginBinding
import com.morex.father.network.ApiClient
import com.morex.father.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val botUrl = "https://t.me/jdhxhxjdhdbdodchndjfjfhfjzzbot"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSendOtp.setOnClickListener { sendOtp() }
    }

    private fun sendOtp() {
        val phone = binding.etPhone.text?.toString()?.trim() ?: ""
        if (phone.length < 7) {
            binding.etPhone.error = "أدخل رقم صحيح"
            return
        }

        val fullPhone = "967$phone"
        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.get().sendOtp(
                        mapOf("phone" to fullPhone, "channel" to "telegram")
                    )
                }
                setLoading(false)

                if (response.isSuccessful) {
                    // ① انتقل لشاشة إدخال الكود
                    val intent = Intent(this@LoginActivity, OtpActivity::class.java)
                    intent.putExtra(Constants.EXTRA_PHONE, fullPhone)
                    startActivity(intent)

                    // ② افتح Telegram على البوت تلقائياً
                    try {
                        val tgIntent = Intent(Intent.ACTION_VIEW, Uri.parse(botUrl))
                        startActivity(tgIntent)
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@LoginActivity,
                            "افتح Telegram يدوياً وشارك رقمك",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "فشل الإرسال، حاول مرة أخرى",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(
                    this@LoginActivity,
                    "خطأ: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSendOtp.isEnabled = !loading
    }
}

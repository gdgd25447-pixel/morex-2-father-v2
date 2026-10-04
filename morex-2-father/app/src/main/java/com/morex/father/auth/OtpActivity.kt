package com.morex.father.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.morex.father.MainActivity
import com.morex.father.databinding.ActivityOtpBinding
import com.morex.father.network.ApiClient
import com.morex.father.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OtpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding
    private var phone: String = ""
    private var countDownTimer: CountDownTimer? = null

    private val fields: List<EditText> get() = listOf(
        binding.etOtp1, binding.etOtp2, binding.etOtp3,
        binding.etOtp4, binding.etOtp5, binding.etOtp6
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        phone = intent.getStringExtra(Constants.EXTRA_PHONE) ?: ""
        binding.tvPhoneNumber.text = "+$phone"

        setupOtpInputs()
        startCountdown()
        setupButtons()

        // ⭐ ابدأ التركيز من أول خانة
        binding.etOtp1.requestFocus()
    }

    private fun setupOtpInputs() {
        val list = fields

        list.forEachIndexed { index, et ->
            et.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun afterTextChanged(s: Editable?) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val text = s?.toString() ?: ""
                    // احتفظ برقم واحد فقط
                    if (text.length > 1) {
                        et.setText(text.last().toString())
                        et.setSelection(1)
                        return
                    }

                    if (text.isNotEmpty()) {
                        // ⭐ انقل التركيز للخانة التالية
                        if (index < list.size - 1) {
                            list[index + 1].requestFocus()
                        } else {
                            // ⭐ عند آخر خانة — أخفِ الكيبورد
                            et.clearFocus()
                            hideKeyboard()
                        }
                        // ⭐ تحقق من امتلاء الكود كاملاً
                        checkAllFilled()
                    }
                }
            })

            // ⭐ Backspace — انتقل للخانة السابقة
            et.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    et.text.isNullOrEmpty() &&
                    index > 0
                ) {
                    list[index - 1].requestFocus()
                    list[index - 1].setText("")
                    true
                } else false
            }
        }
    }

    private fun checkAllFilled() {
        val code = getEnteredCode()
        if (code.length == Constants.OTP_LENGTH) {
            // لا نرسل تلقائياً — نترك المستخدم يضغط "تحقق"
            // (لتجنب إرسال خاطئ أثناء الإدخال)
        }
    }

    private fun getEnteredCode(): String {
        return fields.joinToString("") { it.text?.toString() ?: "" }
            .replace(Regex("[^0-9]"), "")
    }

    private fun setupButtons() {
        binding.btnVerify.setOnClickListener {
            val code = getEnteredCode()
            if (code.length < Constants.OTP_LENGTH) {
                Toast.makeText(this, "أدخل الرمز كاملاً (6 أرقام)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            verifyOtp(code)
        }

        binding.btnChangePhone.setOnClickListener { finish() }

        binding.tvResend.setOnClickListener { resendOtp() }
    }

    private fun verifyOtp(code: String) {
        setLoading(true)
        hideKeyboard()

        lifecycleScope.launch {
            try {
                // ⭐ نرسل code كنص نظيف (لا أرقام ولا فراغات)
                val response = withContext(Dispatchers.IO) {
                    ApiClient.get().verifyOtp(mapOf(
                        "phone" to phone,
                        "code" to code.trim()
                    ))
                }
                setLoading(false)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && !body.token.isNullOrEmpty()) {
                        SessionManager.saveLogin(
                            this@OtpActivity,
                            body.token,
                            body.parent?.id ?: "",
                            body.parent?.name ?: "مستخدم",
                            phone,
                            body.parent?.email,
                            body.parent?.avatar
                        )
                        startActivity(Intent(this@OtpActivity, MainActivity::class.java))
                        finishAffinity()
                    } else {
                        Toast.makeText(this@OtpActivity, "فشل التحقق", Toast.LENGTH_LONG).show()
                        clearOtpFields()
                    }
                } else {
                    // قراءة رسالة الخطأ من السيرفر
                    val errorMsg = try {
                        response.errorBody()?.string() ?: ""
                    } catch (e: Exception) { "" }

                    val userMsg = when {
                        errorMsg.contains("OTP_INVALID") -> "الرمز غير صحيح"
                        errorMsg.contains("OTP_EXPIRED") -> "انتهت صلاحية الرمز، أعد الإرسال"
                        errorMsg.contains("OTP_NOT_FOUND") -> "لا يوجد رمز نشط، أعد الإرسال"
                        else -> "فشل التحقق، حاول مرة أخرى"
                    }

                    Toast.makeText(this@OtpActivity, userMsg, Toast.LENGTH_LONG).show()
                    clearOtpFields()
                }
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(
                    this@OtpActivity,
                    "خطأ في الشبكة: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun clearOtpFields() {
        fields.forEach { it.setText("") }
        binding.etOtp1.requestFocus()
    }

    private fun resendOtp() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.get().sendOtp(
                        mapOf("phone" to phone, "channel" to "telegram")
                    )
                }
                if (response.isSuccessful) {
                    Toast.makeText(this@OtpActivity, "تم إعادة الإرسال", Toast.LENGTH_SHORT).show()
                    clearOtpFields()
                    startCountdown()
                }
            } catch (e: Exception) {
                Toast.makeText(this@OtpActivity, "فشل: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCountdown() {
        countDownTimer?.cancel()
        binding.tvResend.isEnabled = false
        binding.tvResend.alpha = 0.5f

        countDownTimer = object : CountDownTimer(
            Constants.OTP_TIMEOUT_SECONDS * 1000L, 1000L
        ) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = millisUntilFinished / 1000
                binding.tvTimer.text = String.format("00:%02d", sec)
            }

            override fun onFinish() {
                binding.tvTimer.text = "00:00"
                binding.tvResend.isEnabled = true
                binding.tvResend.alpha = 1.0f
            }
        }.start()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnVerify.isEnabled = !loading
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}

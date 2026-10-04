package com.morex.father.auth

import android.content.Intent
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.lifecycle.lifecycleScope
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.morex.father.MainActivity
import com.morex.father.data.Prefs
import com.morex.father.databinding.ActivityPinLockBinding

class PinLockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinLockBinding
    private var enteredPin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNumberPad()
        updateDots()

        if (Prefs.isBiometricEnabled(this)) {
            binding.btnBiometric.visibility = View.VISIBLE
            binding.btnBiometric.setOnClickListener { showBiometric() }
            // Auto-prompt
            binding.root.postDelayed({ showBiometric() }, 500)
        } else {
            binding.btnBiometric.visibility = View.GONE
        }

        binding.btnForgot.setOnClickListener {
            Toast.makeText(this, "سجّل الدخول من جديد", Toast.LENGTH_SHORT).show()
            SessionManager.logout(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupNumberPad() {
        val numbers = mapOf(
            binding.btn0 to "0", binding.btn1 to "1", binding.btn2 to "2",
            binding.btn3 to "3", binding.btn4 to "4", binding.btn5 to "5",
            binding.btn6 to "6", binding.btn7 to "7", binding.btn8 to "8",
            binding.btn9 to "9"
        )
        numbers.forEach { (btn, num) ->
            btn.setOnClickListener { onNumber(num) }
        }
        binding.btnDelete.setOnClickListener { onDelete() }
    }

    private fun onNumber(num: String) {
        if (enteredPin.length >= 4) return
        enteredPin += num
        updateDots()
        if (enteredPin.length == 4) checkPin()
    }

    private fun onDelete() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            updateDots()
        }
    }

    private fun updateDots() {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4)
        dots.forEachIndexed { index, view ->
            view.setBackgroundResource(
                if (index < enteredPin.length) com.morex.father.R.drawable.bg_dot_filled
                else com.morex.father.R.drawable.bg_dot_empty
            )
        }
        binding.dot1.setBackgroundResource(
            if (enteredPin.isEmpty()) com.morex.father.R.drawable.bg_dot_focused
            else if (enteredPin.length >= 1) com.morex.father.R.drawable.bg_dot_filled
            else com.morex.father.R.drawable.bg_dot_empty
        )
    }

    private fun checkPin() {
        val pin = enteredPin
        // bcrypt (cost 12) ثقيل نسبياً: خارج الخيط الرئيسي
        lifecycleScope.launch {
            val ok = withContext(Dispatchers.Default) { Prefs.verifyPin(this@PinLockActivity, pin) }
            if (ok) {
                startActivity(Intent(this@PinLockActivity, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this@PinLockActivity, getString(com.morex.father.R.string.pin_wrong), Toast.LENGTH_SHORT).show()
                enteredPin = ""
                updateDots()
            }
        }
    }

    private fun showBiometric() {
        val manager = BiometricManager.from(this)
        val canAuth = manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) return

        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                startActivity(Intent(this@PinLockActivity, MainActivity::class.java))
                finish()
            }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("دخول آمن")
            .setSubtitle("استخدم بصمة الإصبع")
            .setNegativeButtonText("إلغاء")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .build()
        prompt.authenticate(info)
    }
}

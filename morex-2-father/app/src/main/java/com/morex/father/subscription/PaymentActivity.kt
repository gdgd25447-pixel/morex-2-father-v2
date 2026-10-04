package com.morex.father.subscription

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.morex.father.R
import com.morex.father.databinding.ActivityPaymentBinding
import com.morex.father.network.ApiClient
import com.morex.father.network.models.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * رفع سند الدفع. المحافظ من السيرفر. الحقول التي يشترطها السيرفر:
 * plan، wallet_key، reference_number، sender_name، وصورة `receipt` (حتى 5MB).
 */
class PaymentActivity : AppCompatActivity() {

    private companion object {
        const val MAX_RECEIPT_BYTES = 5L * 1024 * 1024
    }

    private lateinit var binding: ActivityPaymentBinding
    private var planId: String = ""
    private var wallets: List<Wallet> = emptyList()
    private var selectedWallet: Wallet? = null
    private var receiptUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            receiptUri = uri
            binding.ivReceipt.setImageURI(uri)
            binding.uploadHint.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        planId = intent.getStringExtra("plan_id") ?: ""
        val planYer = intent.getDoubleExtra("plan_price_yer", 0.0)
        val planUsd = intent.getDoubleExtra("plan_price_usd", 0.0)

        binding.ivBack.setOnClickListener { finish() }
        binding.tvPlanName.text = intent.getStringExtra("plan_name") ?: planId
        binding.tvAmount.text = getString(R.string.payment_price_usd, planUsd.toString().removeSuffix(".0"))
        binding.tvTotalYer.text = getString(R.string.payment_total_yer, planYer.toLong())

        binding.btnCopyWallet.setOnClickListener {
            val number = selectedWallet?.number ?: return@setOnClickListener
            val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("wallet", number))
            Toast.makeText(this, R.string.payment_copied, Toast.LENGTH_SHORT).show()
        }
        binding.uploadArea.setOnClickListener { pickImage.launch("image/*") }
        binding.btnSubmit.setOnClickListener { submitReceipt() }

        loadWallets()
    }

    private fun loadWallets() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) { ApiClient.get().getWallets() }
                binding.progressBar.visibility = View.GONE
                wallets = resp.body() ?: emptyList()
                renderWallets()
            } catch (_: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@PaymentActivity, R.string.common_network_error, Toast.LENGTH_SHORT).show()
                renderWallets()
            }
        }
    }

    private fun renderWallets() {
        binding.radioWallets.removeAllViews()
        binding.tvWalletsEmpty.visibility = if (wallets.isEmpty()) View.VISIBLE else View.GONE
        wallets.forEachIndexed { index, w ->
            val rb = RadioButton(this).apply {
                id = View.generateViewId()
                text = w.name ?: w.key
                tag = w.key
            }
            binding.radioWallets.addView(rb)
            if (index == 0) rb.isChecked = true
        }
        binding.radioWallets.setOnCheckedChangeListener { group: RadioGroup, checkedId: Int ->
            val key = group.findViewById<RadioButton>(checkedId)?.tag as? String
            select(wallets.firstOrNull { it.key == key })
        }
        select(wallets.firstOrNull())
    }

    private fun select(w: Wallet?) {
        selectedWallet = w
        binding.tvWalletNumber.text = w?.number ?: "—"
        binding.tvWalletOwner.text = w?.accountName.orEmpty()
    }

    private fun submitReceipt() {
        val wallet = selectedWallet ?: run {
            Toast.makeText(this, R.string.payment_pick_wallet, Toast.LENGTH_SHORT).show(); return
        }
        val reference = binding.etReference.text?.toString()?.trim().orEmpty()
        val sender = binding.etSenderName.text?.toString()?.trim().orEmpty()
        val senderPhone = binding.etSenderPhone.text?.toString()?.trim().orEmpty()
        if (reference.isEmpty() || sender.isEmpty()) {
            Toast.makeText(this, R.string.payment_fill_fields, Toast.LENGTH_SHORT).show(); return
        }
        val uri = receiptUri ?: run {
            Toast.makeText(this, R.string.payment_pick_receipt, Toast.LENGTH_SHORT).show(); return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmit.isEnabled = false

        lifecycleScope.launch {
            var temp: File? = null
            try {
                // content:// لا يملك مساراً حقيقياً: ننسخ الصورة إلى الكاش ثم نرفعها
                temp = withContext(Dispatchers.IO) { copyToCache(uri) }
                if (temp == null) {
                    fail(getString(R.string.payment_receipt_too_large)); return@launch
                }
                val mime = contentResolver.getType(uri) ?: "image/jpeg"
                val part = MultipartBody.Part.createFormData(
                    "receipt", temp.name, temp.asRequestBody(mime.toMediaTypeOrNull())
                )
                val text = "text/plain".toMediaTypeOrNull()
                fun body(v: String): RequestBody = v.toRequestBody(text)

                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().uploadReceipt(
                        receipt = part,
                        planId = body(planId),
                        walletKey = body(wallet.key),
                        referenceNumber = body(reference),
                        senderName = body(sender),
                        senderPhone = if (senderPhone.isNotEmpty()) body(senderPhone) else null
                    )
                }
                binding.progressBar.visibility = View.GONE
                binding.btnSubmit.isEnabled = true

                if (resp.isSuccessful) {
                    Toast.makeText(this@PaymentActivity, R.string.payment_sent, Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@PaymentActivity, SubscriptionActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@PaymentActivity, R.string.payment_failed, Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                fail(getString(R.string.common_network_error))
            } finally {
                temp?.delete()
            }
        }
    }

    private fun fail(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.btnSubmit.isEnabled = true
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /** ينسخ الصورة إلى الكاش بحدّ أقصى 5MB، ويعيد null إن تجاوزت الحد. */
    private fun copyToCache(uri: Uri): File? {
        val out = File(cacheDir, "receipt_${System.currentTimeMillis()}.jpg")
        var total = 0L
        contentResolver.openInputStream(uri)?.use { input ->
            out.outputStream().use { os ->
                val buf = ByteArray(16 * 1024)
                while (true) {
                    val n = input.read(buf)
                    if (n < 0) break
                    total += n
                    if (total > MAX_RECEIPT_BYTES) { out.delete(); return null }
                    os.write(buf, 0, n)
                }
            }
        } ?: return null
        return out
    }
}

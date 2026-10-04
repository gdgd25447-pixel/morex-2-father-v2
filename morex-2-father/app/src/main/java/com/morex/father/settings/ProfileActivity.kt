package com.morex.father.settings

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.morex.father.R
import com.morex.father.data.Prefs
import com.morex.father.databinding.ActivityProfileBinding
import com.morex.father.network.ApiClient
import com.morex.father.network.models.Parent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** الملف الشخصي من GET /parent/profile، والاسم يُعدَّل عبر PUT /parent/profile. */
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private var current: Parent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }
        // عرض مبدئي من الجلسة المحلية إلى أن يصل رد السيرفر
        show(Prefs.getParentName(this), Prefs.getParentPhone(this), Prefs.getParentEmail(this))
        binding.btnSave.setOnClickListener { editName() }
        load()
    }

    private fun show(name: String?, phone: String?, email: String?) {
        binding.tvName.text = name?.takeIf { it.isNotBlank() } ?: "—"
        binding.tvPhone.text = phone?.takeIf { it.isNotBlank() } ?: "—"
        binding.tvEmail.text = email?.takeIf { it.isNotBlank() } ?: "—"
    }

    private fun load() {
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) { ApiClient.get().getProfile() }
                val p = resp.body()?.parent
                if (resp.isSuccessful && p != null) {
                    current = p
                    show(p.name, p.phone, p.email)
                } else {
                    Toast.makeText(this@ProfileActivity, R.string.common_loading_failed, Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                Toast.makeText(this@ProfileActivity, R.string.common_network_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun editName() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            setText(current?.name.orEmpty())
            setSelection(text.length)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.profile_edit_name)
            .setView(input)
            .setPositiveButton(R.string.common_save) { _, _ ->
                val name = input.text.toString().trim()
                if (name.length >= 2) save(name)
            }
            .setNegativeButton(R.string.common_cancel, null)
            .show()
    }

    private fun save(name: String) {
        binding.btnSave.isEnabled = false
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) { ApiClient.get().updateProfile(mapOf("full_name" to name)) }
                binding.btnSave.isEnabled = true
                val p = resp.body()?.parent
                if (resp.isSuccessful && p != null) {
                    current = p
                    show(p.name, p.phone, p.email)
                    Prefs.saveParent(this@ProfileActivity, p.id, p.name.orEmpty(), p.phone, p.email, p.avatar)
                    Toast.makeText(this@ProfileActivity, R.string.common_saved, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProfileActivity, R.string.common_loading_failed, Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                binding.btnSave.isEnabled = true
                Toast.makeText(this@ProfileActivity, R.string.common_network_error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

package com.morex.father.children

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.morex.father.R
import com.morex.father.databinding.ActivityAddChildBinding
import com.morex.father.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Calendar

class AddChildActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AddChild"
    }

    private lateinit var binding: ActivityAddChildBinding
    private var selectedGender = "male"
    private var birthDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddChildBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "🔵 onCreate")

        binding.ivBack.setOnClickListener { finish() }

        binding.cardMale.setOnClickListener {
            Log.d(TAG, "👆 male")
            selectGender("male")
        }
        binding.cardFemale.setOnClickListener {
            Log.d(TAG, "👆 female")
            selectGender("female")
        }

        binding.etBirthDate.setOnClickListener {
            Log.d(TAG, "👆 date picker")
            showDatePicker()
        }

        binding.btnNext.setOnClickListener {
            Log.d(TAG, "👆 NEXT")
            submit()
        }

        binding.btnCancel.setOnClickListener { finish() }

        selectGender("male")
    }

    private fun selectGender(gender: String) {
        selectedGender = gender
        if (gender == "male") {
            binding.cardMale.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.light_blue)
            )
            binding.cardMale.strokeColor = ContextCompat.getColor(this, R.color.morex_blue)
            binding.cardMale.strokeWidth = 6

            binding.cardFemale.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.surface)
            )
            binding.cardFemale.strokeColor = ContextCompat.getColor(this, R.color.border_light)
            binding.cardFemale.strokeWidth = 3
        } else {
            binding.cardFemale.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.light_blue)
            )
            binding.cardFemale.strokeColor = ContextCompat.getColor(this, R.color.morex_blue)
            binding.cardFemale.strokeWidth = 6

            binding.cardMale.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.surface)
            )
            binding.cardMale.strokeColor = ContextCompat.getColor(this, R.color.border_light)
            binding.cardMale.strokeWidth = 3
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, y, m, d ->
                val mm = (m + 1).toString().padStart(2, '0')
                val dd = d.toString().padStart(2, '0')
                birthDate = "$y-$mm-$dd"
                binding.etBirthDate.setText("$y/$mm/$dd")
                Log.d(TAG, "📅 date: $birthDate")
            },
            cal.get(Calendar.YEAR) - 10,
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun submit() {
        val name = binding.etName.text?.toString()?.trim() ?: ""
        val phone = binding.etPhone.text?.toString()?.trim() ?: ""
        val grade = binding.etGrade.text?.toString()?.trim() ?: ""

        Log.d(TAG, "═══════════════════════════════")
        Log.d(TAG, "📤 name=$name birth=$birthDate gender=$selectedGender phone=$phone grade=$grade")
        Log.d(TAG, "═══════════════════════════════")

        if (name.length < 2) {
            binding.etName.error = "أدخل الاسم (حرفان على الأقل)"
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnNext.isEnabled = false

        lifecycleScope.launch {
            try {
                val payload = mapOf(
                    "name" to name,
                    "birth_date" to birthDate,
                    "gender" to selectedGender,
                    "phone" to phone,
                    "grade" to grade
                )

                Log.d(TAG, "🌐 POST parent/children payload=$payload")

                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().addChild(payload)
                }

                binding.progressBar.visibility = View.GONE
                binding.btnNext.isEnabled = true

                Log.d(TAG, "📥 code=${resp.code()} success=${resp.isSuccessful}")

                if (resp.isSuccessful && resp.body() != null) {
                    val child = resp.body()!!

                    // ⭐ استخراج id و name من أي مصدر
                    val id = extractChildId(child)
                    val childName = extractChildName(child)

                    Log.d(TAG, "✅ extracted id=$id name=$childName")

                    if (id.isEmpty()) {
                        Log.e(TAG, "❌ child id is EMPTY!")
                        Toast.makeText(
                            this@AddChildActivity,
                            "خطأ: لم يُعِد السيرفر معرّف الابن",
                            Toast.LENGTH_LONG
                        ).show()
                        return@launch
                    }

                    val intent = Intent(this@AddChildActivity, PairingCodeActivity::class.java)
                    intent.putExtra("child_id", id)
                    intent.putExtra("child_name", childName)
                    startActivity(intent)
                    finish()
                } else {
                    val errorBody = try {
                        resp.errorBody()?.string() ?: "no error"
                    } catch (e: Exception) { "read fail" }

                    Log.e(TAG, "❌ HTTP ${resp.code()}: $errorBody")

                    Toast.makeText(
                        this@AddChildActivity,
                        "فشل الحفظ (${resp.code()})",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnNext.isEnabled = true

                Log.e(TAG, "❌ EXCEPTION: ${e.message}", e)

                Toast.makeText(
                    this@AddChildActivity,
                    "خطأ: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * ⭐ استخراج child.id من أي مكان في الاستجابة
     * يحاول: child.id → child.child.id → JSON parsing
     */
    private fun extractChildId(child: com.morex.father.network.models.Child): String {
        // ① محاولة مباشرة
        try {
            val id = child.id
            if (!id.isNullOrEmpty()) return id
        } catch (e: Exception) { }

        // ② محاولة من child.child
        try {
            val nested = child.child
            if (nested?.id?.isNotEmpty() == true) return nested.id!!
        } catch (e: Exception) { }

        return ""
    }

    private fun extractChildName(child: com.morex.father.network.models.Child): String {
        try {
            val n = child.name
            if (!n.isNullOrEmpty()) return n
        } catch (e: Exception) { }

        try {
            val nested = child.child
            if (nested?.name?.isNotEmpty() == true) return nested.name!!
        } catch (e: Exception) { }

        return "الابن"
    }
}

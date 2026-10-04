package com.morex.father.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import com.morex.father.R
import com.morex.father.data.Prefs
import com.morex.father.databinding.ActivityLanguageBinding

class LanguageActivity : AppCompatActivity() {

    private var binding: ActivityLanguageBinding? = null
    private var selectedLang = "ar"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityLanguageBinding.inflate(layoutInflater)
            setContentView(binding!!.root)
        } catch (t: Throwable) {
            finish()
            return
        }

        selectedLang = try { Prefs.getLanguage(this) } catch (t: Throwable) { "ar" }

        try { updateSelection() } catch (t: Throwable) { }

        binding?.cardArabic?.setOnClickListener {
            selectedLang = "ar"
            try { updateSelection() } catch (t: Throwable) { }
        }

        binding?.cardEnglish?.setOnClickListener {
            selectedLang = "en"
            try { updateSelection() } catch (t: Throwable) { }
        }

        binding?.btnContinue?.setOnClickListener {
            try { proceed() } catch (t: Throwable) {
                startActivity(Intent(this, OnboardingActivity::class.java))
                finish()
            }
        }
    }

    private fun updateSelection() {
        val b = binding ?: return

        if (selectedLang == "ar") {
            b.cardArabic.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.surface)
            )
            b.cardArabic.strokeColor = ContextCompat.getColor(this, R.color.morex_gold)
            b.cardArabic.strokeWidth = 6
            b.ivCheckArabic.visibility = View.VISIBLE

            b.cardEnglish.setCardBackgroundColor(0x00000000)
            b.cardEnglish.strokeColor = 0x40FFFFFF
            b.cardEnglish.strokeWidth = 2
            b.ivCheckEnglish.visibility = View.GONE
        } else {
            b.cardArabic.setCardBackgroundColor(0x00000000)
            b.cardArabic.strokeColor = 0x40FFFFFF
            b.cardArabic.strokeWidth = 2
            b.ivCheckArabic.visibility = View.GONE

            b.cardEnglish.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.surface)
            )
            b.cardEnglish.strokeColor = ContextCompat.getColor(this, R.color.morex_gold)
            b.cardEnglish.strokeWidth = 6
            b.ivCheckEnglish.visibility = View.VISIBLE
        }
    }

    private fun proceed() {
        try {
            Prefs.setLanguage(this, selectedLang)
            val locales = LocaleListCompat.forLanguageTags(selectedLang)
            AppCompatDelegate.setApplicationLocales(locales)
        } catch (t: Throwable) { }

        startActivity(Intent(this, OnboardingActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}

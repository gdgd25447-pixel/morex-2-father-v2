package com.morex.father.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.morex.father.auth.LoginActivity
import com.morex.father.auth.SessionManager
import com.morex.father.data.Prefs
import com.morex.father.databinding.FragmentSettingsBinding
import com.morex.father.referral.ReferralActivity
import com.morex.father.reports.ReportsActivity
import com.morex.father.subscription.SubscriptionActivity

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)

        binding.tvName.text = Prefs.getParentName(requireContext()) ?: "—"
        binding.tvPhone.text = Prefs.getParentPhone(requireContext()) ?: "—"

        // ═══ الحساب ═══
        binding.rowProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }
        binding.rowSubscription.setOnClickListener {
            startActivity(Intent(requireContext(), SubscriptionActivity::class.java))
        }
        binding.rowReferral.setOnClickListener {
            startActivity(Intent(requireContext(), ReferralActivity::class.java))
        }

        // ═══ الأدوات ═══ (جديد)
        binding.rowReports.setOnClickListener {
            startActivity(Intent(requireContext(), ReportsActivity::class.java))
        }
        binding.rowDevices.setOnClickListener {
            startActivity(Intent(requireContext(), DevicesActivity::class.java))
        }
        binding.rowLegal.setOnClickListener {
            startActivity(Intent(requireContext(), LegalActivity::class.java))
        }

        // ═══ الدعم ═══
        binding.rowFaq.setOnClickListener {
            startActivity(Intent(requireContext(), FaqActivity::class.java))
        }
        binding.rowChat.setOnClickListener {
            startActivity(Intent(requireContext(), ChatActivity::class.java))
        }

        // ═══ تسجيل الخروج ═══
        binding.btnLogout.setOnClickListener {
            SessionManager.logout(requireContext())
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finishAffinity()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.morex.father.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.morex.father.R
import com.morex.father.databinding.ItemOnboardingBinding

data class OnboardingItem(
    val title: String,
    val description: String,
    val imageRes: Int
)

class OnboardingAdapter : RecyclerView.Adapter<OnboardingAdapter.VH>() {

    private val items = listOf(
        OnboardingItem(
            "حماية شاملة",
            "راقب أبنائك وحمِهم من المخاطر في كل مكان",
            R.drawable.ic_shield_check
        ),
        OnboardingItem(
            "تتبع مباشر",
            "اعرف موقع أبنائك في الوقت الفعلي مع سجل كامل للتنقلات",
            R.drawable.ic_location_pin
        ),
        OnboardingItem(
            "ميزات إسلامية",
            "متابعة أوقات الصلاة وحفظ القرآن وقفل الجهاز تلقائياً",
            R.drawable.ic_lock_secure
        )
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemOnboardingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class VH(private val binding: ItemOnboardingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OnboardingItem) {
            binding.ivImage.setImageResource(item.imageRes)
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.description
        }
    }
}

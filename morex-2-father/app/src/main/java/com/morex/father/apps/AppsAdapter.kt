package com.morex.father.apps

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.morex.father.databinding.ItemAppRuleBinding
import com.morex.father.network.models.AppRule

class AppsAdapter(
    private val items: List<AppRule>,
    private val onToggle: (AppRule, Boolean) -> Unit
) : RecyclerView.Adapter<AppsAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAppRuleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(items[pos])
    override fun getItemCount() = items.size

    inner class VH(private val b: ItemAppRuleBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: AppRule) {
            b.tvAppName.text = item.appName ?: item.packageName
            b.tvUsage.text = "وقت الاستخدام اليوم: ${item.usageTodayMinutes} دقيقة"
            b.tvStatus.text = if (item.isBlocked) "محظور" else "مسموح"
            b.switchBlocked.setOnCheckedChangeListener(null)
            b.switchBlocked.isChecked = !item.isBlocked
            b.switchBlocked.setOnCheckedChangeListener { _, isChecked ->
                onToggle(item, !isChecked)
            }
        }
    }
}

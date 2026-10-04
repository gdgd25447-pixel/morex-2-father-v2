package com.morex.father.control

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.morex.father.R

class ScheduleAdapter(
    private val items: List<ScheduleItem>,
    private val onToggle: (ScheduleItem, Boolean) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.VH>() {

    data class ScheduleItem(
        val id: String,
        val name: String,
        val startTime: String,
        val endTime: String,
        val action: String,
        var isActive: Boolean = true
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvTime: TextView = v.findViewById(R.id.tvTime)
        val tvAction: TextView = v.findViewById(R.id.tvAction)
        val switchActive: Switch = v.findViewById(R.id.switchActive)

        fun bind(item: ScheduleItem) {
            tvName.text = item.name
            tvTime.text = "${item.startTime} - ${item.endTime}"
            tvAction.text = if (item.action == "lock") "🔒 قفل" else "🔓 فتح"

            // ⭐ تجنب استدعاء onToggle أثناء إعادة الربط
            switchActive.setOnCheckedChangeListener(null)
            switchActive.isChecked = item.isActive
            switchActive.setOnCheckedChangeListener { _, isChecked ->
                item.isActive = isChecked
                onToggle(item, isChecked)
            }
        }
    }
}

package com.morex.father.monitor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.morex.father.databinding.ItemAlertBinding
import com.morex.father.network.models.Alert
import com.morex.father.utils.AlertText
import com.morex.father.utils.Helpers

class AlertsAdapter(
    private val items: List<Alert>,
    private val onClick: (Alert) -> Unit
) : RecyclerView.Adapter<AlertsAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(items[pos])
    override fun getItemCount() = items.size

    inner class VH(private val b: ItemAlertBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(alert: Alert) {
            val ctx = b.root.context
            b.tvTitle.text = AlertText.title(ctx, alert)
            b.tvMessage.text = AlertText.message(ctx, alert)
            b.tvTime.text = Helpers.timeAgo(ctx, Helpers.parseIso(alert.createdAt))
            b.root.alpha = if (alert.isRead) 0.6f else 1f
            b.root.setOnClickListener { onClick(alert) }
        }
    }
}

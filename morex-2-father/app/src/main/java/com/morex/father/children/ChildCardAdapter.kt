package com.morex.father.children

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.morex.father.R
import com.morex.father.databinding.ItemChildCardBinding
import com.morex.father.network.models.Child

class ChildCardAdapter(
    private val items: List<Child>,
    private val onClick: (Child) -> Unit
) : RecyclerView.Adapter<ChildCardAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemChildCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(items[pos])

    override fun getItemCount() = items.size

    inner class VH(private val b: ItemChildCardBinding) : RecyclerView.ViewHolder(b.root) {

        fun bind(child: Child) {
            val ctx = b.root.context

            // ═══ الاسم ═══
            val name = child.name?.takeIf { it.isNotBlank() } ?: "بدون اسم"
            b.tvName.text = name

            // ═══ حالة الاتصال ═══
            // 🟢 أخضر = متصل | ⚪ رمادي = غير متصل
            val ringColor = if (child.isOnline) {
                ContextCompat.getColor(ctx, R.color.status_safe)
            } else {
                ContextCompat.getColor(ctx, R.color.disabled)
            }
            b.ivStatusRing.setBackgroundColor(ringColor)

            // ═══ البطارية ═══
            b.tvBattery.text = if (child.isOnline) {
                "🔋 ${child.batteryLevel}%"
            } else if (child.isPaired) {
                "غير متصل"
            } else {
                "غير مربوط"
            }

            // ═══ الأيقونة ═══
            if (!child.avatar.isNullOrBlank()) {
                Glide.with(b.root)
                    .load(child.avatar)
                    .placeholder(R.drawable.ic_nav_children)
                    .circleCrop()
                    .into(b.ivAvatar)
            } else {
                b.ivAvatar.setImageResource(R.drawable.ic_nav_children)
            }

            // ═══ الضغط ═══
            b.root.setOnClickListener { onClick(child) }
        }
    }
}

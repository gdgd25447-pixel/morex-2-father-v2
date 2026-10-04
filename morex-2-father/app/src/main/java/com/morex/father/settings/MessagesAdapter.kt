package com.morex.father.settings

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.morex.father.R
import com.morex.father.databinding.ItemMessageBinding
import com.morex.father.network.models.SupportMessage

class MessagesAdapter(private val items: List<SupportMessage>) :
    RecyclerView.Adapter<MessagesAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(items[pos])
    override fun getItemCount() = items.size

    class VH(private val b: ItemMessageBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(msg: SupportMessage) {
            b.tvMessage.text = msg.message ?: ""
            val isMine = msg.senderType == "parent"
            if (isMine) {
                b.container.gravity = Gravity.START
                b.bubble.setCardBackgroundColor(
                    ContextCompat.getColor(b.root.context, R.color.light_blue)
                )
            } else {
                b.container.gravity = Gravity.END
                b.bubble.setCardBackgroundColor(
                    ContextCompat.getColor(b.root.context, R.color.surface)
                )
            }
        }
    }
    }

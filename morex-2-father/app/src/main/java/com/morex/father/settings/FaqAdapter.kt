package com.morex.father.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.morex.father.databinding.ItemFaqBinding

class FaqAdapter(private val items: List<Pair<String, String>>) :
    RecyclerView.Adapter<FaqAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemFaqBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(items[pos])
    override fun getItemCount() = items.size

    class VH(private val b: ItemFaqBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: Pair<String, String>) {
            b.tvQuestion.text = item.first
            b.tvAnswer.text = item.second
            b.tvAnswer.visibility = View.GONE
            b.ivExpand.rotation = 0f

            b.root.setOnClickListener {
                val show = b.tvAnswer.visibility != View.VISIBLE
                b.tvAnswer.visibility = if (show) View.VISIBLE else View.GONE
                b.ivExpand.animate().rotation(if (show) 180f else 0f).start()
            }
        }
    }
    }

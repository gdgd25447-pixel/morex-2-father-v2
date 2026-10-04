package com.morex.father.media

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.morex.father.databinding.ItemMediaBinding
import com.morex.father.network.models.MediaCapture

class MediaAdapter(private val items: List<MediaCapture>) :
    RecyclerView.Adapter<MediaAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(items[pos])
    override fun getItemCount() = items.size

    class VH(private val b: ItemMediaBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: MediaCapture) {
            Glide.with(b.root).load(item.thumbnail ?: item.url)
                .centerCrop().into(b.ivMedia)
            b.tvTime.text = item.createdAt?.takeLast(8)?.take(5) ?: ""
        }
    }
    }

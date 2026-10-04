package com.morex.father.media

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.morex.father.R
import com.morex.father.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class MediaGalleryActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progress: View
    private var childId: String = ""

    companion object {
        private const val TAG = "MediaGallery"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_gallery)

        childId = intent.getStringExtra("child_id") ?: ""

        findViewById<View>(R.id.ivBack)?.setOnClickListener { finish() }

        rv = findViewById(R.id.rvMedia)
        emptyView = findViewById(R.id.emptyView)
        progress = findViewById(R.id.progressBar)

        rv.layoutManager = GridLayoutManager(this, 3)

        loadMedia()
    }

    private fun loadMedia() {
        if (childId.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            return
        }

        progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().getMediaCaptures(childId)
                }
                progress.visibility = View.GONE

                if (!resp.isSuccessful || resp.body() == null) {
                    emptyView.visibility = View.VISIBLE
                    return@launch
                }

                val items = (resp.body() ?: emptyList()).map { m ->
                    MediaItem(
                        id = m.id,
                        type = m.type,
                        url = m.url.orEmpty(),
                        createdAt = m.createdAt.orEmpty()
                    )
                }

                if (items.isEmpty()) {
                    emptyView.visibility = View.VISIBLE
                } else {
                    rv.adapter = MediaAdapter(items)
                    emptyView.visibility = View.GONE
                }

            } catch (e: Exception) {
                progress.visibility = View.GONE
                Log.e(TAG, "Load failed", e)
                emptyView.visibility = View.VISIBLE
            }
        }
    }

    data class MediaItem(
        val id: String,
        val type: String,
        val url: String,
        val createdAt: String
    )

    private class MediaAdapter(val items: List<MediaItem>) :
        RecyclerView.Adapter<MediaAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_media, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) =
            holder.bind(items[position])

        override fun getItemCount(): Int = items.size

        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val ivMedia: ImageView = v.findViewById(R.id.ivMedia)
            val tvType: TextView = v.findViewById(R.id.tvType)

            fun bind(item: MediaItem) {
                if (item.url.isNotEmpty()) {
                    Glide.with(ivMedia)
                        .load(item.url)
                        .centerCrop()
                        .into(ivMedia)
                }
                tvType.text = when (item.type) {
                    "photo_front", "photo_back", "screenshot" -> "📷"
                    "video" -> "🎥"
                    "audio" -> "🎙️"
                    else -> "📎"
                }
            }
        }
    }
}

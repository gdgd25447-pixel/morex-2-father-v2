package com.morex.father.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.morex.father.databinding.ActivityChatBinding
import com.morex.father.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private var conversationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }
        binding.rvMessages.layoutManager = LinearLayoutManager(this)

        binding.btnSend.setOnClickListener { sendMessage() }

        createOrLoadConversation()
    }

    private fun createOrLoadConversation() {
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().createConversation(emptyMap())
                }
                if (resp.isSuccessful && resp.body() != null) {
                    conversationId = resp.body()?.conversation?.id
                    loadMessages()
                }
            } catch (_: Exception) {}
        }
    }

    private fun loadMessages() {
        val cid = conversationId ?: return
        lifecycleScope.launch {
            try {
                val resp = withContext(Dispatchers.IO) {
                    ApiClient.get().getSupportMessages(cid)
                }
                if (resp.isSuccessful) {
                    binding.rvMessages.adapter = MessagesAdapter(resp.body() ?: emptyList())
                }
            } catch (_: Exception) {}
        }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text?.toString()?.trim() ?: return
        if (text.isEmpty()) return

        val cid = conversationId ?: return
        binding.etMessage.text?.clear()

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    ApiClient.get().sendSupportMessage(cid, mapOf<String, Any>("content" to text))
                }
                loadMessages()
            } catch (_: Exception) {}
        }
    }
}

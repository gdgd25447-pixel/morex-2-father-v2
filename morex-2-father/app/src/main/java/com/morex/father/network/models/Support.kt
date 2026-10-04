package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

/** support_conversations: محادثة واحدة لكل أب. */
data class SupportConversation(
    @SerializedName("id") val id: String,
    @SerializedName("parent_id") val parentId: String? = null,
    @SerializedName("status") val status: String = "open",
    @SerializedName("unread_for_parent") val unreadCount: Int = 0,
    @SerializedName("last_message_preview") val lastMessage: String? = null,
    @SerializedName("last_message_at") val lastMessageAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

/** POST /support/conversations تعيد { conversation: {...} } */
data class SupportConversationEnvelope(
    @SerializedName("conversation") val conversation: SupportConversation? = null
)

/** support_messages: id, conversation_id, sender_type (parent|admin|bot), content, attachment_url */
data class SupportMessage(
    @SerializedName("id") val id: String,
    @SerializedName("conversation_id") val conversationId: String? = null,
    @SerializedName("sender_type") val senderType: String = "parent",
    @SerializedName("content") val message: String? = null,
    @SerializedName("message_type") val messageType: String? = null,
    @SerializedName("attachment_url") val imageUrl: String? = null,
    @SerializedName("attachment_name") val attachmentName: String? = null,
    @SerializedName("is_read") val isRead: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null
)

/** POST .../messages تعيد { ok, message: {...} } */
data class SupportMessageEnvelope(
    @SerializedName("ok") val ok: Boolean = false,
    @SerializedName("message") val message: SupportMessage? = null
)

/** POST /support/upload */
data class SupportUploadResponse(
    @SerializedName("url") val url: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("size_kb") val sizeKb: String? = null
)

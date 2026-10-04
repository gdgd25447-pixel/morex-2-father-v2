package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

/** commands: id, device_id, type (LOCK_NOW|UNLOCK|RING|VIBRATE|NOTIFY_TEXT|CAMERA_FRONT|...)، payload, status, result */
data class Command(
    @SerializedName("id") val id: String? = null,
    @SerializedName("device_id") val deviceId: String? = null,
    @SerializedName("type") val type: String = "",
    @SerializedName("payload") val payload: Map<String, Any?>? = null,
    @SerializedName("status") val status: String = "pending", // pending | sent | delivered | executed | failed | cancelled
    @SerializedName("result") val result: Map<String, Any?>? = null,
    @SerializedName("device_model") val deviceModel: String? = null,
    @SerializedName("child_name") val childName: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("completed_at") val completedAt: String? = null
)

/** POST /parent/commands تعيد { command: {...} } */
data class CommandEnvelope(
    @SerializedName("command") val command: Command? = null,
    @SerializedName("error") val error: String? = null
)

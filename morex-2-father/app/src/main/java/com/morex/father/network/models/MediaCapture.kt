package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

/** media_captures: id, device_id, command_id, media_type (photo_front|photo_back|screenshot|audio), file_url, file_size_kb, duration_sec */
data class MediaCapture(
    @SerializedName("id") val id: String,
    @SerializedName("device_id") val deviceId: String? = null,
    @SerializedName("media_type") val type: String = "",
    @SerializedName("file_url") val url: String? = null,
    @SerializedName("file_size_kb") val fileSizeKb: Int? = null,
    @SerializedName("duration_sec") val duration: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

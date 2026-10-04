package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

/** مشاركة الموقع (غير مستخدمة حالياً؛ لا يوجد مسار في السيرفر). */
data class LocationShareRequest(
    @SerializedName("child_id") val childId: String,
    @SerializedName("expires_in_hours") val expiresInHours: Int = 24
)

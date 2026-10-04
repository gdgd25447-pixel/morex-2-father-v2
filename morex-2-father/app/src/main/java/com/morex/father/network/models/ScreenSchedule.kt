package com.morex.father.network.models

import com.google.gson.annotations.SerializedName

/** screen_schedules: id, child_id, name, days_of_week INT[], start_time, end_time, action (lock), is_active */
data class ScreenSchedule(
    @SerializedName("id") val id: String? = null,
    @SerializedName("child_id") val childId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("days_of_week") val days: List<Int> = emptyList(),
    @SerializedName("start_time") val startTime: String = "",
    @SerializedName("end_time") val endTime: String = "",
    @SerializedName("action") val action: String = "lock",
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("created_at") val createdAt: String? = null
) {
    /** السيرفر يعيد TIME بصيغة HH:mm:ss — نعرض HH:mm. */
    val startHm: String get() = startTime.take(5)
    val endHm: String get() = endTime.take(5)
}

data class ScheduleEnvelope(
    @SerializedName("schedule") val schedule: ScreenSchedule? = null
)

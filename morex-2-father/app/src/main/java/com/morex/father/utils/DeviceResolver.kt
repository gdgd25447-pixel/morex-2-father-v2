package com.morex.father.utils

import com.morex.father.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * يحوّل child_id إلى device_id (الأوامر والوسائط تُرسل لجهاز، لا لطفل).
 * المصدر: GET /parent/children (يحتوي device_id للجهاز المربوط).
 */
object DeviceResolver {
    suspend fun deviceIdOf(childId: String): String? = withContext(Dispatchers.IO) {
        try {
            val resp = ApiClient.get().getChildren()
            resp.body()?.firstOrNull { it.id == childId }?.deviceId?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }
}

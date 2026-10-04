package com.morex.father.network

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.morex.father.data.Prefs
import com.morex.father.utils.Constants
import com.morex.father.utils.Notifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FCMService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔑 New FCM token: $token")
        Prefs.setFcmToken(applicationContext, token)

        // Send to server
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ApiClient.get().updateFcmToken(mapOf("fcm_token" to token))
                Log.d(TAG, "✅ FCM token sent to server")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to send FCM token: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "📩 Message received from: ${message.from}")

        val data = message.data
        val type = data["type"] ?: "generic"
        val title = message.notification?.title ?: data["title"] ?: "MOREX"
        val body = message.notification?.body ?: data["body"] ?: ""

        when (type) {
            "sos" -> {
                Notifications.showSosAlert(
                    applicationContext,
                    data["child_name"] ?: "الطفل",
                    data["location"]
                )
            }
            "prayer" -> {
                Notifications.showPrayerAlert(
                    applicationContext,
                    data["prayer_name"] ?: "الصلاة"
                )
            }
            "quran" -> {
                Notifications.showQuranReminder(
                    applicationContext,
                    data["target_pages"]?.toIntOrNull() ?: 5
                )
            }
            "alert" -> {
                Notifications.showAlert(
                    applicationContext,
                    title,
                    body,
                    Constants.CHANNEL_ALERTS,
                    Constants.NOTIF_ALERT
                )
            }
            "chat" -> {
                Notifications.showAlert(
                    applicationContext,
                    title,
                    body,
                    Constants.CHANNEL_SUPPORT,
                    Constants.NOTIF_CHAT
                )
            }
            else -> {
                Notifications.showAlert(applicationContext, title, body)
            }
        }
    }
}

package com.morex.father.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.morex.father.MainActivity
import com.morex.father.R

object Notifications {

    fun showAlert(
        context: Context,
        title: String,
        message: String,
        channelId: String = Constants.CHANNEL_ALERTS,
        notificationId: Int = Constants.NOTIF_GENERIC
    ) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(notificationId, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showSosAlert(context: Context, childName: String, location: String?) {
        val msg = if (location.isNullOrBlank()) {
            "تم إرسال تنبيه طوارئ من $childName"
        } else {
            "تنبيه طوارئ من $childName - الموقع: $location"
        }
        showAlert(context, "🚨 تنبيه طارئ", msg, Constants.CHANNEL_SOS, Constants.NOTIF_SOS)
    }

    fun showPrayerAlert(context: Context, prayerName: String) {
        showAlert(
            context,
            "🕌 $prayerName",
            "حان وقت صلاة $prayerName",
            Constants.CHANNEL_PRAYER,
            Constants.NOTIF_PRAYER
        )
    }

    fun showQuranReminder(context: Context, pages: Int) {
        showAlert(
            context,
            "📖 تذكير القرآن",
            "لم يُحقق هدف القرآن اليوم - الهدف: $pages صفحات",
            Constants.CHANNEL_QURAN,
            Constants.NOTIF_QURAN
        )
    }

    fun cancelAll(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
    }

    fun cancel(context: Context, notificationId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)
    }
}

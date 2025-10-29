package com.example.dailyreminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    private const val CHANNEL_ID = "daily_channel"
    private const val CHANNEL_NAME = "Hello Bubble"

    fun showNow(context: Context, content: String? = null) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
                channel.enableVibration(true)
                channel.setShowBadge(true)
                nm.createNotificationChannel(channel)
            }

            val text = content ?: "Coucou 👋" // Fallback if no content provided
            
            // Always use BigText style for expandable notifications
            val shortText = if (text.length > 50) text.take(47) + "..." else text
            
            val notif = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("Hello Bubble")
                .setContentText(shortText)
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(text)
                    .setBigContentTitle("Hello Bubble"))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build()

            nm.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notif)
        } catch (e: Exception) {
            // If notification fails, at least log it
            android.util.Log.e("NotificationHelper", "Failed to show notification: ${e.message}")
        }
    }
}

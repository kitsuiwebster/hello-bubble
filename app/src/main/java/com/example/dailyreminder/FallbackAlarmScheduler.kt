package com.example.dailyreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

/**
 * Fallback alarm that triggers 3 hours after the main alarm should have triggered
 * This provides an additional safety net if all other systems fail
 */
object FallbackAlarmScheduler {
    
    fun scheduleFallbackIfNeeded(context: Context) {
        try {
            val nextMainAlarm = PreferenceManager.getNextScheduledTime(context)
            if (nextMainAlarm > 0) {
                val fallbackTime = nextMainAlarm + (3 * 60 * 60 * 1000L) // 3 hours later
                
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val pi = PendingIntent.getBroadcast(
                    context, 
                    2002, // Different ID
                    Intent(context, FallbackAlarmReceiver::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fallbackTime, pi)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, fallbackTime, pi)
                }
                
                Log.d("FallbackAlarm", "Fallback alarm scheduled 3h after main alarm")
            }
        } catch (e: Exception) {
            Log.w("FallbackAlarm", "Failed to schedule fallback alarm: ${e.message}")
        }
    }
    
    fun cancelFallbackAlarm(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pi = PendingIntent.getBroadcast(
                context, 
                2002,
                Intent(context, FallbackAlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pi)
            Log.d("FallbackAlarm", "Fallback alarm cancelled")
        } catch (e: Exception) {
            Log.w("FallbackAlarm", "Failed to cancel fallback alarm: ${e.message}")
        }
    }
}
package com.example.dailyreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

object BackupAlarmScheduler {
    
    fun scheduleBackupForTomorrow(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val trigger = Calendar.getInstance().apply {
            // Schedule for tomorrow at 23:58 (1 minute before midnight to avoid race conditions)
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 58)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val pi = PendingIntent.getBroadcast(
            context, 
            2001, // Different ID from main alarm (1001)
            Intent(context, BackupAlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.timeInMillis, pi)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, trigger.timeInMillis, pi)
        }
        
        Log.d("BackupAlarm", "Backup alarm scheduled for tomorrow at 23:58: ${trigger.time}")
    }
    
    fun cancelBackupAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            context, 
            2001,
            Intent(context, BackupAlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pi)
        Log.d("BackupAlarm", "Backup alarm cancelled")
    }
}
package com.example.dailyreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

object AlarmScheduler {

    fun scheduleNext(context: Context) {
        // Only schedule if notifications are enabled
        if (!PreferenceManager.areNotificationsEnabled(context)) {
            Log.d("AlarmScheduler", "Notifications disabled, not scheduling")
            return
        }
        
        // Don't schedule if we already have an alarm scheduled for today
        if (SafePreferenceManager.hasAlarmAlreadyScheduledForToday(context)) {
            Log.d("AlarmScheduler", "Alarm already scheduled for today, skipping")
            return
        }
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance().apply {
            val (hour, minute) = HoursRepository.randomHourAndMinute(context)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }

        val pi = PendingIntent.getBroadcast(
            context, 1001, Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.timeInMillis, pi)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, trigger.timeInMillis, pi)
        }
        
        // Store the scheduled time
        PreferenceManager.setNextScheduledTime(context, trigger.timeInMillis)
        Log.d("AlarmScheduler", "Main alarm scheduled for: ${trigger.time}")
        
        // Also schedule backup alarm for tomorrow
        BackupAlarmScheduler.scheduleBackupForTomorrow(context)
        
        // Schedule fallback alarm as last resort
        FallbackAlarmScheduler.scheduleFallbackIfNeeded(context)
    }

    fun cancelNext(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(
            context, 1001, Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pi)
        // Clear the scheduled time
        PreferenceManager.setNextScheduledTime(context, 0L)
        Log.d("AlarmScheduler", "Main alarm cancelled")
        
        // Also cancel backup alarm and fallback alarm
        BackupAlarmScheduler.cancelBackupAlarm(context)
        FallbackAlarmScheduler.cancelFallbackAlarm(context)
    }
    
    fun forceScheduleForTomorrow(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val trigger = Calendar.getInstance().apply {
            val (hour, minute) = HoursRepository.randomHourAndMinute(context)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1) // Always schedule for tomorrow
        }

        val pi = PendingIntent.getBroadcast(
            context, 1001, Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.timeInMillis, pi)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, trigger.timeInMillis, pi)
        }
        
        // Store the scheduled time
        PreferenceManager.setNextScheduledTime(context, trigger.timeInMillis)
        Log.d("AlarmScheduler", "Force scheduled for tomorrow: ${trigger.time}")
        
        // Also schedule backup alarm for tomorrow
        BackupAlarmScheduler.scheduleBackupForTomorrow(context)
        
        // Schedule fallback alarm as last resort
        FallbackAlarmScheduler.scheduleFallbackIfNeeded(context)
    }
}

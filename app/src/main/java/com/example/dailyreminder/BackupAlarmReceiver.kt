package com.example.dailyreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BackupAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("BackupAlarm", "Backup alarm triggered at 23:58")
        
        // Check if notifications are enabled
        if (!PreferenceManager.areNotificationsEnabled(context)) {
            Log.d("BackupAlarm", "Notifications disabled, skipping backup check")
            return
        }
        
        // Check if we sent a notification today AND if we're not too close to midnight
        if (SafePreferenceManager.shouldSendNotificationTodaySafe(context)) {
            if (SafePreferenceManager.isCloseToMidnight()) {
                Log.w("BackupAlarm", "Too close to midnight, skipping backup to avoid date confusion")
            } else {
                Log.w("BackupAlarm", "No notification sent today! Sending backup notification")
                
                // Send backup notification
                val message = MessageRepository.randomMessage(context)
                NotificationHelper.showNow(context, message)
                
                // Mark as sent and track message
                PreferenceManager.markNotificationSentToday(context)
                MessageRepository.markMessageAsSent(context, message)
            }
        } else {
            Log.d("BackupAlarm", "Notification already sent today, backup not needed")
        }
        
        // Schedule backup for tomorrow
        BackupAlarmScheduler.scheduleBackupForTomorrow(context)
        
        // Ensure main alarm is scheduled for tomorrow, but check if already scheduled
        if (PreferenceManager.getNextScheduledTime(context) <= System.currentTimeMillis()) {
            Log.d("BackupAlarm", "No main alarm scheduled for tomorrow, scheduling now")
            AlarmScheduler.forceScheduleForTomorrow(context)
        } else {
            Log.d("BackupAlarm", "Main alarm already scheduled for tomorrow")
        }
    }
}
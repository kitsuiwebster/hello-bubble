package com.example.dailyreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            Log.d("BootReceiver", "Device rebooted, checking if alarms need rescheduling")
            // Only reschedule if notifications were enabled before reboot
            if (PreferenceManager.areNotificationsEnabled(context)) {
                // Check if we already sent a notification today before rescheduling
                if (SafePreferenceManager.shouldSendNotificationTodaySafe(context)) {
                    Log.d("BootReceiver", "No notification sent today, rescheduling immediately")
                    AlarmScheduler.scheduleNext(context)
                } else {
                    Log.d("BootReceiver", "Notification already sent today, scheduling for tomorrow")
                    AlarmScheduler.forceScheduleForTomorrow(context)
                }
            } else {
                Log.d("BootReceiver", "Notifications disabled, not rescheduling")
            }
        }
    }
}

package com.example.dailyreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Last resort alarm that triggers if main alarm and backup alarm both failed
 */
class FallbackAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.w("FallbackAlarm", "Fallback alarm triggered - main system may have failed")
        
        // Check if notifications are enabled
        if (!PreferenceManager.areNotificationsEnabled(context)) {
            Log.d("FallbackAlarm", "Notifications disabled, skipping fallback")
            return
        }
        
        // Check if we already sent a notification today
        if (SafePreferenceManager.shouldSendNotificationTodaySafe(context)) {
            Log.w("FallbackAlarm", "Main system failed! Sending fallback notification")
            
            // Send fallback notification
            val message = MessageRepository.randomMessage(context)
            NotificationHelper.showNow(context, message)
            
            // Mark as sent and track message
            PreferenceManager.markNotificationSentToday(context)
            MessageRepository.markMessageAsSent(context, message)
            
            // Reschedule everything for tomorrow
            AlarmScheduler.forceScheduleForTomorrow(context)
        } else {
            Log.d("FallbackAlarm", "Notification already sent today, fallback not needed")
        }
    }
}
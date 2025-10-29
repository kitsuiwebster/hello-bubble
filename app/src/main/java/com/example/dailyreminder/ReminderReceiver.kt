package com.example.dailyreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("ReminderReceiver", "Notification alarm triggered")
        
        // Si c'est un test avec message personnalisé
        val testMessage = intent?.getStringExtra("test_message")
        if (testMessage != null) {
            Log.d("ReminderReceiver", "Test message notification: $testMessage")
            NotificationHelper.showNow(context, testMessage)
            return
        }
        
        // Check if notifications are enabled and if we should send one today
        if (!PreferenceManager.areNotificationsEnabled(context)) {
            Log.d("ReminderReceiver", "Notifications disabled, skipping")
            return
        }
        
        if (!SafePreferenceManager.shouldSendNotificationTodaySafe(context)) {
            Log.d("ReminderReceiver", "Already sent notification today, rescheduling for tomorrow")
            // We already sent a notification today, schedule for tomorrow
            AlarmScheduler.forceScheduleForTomorrow(context)
            return
        }
        
        // Si une phrase est définie pour AUJOURD'HUI (overrides.json), on l'utilise
        val override = DateOverrideRepository.messageForTodayOrNull(context)
        if (override != null) {
            Log.d("ReminderReceiver", "Using override message: $override")
            // Date override - don't track this message, just show it
            NotificationHelper.showNow(context, override)
        } else {
            // Regular message - get from repository and track it
            val message = MessageRepository.randomMessage(context)
            Log.d("ReminderReceiver", "Sending regular message: $message")
            NotificationHelper.showNow(context, message)
            // Mark this message as sent (for cycle tracking)
            MessageRepository.markMessageAsSent(context, message)
        }
        
        // Mark that we sent a notification today
        PreferenceManager.markNotificationSentToday(context)
        Log.d("ReminderReceiver", "Notification sent successfully, scheduling for tomorrow")
        
        // Schedule the next notification for tomorrow
        AlarmScheduler.forceScheduleForTomorrow(context)
    }
}

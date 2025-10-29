package com.example.dailyreminder

import android.content.Context
import android.content.SharedPreferences
import java.util.Calendar
import java.util.concurrent.TimeUnit

object PreferenceManager {
    private const val PREFS_NAME = "daily_reminder_prefs"
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_LAST_NOTIFICATION_DATE = "last_notification_date"
    private const val KEY_NEXT_SCHEDULED_TIME = "next_scheduled_time"
    private const val KEY_SENT_MESSAGES = "sent_messages"
    private const val KEY_TOTAL_MESSAGES_COUNT = "total_messages_count"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun areNotificationsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_NOTIFICATIONS_ENABLED, false)
    }
    
    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit()
            .putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
            .apply()
    }
    
    fun shouldSendNotificationToday(context: Context): Boolean {
        val today = getCurrentDateString()
        val lastNotificationDate = getPrefs(context).getString(KEY_LAST_NOTIFICATION_DATE, "")
        return lastNotificationDate != today
    }
    
    fun markNotificationSentToday(context: Context) {
        val today = getCurrentDateString()
        getPrefs(context).edit()
            .putString(KEY_LAST_NOTIFICATION_DATE, today)
            .apply()
    }
    
    fun setNextScheduledTime(context: Context, timeInMillis: Long) {
        getPrefs(context).edit()
            .putLong(KEY_NEXT_SCHEDULED_TIME, timeInMillis)
            .apply()
    }
    
    fun getNextScheduledTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_NEXT_SCHEDULED_TIME, 0L)
    }
    
    private fun getCurrentDateString(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)+1}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }
    
    fun hasAlarmAlreadyScheduledForToday(context: Context): Boolean {
        val nextScheduledTime = getNextScheduledTime(context)
        if (nextScheduledTime == 0L) return false
        
        val now = System.currentTimeMillis()
        if (nextScheduledTime <= now) return false
        
        // Check if the scheduled time is actually TODAY (not tomorrow)
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val todayEnd = todayStart + TimeUnit.DAYS.toMillis(1) - 1
        
        return nextScheduledTime in todayStart..todayEnd
    }
    
    // Message tracking methods
    fun getSentMessages(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_SENT_MESSAGES, emptySet()) ?: emptySet()
    }
    
    fun addSentMessage(context: Context, message: String) {
        val currentSent = getSentMessages(context).toMutableSet()
        currentSent.add(message)
        getPrefs(context).edit()
            .putStringSet(KEY_SENT_MESSAGES, currentSent)
            .apply()
    }
    
    fun resetSentMessages(context: Context) {
        getPrefs(context).edit()
            .putStringSet(KEY_SENT_MESSAGES, emptySet())
            .apply()
    }
    
    fun getTotalMessagesCount(context: Context): Int {
        return getPrefs(context).getInt(KEY_TOTAL_MESSAGES_COUNT, 0)
    }
    
    fun setTotalMessagesCount(context: Context, count: Int) {
        getPrefs(context).edit()
            .putInt(KEY_TOTAL_MESSAGES_COUNT, count)
            .apply()
    }
    
    fun shouldResetMessageCycle(context: Context, totalAvailableMessages: Int): Boolean {
        val sentMessages = getSentMessages(context)
        val storedCount = getTotalMessagesCount(context)
        
        // Reset if we've sent all messages or if the total count has changed (new messages added)
        return sentMessages.size >= totalAvailableMessages || storedCount != totalAvailableMessages
    }
}
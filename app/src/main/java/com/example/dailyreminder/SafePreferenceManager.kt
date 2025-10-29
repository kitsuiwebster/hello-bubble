package com.example.dailyreminder

import android.content.Context
import java.util.Calendar

object SafePreferenceManager {
    
    /**
     * Safe version of hasAlarmAlreadyScheduledForToday that handles timezone changes
     */
    fun hasAlarmAlreadyScheduledForToday(context: Context): Boolean {
        return try {
            val nextScheduledTime = PreferenceManager.getNextScheduledTime(context)
            if (nextScheduledTime == 0L) return false
            
            val now = System.currentTimeMillis()
            if (nextScheduledTime <= now) return false
            
            // Validate the timestamp is reasonable (not corrupted)
            if (nextScheduledTime < 0 || nextScheduledTime > now + 86400000L * 7) {
                // Corrupted timestamp, clear it
                PreferenceManager.setNextScheduledTime(context, 0L)
                return false
            }
            
            // Use a more robust way to check if it's today
            val scheduledCalendar = Calendar.getInstance().apply {
                timeInMillis = nextScheduledTime
            }
            
            val todayCalendar = Calendar.getInstance()
            
            scheduledCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                   scheduledCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)
        } catch (e: Exception) {
            // If anything goes wrong, assume no alarm is scheduled
            false
        }
    }
    
    /**
     * Check if we're close to midnight to avoid double notifications
     */
    fun isCloseToMidnight(): Boolean {
        return try {
            val now = Calendar.getInstance()
            val hour = now.get(Calendar.HOUR_OF_DAY)
            val minute = now.get(Calendar.MINUTE)
            
            // If it's after 23:55, we're too close to midnight
            hour == 23 && minute >= 55
        } catch (e: Exception) {
            // If time calculation fails, assume it's safe
            false
        }
    }
    
    /**
     * Safely check if notifications should be sent today with date validation
     */
    fun shouldSendNotificationTodaySafe(context: Context): Boolean {
        return try {
            PreferenceManager.shouldSendNotificationToday(context)
        } catch (e: Exception) {
            // If date comparison fails, assume we should send
            true
        }
    }
}
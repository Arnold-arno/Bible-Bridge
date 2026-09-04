package com.example

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

object DevotionalReminderHelper {
    fun scheduleReminder(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DevotionalReminderReceiver::class.java)
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            102,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            // Cancel any existing alarm first
            alarmManager.cancel(pendingIntent)
            
            // Set repeating alarm
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
            Log.d("DevotionalReminder", "Successfully scheduled repeating reminder alarm for $hour:$minute")
        } catch (e: Exception) {
            Log.e("DevotionalReminder", "Failed to schedule alarm", e)
        }
    }

    fun cancelReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DevotionalReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            102,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            alarmManager.cancel(pendingIntent)
            Log.d("DevotionalReminder", "Successfully cancelled reminder alarm")
        } catch (e: Exception) {
            Log.e("DevotionalReminder", "Failed to cancel alarm", e)
        }
    }
}

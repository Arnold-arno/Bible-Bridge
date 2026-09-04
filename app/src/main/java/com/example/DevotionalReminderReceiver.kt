package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import android.util.Log

class DevotionalReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("DevotionalReminder", "Received reminder broadcast!")
        
        // Show the notification
        showNotification(context)
        
        // Reschedule alarm on boot completed
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val sharedPrefs = context.getSharedPreferences("bible_companion_prefs", Context.MODE_PRIVATE)
            val isEnabled = sharedPrefs.getBoolean("reminder_enabled", false)
            if (isEnabled) {
                val hour = sharedPrefs.getInt("reminder_hour", 8)
                val minute = sharedPrefs.getInt("reminder_minute", 0)
                DevotionalReminderHelper.scheduleReminder(context, hour, minute)
            }
        }
    }

    private fun showNotification(context: Context) {
        val channelId = "daily_devotional_reminder"
        val notificationId = 1001

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Devotional Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Triggers daily notification at your preferred time to read the scripture devotion."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "devotions")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sharedPrefs = context.getSharedPreferences("bible_companion_prefs", Context.MODE_PRIVATE)
        val name = sharedPrefs.getString("user_name", "Faithful Reader") ?: "Faithful Reader"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Daily Devotional Study")
            .setContentText("Hello $name, it's time for your daily scripture reading & reflection!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}

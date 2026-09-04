package com.example

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast

class DailyVerseNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TRIGGER_DAILY_VERSE -> {
                val prefs = context.getSharedPreferences("bible_companion_prefs", Context.MODE_PRIVATE)
                val frequency = prefs.getString("daily_notification_frequency", "Daily") ?: "Daily"
                val cal = java.util.Calendar.getInstance()
                val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
                val isWeekday = dayOfWeek in java.util.Calendar.MONDAY..java.util.Calendar.FRIDAY
                val isWeekend = dayOfWeek == java.util.Calendar.SATURDAY || dayOfWeek == java.util.Calendar.SUNDAY

                val shouldShowToday = when (frequency) {
                    "Weekdays" -> isWeekday
                    "Weekends" -> isWeekend
                    else -> true
                }

                if (shouldShowToday) {
                    val customTitle = prefs.getString("daily_notification_title", "Daily Devotion — Verse of the Day")
                    val verse = DailyVerseNotificationHelper.getVerseForCurrentPreferences(context)
                    DailyVerseNotificationHelper.showDailyVerseNotification(
                        context = context,
                        verse = verse,
                        isPersistent = false,
                        customTitle = customTitle
                    )
                }

                // Also update persistent notification if enabled
                DailyVerseNotificationHelper.updatePersistentNotificationIfActive(context)

                // Update home screen widgets
                VerseOfTheDayWidgetProvider.updateAllWidgets(context)
            }

            ACTION_SHARE_VERSE -> {
                val verseText = intent.getStringExtra("verse_text") ?: ""
                if (verseText.isNotEmpty()) {
                    try {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Daily Scripture Verse")
                            putExtra(Intent.EXTRA_TEXT, verseText)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Scripture Verse").apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            ACTION_NEXT_VERSE -> {
                val isPersistent = intent.getBooleanExtra("is_persistent", false)
                
                // Rotate verse of the day
                rotateNextVerse(context)
                val newVerse = VerseOfTheDayWidgetProvider.getCurrentVerse(context)

                // Update notification
                DailyVerseNotificationHelper.showDailyVerseNotification(context, newVerse, isPersistent = isPersistent)

                // Update widgets
                VerseOfTheDayWidgetProvider.updateAllWidgets(context)
            }

            ACTION_COPY_VERSE -> {
                val verseText = intent.getStringExtra("verse_text") ?: ""
                if (verseText.isNotEmpty()) {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Verse of the Day", verseText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                }
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                val prefs = context.getSharedPreferences("bible_companion_prefs", Context.MODE_PRIVATE)
                
                // Restore scheduled daily alarm if enabled
                val dailyAlarmEnabled = prefs.getBoolean("daily_votd_alarm_enabled", false)
                if (dailyAlarmEnabled) {
                    val hour = prefs.getInt("daily_votd_alarm_hour", 8)
                    val minute = prefs.getInt("daily_votd_alarm_minute", 0)
                    DailyVerseNotificationHelper.scheduleDailyAlarm(context, hour, minute)
                }

                // Restore persistent status bar notification if enabled
                val persistentEnabled = prefs.getBoolean("persistent_votd_enabled", false)
                if (persistentEnabled) {
                    val verse = VerseOfTheDayWidgetProvider.getCurrentVerse(context)
                    DailyVerseNotificationHelper.showDailyVerseNotification(context, verse, isPersistent = true)
                }

                // Update home screen widgets
                VerseOfTheDayWidgetProvider.updateAllWidgets(context)
            }
        }
    }

    private fun rotateNextVerse(context: Context) {
        val prefs = context.getSharedPreferences("bible_companion_prefs", Context.MODE_PRIVATE)
        val offlineList = VerseOfTheDayWidgetProvider.getOfflineVerses()
        val currentIndex = prefs.getInt("votd_offline_index", 0)
        val nextIndex = (currentIndex + 1) % offlineList.size
        val nextVerse = offlineList[nextIndex]

        prefs.edit()
            .putInt("votd_offline_index", nextIndex)
            .putString("votd_text", nextVerse.text)
            .putString("votd_book", nextVerse.bookName)
            .putInt("votd_chapter", nextVerse.chapter)
            .putInt("votd_number", nextVerse.verseNumber)
            .putString("votd_translation", nextVerse.translation)
            .apply()
    }

    companion object {
        const val ACTION_TRIGGER_DAILY_VERSE = "com.example.ACTION_TRIGGER_DAILY_VERSE"
        const val ACTION_NEXT_VERSE = "com.example.ACTION_NEXT_VERSE"
        const val ACTION_COPY_VERSE = "com.example.ACTION_COPY_VERSE"
        const val ACTION_SHARE_VERSE = "com.example.ACTION_SHARE_VERSE"
    }
}

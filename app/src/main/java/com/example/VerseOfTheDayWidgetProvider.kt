package com.example

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.data.BibleVerse
import java.util.Calendar

class VerseOfTheDayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET) {
            // Rotate verse of the day in prefs
            rotateNextVerse(context)
            
            // Update all widgets
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, VerseOfTheDayWidgetProvider::class.java)
            )
            for (id in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }

            // Also update persistent notification if enabled
            DailyVerseNotificationHelper.updatePersistentNotificationIfActive(context)
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.ACTION_REFRESH_WIDGET"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, VerseOfTheDayWidgetProvider::class.java)
            )
            for (id in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_daily_verse)

            val verse = getCurrentVerse(context)

            views.setTextViewText(R.id.widget_verse_text, "“${verse.text}”")
            views.setTextViewText(R.id.widget_verse_ref, "— ${verse.bookName} ${verse.chapter}:${verse.verseNumber}")
            views.setTextViewText(R.id.widget_verse_translation, verse.translation)

            // Click container -> Open MainActivity at Scripture verse
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", "read")
                putExtra("book", verse.bookName)
                putExtra("chapter", verse.chapter)
                putExtra("verse", verse.verseNumber)
            }
            val mainPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, mainPendingIntent)

            // Click refresh button -> broadcast refresh
            val refreshIntent = Intent(context, VerseOfTheDayWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId + 10000,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun getCurrentVerse(context: Context): BibleVerse {
            val prefs = context.getSharedPreferences("bible_companion_prefs", Context.MODE_PRIVATE)
            val savedText = prefs.getString("votd_text", "") ?: ""
            val savedBook = prefs.getString("votd_book", "") ?: ""
            val savedChapter = prefs.getInt("votd_chapter", 0)
            val savedNumber = prefs.getInt("votd_number", 0)
            val savedTranslation = prefs.getString("votd_translation", "KJV") ?: "KJV"

            if (savedText.isNotEmpty() && savedBook.isNotEmpty() && savedChapter > 0 && savedNumber > 0) {
                return BibleVerse(
                    translation = savedTranslation,
                    bookName = savedBook,
                    chapter = savedChapter,
                    verseNumber = savedNumber,
                    text = savedText
                )
            }

            // Fallback deterministic selection
            val offlineList = getOfflineVerses()
            val index = prefs.getInt("votd_offline_index", Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
            return offlineList[index % offlineList.size]
        }

        private fun rotateNextVerse(context: Context) {
            val prefs = context.getSharedPreferences("bible_companion_prefs", Context.MODE_PRIVATE)
            val offlineList = getOfflineVerses()
            val currentIndex = prefs.getInt("votd_offline_index", Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
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

        fun getOfflineVerses(): List<BibleVerse> {
            return listOf(
                BibleVerse(translation = "KJV", bookName = "Romans", chapter = 8, verseNumber = 28, text = "And we know that all things work together for good to them that love God, to them who are the called according to his purpose."),
                BibleVerse(translation = "KJV", bookName = "Psalms", chapter = 23, verseNumber = 1, text = "The LORD is my shepherd; I shall not want."),
                BibleVerse(translation = "KJV", bookName = "Proverbs", chapter = 3, verseNumber = 5, text = "Trust in the LORD with all thine heart; and lean not unto thine own understanding."),
                BibleVerse(translation = "KJV", bookName = "Proverbs", chapter = 3, verseNumber = 6, text = "In all thy ways acknowledge him, and he shall direct thy paths."),
                BibleVerse(translation = "KJV", bookName = "Isaiah", chapter = 40, verseNumber = 31, text = "But they that wait upon the LORD shall renew their strength; they shall mount up with wings as eagles; they shall run, and not be weary; and they shall walk, and not faint."),
                BibleVerse(translation = "KJV", bookName = "Philippians", chapter = 4, verseNumber = 13, text = "I can do all things through Christ which strengtheneth me."),
                BibleVerse(translation = "KJV", bookName = "John", chapter = 3, verseNumber = 16, text = "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life."),
                BibleVerse(translation = "KJV", bookName = "Romans", chapter = 12, verseNumber = 2, text = "And be not conformed to this world: but be ye transformed by the renewing of your mind, that ye may prove what is that good, and acceptable, and perfect, will of God."),
                BibleVerse(translation = "KJV", bookName = "Joshua", chapter = 1, verseNumber = 9, text = "Have not I commanded thee? Be strong and of a good courage; be not afraid, neither be thou dismayed: for the LORD thy God is with thee whithersoever thou goest."),
                BibleVerse(translation = "KJV", bookName = "Matthew", chapter = 6, verseNumber = 33, text = "But seek ye first the kingdom of God, and his righteousness; and all these things shall be added unto you.")
            )
        }
    }
}

package com.example

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.data.BibleVerse
import java.util.Calendar

object DailyVerseNotificationHelper {

    const val CHANNEL_ID = "daily_verse_of_the_day_channel"
    const val NOTIFICATION_ID_DAILY = 2001
    const val NOTIFICATION_ID_PERSISTENT = 2002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Verse of the Day"
            val descriptionText = "Daily verse notifications and persistent status bar updates"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showDailyVerseNotification(
        context: Context,
        verse: BibleVerse = VerseOfTheDayWidgetProvider.getCurrentVerse(context),
        isPersistent: Boolean = false,
        customTitle: String? = null
    ) {
        createNotificationChannel(context)

        val prefs = context.getSharedPreferences("bible_companion_prefs", Context.MODE_PRIVATE)
        val titleText = when {
            !customTitle.isNullOrBlank() -> customTitle
            else -> prefs.getString("daily_notification_title", "Daily Devotion — Verse of the Day") ?: "Daily Devotion — Verse of the Day"
        }

        val notificationId = if (isPersistent) NOTIFICATION_ID_PERSISTENT else NOTIFICATION_ID_DAILY

        // 1. Read in App Intent
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "read")
            putExtra("book", verse.bookName)
            putExtra("chapter", verse.chapter)
            putExtra("verse", verse.verseNumber)
        }
        val readPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Copy Verse Action Intent
        val copyIntent = Intent(context, DailyVerseNotificationReceiver::class.java).apply {
            action = DailyVerseNotificationReceiver.ACTION_COPY_VERSE
            putExtra("verse_text", "“${verse.text}” — ${verse.bookName} ${verse.chapter}:${verse.verseNumber} (${verse.translation})")
        }
        val copyPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 10,
            copyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Share Verse Action Intent
        val shareIntent = Intent(context, DailyVerseNotificationReceiver::class.java).apply {
            action = DailyVerseNotificationReceiver.ACTION_SHARE_VERSE
            putExtra("verse_text", "“${verse.text}”\n— ${verse.bookName} ${verse.chapter}:${verse.verseNumber} (${verse.translation})\n\nShared via Holy Scripture App")
        }
        val sharePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 30,
            shareIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Next Verse Action Intent
        val nextIntent = Intent(context, DailyVerseNotificationReceiver::class.java).apply {
            action = DailyVerseNotificationReceiver.ACTION_NEXT_VERSE
            putExtra("is_persistent", isPersistent)
        }
        val nextPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 20,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.btn_star_big_on)
            .setContentTitle("$titleText — ${verse.bookName} ${verse.chapter}:${verse.verseNumber}")
            .setContentText("“${verse.text}” (${verse.translation})")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("“${verse.text}”\n\n— ${verse.bookName} ${verse.chapter}:${verse.verseNumber} (${verse.translation})")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(readPendingIntent)
            .setOngoing(isPersistent)
            .setAutoCancel(!isPersistent)
            .addAction(android.R.drawable.ic_menu_view, "Read", readPendingIntent)
            .addAction(android.R.drawable.ic_menu_send, "Copy", copyPendingIntent)
            .addAction(android.R.drawable.ic_menu_share, "Share", sharePendingIntent)
            .addAction(android.R.drawable.ic_popup_sync, "Next Verse", nextPendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    fun getVerseForCurrentPreferences(context: Context): BibleVerse {
        val prefs = context.getSharedPreferences("bible_companion_prefs", Context.MODE_PRIVATE)

        // Check if user set a custom verse
        val customText = prefs.getString("custom_notif_verse_text", "") ?: ""
        val customBook = prefs.getString("custom_notif_verse_book", "") ?: ""
        val customChapter = prefs.getInt("custom_notif_verse_chapter", 0)
        val customNumber = prefs.getInt("custom_notif_verse_number", 0)
        if (customText.isNotEmpty() && customBook.isNotEmpty() && customChapter > 0 && customNumber > 0) {
            return BibleVerse(
                translation = prefs.getString("custom_notif_verse_translation", "KJV") ?: "KJV",
                bookName = customBook,
                chapter = customChapter,
                verseNumber = customNumber,
                text = customText
            )
        }

        // Otherwise filter by category preference
        val category = prefs.getString("daily_notification_category", "All Verses") ?: "All Verses"
        val categoryVerses = getVersesByCategory(category)
        val index = (System.currentTimeMillis() / (1000 * 60 * 60 * 24)).toInt() % categoryVerses.size
        return categoryVerses[Math.abs(index)]
    }

    fun getVersesByCategory(category: String): List<BibleVerse> {
        return when (category.lowercase()) {
            "peace & comfort", "peace", "comfort" -> listOf(
                BibleVerse(translation = "KJV", bookName = "Psalms", chapter = 23, verseNumber = 1, text = "The LORD is my shepherd; I shall not want."),
                BibleVerse(translation = "KJV", bookName = "Philippians", chapter = 4, verseNumber = 6, text = "Be careful for nothing; but in every thing by prayer and supplication with thanksgiving let your requests be made known unto God."),
                BibleVerse(translation = "KJV", bookName = "Philippians", chapter = 4, verseNumber = 7, text = "And the peace of God, which passeth all understanding, shall keep your hearts and minds through Christ Jesus."),
                BibleVerse(translation = "KJV", bookName = "John", chapter = 14, verseNumber = 27, text = "Peace I leave with you, my peace I give unto you: not as the world giveth, give I unto you. Let not your heart be troubled, neither let it be afraid."),
                BibleVerse(translation = "KJV", bookName = "Matthew", chapter = 11, verseNumber = 28, text = "Come unto me, all ye that labour and are heavy laden, and I will give you rest."),
                BibleVerse(translation = "KJV", bookName = "Isaiah", chapter = 26, verseNumber = 3, text = "Thou wilt keep him in perfect peace, whose mind is stayed on thee: because he trusteth in thee.")
            )
            "faith & hope", "faith", "hope" -> listOf(
                BibleVerse(translation = "KJV", bookName = "Hebrews", chapter = 11, verseNumber = 1, text = "Now faith is the substance of things hoped for, the evidence of things not seen."),
                BibleVerse(translation = "KJV", bookName = "Romans", chapter = 8, verseNumber = 28, text = "And we know that all things work together for good to them that love God, to them who are the called according to his purpose."),
                BibleVerse(translation = "KJV", bookName = "Jeremiah", chapter = 29, verseNumber = 11, text = "For I know the thoughts that I think toward you, saith the LORD, thoughts of peace, and not of evil, to give you an expected end."),
                BibleVerse(translation = "KJV", bookName = "Proverbs", chapter = 3, verseNumber = 5, text = "Trust in the LORD with all thine heart; and lean not unto thine own understanding."),
                BibleVerse(translation = "KJV", bookName = "Proverbs", chapter = 3, verseNumber = 6, text = "In all thy ways acknowledge him, and he shall direct thy paths."),
                BibleVerse(translation = "KJV", bookName = "Isaiah", chapter = 40, verseNumber = 31, text = "But they that wait upon the LORD shall renew their strength; they shall mount up with wings as eagles; they shall run, and not be weary; and they shall walk, and not faint.")
            )
            "strength & courage", "strength", "courage" -> listOf(
                BibleVerse(translation = "KJV", bookName = "Joshua", chapter = 1, verseNumber = 9, text = "Have not I commanded thee? Be strong and of a good courage; be not afraid, neither be thou dismayed: for the LORD thy God is with thee whithersoever thou goest."),
                BibleVerse(translation = "KJV", bookName = "Philippians", chapter = 4, verseNumber = 13, text = "I can do all things through Christ which strengtheneth me."),
                BibleVerse(translation = "KJV", bookName = "Psalms", chapter = 46, verseNumber = 1, text = "God is our refuge and strength, a very present help in trouble."),
                BibleVerse(translation = "KJV", bookName = "2 Timothy", chapter = 1, verseNumber = 7, text = "For God hath not given us the spirit of fear; but of power, and of love, and of a sound mind."),
                BibleVerse(translation = "KJV", bookName = "Isaiah", chapter = 41, verseNumber = 10, text = "Fear thou not; for I am with thee: be not dismayed; for I am thy God: I will strengthen thee; yea, I will help thee; yea, I will uphold thee with the right hand of my righteousness.")
            )
            "love & grace", "love", "grace" -> listOf(
                BibleVerse(translation = "KJV", bookName = "John", chapter = 3, verseNumber = 16, text = "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life."),
                BibleVerse(translation = "KJV", bookName = "Romans", chapter = 5, verseNumber = 8, text = "But God commendeth his love toward us, in that, while we were yet sinners, Christ died for us."),
                BibleVerse(translation = "KJV", bookName = "1 Corinthians", chapter = 13, verseNumber = 4, text = "Charity suffereth long, and is kind; charity envieth not; charity vaunteth not itself, is not puffed up,"),
                BibleVerse(translation = "KJV", bookName = "1 John", chapter = 4, verseNumber = 19, text = "We love him, because he first loved us."),
                BibleVerse(translation = "KJV", bookName = "Ephesians", chapter = 2, verseNumber = 8, text = "For by grace are ye saved through faith; and that not of yourselves: it is the gift of God:")
            )
            "wisdom & guidance", "wisdom", "guidance" -> listOf(
                BibleVerse(translation = "KJV", bookName = "James", chapter = 1, verseNumber = 5, text = "If any of you lack wisdom, let him ask of God, that giveth to all men liberally, and upbraideth not; and it shall be given him."),
                BibleVerse(translation = "KJV", bookName = "Psalms", chapter = 119, verseNumber = 105, text = "Thy word is a lamp unto my feet, and a light unto my path."),
                BibleVerse(translation = "KJV", bookName = "Proverbs", chapter = 16, verseNumber = 3, text = "Commit thy works unto the LORD, and thy thoughts shall be established."),
                BibleVerse(translation = "KJV", bookName = "Psalms", chapter = 32, verseNumber = 8, text = "I will instruct thee and teach thee in the way which thou shalt go: I will guide thee with mine eye.")
            )
            else -> VerseOfTheDayWidgetProvider.getOfflineVerses()
        }
    }

    fun cancelPersistentNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID_PERSISTENT)
    }

    fun updatePersistentNotificationIfActive(context: Context) {
        val prefs = context.getSharedPreferences("bible_companion_prefs", Context.MODE_PRIVATE)
        val isPersistentEnabled = prefs.getBoolean("persistent_votd_enabled", false)
        if (isPersistentEnabled) {
            val verse = VerseOfTheDayWidgetProvider.getCurrentVerse(context)
            showDailyVerseNotification(context, verse, isPersistent = true)
        }
    }

    fun scheduleDailyAlarm(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailyVerseNotificationReceiver::class.java).apply {
            action = DailyVerseNotificationReceiver.ACTION_TRIGGER_DAILY_VERSE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            3001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelDailyAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DailyVerseNotificationReceiver::class.java).apply {
            action = DailyVerseNotificationReceiver.ACTION_TRIGGER_DAILY_VERSE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            3001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

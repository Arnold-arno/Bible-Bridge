package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "bible_verses_fts")
@Fts4
data class BibleVerseFts(
    @PrimaryKey @ColumnInfo(name = "rowid") val rowid: Int = 0,
    val translation: String,
    val bookName: String,
    val chapter: Int,
    val verseNumber: Int,
    val text: String
)

@Entity(tableName = "bible_chapters_fts")
@Fts4
data class BibleChapterFts(
    @PrimaryKey @ColumnInfo(name = "rowid") val rowid: Int = 0,
    val bookName: String,
    val chapterNumber: Int,
    val translation: String,
    val summary: String
)

@Entity(
    tableName = "bible_verses",
    indices = [
        Index(value = ["translation", "bookName", "chapter"]),
        Index(value = ["translation"]),
        Index(value = ["bookName"])
    ]
)
data class BibleVerse(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val translation: String, // KJV, WEB, ASV, GNT, NIV, ESV
    val bookName: String,
    val chapter: Int,
    val verseNumber: Int,
    val text: String
)

@Entity(
    tableName = "bible_books",
    indices = [
        Index(value = ["bookName"]),
        Index(value = ["bookOrder"])
    ]
)
data class BibleBookEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookName: String, // e.g. "Genesis", "Exodus", "Matthew"
    val bookOrder: Int, // 1 to 66
    val testament: String, // "Old Testament" or "New Testament"
    val category: String, // e.g. "Pentateuch", "History", "Poetry", "Prophecy", "Gospels", "Epistles", "Revelation"
    val totalChapters: Int,
    val abbreviation: String,
    val translation: String = "ALL"
)

@Entity(
    tableName = "bible_chapters",
    indices = [
        Index(value = ["translation", "bookName", "chapterNumber"])
    ]
)
data class BibleChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookName: String,
    val chapterNumber: Int,
    val totalVerses: Int,
    val translation: String = "ALL",
    val summary: String? = null
)

@Entity(tableName = "bible_translations")
data class BibleTranslationEntity(
    @PrimaryKey val code: String, // e.g. GNT, NIV, ESV, KJV, NKJV, NLT
    val name: String,
    val language: String,
    val philosophy: String,
    val verseCount: Int = 0,
    val isDownloaded: Boolean = false,
    val downloadedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_highlights")
data class UserHighlight(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val verseId: Int = 0,
    val translation: String,
    val bookName: String,
    val chapter: Int,
    val verseNumber: Int,
    val text: String,
    val colorHex: String = "#FFF59D",
    val note: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val userEmail: String? = null
)

@Entity(tableName = "favorite_verses")
data class FavoriteVerse(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val verseId: Int,
    val translation: String,
    val bookName: String,
    val chapter: Int,
    val verseNumber: Int,
    val text: String,
    val colorHex: String, // e.g., "#FFF59D" (Yellow), "#A5D6A7" (Green), "#90CAF9" (Blue)
    val timestamp: Long = System.currentTimeMillis(),
    val userEmail: String? = null
)

@Entity(tableName = "bookmark_verses")
data class BookmarkedVerse(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val verseId: Int,
    val translation: String,
    val bookName: String,
    val chapter: Int,
    val verseNumber: Int,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val userEmail: String? = null
)

@Entity(
    tableName = "reading_history",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["bookName", "chapter"])
    ]
)
data class ReadingHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookName: String,
    val chapter: Int,
    val verseNumber: Int? = null,
    val verseText: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val userEmail: String? = null
)

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey val email: String,
    val name: String,
    val passwordHash: String,
    val profilePic: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val securityQuestion: String = "",
    val securityAnswerHash: String = "",
    val backupRecoveryCode: String = "",
    val phone: String = "",
    val linkedProviders: String = "Email",
    val failedLoginAttempts: Int = 0,
    val lockoutUntil: Long = 0L,
    val twoFactorEnabled: Boolean = false,
    val twoFactorPin: String = "",
    val lastLoginAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "devotionals")
data class Devotional(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String, // "Monday", "Tuesday", etc. or a specific date
    val scripture: String,
    val content: String,
    val prayer: String,
    val isCustom: Boolean = false, // True if generated via Gemini
    val timestamp: Long = System.currentTimeMillis()
)

// Data class representing a timeline event
data class TimelineEvent(
    val title: String,
    val period: String, // e.g., "c. 4000 BC"
    val description: String,
    val scriptureRef: String,
    val iconName: String // To show matching icons in UI
)

// Data class representing a book overview summary
data class BookOverview(
    val name: String,
    val category: String, // e.g., "Pentateuch", "Gospels", "Epistles"
    val author: String,
    val dateWritten: String,
    val theme: String,
    val keyVerse: String,
    val summary: String,
    val characters: String = "",
    val lessons: String = "",
    val majorScenes: String = "",
    val keyCharacters: List<String> = emptyList(),
    val centralLessons: List<String> = emptyList()
)

@Entity(
    tableName = "recent_searches",
    indices = [
        Index(value = ["query"], unique = true),
        Index(value = ["timestamp"])
    ]
)
data class RecentSearch(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)


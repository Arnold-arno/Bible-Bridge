package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BibleDao {
    // --- Bible Books ---
    @Query("SELECT * FROM bible_books ORDER BY bookOrder ASC")
    fun getAllBookEntities(): Flow<List<BibleBookEntity>>

    @Query("SELECT * FROM bible_books WHERE testament = :testament ORDER BY bookOrder ASC")
    fun getBooksByTestament(testament: String): Flow<List<BibleBookEntity>>

    @Query("SELECT * FROM bible_books WHERE bookName = :bookName LIMIT 1")
    suspend fun getBookByName(bookName: String): BibleBookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BibleBookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBooks(books: List<BibleBookEntity>)

    @Query("SELECT COUNT(*) FROM bible_books")
    suspend fun getBookCount(): Int

    // --- Bible Chapters ---
    @Query("SELECT * FROM bible_chapters WHERE bookName = :bookName AND (translation = :translation OR translation = 'ALL') ORDER BY chapterNumber ASC")
    fun getChaptersForBookEntity(bookName: String, translation: String = "ALL"): Flow<List<BibleChapterEntity>>

    @Query("SELECT * FROM bible_chapters WHERE bookName = :bookName AND chapterNumber = :chapterNumber AND (translation = :translation OR translation = 'ALL') LIMIT 1")
    suspend fun getChapter(bookName: String, chapterNumber: Int, translation: String = "ALL"): BibleChapterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: BibleChapterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllChapters(chapters: List<BibleChapterEntity>)

    @Query("SELECT COUNT(*) FROM bible_chapters")
    suspend fun getChapterCount(): Int

    @Query("DELETE FROM bible_chapters WHERE translation = :translation")
    suspend fun deleteChaptersByTranslation(translation: String)

    // --- Bible Verses ---
    @Query("SELECT * FROM bible_verses WHERE translation = :translation AND bookName = :bookName AND chapter = :chapter ORDER BY verseNumber ASC")
    fun getVerses(translation: String, bookName: String, chapter: Int): Flow<List<BibleVerse>>

    @Query("SELECT * FROM bible_verses WHERE bookName = :bookName AND chapter = :chapter AND verseNumber = :verseNumber")
    fun getVerseAcrossTranslations(bookName: String, chapter: Int, verseNumber: Int): Flow<List<BibleVerse>>

    @Query("SELECT DISTINCT bookName FROM bible_verses ORDER BY id ASC")
    fun getAvailableBooks(): Flow<List<String>>

    @Query("SELECT DISTINCT chapter FROM bible_verses WHERE bookName = :bookName ORDER BY chapter ASC")
    fun getChaptersForBook(bookName: String): Flow<List<Int>>

    @Query("SELECT * FROM bible_verses WHERE text LIKE '%' || :query || '%' OR bookName LIKE '%' || :query || '%' ORDER BY CASE WHEN bookName LIKE :query || '%' THEN 0 ELSE 1 END, id ASC LIMIT 150")
    suspend fun searchVerses(query: String): List<BibleVerse>

    @Query("SELECT * FROM bible_verses WHERE (LOWER(bookName) LIKE LOWER(:bookName) || '%') AND (:chapter = 0 OR chapter = :chapter) AND (:verse = 0 OR verseNumber = :verse) ORDER BY id ASC")
    suspend fun searchPassage(bookName: String, chapter: Int = 0, verse: Int = 0): List<BibleVerse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllVerses(verses: List<BibleVerse>)

    @Query("DELETE FROM bible_verses WHERE translation = :translation")
    suspend fun deleteVersesByTranslation(translation: String)

    @Query("SELECT COUNT(*) FROM bible_verses WHERE translation = :translation")
    suspend fun getVerseCountForTranslation(translation: String): Int

    @Query("SELECT COUNT(*) FROM bible_verses")
    suspend fun getVerseCount(): Int

    // --- FTS4 Search Operations ---
    @Query("SELECT * FROM bible_verses WHERE id IN (SELECT rowid FROM bible_verses_fts WHERE bible_verses_fts MATCH :query) ORDER BY id ASC LIMIT 150")
    suspend fun searchVersesFts(query: String): List<BibleVerse>

    @Query("SELECT * FROM bible_verses WHERE translation = :translation AND id IN (SELECT rowid FROM bible_verses_fts WHERE bible_verses_fts MATCH :query) ORDER BY id ASC LIMIT 150")
    suspend fun searchVersesFtsByTranslation(query: String, translation: String): List<BibleVerse>

    @Query("SELECT * FROM bible_chapters WHERE id IN (SELECT rowid FROM bible_chapters_fts WHERE bible_chapters_fts MATCH :query) ORDER BY id ASC")
    suspend fun searchChaptersFts(query: String): List<BibleChapterEntity>

    @Query("SELECT * FROM bible_verses")
    suspend fun getAllVersesSync(): List<BibleVerse>

    @Query("SELECT * FROM bible_verses WHERE translation = :translation")
    suspend fun getVersesForTranslationSync(translation: String): List<BibleVerse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllVersesFts(ftsVerses: List<BibleVerseFts>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllChaptersFts(ftsChapters: List<BibleChapterFts>)

    @Query("DELETE FROM bible_verses_fts WHERE translation = :translation")
    suspend fun deleteVersesFtsByTranslation(translation: String)

    @Query("SELECT COUNT(*) FROM bible_verses_fts")
    suspend fun getFtsVerseCount(): Int

    // --- Favorites & Highlights ---
    @Query("SELECT * FROM favorite_verses ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteVerse>>

    @Query("SELECT * FROM favorite_verses WHERE translation = :translation AND bookName = :bookName AND chapter = :chapter AND verseNumber = :verseNumber AND (userEmail = :userEmail OR (userEmail IS NULL AND :userEmail IS NULL)) LIMIT 1")
    suspend fun getFavorite(translation: String, bookName: String, chapter: Int, verseNumber: Int, userEmail: String?): FavoriteVerse?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteVerse)

    @Query("DELETE FROM favorite_verses WHERE translation = :translation AND bookName = :bookName AND chapter = :chapter AND verseNumber = :verseNumber AND (userEmail = :userEmail OR (userEmail IS NULL AND :userEmail IS NULL))")
    suspend fun removeFavorite(translation: String, bookName: String, chapter: Int, verseNumber: Int, userEmail: String?)

    @Query("DELETE FROM favorite_verses WHERE id = :id")
    suspend fun deleteFavoriteById(id: Int)

    // --- Bookmarks ---
    @Query("SELECT * FROM bookmark_verses ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkedVerse>>

    @Query("SELECT * FROM bookmark_verses WHERE translation = :translation AND bookName = :bookName AND chapter = :chapter AND verseNumber = :verseNumber AND (userEmail = :userEmail OR (userEmail IS NULL AND :userEmail IS NULL)) LIMIT 1")
    suspend fun getBookmark(translation: String, bookName: String, chapter: Int, verseNumber: Int, userEmail: String?): BookmarkedVerse?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkedVerse)

    @Query("DELETE FROM bookmark_verses WHERE translation = :translation AND bookName = :bookName AND chapter = :chapter AND verseNumber = :verseNumber AND (userEmail = :userEmail OR (userEmail IS NULL AND :userEmail IS NULL))")
    suspend fun removeBookmark(translation: String, bookName: String, chapter: Int, verseNumber: Int, userEmail: String?)

    @Query("DELETE FROM bookmark_verses WHERE id = :id")
    suspend fun deleteBookmarkById(id: Int)

    // --- History Tracking ---
    @Query("SELECT * FROM reading_history ORDER BY timestamp DESC LIMIT 50")
    fun getReadingHistory(): Flow<List<ReadingHistory>>

    @Query("SELECT * FROM reading_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentlyViewed(limit: Int = 20): Flow<List<ReadingHistory>>

    @Query("SELECT * FROM reading_history WHERE userEmail = :email OR (userEmail IS NULL AND :email IS NULL) ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentlyViewedForUser(email: String?, limit: Int = 20): Flow<List<ReadingHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingHistory(history: ReadingHistory)

    @Query("DELETE FROM reading_history")
    suspend fun clearHistory()

    // --- Recent Searches ---
    @Query("SELECT * FROM recent_searches ORDER BY timestamp DESC LIMIT 50")
    fun getRecentSearches(): Flow<List<RecentSearch>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentSearch(recentSearch: RecentSearch)

    @Query("DELETE FROM recent_searches WHERE query = :query")
    suspend fun deleteRecentSearch(query: String)

    @Query("DELETE FROM recent_searches")
    suspend fun clearRecentSearches()

    // --- Devotionals ---
    @Query("SELECT * FROM devotionals ORDER BY timestamp DESC")
    fun getAllDevotionals(): Flow<List<Devotional>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevotional(devotional: Devotional)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDevotionals(devotionals: List<Devotional>)

    @Query("SELECT COUNT(*) FROM devotionals")
    suspend fun getDevotionalCount(): Int

    // --- User Accounts ---
    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getUserAccount(email: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(user: UserAccount)

    @Query("SELECT * FROM favorite_verses WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getFavoritesForUser(email: String): Flow<List<FavoriteVerse>>

    @Query("SELECT * FROM favorite_verses WHERE userEmail IS NULL ORDER BY timestamp DESC")
    fun getFavoritesForGuest(): Flow<List<FavoriteVerse>>

    @Query("SELECT * FROM bookmark_verses WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getBookmarksForUser(email: String): Flow<List<BookmarkedVerse>>

    @Query("SELECT * FROM bookmark_verses WHERE userEmail IS NULL ORDER BY timestamp DESC")
    fun getBookmarksForGuest(): Flow<List<BookmarkedVerse>>

    @Query("SELECT * FROM reading_history WHERE userEmail = :email ORDER BY timestamp DESC LIMIT 50")
    fun getReadingHistoryForUser(email: String): Flow<List<ReadingHistory>>

    @Query("SELECT * FROM reading_history WHERE userEmail IS NULL ORDER BY timestamp DESC LIMIT 50")
    fun getReadingHistoryForGuest(): Flow<List<ReadingHistory>>

    @Query("SELECT * FROM reading_history WHERE userEmail = :email")
    fun getAllReadingHistoryForUser(email: String): Flow<List<ReadingHistory>>

    @Query("SELECT * FROM reading_history WHERE userEmail IS NULL")
    fun getAllReadingHistoryForGuest(): Flow<List<ReadingHistory>>

    @Query("DELETE FROM reading_history WHERE bookName = :bookName AND chapter = :chapter AND userEmail = :email")
    suspend fun deleteReadingHistoryForUser(bookName: String, chapter: Int, email: String)

    @Query("DELETE FROM reading_history WHERE bookName = :bookName AND chapter = :chapter AND userEmail IS NULL")
    suspend fun deleteReadingHistoryForGuest(bookName: String, chapter: Int)

    // --- Guest Data Migration ---
    @Query("UPDATE favorite_verses SET userEmail = :email WHERE userEmail IS NULL OR userEmail = ''")
    suspend fun migrateGuestFavoritesToUser(email: String)

    @Query("UPDATE bookmark_verses SET userEmail = :email WHERE userEmail IS NULL OR userEmail = ''")
    suspend fun migrateGuestBookmarksToUser(email: String)

    @Query("UPDATE reading_history SET userEmail = :email WHERE userEmail IS NULL OR userEmail = ''")
    suspend fun migrateGuestHistoryToUser(email: String)

    // --- Bible Translations ---
    @Query("SELECT * FROM bible_translations ORDER BY code ASC")
    fun getAllTranslations(): Flow<List<BibleTranslationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslation(translation: BibleTranslationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTranslations(translations: List<BibleTranslationEntity>)

    @Query("UPDATE bible_translations SET isDownloaded = :isDownloaded, verseCount = :verseCount WHERE code = :code")
    suspend fun updateTranslationStatus(code: String, isDownloaded: Boolean, verseCount: Int)

    // --- User Highlights ---
    @Query("SELECT * FROM user_highlights ORDER BY timestamp DESC")
    fun getAllHighlights(): Flow<List<UserHighlight>>

    @Query("SELECT * FROM user_highlights WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getHighlightsForUser(email: String): Flow<List<UserHighlight>>

    @Query("SELECT * FROM user_highlights WHERE userEmail IS NULL ORDER BY timestamp DESC")
    fun getHighlightsForGuest(): Flow<List<UserHighlight>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: UserHighlight)

    @Query("DELETE FROM user_highlights WHERE id = :id")
    suspend fun deleteHighlightById(id: Int)

    @Query("DELETE FROM user_highlights WHERE translation = :translation AND bookName = :bookName AND chapter = :chapter AND verseNumber = :verseNumber")
    suspend fun removeHighlight(translation: String, bookName: String, chapter: Int, verseNumber: Int)
}

@Database(
    entities = [
        BibleVerse::class,
        BibleVerseFts::class,
        BibleBookEntity::class,
        BibleChapterEntity::class,
        BibleChapterFts::class,
        BibleTranslationEntity::class,
        UserHighlight::class,
        FavoriteVerse::class,
        BookmarkedVerse::class,
        ReadingHistory::class,
        Devotional::class,
        UserAccount::class,
        RecentSearch::class
    ],
    version = 10,
    exportSchema = false
)
abstract class BibleDatabase : RoomDatabase() {
    abstract fun bibleDao(): BibleDao

    companion object {
        @Volatile
        private var INSTANCE: BibleDatabase? = null

        fun getDatabase(context: Context): BibleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BibleDatabase::class.java,
                    "bible_companion_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}

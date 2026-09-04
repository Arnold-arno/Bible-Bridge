package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BibleRepository(private val bibleDao: BibleDao) {

    val availableBooks: Flow<List<String>> = bibleDao.getAvailableBooks()

    val allBookEntities: Flow<List<BibleBookEntity>> = bibleDao.getAllBookEntities()

    val allFavorites: Flow<List<FavoriteVerse>> = bibleDao.getAllFavorites()

    val readingHistory: Flow<List<ReadingHistory>> = bibleDao.getReadingHistory()

    val allDevotionals: Flow<List<Devotional>> = bibleDao.getAllDevotionals()

    fun getBooksByTestament(testament: String): Flow<List<BibleBookEntity>> {
        return bibleDao.getBooksByTestament(testament)
    }

    suspend fun getBookByName(bookName: String): BibleBookEntity? = withContext(Dispatchers.IO) {
        bibleDao.getBookByName(bookName)
    }

    fun getChaptersForBook(bookName: String): Flow<List<Int>> {
        return bibleDao.getChaptersForBook(bookName)
    }

    fun getChaptersForBookEntity(bookName: String, translation: String = "ALL"): Flow<List<BibleChapterEntity>> {
        return bibleDao.getChaptersForBookEntity(bookName, translation)
    }

    suspend fun getChapter(bookName: String, chapterNumber: Int, translation: String = "ALL"): BibleChapterEntity? = withContext(Dispatchers.IO) {
        bibleDao.getChapter(bookName, chapterNumber, translation)
    }

    fun getVerses(translation: String, bookName: String, chapter: Int): Flow<List<BibleVerse>> {
        return bibleDao.getVerses(translation, bookName, chapter)
    }

    fun getVerseAcrossTranslations(bookName: String, chapter: Int, verseNumber: Int): Flow<List<BibleVerse>> {
        return bibleDao.getVerseAcrossTranslations(bookName, chapter, verseNumber)
    }

    suspend fun searchVerses(query: String): List<BibleVerse> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        // Format query terms for FTS search
        val cleanQuery = query.trim().replace(Regex("[^a-zA-Z0-9\\s]"), "")
        val ftsQuery = cleanQuery.split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .joinToString(" ") { "$it*" }
        
        if (ftsQuery.isBlank()) return@withContext bibleDao.searchVerses(query)
        
        try {
            val results = bibleDao.searchVersesFts(ftsQuery)
            if (results.isNotEmpty()) results else bibleDao.searchVerses(query)
        } catch (e: Exception) {
            bibleDao.searchVerses(query)
        }
    }

    suspend fun searchVersesFts(query: String, translation: String? = null): List<BibleVerse> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val cleanQuery = query.trim().replace(Regex("[^a-zA-Z0-9\\s]"), "")
        val ftsQuery = cleanQuery.split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .joinToString(" ") { "$it*" }
        
        if (ftsQuery.isBlank()) return@withContext bibleDao.searchVerses(query)
        
        try {
            if (translation != null) {
                val results = bibleDao.searchVersesFtsByTranslation(ftsQuery, translation)
                if (results.isNotEmpty()) results else bibleDao.searchVerses(query)
            } else {
                val results = bibleDao.searchVersesFts(ftsQuery)
                if (results.isNotEmpty()) results else bibleDao.searchVerses(query)
            }
        } catch (e: Exception) {
            bibleDao.searchVerses(query)
        }
    }

    suspend fun searchChaptersFts(query: String): List<BibleChapterEntity> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val cleanQuery = query.trim().replace(Regex("[^a-zA-Z0-9\\s]"), "")
        val ftsQuery = cleanQuery.split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .joinToString(" ") { "$it*" }
        if (ftsQuery.isBlank()) return@withContext emptyList()
        try {
            bibleDao.searchChaptersFts(ftsQuery)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchPassage(bookName: String, chapter: Int = 0, verse: Int = 0): List<BibleVerse> = withContext(Dispatchers.IO) {
        if (bookName.isBlank()) emptyList() else bibleDao.searchPassage(bookName, chapter, verse)
    }

    suspend fun getFavorite(translation: String, bookName: String, chapter: Int, verseNumber: Int, userEmail: String?): FavoriteVerse? = withContext(Dispatchers.IO) {
        bibleDao.getFavorite(translation, bookName, chapter, verseNumber, userEmail)
    }

    suspend fun getBookmark(translation: String, bookName: String, chapter: Int, verseNumber: Int, userEmail: String?): BookmarkedVerse? = withContext(Dispatchers.IO) {
        bibleDao.getBookmark(translation, bookName, chapter, verseNumber, userEmail)
    }

    fun getFavoritesForUser(email: String?): Flow<List<FavoriteVerse>> {
        return if (email.isNullOrBlank()) {
            bibleDao.getFavoritesForGuest()
        } else {
            bibleDao.getFavoritesForUser(email)
        }
    }

    fun getBookmarksForUser(email: String?): Flow<List<BookmarkedVerse>> {
        return if (email.isNullOrBlank()) {
            bibleDao.getBookmarksForGuest()
        } else {
            bibleDao.getBookmarksForUser(email)
        }
    }

    fun getReadingHistoryForUser(email: String?): Flow<List<ReadingHistory>> {
        return if (email.isNullOrBlank()) {
            bibleDao.getReadingHistoryForGuest()
        } else {
            bibleDao.getReadingHistoryForUser(email)
        }
    }

    fun getRecentlyViewed(limit: Int = 20): Flow<List<ReadingHistory>> {
        return bibleDao.getRecentlyViewed(limit)
    }

    fun getRecentlyViewedForUser(email: String?, limit: Int = 20): Flow<List<ReadingHistory>> {
        return bibleDao.getRecentlyViewedForUser(email, limit)
    }

    // --- Recent Searches ---
    val recentSearchesFlow: Flow<List<RecentSearch>> = bibleDao.getRecentSearches()

    suspend fun saveRecentSearch(query: String) = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isNotBlank()) {
            bibleDao.deleteRecentSearch(trimmed)
            bibleDao.insertRecentSearch(RecentSearch(query = trimmed, timestamp = System.currentTimeMillis()))
        }
    }

    suspend fun deleteRecentSearch(query: String) = withContext(Dispatchers.IO) {
        bibleDao.deleteRecentSearch(query)
    }

    suspend fun clearRecentSearches() = withContext(Dispatchers.IO) {
        bibleDao.clearRecentSearches()
    }

    fun getAllReadingHistoryForUser(email: String?): Flow<List<ReadingHistory>> {
        return if (email.isNullOrBlank()) {
            bibleDao.getAllReadingHistoryForGuest()
        } else {
            bibleDao.getAllReadingHistoryForUser(email)
        }
    }

    suspend fun getUserAccount(email: String): UserAccount? = withContext(Dispatchers.IO) {
        if (email.isBlank()) null else bibleDao.getUserAccount(email)
    }

    suspend fun createUserAccount(user: UserAccount) = withContext(Dispatchers.IO) {
        bibleDao.insertUserAccount(user)
    }

    suspend fun addFavorite(favorite: FavoriteVerse) = withContext(Dispatchers.IO) {
        bibleDao.insertFavorite(favorite)
    }

    suspend fun addBookmark(bookmark: BookmarkedVerse) = withContext(Dispatchers.IO) {
        bibleDao.insertBookmark(bookmark)
    }

    suspend fun removeFavorite(translation: String, bookName: String, chapter: Int, verseNumber: Int, userEmail: String?) = withContext(Dispatchers.IO) {
        bibleDao.removeFavorite(translation, bookName, chapter, verseNumber, userEmail)
    }

    suspend fun removeBookmark(translation: String, bookName: String, chapter: Int, verseNumber: Int, userEmail: String?) = withContext(Dispatchers.IO) {
        bibleDao.removeBookmark(translation, bookName, chapter, verseNumber, userEmail)
    }

    suspend fun deleteFavoriteById(id: Int) = withContext(Dispatchers.IO) {
        bibleDao.deleteFavoriteById(id)
    }

    suspend fun deleteBookmarkById(id: Int) = withContext(Dispatchers.IO) {
        bibleDao.deleteBookmarkById(id)
    }

    suspend fun addReadingHistory(bookName: String, chapter: Int, verseNumber: Int? = null, verseText: String? = null, userEmail: String? = null) = withContext(Dispatchers.IO) {
        // Log recently viewed chapter or verse
        val history = ReadingHistory(
            bookName = bookName,
            chapter = chapter,
            verseNumber = verseNumber,
            verseText = verseText,
            timestamp = System.currentTimeMillis(),
            userEmail = userEmail
        )
        bibleDao.insertReadingHistory(history)
    }

    suspend fun deleteReadingHistory(bookName: String, chapter: Int, userEmail: String?) = withContext(Dispatchers.IO) {
        if (userEmail.isNullOrBlank()) {
            bibleDao.deleteReadingHistoryForGuest(bookName, chapter)
        } else {
            bibleDao.deleteReadingHistoryForUser(bookName, chapter, userEmail)
        }
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        bibleDao.clearHistory()
    }

    suspend fun addDevotional(devotional: Devotional) = withContext(Dispatchers.IO) {
        bibleDao.insertDevotional(devotional)
    }

    suspend fun addDownloadedVerses(verses: List<BibleVerse>) = withContext(Dispatchers.IO) {
        bibleDao.insertAllVerses(verses)
        val translation = verses.firstOrNull()?.translation ?: ""
        if (translation.isNotBlank()) {
            val insertedVerses = bibleDao.getVersesForTranslationSync(translation)
            val ftsVerses = insertedVerses.map {
                BibleVerseFts(
                    rowid = it.id,
                    translation = it.translation,
                    bookName = it.bookName,
                    chapter = it.chapter,
                    verseNumber = it.verseNumber,
                    text = it.text
                )
            }
            bibleDao.insertAllVersesFts(ftsVerses)
        }
    }

    suspend fun deleteDownloadedVerses(translation: String) = withContext(Dispatchers.IO) {
        bibleDao.deleteVersesByTranslation(translation)
        bibleDao.deleteVersesFtsByTranslation(translation)
    }

    suspend fun getVerseCountForTranslation(translation: String): Int = withContext(Dispatchers.IO) {
        bibleDao.getVerseCountForTranslation(translation)
    }

    // Runs on initial start to populate Bible and devotions if they are empty
    suspend fun ensureDatabaseSeeded() = withContext(Dispatchers.IO) {
        val bookCount = bibleDao.getBookCount()
        if (bookCount == 0) {
            val books = BibleData.getSeedBooks()
            bibleDao.insertAllBooks(books)
        }

        val chapterCount = bibleDao.getChapterCount()
        if (chapterCount == 0) {
            val chapters = BibleData.getSeedChapters()
            bibleDao.insertAllChapters(chapters)
        }

        val verseCount = bibleDao.getVerseCount()
        if (verseCount == 0) {
            val verses = BibleData.getSeedVerses()
            bibleDao.insertAllVerses(verses)
        }

        // Seed FTS tables if empty
        val ftsCount = bibleDao.getFtsVerseCount()
        if (ftsCount == 0) {
            val allVerses = bibleDao.getAllVersesSync()
            val ftsVerses = allVerses.map {
                BibleVerseFts(
                    rowid = it.id,
                    translation = it.translation,
                    bookName = it.bookName,
                    chapter = it.chapter,
                    verseNumber = it.verseNumber,
                    text = it.text
                )
            }
            bibleDao.insertAllVersesFts(ftsVerses)
        }

        val devotionalCount = bibleDao.getDevotionalCount()
        if (devotionalCount == 0) {
            val devotionals = DevotionalData.getSeedDevotionals()
            bibleDao.insertAllDevotionals(devotionals)
        }

        if (bibleDao.getUserAccount("reader@example.com") == null) {
            bibleDao.insertUserAccount(
                UserAccount(
                    email = "reader@example.com",
                    name = "Faithful Reader",
                    passwordHash = "password123",
                    profilePic = "img_avatar_cross"
                )
            )
        }
    }

    suspend fun migrateGuestDataToUser(email: String) = withContext(Dispatchers.IO) {
        if (email.isNotBlank()) {
            bibleDao.migrateGuestFavoritesToUser(email)
            bibleDao.migrateGuestBookmarksToUser(email)
            bibleDao.migrateGuestHistoryToUser(email)
        }
    }
}

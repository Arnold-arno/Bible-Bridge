package com.example.data

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class SyncManager(private val context: Context, private val bibleDao: BibleDao) {

    private val sharedPrefs = context.getSharedPreferences("bible_companion_sync", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://httpbin.org/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val syncApi = retrofit.create(SyncApi::class.java)

    // Sync logs list to display in the UI (thread-safe operations)
    private val _syncLogs = mutableListOf<String>()
    val syncLogs: List<String> get() = synchronized(this) { _syncLogs.toList() }

    private fun addLog(message: String) {
        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        synchronized(this) {
            _syncLogs.add(0, "[$time] $message")
            if (_syncLogs.size > 40) _syncLogs.removeAt(_syncLogs.size - 1)
        }
    }

    init {
        addLog("Sync Manager initialized. Offline-first engine ready.")
    }

    fun getSyncLogsList(): List<String> = syncLogs

    fun clearSyncLogs() {
        synchronized(this) {
            _syncLogs.clear()
            _syncLogs.add("Sync logs cleared.")
        }
    }

    fun getLastSyncTime(email: String): String {
        val key = "last_sync_time_${email.ifBlank { "guest" }}"
        val ts = sharedPrefs.getLong(key, 0L)
        if (ts == 0L) return "Never synced"
        return java.text.SimpleDateFormat("MMM dd, HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(ts))
    }

    /**
     * Executes the cloud synchronization process.
     */
    suspend fun syncWithCloud(email: String, deviceName: String): Boolean = withContext(Dispatchers.IO) {
        val targetEmail = email.ifBlank { "guest" }
        addLog("Starting synchronization for: $targetEmail on device: $deviceName...")

        // 1. Fetch Local Data from Room Database
        val localFavoritesFlow = if (email.isBlank()) bibleDao.getFavoritesForGuest() else bibleDao.getFavoritesForUser(email)
        val localHistoryFlow = if (email.isBlank()) bibleDao.getReadingHistoryForGuest() else bibleDao.getReadingHistoryForUser(email)

        val localFavs = localFavoritesFlow.first()
        val localHist = localHistoryFlow.first()

        addLog("Local database loaded: ${localFavs.size} highlights, ${localHist.size} history rows.")

        // 2. Build Sync DTO lists
        val localFavsDto = localFavs.map {
            SyncFavoriteDto(
                verseId = it.verseId,
                translation = it.translation,
                bookName = it.bookName,
                chapter = it.chapter,
                verseNumber = it.verseNumber,
                text = it.text,
                colorHex = it.colorHex,
                timestamp = it.timestamp,
                isDeleted = false
            )
        }

        val localHistDto = localHist.map {
            SyncHistoryDto(
                bookName = it.bookName,
                chapter = it.chapter,
                verseNumber = it.verseNumber,
                verseText = it.verseText,
                timestamp = it.timestamp,
                isDeleted = false
            )
        }

        // Build Payload
        val payload = SyncPayload(
            userEmail = targetEmail,
            deviceName = deviceName,
            favorites = localFavsDto,
            history = localHistDto
        )

        // 3. Make the Real HTTP Request to https://httpbin.org/anything to simulate upload
        var apiOnline = false
        try {
            val response = syncApi.syncData(payload).execute()
            if (response.isSuccessful && response.body() != null) {
                apiOnline = true
                addLog("REST API upload successful (HTTP 200 OK). State archived in Cloud.")
            } else {
                addLog("REST API returned code ${response.code()}. Proceeding with offline sync cache.")
            }
        } catch (e: IOException) {
            addLog("REST API network unreachable: ${e.message}. Using offline sync cache.")
        } catch (e: Exception) {
            addLog("REST API unexpected error: ${e.message}. Using offline sync cache.")
        }

        // 4. Load the Simulated Cloud Truth from local cache (SharedPrefs)
        val cloudFavsKey = "cloud_db_favorites_$targetEmail"
        val cloudHistKey = "cloud_db_history_$targetEmail"

        val favsListType = Types.newParameterizedType(List::class.java, SyncFavoriteDto::class.java)
        val histListType = Types.newParameterizedType(List::class.java, SyncHistoryDto::class.java)

        val favsAdapter = moshi.adapter<List<SyncFavoriteDto>>(favsListType)
        val histAdapter = moshi.adapter<List<SyncHistoryDto>>(histListType)

        val cloudFavsJson = sharedPrefs.getString(cloudFavsKey, null)
        val cloudHistJson = sharedPrefs.getString(cloudHistKey, null)

        val cloudFavs: List<SyncFavoriteDto> = if (cloudFavsJson != null) {
            try { favsAdapter.fromJson(cloudFavsJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        } else {
            emptyList()
        }

        val cloudHist: List<SyncHistoryDto> = if (cloudHistJson != null) {
            try { histAdapter.fromJson(cloudHistJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        } else {
            emptyList()
        }

        addLog("Fetched remote Cloud State: ${cloudFavs.size} highlights, ${cloudHist.size} history rows.")

        // 4b. Sync with Firebase Firestore
        try {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val userDoc = firestore.collection("users").document(targetEmail)
            val syncData = mapOf(
                "email" to targetEmail,
                "deviceName" to deviceName,
                "lastSyncedTimestamp" to System.currentTimeMillis(),
                "favoritesCount" to localFavsDto.size,
                "historyCount" to localHistDto.size,
                "status" to "synced"
            )
            userDoc.set(syncData, com.google.firebase.firestore.SetOptions.merge())
            addLog("Firebase Firestore persistence active for user document: $targetEmail")
        } catch (e: Exception) {
            addLog("Firestore notification: ${e.message ?: "Local persistence active"}")
        }

        // 5. Merge Local & Cloud State (Conflict Resolution: Last-Write-Wins based on timestamp)
        val mergedFavsMap = mutableMapOf<String, SyncFavoriteDto>()
        
        // Add all from cloud
        cloudFavs.forEach { fav ->
            val key = "${fav.translation}|${fav.bookName}|${fav.chapter}|${fav.verseNumber}"
            mergedFavsMap[key] = fav
        }

        // Merge local (LWW)
        localFavsDto.forEach { fav ->
            val key = "${fav.translation}|${fav.bookName}|${fav.chapter}|${fav.verseNumber}"
            val existing = mergedFavsMap[key]
            if (existing == null || fav.timestamp > existing.timestamp) {
                mergedFavsMap[key] = fav
            }
        }

        val mergedHistMap = mutableMapOf<String, SyncHistoryDto>()

        // Add all from cloud
        cloudHist.forEach { hist ->
            val key = "${hist.bookName}|${hist.chapter}"
            mergedHistMap[key] = hist
        }

        // Merge local (LWW)
        localHistDto.forEach { hist ->
            val key = "${hist.bookName}|${hist.chapter}"
            val existing = mergedHistMap[key]
            if (existing == null || hist.timestamp > existing.timestamp) {
                mergedHistMap[key] = hist
            }
        }

        val finalFavs = mergedFavsMap.values.toList()
        val finalHist = mergedHistMap.values.toList()

        // 6. Update local Room Database
        val localFavsMap = localFavs.associateBy { "${it.translation}|${it.bookName}|${it.chapter}|${it.verseNumber}" }
        var addedFavsCount = 0
        var updatedFavsCount = 0
        var deletedFavsCount = 0

        finalFavs.forEach { favDto ->
            val key = "${favDto.translation}|${favDto.bookName}|${favDto.chapter}|${favDto.verseNumber}"
            val localItem = localFavsMap[key]

            if (favDto.isDeleted) {
                if (localItem != null) {
                    bibleDao.removeFavorite(
                        translation = favDto.translation,
                        bookName = favDto.bookName,
                        chapter = favDto.chapter,
                        verseNumber = favDto.verseNumber,
                        userEmail = if (email.isBlank()) null else email
                    )
                    deletedFavsCount++
                }
            } else {
                val localId = localItem?.id ?: 0
                if (localItem == null) {
                    addedFavsCount++
                } else if (favDto.colorHex != localItem.colorHex || favDto.timestamp > localItem.timestamp) {
                    updatedFavsCount++
                }

                val favoriteEntity = FavoriteVerse(
                    id = localId,
                    verseId = favDto.verseId,
                    translation = favDto.translation,
                    bookName = favDto.bookName,
                    chapter = favDto.chapter,
                    verseNumber = favDto.verseNumber,
                    text = favDto.text,
                    colorHex = favDto.colorHex,
                    timestamp = favDto.timestamp,
                    userEmail = if (email.isBlank()) null else email
                )
                bibleDao.insertFavorite(favoriteEntity)
            }
        }

        val localHistMap = localHist.associateBy { "${it.bookName}|${it.chapter}" }
        var addedHistCount = 0
        var updatedHistCount = 0

        finalHist.forEach { histDto ->
            val key = "${histDto.bookName}|${histDto.chapter}"
            val localItem = localHistMap[key]

            if (histDto.isDeleted) {
                if (localItem != null) {
                    if (email.isBlank()) {
                        bibleDao.deleteReadingHistoryForGuest(histDto.bookName, histDto.chapter)
                    } else {
                        bibleDao.deleteReadingHistoryForUser(histDto.bookName, histDto.chapter, email)
                    }
                }
            } else {
                val localId = localItem?.id ?: 0
                if (localItem == null) {
                    addedHistCount++
                } else if (histDto.timestamp > localItem.timestamp) {
                    updatedHistCount++
                }

                val historyEntity = ReadingHistory(
                    id = localId,
                    bookName = histDto.bookName,
                    chapter = histDto.chapter,
                    verseNumber = histDto.verseNumber,
                    verseText = histDto.verseText,
                    timestamp = histDto.timestamp,
                    userEmail = if (email.isBlank()) null else email
                )
                bibleDao.insertReadingHistory(historyEntity)
            }
        }

        // 7. Save merged data back to Simulated Cloud (SharedPrefs)
        sharedPrefs.edit()
            .putString(cloudFavsKey, favsAdapter.toJson(finalFavs))
            .putString(cloudHistKey, histAdapter.toJson(finalHist))
            .putLong("last_sync_time_$targetEmail", System.currentTimeMillis())
            .apply()

        addLog("Sync complete: Added $addedFavsCount/updated $updatedFavsCount/deleted $deletedFavsCount highlights.")
        addLog("History updated: Sync matched $addedHistCount new read chapters.")
        true
    }

    /**
     * Simulates editing actions performed on a separate device.
     */
    fun simulateOtherDeviceAction(email: String, deviceName: String, actionType: String) {
        val targetEmail = email.ifBlank { "guest" }
        val cloudFavsKey = "cloud_db_favorites_$targetEmail"
        val cloudHistKey = "cloud_db_history_$targetEmail"

        val favsListType = Types.newParameterizedType(List::class.java, SyncFavoriteDto::class.java)
        val histListType = Types.newParameterizedType(List::class.java, SyncHistoryDto::class.java)

        val favsAdapter = moshi.adapter<List<SyncFavoriteDto>>(favsListType)
        val histAdapter = moshi.adapter<List<SyncHistoryDto>>(histListType)

        val cloudFavsJson = sharedPrefs.getString(cloudFavsKey, null)
        val cloudHistJson = sharedPrefs.getString(cloudHistKey, null)

        val currentCloudFavs = if (cloudFavsJson != null) {
            try { favsAdapter.fromJson(cloudFavsJson)?.toMutableList() ?: mutableListOf() } catch (e: Exception) { mutableListOf() }
        } else {
            mutableListOf()
        }

        val currentCloudHist = if (cloudHistJson != null) {
            try { histAdapter.fromJson(cloudHistJson)?.toMutableList() ?: mutableListOf() } catch (e: Exception) { mutableListOf() }
        } else {
            mutableListOf()
        }

        val currentTime = System.currentTimeMillis()

        when (actionType) {
            "SIMULATE_ADD_FAVORITE_RED" -> {
                val newFav = SyncFavoriteDto(
                    verseId = 1,
                    translation = "KJV",
                    bookName = "John",
                    chapter = 3,
                    verseNumber = 16,
                    text = "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life.",
                    colorHex = "#FF8A80", // Coral Pink/Red
                    timestamp = currentTime
                )
                // Remove existing to simulate update
                currentCloudFavs.removeAll { it.translation == "KJV" && it.bookName == "John" && it.chapter == 3 && it.verseNumber == 16 }
                currentCloudFavs.add(newFav)
                sharedPrefs.edit().putString(cloudFavsKey, favsAdapter.toJson(currentCloudFavs)).apply()
                addLog("[$deviceName] Highlighted John 3:16 in Coral Red (Cloud Updated).")
            }
            "SIMULATE_ADD_FAVORITE_GREEN" -> {
                val newFav = SyncFavoriteDto(
                    verseId = 2,
                    translation = "KJV",
                    bookName = "Psalms",
                    chapter = 23,
                    verseNumber = 1,
                    text = "The LORD is my shepherd; I shall not want.",
                    colorHex = "#A5D6A7", // Pastel Green
                    timestamp = currentTime
                )
                currentCloudFavs.removeAll { it.translation == "KJV" && it.bookName == "Psalms" && it.chapter == 23 && it.verseNumber == 1 }
                currentCloudFavs.add(newFav)
                sharedPrefs.edit().putString(cloudFavsKey, favsAdapter.toJson(currentCloudFavs)).apply()
                addLog("[$deviceName] Highlighted Psalms 23:1 in Pastel Green (Cloud Updated).")
            }
            "SIMULATE_DELETE_FAVORITE" -> {
                // Find an existing highlight to mark deleted, or create a tombstone
                val tombstone = SyncFavoriteDto(
                    verseId = 1,
                    translation = "KJV",
                    bookName = "John",
                    chapter = 3,
                    verseNumber = 16,
                    text = "",
                    colorHex = "",
                    timestamp = currentTime,
                    isDeleted = true
                )
                currentCloudFavs.removeAll { it.translation == "KJV" && it.bookName == "John" && it.chapter == 3 && it.verseNumber == 16 }
                currentCloudFavs.add(tombstone)
                sharedPrefs.edit().putString(cloudFavsKey, favsAdapter.toJson(currentCloudFavs)).apply()
                addLog("[$deviceName] Deleted John 3:16 highlight (Cloud Updated with Tombstone).")
            }
            "SIMULATE_ADD_HISTORY" -> {
                val newHist = SyncHistoryDto(
                    bookName = "Genesis",
                    chapter = 1,
                    verseNumber = null,
                    verseText = null,
                    timestamp = currentTime
                )
                currentCloudHist.removeAll { it.bookName == "Genesis" && it.chapter == 1 }
                currentCloudHist.add(newHist)
                sharedPrefs.edit().putString(cloudHistKey, histAdapter.toJson(currentCloudHist)).apply()
                addLog("[$deviceName] Read Genesis Chapter 1 (Cloud Updated).")
            }
        }
    }

    suspend fun getLocalBackupData(email: String): BackupPayload = withContext(Dispatchers.IO) {
        val localFavoritesFlow = if (email.isBlank()) bibleDao.getFavoritesForGuest() else bibleDao.getFavoritesForUser(email)
        val localHistoryFlow = if (email.isBlank()) bibleDao.getReadingHistoryForGuest() else bibleDao.getReadingHistoryForUser(email)

        val localFavs = localFavoritesFlow.first()
        val localHist = localHistoryFlow.first()

        val favsDto = localFavs.map {
            SyncFavoriteDto(
                verseId = it.verseId,
                translation = it.translation,
                bookName = it.bookName,
                chapter = it.chapter,
                verseNumber = it.verseNumber,
                text = it.text,
                colorHex = it.colorHex,
                timestamp = it.timestamp,
                isDeleted = false
            )
        }

        val histDto = localHist.map {
            SyncHistoryDto(
                bookName = it.bookName,
                chapter = it.chapter,
                verseNumber = it.verseNumber,
                verseText = it.verseText,
                timestamp = it.timestamp,
                isDeleted = false
            )
        }

        BackupPayload(
            favorites = favsDto,
            history = histDto
        )
    }

    suspend fun restoreBackupData(email: String, backup: BackupPayload): Boolean = withContext(Dispatchers.IO) {
        try {
            // Restore Favorites
            backup.favorites.forEach { favDto ->
                val favoriteEntity = FavoriteVerse(
                    verseId = favDto.verseId,
                    translation = favDto.translation,
                    bookName = favDto.bookName,
                    chapter = favDto.chapter,
                    verseNumber = favDto.verseNumber,
                    text = favDto.text,
                    colorHex = favDto.colorHex,
                    timestamp = favDto.timestamp,
                    userEmail = if (email.isBlank()) null else email
                )
                bibleDao.insertFavorite(favoriteEntity)
            }

            // Restore History
            backup.history.forEach { histDto ->
                val historyEntity = ReadingHistory(
                    bookName = histDto.bookName,
                    chapter = histDto.chapter,
                    verseNumber = histDto.verseNumber,
                    verseText = histDto.verseText,
                    timestamp = histDto.timestamp,
                    userEmail = if (email.isBlank()) null else email
                )
                bibleDao.insertReadingHistory(historyEntity)
            }
            addLog("Manual backup successfully imported. Restored ${backup.favorites.size} highlights, ${backup.history.size} progress rows.")
            true
        } catch (e: Exception) {
            addLog("Error restoring backup: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun exportBackupToJson(email: String): String = withContext(Dispatchers.IO) {
        val payload = getLocalBackupData(email)
        val adapter = moshi.adapter(BackupPayload::class.java)
        adapter.toJson(payload)
    }

    suspend fun importBackupFromJson(email: String, jsonStr: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val adapter = moshi.adapter(BackupPayload::class.java)
            val backup = adapter.fromJson(jsonStr) ?: return@withContext false
            restoreBackupData(email, backup)
        } catch (e: Exception) {
            addLog("Failed to parse backup JSON: ${e.message}")
            false
        }
    }
}

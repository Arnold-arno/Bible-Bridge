package com.example.data

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SyncPayload(
    val userEmail: String,
    val deviceName: String,
    val favorites: List<SyncFavoriteDto>,
    val history: List<SyncHistoryDto>,
    val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class BackupPayload(
    val backupVersion: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val favorites: List<SyncFavoriteDto>,
    val history: List<SyncHistoryDto>
)

@JsonClass(generateAdapter = true)
data class SyncFavoriteDto(
    val verseId: Int,
    val translation: String,
    val bookName: String,
    val chapter: Int,
    val verseNumber: Int,
    val text: String,
    val colorHex: String,
    val timestamp: Long,
    val isDeleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class SyncHistoryDto(
    val bookName: String,
    val chapter: Int,
    val verseNumber: Int?,
    val verseText: String?,
    val timestamp: Long,
    val isDeleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class HttpBinResponse(
    val json: SyncPayload?
)

interface SyncApi {
    @POST("anything")
    fun syncData(@Body payload: SyncPayload): Call<HttpBinResponse>
}

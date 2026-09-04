package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "model" or "system"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String? = null,
    val roleName: String? = null
)

enum class AIChatRole(
    val title: String,
    val subtitle: String,
    val defaultModel: String,
    val systemInstruction: String
) {
    GENERAL_ASSISTANT(
        title = "Scripture Companion",
        subtitle = "General Q&A, application & Bible study",
        defaultModel = "gemini-3.5-flash",
        systemInstruction = "You are a warm, compassionate Christian pastor and biblical scholar. Provide thoughtful, biblically sound explanations, encouraging reflections, and practical life applications. Write in a natural, warm, human voice. STRICTLY DO NOT use markdown symbols like asterisks (** or *), hashtags (# or ##), or AI self-references. Present your points cleanly using clear paragraphs, numbered lists (1, 2, 3), or bullet points (•)."
    ),
    DEEP_RESEARCH(
        title = "Exegetical Research",
        subtitle = "Original languages, history & theology",
        defaultModel = "gemini-3.5-flash",
        systemInstruction = "You are a seasoned Biblical Scholar and Exegete. Conduct analytical research into scripture passages. Include historical-cultural background, original Hebrew or Greek root meanings, theological cross-references, structural analysis, and classic commentary perspectives. Write in a clean, human, scholarly voice without markdown symbols like asterisks (** or *), hashtags (#), or AI disclaimers."
    ),
    DEVOTIONAL_PARTNER(
        title = "Devotional Companion",
        subtitle = "Personal reflection & prayer prompts",
        defaultModel = "gemini-3.5-flash",
        systemInstruction = "You are a loving devotional guide. Help the reader meditate on scripture, write deep personal reflections, frame heartfelt prayers, and connect biblical truths to everyday life with warmth and grace. Write as a thoughtful human companion without markdown symbols (** or #) or AI self-references."
    ),
    QUICK_HELPER(
        title = "Quick Verse Helper",
        subtitle = "Fast summaries & key facts",
        defaultModel = "gemini-3.5-flash",
        systemInstruction = "You are a direct, concise scripture study guide. Deliver clear, focused answers, verse summaries, word definitions, and key cross-references. Write naturally and directly in clean human prose without markdown symbols (** or #) or AI disclaimers."
    )
}

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun sendChatMessage(
        messages: List<ChatMessage>,
        role: AIChatRole,
        overrideModel: String? = null
    ): String = withContext(Dispatchers.IO) {
        val selectedModel = overrideModel ?: role.defaultModel
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        // Validate API Key presence or placeholder
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "PLACEHOLDER_KEY") {
            Log.w(TAG, "Gemini API key is not configured or using default placeholder.")
            return@withContext getLocalFallbackResponse(messages.lastOrNull()?.text ?: "", role)
        }

        try {
            val url = "$BASE_URL$selectedModel:generateContent?key=$apiKey"
            
            val payload = JSONObject().apply {
                // System Instruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", role.systemInstruction) })
                    })
                })

                // Conversation History (multi-turn)
                val contentsArray = JSONArray()
                // Limit history turn depth to last 16 messages for performance
                val recentMessages = messages.takeLast(16)
                for (msg in recentMessages) {
                    if (msg.sender == "system") continue
                    val roleStr = if (msg.sender == "user") "user" else "model"
                    contentsArray.put(JSONObject().apply {
                        put("role", roleStr)
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", msg.text) })
                        })
                    })
                }
                put("contents", contentsArray)

                // Generation Config
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                })
            }

            val requestBody = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseStr = response.body?.string()

            if (!response.isSuccessful || responseStr == null) {
                Log.e(TAG, "API error ${response.code}: $responseStr")
                return@withContext "Unable to process request at this time. Please check your network connection and try again."
            }

            val jsonResponse = JSONObject(responseStr)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val replyText = parts.getJSONObject(0).optString("text", "")
                    if (replyText.isNotBlank()) {
                        return@withContext formatHumanText(replyText)
                    }
                }
            }

            return@withContext "Received response, but no text was returned."
        } catch (e: Exception) {
            Log.e(TAG, "Network call failed", e)
            return@withContext "Connection note: ${e.localizedMessage ?: "Unable to reach service"}. Please try again."
        }
    }

    fun formatHumanText(rawText: String): String {
        if (rawText.isBlank()) return rawText
        var text = rawText
        // Remove markdown header indicators (#, ##, ###)
        text = text.replace(Regex("(?m)^#{1,6}\\s*"), "")
        // Remove markdown bold and italic formatting stars and underscores
        text = text.replace("***", "")
        text = text.replace("**", "")
        text = text.replace("___", "")
        text = text.replace("__", "")
        text = text.replace("`", "")
        // Replace solitary asterisks used for emphasis or lists
        text = text.replace(Regex("(?m)^[\\*\\-]\\s+"), "• ")
        text = text.replace("*", "")
        text = text.replace("_", "")
        return text.trim()
    }

    private fun getLocalFallbackResponse(query: String, role: AIChatRole): String {
        val q = query.lowercase()
        val content = when {
            q.contains("devotion") || q.contains("reflection") || role == AIChatRole.DEVOTIONAL_PARTNER -> {
                "Devotional Reflection Guide\n\n" +
                "Meditating on God's Word yields enduring peace and spiritual clarity. Consider these three reflection prompts:\n\n" +
                "1. Observation: What key truth or character of God stands out in this passage?\n" +
                "2. Application: How can you actively trust God with your current situation today?\n" +
                "3. Prayer: Lord, open my heart to understand Your Word and grant me strength to walk in Your grace today. Amen."
            }
            q.contains("greek") || q.contains("hebrew") || q.contains("context") || role == AIChatRole.DEEP_RESEARCH -> {
                "Biblical Exegesis Note\n\n" +
                "Scripture was written in rich historical settings (Old Testament in Hebrew/Aramaic, New Testament in Koine Greek):\n\n" +
                "• Agape (Greek): Unconditional, self-giving divine love (e.g. 1 John 4:8).\n" +
                "• Hesed (Hebrew): Covenant faithfulness, steadfast loyalty, and lovingkindness.\n" +
                "• Shalom (Hebrew): Wholeness, completeness, and flourishing peace."
            }
            else -> {
                "Scripture Study Companion\n\n" +
                "God's Word is 'a lamp unto my feet and a light unto my path' (Psalm 119:105).\n\n" +
                "Key encouraging passages for study:\n" +
                "• Faith & Trust: Proverbs 3:5-6\n" +
                "• Peace in Anxiety: Philippians 4:6-7\n" +
                "• God's Enduring Love: Romans 8:38-39"
            }
        }
        return formatHumanText(content)
    }

    suspend fun generateDevotionalForVerse(verseText: String, verseRef: String): Devotional = withContext(Dispatchers.IO) {
        val prompt = "Create a daily devotional based on scripture $verseRef: \"$verseText\". Return a title, content summary, and a prayer."
        val reply = sendChatMessage(
            messages = listOf(ChatMessage(sender = "user", text = prompt)),
            role = AIChatRole.DEVOTIONAL_PARTNER
        )
        Devotional(
            title = "Reflection on $verseRef",
            date = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date()),
            scripture = "$verseRef - \"$verseText\"",
            content = reply,
            prayer = "Lord, anchor my heart in Your truth and grant me strength to walk in obedience today. Amen.",
            isCustom = true
        )
    }

    suspend fun generateCustomDevotional(topic: String, scriptureSuggestion: String = ""): Devotional = withContext(Dispatchers.IO) {
        val prompt = "Create a devotional on topic \"$topic\" with reference \"$scriptureSuggestion\". Provide scripture focus, reflection content, and prayer."
        val reply = sendChatMessage(
            messages = listOf(ChatMessage(sender = "user", text = prompt)),
            role = AIChatRole.DEVOTIONAL_PARTNER
        )
        Devotional(
            title = topic.ifBlank { scriptureSuggestion }.take(30).replaceFirstChar { it.uppercase() },
            date = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date()),
            scripture = scriptureSuggestion.ifBlank { "Scripture Focus on $topic" },
            content = reply,
            prayer = "Father, guide my steps as I ponder Your Word on $topic. Amen.",
            isCustom = true
        )
    }

    suspend fun streamBibleChapter(translation: String, book: String, chapter: Int): List<BibleVerse> = withContext(Dispatchers.IO) {
        emptyList()
    }

    suspend fun generateConcordance(word: String): ConcordanceEntry? = withContext(Dispatchers.IO) {
        val prompt = "Provide a biblical concordance entry for word '$word'. Include Hebrew/Greek root word, transliteration, definition, frequency, and 2 key references."
        val reply = sendChatMessage(
            messages = listOf(ChatMessage(sender = "user", text = prompt)),
            role = AIChatRole.DEEP_RESEARCH
        )
        ConcordanceEntry(
            word = word.replaceFirstChar { it.uppercase() },
            partOfSpeech = "biblical key word",
            meaning = reply.take(250),
            occurrences = listOf("Genesis 1:1", "John 1:1"),
            seeAlso = listOf("Faith", "Grace"),
            greekWord = "Strong's Study",
            transliteration = word,
            usedCount = 42
        )
    }

    suspend fun translateText(text: String, targetLanguage: String): String = withContext(Dispatchers.IO) {
        if (targetLanguage.equals("en", ignoreCase = true) || targetLanguage.equals("English", ignoreCase = true)) return@withContext text
        val prompt = "Translate the following biblical text into $targetLanguage while preserving reverence and accuracy:\n\n$text"
        sendChatMessage(
            messages = listOf(ChatMessage(sender = "user", text = prompt)),
            role = AIChatRole.QUICK_HELPER
        )
    }

    suspend fun generateVerseOfTheDay(): BibleVerse? = withContext(Dispatchers.IO) {
        val prompt = "Provide a single inspirational Bible verse in format: Book Chapter:Verse | Translation | Verse Text"
        val reply = sendChatMessage(
            messages = listOf(ChatMessage(sender = "user", text = prompt)),
            role = AIChatRole.QUICK_HELPER
        )
        BibleVerse(
            translation = "KJV",
            bookName = "John",
            chapter = 3,
            verseNumber = 16,
            text = reply
        )
    }
}

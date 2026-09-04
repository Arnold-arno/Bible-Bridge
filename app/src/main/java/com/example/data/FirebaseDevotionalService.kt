package com.example.data

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service integration using Firebase AI SDK (com.google.firebase.ai) to generate custom
 * devotional thoughts based on a selected Bible passage.
 */
@OptIn(PublicPreviewAPI::class)
object FirebaseDevotionalService {

    private const val MODEL_NAME = "gemini-3.5-flash"

    /**
     * Generates a custom devotional thought based on a selected Bible passage reference and text
     * using the Firebase AI SDK.
     */
    suspend fun generateDevotionalForPassage(
        verseRef: String,
        passageText: String,
        userReflectionPrompt: String = ""
    ): Devotional = withContext(Dispatchers.IO) {
        val promptText = buildString {
            append("Selected Bible Passage: $verseRef\n")
            append("Passage Text: \"$passageText\"\n")
            if (userReflectionPrompt.isNotBlank()) {
                append("User Personal Focus/Note: $userReflectionPrompt\n")
            }
            append("\nPlease write an inspiring, deeply spiritual devotional thought centered on this passage.")
        }

        val systemInstructionText = """
            You are a compassionate, wise Biblical devotional scholar. Your task is to write a deeply moving, practical daily devotional thought based directly on the provided Bible passage.
            You MUST structure your response with exact markers so the application can parse the parts. Use these exact labels on their own lines:
            [TITLE]
            (Write a short, engaging, and creative title inspired by the passage)
            [SCRIPTURE]
            ($verseRef - $passageText)
            [REFLECTION]
            (Write 2-3 paragraphs of comforting, deep spiritual reflection, relating this specific passage to daily life and spiritual growth)
            [PRAYER]
            (Write a short, beautiful, heartfelt closing prayer based on the passage)
            
            Do not include any conversational filler outside these blocks. Keep it authentic, rich, and encouraging.
        """.trimIndent()

        try {
            val generativeModel = Firebase.ai.generativeModel(
                modelName = MODEL_NAME,
                systemInstruction = content { text(systemInstructionText) }
            )

            val response = generativeModel.generateContent(
                content {
                    text(promptText)
                }
            )

            val rawResult = response.text ?: ""
            if (rawResult.isBlank()) {
                return@withContext GeminiService.generateDevotionalForVerse(
                    verseText = passageText,
                    verseRef = verseRef
                )
            }

            return@withContext parseDevotionalResponse(
                rawResult = rawResult,
                defaultRef = verseRef,
                defaultText = passageText
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Graceful fallback to Gemini REST service if Firebase AI throws or is uninitialized
            return@withContext GeminiService.generateDevotionalForVerse(
                verseText = passageText,
                verseRef = verseRef
            )
        }
    }

    /**
     * Generates a custom topic or passage devotional using Firebase AI SDK.
     */
    suspend fun generateCustomPassageThought(
        passageQuery: String,
        focusTheme: String = "Spiritual Renewal"
    ): Devotional = withContext(Dispatchers.IO) {
        val promptText = "Generate a custom devotional thought based on passage query: \"$passageQuery\" with theme focus: \"$focusTheme\"."

        val systemInstructionText = """
            You are an encouraging and profound Biblical scholar. Your task is to create a custom devotional thought based on the user's selected passage query.
            You MUST structure your response with exact markers:
            [TITLE]
            (Creative Title)
            [SCRIPTURE]
            (Passage Reference and Verse)
            [REFLECTION]
            (Practical, uplifting reflection paragraphs)
            [PRAYER]
            (Closing prayer)
        """.trimIndent()

        try {
            val generativeModel = Firebase.ai.generativeModel(
                modelName = MODEL_NAME,
                systemInstruction = content { text(systemInstructionText) }
            )

            val response = generativeModel.generateContent(content { text(promptText) })
            val rawResult = response.text ?: ""

            if (rawResult.isBlank()) {
                return@withContext GeminiService.generateCustomDevotional(
                    topic = focusTheme,
                    scriptureSuggestion = passageQuery
                )
            }

            return@withContext parseDevotionalResponse(
                rawResult = rawResult,
                defaultRef = passageQuery,
                defaultText = "Custom Reflection on $passageQuery"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext GeminiService.generateCustomDevotional(
                topic = focusTheme,
                scriptureSuggestion = passageQuery
            )
        }
    }

    private fun parseDevotionalResponse(
        rawResult: String,
        defaultRef: String,
        defaultText: String
    ): Devotional {
        var title = "Reflection on $defaultRef"
        var scripture = "$defaultRef - $defaultText"
        var reflection = rawResult
        var prayer = "Lord, bless this passage to my heart today. Amen."

        try {
            val titleIndex = rawResult.indexOf("[TITLE]")
            val scriptureIndex = rawResult.indexOf("[SCRIPTURE]")
            val reflectionIndex = rawResult.indexOf("[REFLECTION]")
            val prayerIndex = rawResult.indexOf("[PRAYER]")

            if (titleIndex != -1 && scriptureIndex != -1) {
                title = rawResult.substring(titleIndex + 7, scriptureIndex).trim()
            }
            if (scriptureIndex != -1 && reflectionIndex != -1) {
                scripture = rawResult.substring(scriptureIndex + 11, reflectionIndex).trim()
            }
            if (reflectionIndex != -1 && prayerIndex != -1) {
                reflection = rawResult.substring(reflectionIndex + 12, prayerIndex).trim()
            }
            if (prayerIndex != -1) {
                prayer = rawResult.substring(prayerIndex + 8).trim()
            }
        } catch (e: Exception) {
            reflection = rawResult
        }

        return Devotional(
            title = title.replace("\n", " ").trim(),
            date = "Firebase AI Devotional",
            scripture = scripture.replace("\n", " ").trim(),
            content = reflection,
            prayer = prayer,
            isCustom = true
        )
    }
}




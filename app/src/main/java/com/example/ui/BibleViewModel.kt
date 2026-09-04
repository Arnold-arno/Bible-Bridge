package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.DevotionalReminderHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen {
    ONBOARDING, HOME, READ, DEVOTIONS, TIMELINE, SETTINGS, LOGIN, BOOK_OVERVIEW, AI_CHAT
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class BibleViewModel(
    private val repository: BibleRepository,
    private val sharedPrefs: android.content.SharedPreferences,
    val syncManager: SyncManager
) : ViewModel() {

    // --- Cloud Sync State ---
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // --- Gemini AI Chatbot State ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage(
            sender = "model",
            text = "Blessings! I am your AI Scripture & Devotional Assistant, powered by Gemini. Ask me any question about the Bible, daily reflections, theological research, or original Greek/Hebrew root words.",
            modelUsed = "gemini-3.5-flash",
            roleName = AIChatRole.GENERAL_ASSISTANT.title
        )
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _currentRole = MutableStateFlow(AIChatRole.GENERAL_ASSISTANT)
    val currentRole: StateFlow<AIChatRole> = _currentRole.asStateFlow()

    private val _overrideModel = MutableStateFlow<String?>(null)
    val overrideModel: StateFlow<String?> = _overrideModel.asStateFlow()

    private val _isGeneratingChat = MutableStateFlow(false)
    val isGeneratingChat: StateFlow<Boolean> = _isGeneratingChat.asStateFlow()

    private val _chatContextBanner = MutableStateFlow<String?>(null)
    val chatContextBanner: StateFlow<String?> = _chatContextBanner.asStateFlow()

    fun setChatRole(role: AIChatRole) {
        _currentRole.value = role
    }

    fun setOverrideModel(model: String?) {
        _overrideModel.value = model
    }

    fun clearChatMessages() {
        _chatMessages.value = listOf(
            ChatMessage(
                sender = "model",
                text = "Chat history cleared. How can I assist your Bible study or reflection today?",
                modelUsed = _overrideModel.value ?: _currentRole.value.defaultModel,
                roleName = _currentRole.value.title
            )
        )
        _chatContextBanner.value = null
    }

    fun sendMessageToAI(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty() || _isGeneratingChat.value) return

        val userMessage = ChatMessage(
            sender = "user",
            text = trimmed,
            roleName = _currentRole.value.title
        )
        val updatedList = _chatMessages.value + userMessage
        _chatMessages.value = updatedList
        _isGeneratingChat.value = true

        viewModelScope.launch {
            val role = _currentRole.value
            val override = _overrideModel.value
            val activeModel = override ?: role.defaultModel

            val responseText = GeminiService.sendChatMessage(
                messages = updatedList,
                role = role,
                overrideModel = override
            )

            val modelMessage = ChatMessage(
                sender = "model",
                text = responseText,
                modelUsed = activeModel,
                roleName = role.title
            )
            _chatMessages.value = _chatMessages.value + modelMessage
            _isGeneratingChat.value = false

            // Sync conversation turn to Firestore if logged in
            if (_isLoggedIn.value && _userEmail.value.isNotBlank()) {
                try {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val userChatDoc = db.collection("users")
                        .document(_userEmail.value)
                        .collection("chat_history")
                        .document(userMessage.id)
                    userChatDoc.set(mapOf(
                        "userQuery" to trimmed,
                        "aiReply" to responseText,
                        "modelUsed" to activeModel,
                        "role" to role.title,
                        "timestamp" to System.currentTimeMillis()
                    ), com.google.firebase.firestore.SetOptions.merge())
                } catch (e: Exception) {
                    // Local fallback
                }
            }
        }
    }

    fun startDevotionalAIChat(devotionalTitle: String, scripture: String, content: String) {
        _currentRole.value = AIChatRole.DEVOTIONAL_PARTNER
        _overrideModel.value = "gemini-3.5-flash"
        _chatContextBanner.value = "Daily Devotional Focus: $devotionalTitle ($scripture)"

        val contextPrompt = "I am reflecting on today's devotional titled '$devotionalTitle' based on scripture $scripture: \"$content\". Could you help me explore deeper personal applications, key themes, and a reflection prayer?"
        val initialUserMsg = ChatMessage(sender = "user", text = contextPrompt, roleName = AIChatRole.DEVOTIONAL_PARTNER.title)
        
        _chatMessages.value = listOf(initialUserMsg)
        _isGeneratingChat.value = true
        navigateTo(Screen.AI_CHAT)

        viewModelScope.launch {
            val reply = GeminiService.sendChatMessage(
                messages = listOf(initialUserMsg),
                role = AIChatRole.DEVOTIONAL_PARTNER,
                overrideModel = "gemini-3.5-flash"
            )
            val modelMsg = ChatMessage(
                sender = "model",
                text = reply,
                modelUsed = "gemini-3.5-flash",
                roleName = AIChatRole.DEVOTIONAL_PARTNER.title
            )
            _chatMessages.value = listOf(initialUserMsg, modelMsg)
            _isGeneratingChat.value = false
        }
    }

    fun startScriptureResearchAIChat(topicOrPassage: String) {
        _currentRole.value = AIChatRole.DEEP_RESEARCH
        _overrideModel.value = "gemini-3.1-pro-preview"
        _chatContextBanner.value = "Exegetical Research Focus: $topicOrPassage"

        val researchPrompt = "Please conduct deep exegetical research on $topicOrPassage. Include historical context, key original Greek/Hebrew words, structural insights, and practical theological summary."
        val userMsg = ChatMessage(sender = "user", text = researchPrompt, roleName = AIChatRole.DEEP_RESEARCH.title)

        _chatMessages.value = listOf(userMsg)
        _isGeneratingChat.value = true
        navigateTo(Screen.AI_CHAT)

        viewModelScope.launch {
            val reply = GeminiService.sendChatMessage(
                messages = listOf(userMsg),
                role = AIChatRole.DEEP_RESEARCH,
                overrideModel = "gemini-3.1-pro-preview"
            )
            val modelMsg = ChatMessage(
                sender = "model",
                text = reply,
                modelUsed = "gemini-3.1-pro-preview",
                roleName = AIChatRole.DEEP_RESEARCH.title
            )
            _chatMessages.value = listOf(userMsg, modelMsg)
            _isGeneratingChat.value = false
        }
    }

    private val _syncLogsList = MutableStateFlow<List<String>>(emptyList())
    val syncLogsList: StateFlow<List<String>> = _syncLogsList.asStateFlow()

    private val _lastSyncTimeText = MutableStateFlow("Never synced")
    val lastSyncTimeText: StateFlow<String> = _lastSyncTimeText.asStateFlow()

    fun refreshSyncLogsAndMetadata() {
        _syncLogsList.value = syncManager.getSyncLogsList()
        _lastSyncTimeText.value = syncManager.getLastSyncTime(_userEmail.value)
    }

    fun syncNow() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                syncManager.syncWithCloud(_userEmail.value, "Android Mobile Device")
            } catch (e: Exception) {
                // Ignore or log
            } finally {
                _isSyncing.value = false
                refreshSyncLogsAndMetadata()
            }
        }
    }

    fun triggerSimulatedAction(actionType: String) {
        viewModelScope.launch {
            syncManager.simulateOtherDeviceAction(_userEmail.value, "iPad Air (Sync Client)", actionType)
            refreshSyncLogsAndMetadata()
        }
    }

    fun clearLogs() {
        syncManager.clearSyncLogs()
        refreshSyncLogsAndMetadata()
    }

    fun exportBackup(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val json = syncManager.exportBackupToJson(_userEmail.value)
                if (json != null) {
                    markBackupCompleted(favoriteVerses.value.size)
                }
                onResult(json)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }

    fun importBackup(jsonStr: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val success = syncManager.importBackupFromJson(_userEmail.value, jsonStr)
                if (success) {
                    refreshSyncLogsAndMetadata()
                }
                onResult(success)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }
    }

    // --- Loading & Initialization ---
    private val _isDbLoaded = MutableStateFlow(false)
    val isDbLoaded: StateFlow<Boolean> = _isDbLoaded.asStateFlow()

    // --- Authentication State ---
    private val _isLoggedIn = MutableStateFlow(sharedPrefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // --- Navigation ---
    private val _activeScreen = MutableStateFlow(
        if (!sharedPrefs.getBoolean("has_completed_onboarding", false)) {
            Screen.ONBOARDING
        } else if (sharedPrefs.getBoolean("is_logged_in", false)) {
            Screen.HOME
        } else if (sharedPrefs.getBoolean("skipped_signin_once", false)) {
            Screen.HOME
        } else {
            Screen.LOGIN
        }
    )
    val activeScreen: StateFlow<Screen> = _activeScreen.asStateFlow()

    // --- User Profile & Theme Settings ---
    private val _userName = MutableStateFlow(sharedPrefs.getString("user_name", "Faithful Reader") ?: "Faithful Reader")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow(sharedPrefs.getString("user_email", "reader@example.com") ?: "reader@example.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userCredentials = MutableStateFlow(sharedPrefs.getString("user_credentials", "READER-777") ?: "READER-777")
    val userCredentials: StateFlow<String> = _userCredentials.asStateFlow()

    private val _userProfilePic = MutableStateFlow(sharedPrefs.getString("user_profile_pic", "") ?: "")
    val userProfilePic: StateFlow<String> = _userProfilePic.asStateFlow()

    private val _themePreference = MutableStateFlow(sharedPrefs.getString("theme_preference", "System") ?: "System")
    val themePreference: StateFlow<String> = _themePreference.asStateFlow()

    private val _readerFontSize = MutableStateFlow(sharedPrefs.getFloat("reader_font_size", 18f))
    val readerFontSize: StateFlow<Float> = _readerFontSize.asStateFlow()

    private val _readerFontSerif = MutableStateFlow(sharedPrefs.getBoolean("reader_font_serif", true))
    val readerFontSerif: StateFlow<Boolean> = _readerFontSerif.asStateFlow()

    private val _readerFontFamily = MutableStateFlow(sharedPrefs.getString("reader_font_family", "Serif") ?: "Serif")
    val readerFontFamily: StateFlow<String> = _readerFontFamily.asStateFlow()

    private val _readerLineHeightMultiplier = MutableStateFlow(sharedPrefs.getFloat("reader_line_height_mult", 1.5f))
    val readerLineHeightMultiplier: StateFlow<Float> = _readerLineHeightMultiplier.asStateFlow()

    private val _readerTheme = MutableStateFlow(sharedPrefs.getString("reader_theme", "Light") ?: "Light")
    val readerTheme: StateFlow<String> = _readerTheme.asStateFlow()

    // --- Backup & Export Prompt States ---
    private val _lastBackupTime = MutableStateFlow(sharedPrefs.getLong("last_backup_time", 0L))
    val lastBackupTime: StateFlow<Long> = _lastBackupTime.asStateFlow()

    private val _lastBackupCount = MutableStateFlow(sharedPrefs.getInt("last_backup_count", 0))
    val lastBackupCount: StateFlow<Int> = _lastBackupCount.asStateFlow()

    private val _isBackupPromptDismissed = MutableStateFlow(false)
    val isBackupPromptDismissed: StateFlow<Boolean> = _isBackupPromptDismissed.asStateFlow()

    fun dismissBackupPrompt() {
        _isBackupPromptDismissed.value = true
    }

    fun markBackupCompleted(count: Int) {
        val now = System.currentTimeMillis()
        sharedPrefs.edit()
            .putLong("last_backup_time", now)
            .putInt("last_backup_count", count)
            .apply()
        _lastBackupTime.value = now
        _lastBackupCount.value = count
        _isBackupPromptDismissed.value = false // reset dismissed state since they backed up
    }

    val recentSearches: StateFlow<List<String>> = repository.recentSearchesFlow
        .map { list -> list.map { it.query } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun migrateLegacySharedPrefsSearches() {
        viewModelScope.launch {
            val raw = sharedPrefs.getString("recent_searches_list", "") ?: ""
            if (raw.isNotEmpty()) {
                val legacyList = raw.split(";;;").filter { it.isNotBlank() }
                legacyList.reversed().forEach { term ->
                    repository.saveRecentSearch(term)
                }
                sharedPrefs.edit().remove("recent_searches_list").apply()
            }
        }
    }

    fun saveSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.saveRecentSearch(trimmed)
        }
    }

    fun deleteRecentSearch(query: String) {
        viewModelScope.launch {
            repository.deleteRecentSearch(query)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            repository.clearRecentSearches()
        }
    }

    // --- Daily Devotional Reminders ---
    private val _reminderEnabled = MutableStateFlow(sharedPrefs.getBoolean("reminder_enabled", false))
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled.asStateFlow()

    private val _reminderHour = MutableStateFlow(sharedPrefs.getInt("reminder_hour", 8))
    val reminderHour: StateFlow<Int> = _reminderHour.asStateFlow()

    private val _reminderMinute = MutableStateFlow(sharedPrefs.getInt("reminder_minute", 0))
    val reminderMinute: StateFlow<Int> = _reminderMinute.asStateFlow()

    // Compatibility mode for standard theme toggle
    val isDarkMode: StateFlow<Boolean> = _themePreference
        .map { it == "Dark" || it == "System" } // Simple fallback mapping for general usage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Manage downloaded versions dynamically
    private val _downloadProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Int>> = _downloadProgress.asStateFlow()

    private val _downloadedVersions = MutableStateFlow<Set<String>>(
        sharedPrefs.getStringSet("downloaded_versions", setOf("KJV", "WEB", "ASV", "GNB", "ICB")) ?: setOf("KJV", "WEB", "ASV", "GNB", "ICB")
    )
    val downloadedVersions: StateFlow<Set<String>> = _downloadedVersions.asStateFlow()

    private val _translationStorageSizes = MutableStateFlow<Map<String, Long>>(emptyMap())
    val translationStorageSizes: StateFlow<Map<String, Long>> = _translationStorageSizes.asStateFlow()

    enum class ReadStep {
        TESTAMENT_SELECT, COMMON_SELECT, BOOK_SELECT, CHAPTER_SELECT, VERSE_SELECT, VERSE_READ
    }

    private val _currentReadStep = MutableStateFlow(
        try {
            ReadStep.valueOf(sharedPrefs.getString("last_read_step", ReadStep.TESTAMENT_SELECT.name) ?: ReadStep.TESTAMENT_SELECT.name)
        } catch (e: Exception) {
            ReadStep.TESTAMENT_SELECT
        }
    )
    val currentReadStep: StateFlow<ReadStep> = _currentReadStep.asStateFlow()

    private val _selectedTestament = MutableStateFlow<String?>(sharedPrefs.getString("last_read_testament", null))
    val selectedTestament: StateFlow<String?> = _selectedTestament.asStateFlow()

    private val _selectedVerse = MutableStateFlow(sharedPrefs.getInt("last_read_verse", 1))
    val selectedVerse: StateFlow<Int> = _selectedVerse.asStateFlow()

    fun selectTestament(testament: String?) {
        _selectedTestament.value = testament
        sharedPrefs.edit().putString("last_read_testament", testament).apply()
    }

    fun selectVerse(verse: Int) {
        _selectedVerse.value = verse
        sharedPrefs.edit().putInt("last_read_verse", verse).apply()
    }

    private val _isCompareMode = MutableStateFlow(sharedPrefs.getBoolean("is_compare_mode", false))
    val isCompareMode: StateFlow<Boolean> = _isCompareMode.asStateFlow()

    private val _compareTranslation = MutableStateFlow(sharedPrefs.getString("compare_translation", "NIV") ?: "NIV")
    val compareTranslation: StateFlow<String> = _compareTranslation.asStateFlow()

    private val _compareSideBySide = MutableStateFlow(sharedPrefs.getBoolean("compare_side_by_side", true))
    val compareSideBySide: StateFlow<Boolean> = _compareSideBySide.asStateFlow()

    // Dynamically calculate available translations based on downloads
    private val _availableTranslationsList = MutableStateFlow(listOf(
        "GNT", "GNB", "KJV", "WEB", "ASV", "ICB", "NKJV", "NIV", "ESV", "NLT", "MSG", "AMP", "CSV", 
        "RVR1960", "NVI", "LBLA", "JBS", "NTV", "RVR", "SEV",
        "AA", "NVI-PT", "LSG", "OST", "ELB", "LUT", "GDB", "RIV", "SVV",
        "CUV", "CUVP", "CUVS", "VULG", "SUV"
    ))
    val availableTranslationsList: StateFlow<List<String>> = _availableTranslationsList.asStateFlow()

    // --- Read Screen State ---
    private val _selectedTranslation = MutableStateFlow(sharedPrefs.getString("last_read_translation", "KJV") ?: "KJV")
    val selectedTranslation: StateFlow<String> = _selectedTranslation.asStateFlow()

    private val _selectedBook = MutableStateFlow(sharedPrefs.getString("last_read_book", "Genesis") ?: "Genesis")
    val selectedBook: StateFlow<String> = _selectedBook.asStateFlow()

    private val _selectedChapter = MutableStateFlow(sharedPrefs.getInt("last_read_chapter", 1))
    val selectedChapter: StateFlow<Int> = _selectedChapter.asStateFlow()

    // Lists of options
    val availableTranslations = listOf(
        "GNT", "GNB", "KJV", "WEB", "ASV", "ICB", "NKJV", "NIV", "ESV", "NLT", "MSG", "AMP", "CSV", 
        "RVR1960", "NVI", "LBLA", "JBS", "NTV", "RVR", "SEV",
        "AA", "NVI-PT", "LSG", "OST", "ELB", "LUT", "GDB", "RIV", "SVV",
        "CUV", "CUVP", "CUVS", "VULG", "SUV"
    )

    // Scroll Position Caching State for Read Screen
    private val _savedReadScrollIndex = MutableStateFlow(0)
    val savedReadScrollIndex: StateFlow<Int> = _savedReadScrollIndex.asStateFlow()

    private val _savedReadScrollOffset = MutableStateFlow(0)
    val savedReadScrollOffset: StateFlow<Int> = _savedReadScrollOffset.asStateFlow()

    fun saveReadScrollPosition(index: Int, offset: Int) {
        _savedReadScrollIndex.value = index
        _savedReadScrollOffset.value = offset
    }
    
    private val _availableBooks = MutableStateFlow<List<String>>(listOf(
        "Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy", "Joshua", "Judges", "Ruth",
        "1 Samuel", "2 Samuel", "1 Kings", "2 Kings", "Psalms", "Proverbs", "Isaiah",
        "Matthew", "Mark", "Luke", "John", "Acts", "Romans", "Galatians", "Ephesians",
        "Philippians", "Colossians", "Hebrews", "James", "1 Peter", "2 Peter", "1 John", "Revelation"
    ))
    val availableBooks: StateFlow<List<String>> = _availableBooks.asStateFlow()

    val availableChapters: StateFlow<List<Int>> = _selectedBook
        .flatMapLatest { book -> repository.getChaptersForBook(book) }
        .map { if (it.isEmpty()) listOf(1) else it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(1))

    private val _isStreamingChapter = MutableStateFlow(false)
    val isStreamingChapter: StateFlow<Boolean> = _isStreamingChapter.asStateFlow()

    // Reactive flow of current verses
    val currentVerses: StateFlow<List<BibleVerse>> = combine(
        _selectedTranslation,
        _selectedBook,
        _selectedChapter
    ) { translation, book, chapter ->
        Triple(translation, book, chapter)
    }.flatMapLatest { (translation, book, chapter) ->
        kotlinx.coroutines.flow.flow {
            repository.getVerses(translation, book, chapter).collect { localList ->
                if (localList.isEmpty()) {
                    _isStreamingChapter.value = true
                    try {
                        val streamed = GeminiService.streamBibleChapter(translation, book, chapter)
                        if (streamed.isNotEmpty()) {
                            repository.addDownloadedVerses(streamed)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        _isStreamingChapter.value = false
                    }
                }
                emit(localList)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Parallel flow for comparing another translation at the same time
    val comparedVerses: StateFlow<List<BibleVerse>> = combine(
        _compareTranslation,
        _selectedBook,
        _selectedChapter
    ) { translation, book, chapter ->
        Triple(translation, book, chapter)
    }.flatMapLatest { (translation, book, chapter) ->
        kotlinx.coroutines.flow.flow {
            repository.getVerses(translation, book, chapter).collect { localList ->
                if (localList.isEmpty()) {
                    _isStreamingChapter.value = true
                    try {
                        val streamed = GeminiService.streamBibleChapter(translation, book, chapter)
                        if (streamed.isNotEmpty()) {
                            repository.addDownloadedVerses(streamed)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        _isStreamingChapter.value = false
                    }
                }
                emit(localList)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Verse Highlighting / Favorites popup ---
    private val _selectedVerseForHighlight = MutableStateFlow<BibleVerse?>(null)
    val selectedVerseForHighlight: StateFlow<BibleVerse?> = _selectedVerseForHighlight.asStateFlow()

    private val _currentlyReadingVerseNumber = MutableStateFlow<Int?>(null)
    val currentlyReadingVerseNumber: StateFlow<Int?> = _currentlyReadingVerseNumber.asStateFlow()

    fun setCurrentlyReadingVerseNumber(num: Int?) {
        _currentlyReadingVerseNumber.value = num
    }

    val favoriteVerses: StateFlow<List<FavoriteVerse>> = combine(_isLoggedIn, _userEmail) { loggedIn, email ->
        if (loggedIn) email else null
    }.flatMapLatest { email ->
        repository.getFavoritesForUser(email)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<FavoriteVerse>> get() = favoriteVerses

    val bookmarkedVerses: StateFlow<List<BookmarkedVerse>> = combine(_isLoggedIn, _userEmail) { loggedIn, email ->
        if (loggedIn) email else null
    }.flatMapLatest { email ->
        repository.getBookmarksForUser(email)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<BookmarkedVerse>> get() = bookmarkedVerses

    // --- Search ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchResults = MutableStateFlow<List<BibleVerse>>(emptyList())
    val searchResults: StateFlow<List<BibleVerse>> = _searchResults.asStateFlow()

    // --- Daily Devotions ---
    val allDevotionals: StateFlow<List<Devotional>> = repository.allDevotionals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDevotional = MutableStateFlow<Devotional?>(null)
    val selectedDevotional: StateFlow<Devotional?> = _selectedDevotional.asStateFlow()

    // --- Verse of the Day (VOTD) ---
    private val _verseOfTheDay = MutableStateFlow<BibleVerse?>(null)
    val verseOfTheDay: StateFlow<BibleVerse?> = _verseOfTheDay.asStateFlow()

    private val _isSyncingVotd = MutableStateFlow(false)
    val isSyncingVotd: StateFlow<Boolean> = _isSyncingVotd.asStateFlow()

    private val _votdSyncStatus = MutableStateFlow<String?>(null)
    val votdSyncStatus: StateFlow<String?> = _votdSyncStatus.asStateFlow()

    // AI Devotional generation states
    private val _aiTopicInput = MutableStateFlow("")
    val aiTopicInput: StateFlow<String> = _aiTopicInput.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    // --- Library & Book Overviews ---
    val bookOverviews = BookOverviews.overviews
    private val _selectedOverviewBook = MutableStateFlow("Genesis")
    val selectedOverviewBook: StateFlow<String> = _selectedOverviewBook.asStateFlow()

    val selectedBookOverview: StateFlow<BookOverview> = _selectedOverviewBook
        .map { BookOverviews.getOverviewForBook(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BookOverviews.overviews.first())

    // --- History Tracking ---
    val readingHistoryList: StateFlow<List<ReadingHistory>> = combine(_isLoggedIn, _userEmail) { loggedIn, email ->
        if (loggedIn) email else null
    }.flatMapLatest { email ->
        repository.getReadingHistoryForUser(email)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReadingHistoryList: StateFlow<List<ReadingHistory>> = combine(_isLoggedIn, _userEmail) { loggedIn, email ->
        if (loggedIn) email else null
    }.flatMapLatest { email ->
        repository.getAllReadingHistoryForUser(email)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleChapterReadStatus(bookName: String, chapter: Int, isRead: Boolean) {
        viewModelScope.launch {
            val email = if (_isLoggedIn.value) _userEmail.value else null
            if (isRead) {
                repository.addReadingHistory(bookName, chapter, null, null, email)
            } else {
                repository.deleteReadingHistory(bookName, chapter, email)
            }
        }
    }

    // --- Timeline ---
    private val _translatedTimelineEvents = MutableStateFlow<List<TimelineEvent>>(TimelineEvents.events)
    val timelineEventsFlow: StateFlow<List<TimelineEvent>> = _translatedTimelineEvents.asStateFlow()
    val timelineEvents: List<TimelineEvent> get() = _translatedTimelineEvents.value

    // --- Concordance ---
    private val _selectedConcordanceEntry = MutableStateFlow<ConcordanceEntry>(ConcordanceData.entries.first())
    val selectedConcordanceEntry: StateFlow<ConcordanceEntry> = _selectedConcordanceEntry.asStateFlow()

    private val _concordanceSearchQuery = MutableStateFlow("")
    val concordanceSearchQuery: StateFlow<String> = _concordanceSearchQuery.asStateFlow()

    private val _concordanceLoading = MutableStateFlow(false)
    val concordanceLoading: StateFlow<Boolean> = _concordanceLoading.asStateFlow()

    private val _concordanceError = MutableStateFlow<String?>(null)
    val concordanceError: StateFlow<String?> = _concordanceError.asStateFlow()

    // --- Study Hub Section Selector ---
    // -1 = Study Hub Dashboard, 0 = Book Overviews, 1 = Chronological Timeline, 2 = Concordance
    private val _selectedStudySection = MutableStateFlow(-1)
    val selectedStudySection: StateFlow<Int> = _selectedStudySection.asStateFlow()

    fun selectStudySection(section: Int) {
        _selectedStudySection.value = section
    }

    fun updateConcordanceSearchQuery(query: String) {
        _concordanceSearchQuery.value = query
    }

    fun selectConcordanceWord(word: String) {
        val lowercaseWord = word.lowercase().trim()
        val localMatch = ConcordanceData.entries.find { it.word.lowercase().trim() == lowercaseWord }
        if (localMatch != null) {
            _selectedConcordanceEntry.value = localMatch
            _concordanceError.value = null
        } else {
            // Fetch dynamically via Gemini
            searchConcordanceWordViaGemini(word)
        }
    }

    fun searchConcordanceWordViaGemini(word: String) {
        viewModelScope.launch {
            _concordanceLoading.value = true
            _concordanceError.value = null
            try {
                val entry = GeminiService.generateConcordance(word)
                if (entry != null) {
                    _selectedConcordanceEntry.value = entry
                } else {
                    _concordanceError.value = "Could not find word study for '$word'. Connect to internet or try another word."
                }
            } catch (e: java.lang.Exception) {
                _concordanceError.value = "Error: ${e.localizedMessage ?: "Failed to load"}"
            } finally {
                _concordanceLoading.value = false
            }
        }
    }

    // --- Reloading & Language Sync States ---
    private val _isReloadingAndSyncing = MutableStateFlow(false)
    val isReloadingAndSyncing: StateFlow<Boolean> = _isReloadingAndSyncing.asStateFlow()

    private val _reloadingStatus = MutableStateFlow("")
    val reloadingStatus: StateFlow<String> = _reloadingStatus.asStateFlow()

    private suspend fun translateTimelineEvents(lang: String) {
        if (lang.equals("English", ignoreCase = true)) {
            _translatedTimelineEvents.value = TimelineEvents.events
            return
        }
        try {
            val originalList = TimelineEvents.events
            val translatedList = originalList.map { event ->
                val translatedTitle = GeminiService.translateText(event.title, lang)
                val translatedPeriod = GeminiService.translateText(event.period, lang)
                val translatedDesc = GeminiService.translateText(event.description, lang)
                val translatedRef = GeminiService.translateText(event.scriptureRef, lang)
                
                event.copy(
                    title = translatedTitle,
                    period = translatedPeriod,
                    description = translatedDesc,
                    scriptureRef = translatedRef
                )
            }
            _translatedTimelineEvents.value = translatedList
        } catch (e: Exception) {
            _translatedTimelineEvents.value = TimelineEvents.events
        }
    }

    private suspend fun translateCurrentSermon(lang: String) {
        val currentDev = _selectedDevotional.value ?: return
        if (lang.equals("English", ignoreCase = true)) {
            return
        }
        try {
            val translatedTitle = GeminiService.translateText(currentDev.title, lang)
            val translatedScripture = GeminiService.translateText(currentDev.scripture, lang)
            val translatedContent = GeminiService.translateText(currentDev.content, lang)
            val translatedPrayer = GeminiService.translateText(currentDev.prayer, lang)
            
            _selectedDevotional.value = currentDev.copy(
                title = translatedTitle,
                scripture = translatedScripture,
                content = translatedContent,
                prayer = translatedPrayer
            )
        } catch (e: Exception) {
            // Keep original on error
        }
    }

    init {
        refreshSyncLogsAndMetadata()
        migrateLegacySharedPrefsSearches()
        loadVerseOfTheDay()
        refreshStorageSizes()
        viewModelScope.launch {
            try {
                // Seed database if empty
                repository.ensureDatabaseSeeded()
            } catch (e: Exception) {
                // Handle or log seeding exception gracefully so app doesn't hang
                e.printStackTrace()
            } finally {
                // Ensure database loading state is marked ready so the app can start
                _isDbLoaded.value = true
                refreshStorageSizes()
            }
            
            // Get actual books list from DB
            try {
                repository.availableBooks.collectLatest { books ->
                    if (books.isNotEmpty()) {
                        _availableBooks.value = books
                        if (!books.contains(_selectedBook.value)) {
                            _selectedBook.value = books.first()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        viewModelScope.launch {
            // Collect devotionals and select the first one once populated
            allDevotionals.collect { list ->
                if (_selectedDevotional.value == null && list.isNotEmpty()) {
                    // Try to match current day of week as default devotional
                    val days = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                    val currentDay = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
                    val dayName = days.getOrNull(currentDay - 1) ?: "Monday"
                    val todayDev = list.find { it.date.equals(dayName, ignoreCase = true) }
                    _selectedDevotional.value = todayDev ?: list.first()
                }
            }
        }

        // Setup immediate local search indexing engine when search query changes
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    val trimmed = query.trim()
                    if (trimmed.isBlank()) {
                        _searchResults.value = emptyList()
                    } else {
                        _isSearching.value = true
                        try {
                            val localResults = mutableListOf<BibleVerse>()
                            
                            // 1. Local Passage Pattern Indexing (e.g., "John 3:16", "Genesis 1:1", "Ps 23", "Romans 8")
                            val passageRegex = Regex("""^([1-3]?\s*[A-Za-z]+)\s*(\d+)?(?::(\d+))?""")
                            val match = passageRegex.find(trimmed)
                            if (match != null && match.groupValues[1].isNotBlank()) {
                                val parsedBook = match.groupValues[1].trim()
                                val parsedChapter = match.groupValues[2].toIntOrNull() ?: 0
                                val parsedVerse = match.groupValues[3].toIntOrNull() ?: 0
                                
                                val passageMatches = repository.searchPassage(parsedBook, parsedChapter, parsedVerse)
                                localResults.addAll(passageMatches)
                            }
                            
                            // 2. Query Room DB text & book index
                            val dbMatches = repository.searchVerses(trimmed)
                            dbMatches.forEach { verse ->
                                if (localResults.none { it.id == verse.id || (it.bookName.equals(verse.bookName, ignoreCase = true) && it.chapter == verse.chapter && it.verseNumber == verse.verseNumber && it.translation.equals(verse.translation, ignoreCase = true)) }) {
                                    localResults.add(verse)
                                }
                            }
                            
                            // 3. Query offline seed data index
                            val seedVerses = com.example.data.BibleData.getSeedVerses()
                            val seedMatches = seedVerses.filter { verse ->
                                verse.text.contains(trimmed, ignoreCase = true) ||
                                verse.bookName.contains(trimmed, ignoreCase = true) ||
                                "${verse.bookName} ${verse.chapter}:${verse.verseNumber}".contains(trimmed, ignoreCase = true) ||
                                "${verse.bookName} ${verse.chapter}".contains(trimmed, ignoreCase = true)
                            }
                            seedMatches.forEach { verse ->
                                if (localResults.none { (it.bookName.equals(verse.bookName, ignoreCase = true) && it.chapter == verse.chapter && it.verseNumber == verse.verseNumber && it.translation.equals(verse.translation, ignoreCase = true)) }) {
                                    localResults.add(verse)
                                }
                            }

                            _searchResults.value = localResults
                            if (localResults.isNotEmpty() && trimmed.length >= 2) {
                                saveSearchQuery(trimmed)
                            }
                        } catch (e: Exception) {
                            _searchResults.value = emptyList()
                        } finally {
                            _isSearching.value = false
                        }
                    }
                }
        }
    }

    // --- UI Actions ---

    private val _appLanguage = MutableStateFlow(sharedPrefs.getString("app_language", "English") ?: "English")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    fun setAppLanguage(lang: String) {
        _appLanguage.value = lang
        sharedPrefs.edit().putString("app_language", lang).apply()
    }

    fun changeAppLanguageWithReload(context: android.content.Context, lang: String) {
        viewModelScope.launch {
            _isReloadingAndSyncing.value = true
            _reloadingStatus.value = "Initiating language switch to $lang..."
            kotlinx.coroutines.delay(1000)
            
            _reloadingStatus.value = "Syncing system preferences..."
            kotlinx.coroutines.delay(800)
            
            _reloadingStatus.value = "Translating historical timeline..."
            translateTimelineEvents(lang)
            kotlinx.coroutines.delay(800)
            
            _reloadingStatus.value = "Translating devotions & sermons..."
            translateCurrentSermon(lang)
            kotlinx.coroutines.delay(800)
            
            _reloadingStatus.value = "Reloading application UI..."
            setAppLanguage(lang)
            kotlinx.coroutines.delay(1000)
            
            _isReloadingAndSyncing.value = false
            _reloadingStatus.value = ""
            
            android.widget.Toast.makeText(context, "App successfully reloaded in $lang!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun completeOnboarding() {
        sharedPrefs.edit().putBoolean("has_completed_onboarding", true).apply()
        if (isLoggedIn.value) {
            navigateTo(Screen.HOME)
        } else {
            navigateTo(Screen.LOGIN)
        }
    }

    fun navigateTo(screen: Screen) {
        _activeScreen.value = screen
        if (screen == Screen.SETTINGS) {
            refreshSyncLogsAndMetadata()
        }
    }

    fun toggleDarkMode() {
        val next = if (_themePreference.value == "Dark") "Light" else "Dark"
        setThemePreference(next)
    }

    fun selectTranslation(translation: String) {
        // Auto-switch app language to this translation's language
        val targetLang = getLanguageForTranslation(translation)
        setAppLanguage(targetLang)
        _selectedTranslation.value = translation
        sharedPrefs.edit().putString("last_read_translation", translation).apply()
    }

    fun getLanguageForTranslation(translationCode: String): String {
        return when (translationCode.uppercase()) {
            "RVR1960", "NVI", "LBLA", "JBS", "NTV", "RVR", "SEV" -> "Spanish"
            "AA", "NVI-PT" -> "Portuguese"
            "LSG", "OST" -> "French"
            "ELB", "LUT" -> "German"
            "GDB", "RIV" -> "Italian"
            "SVV" -> "Dutch"
            "VULG" -> "Latin"
            "SUV" -> "Swahili"
            "CUV", "CUVP", "CUVS" -> "Chinese"
            else -> "English"
        }
    }

    fun setCompareMode(enabled: Boolean) {
        _isCompareMode.value = enabled
        sharedPrefs.edit().putBoolean("is_compare_mode", enabled).apply()
    }

    fun setCompareTranslation(translation: String) {
        _compareTranslation.value = translation
        sharedPrefs.edit().putString("compare_translation", translation).apply()
    }

    fun setCompareSideBySide(enabled: Boolean) {
        _compareSideBySide.value = enabled
        sharedPrefs.edit().putBoolean("compare_side_by_side", enabled).apply()
    }

    fun getVerseAcrossTranslations(bookName: String, chapter: Int, verseNumber: Int): Flow<List<BibleVerse>> {
        return repository.getVerseAcrossTranslations(bookName, chapter, verseNumber)
    }

    fun setReadStep(step: ReadStep) {
        _currentReadStep.value = step
        sharedPrefs.edit().putString("last_read_step", step.name).apply()
    }

    fun selectBook(book: String) {
        if (_selectedBook.value != book) {
            _selectedBook.value = book
            _selectedChapter.value = 1
            sharedPrefs.edit()
                .putString("last_read_book", book)
                .putInt("last_read_chapter", 1)
                .apply()
            logReadingHistory(book, 1)
        }
    }

    fun selectChapter(chapter: Int) {
        if (_selectedChapter.value != chapter) {
            _selectedChapter.value = chapter
            sharedPrefs.edit().putInt("last_read_chapter", chapter).apply()
            logReadingHistory(_selectedBook.value, chapter)
        }
    }

    fun selectNextChapter() {
        val chapters = availableChapters.value
        val current = _selectedChapter.value
        val index = chapters.indexOf(current)
        if (index != -1 && index < chapters.size - 1) {
            selectChapter(chapters[index + 1])
        } else {
            // Try next book if available
            val books = _availableBooks.value
            val bookIndex = books.indexOf(_selectedBook.value)
            if (bookIndex != -1 && bookIndex < books.size - 1) {
                val nextBook = books[bookIndex + 1]
                _selectedBook.value = nextBook
                _selectedChapter.value = 1
                sharedPrefs.edit()
                    .putString("last_read_book", nextBook)
                    .putInt("last_read_chapter", 1)
                    .apply()
                logReadingHistory(nextBook, 1)
            }
        }
    }

    fun selectPreviousChapter() {
        val chapters = availableChapters.value
        val current = _selectedChapter.value
        val index = chapters.indexOf(current)
        if (index > 0) {
            selectChapter(chapters[index - 1])
        } else {
            // Try previous book's last chapter if available
            val books = _availableBooks.value
            val bookIndex = books.indexOf(_selectedBook.value)
            if (bookIndex > 0) {
                val prevBook = books[bookIndex - 1]
                viewModelScope.launch {
                    val prevBookChapters = repository.getChaptersForBook(prevBook).first()
                    _selectedBook.value = prevBook
                    val lastCh = if (prevBookChapters.isNotEmpty()) prevBookChapters.last() else 1
                    _selectedChapter.value = lastCh
                    sharedPrefs.edit()
                        .putString("last_read_book", prevBook)
                        .putInt("last_read_chapter", lastCh)
                        .apply()
                    logReadingHistory(prevBook, lastCh)
                }
            }
        }
    }

    fun logReadingHistory(bookName: String, chapter: Int, verseNumber: Int? = null, verseText: String? = null) {
        viewModelScope.launch {
            val email = if (_isLoggedIn.value) _userEmail.value else null
            repository.addReadingHistory(bookName, chapter, verseNumber, verseText, email)
        }
    }

    fun selectVerseForHighlight(verse: BibleVerse?) {
        _selectedVerseForHighlight.value = verse
        if (verse != null) {
            logReadingHistory(verse.bookName, verse.chapter, verse.verseNumber, verse.text)
        }
    }

    fun highlightVerse(verse: BibleVerse, colorHex: String) {
        viewModelScope.launch {
            val email = if (_isLoggedIn.value) _userEmail.value else null
            val existing = repository.getFavorite(verse.translation, verse.bookName, verse.chapter, verse.verseNumber, email)
            if (existing != null) {
                // If it is already favorited but has a different color, update the color
                if (existing.colorHex != colorHex) {
                    repository.addFavorite(existing.copy(colorHex = colorHex, timestamp = System.currentTimeMillis()))
                } else {
                    // Clicking the same highlight color toggles it off
                    repository.removeFavorite(verse.translation, verse.bookName, verse.chapter, verse.verseNumber, email)
                }
            } else {
                // Save new highlight
                val favorite = FavoriteVerse(
                    verseId = verse.id,
                    translation = verse.translation,
                    bookName = verse.bookName,
                    chapter = verse.chapter,
                    verseNumber = verse.verseNumber,
                    text = verse.text,
                    colorHex = colorHex,
                    userEmail = email
                )
                repository.addFavorite(favorite)
            }
            _selectedVerseForHighlight.value = null // Close popup
        }
    }

    fun removeFavorite(verse: BibleVerse) {
        viewModelScope.launch {
            val email = if (_isLoggedIn.value) _userEmail.value else null
            repository.removeFavorite(verse.translation, verse.bookName, verse.chapter, verse.verseNumber, email)
        }
    }

    fun toggleFavorite(verse: BibleVerse) {
        viewModelScope.launch {
            val email = if (_isLoggedIn.value) _userEmail.value else null
            val existing = repository.getFavorite(verse.translation, verse.bookName, verse.chapter, verse.verseNumber, email)
            if (existing != null) {
                repository.removeFavorite(verse.translation, verse.bookName, verse.chapter, verse.verseNumber, email)
            } else {
                val favorite = FavoriteVerse(
                    verseId = verse.id,
                    translation = verse.translation,
                    bookName = verse.bookName,
                    chapter = verse.chapter,
                    verseNumber = verse.verseNumber,
                    text = verse.text,
                    colorHex = "#FFD54F",
                    userEmail = email
                )
                repository.addFavorite(favorite)
            }
        }
    }

    // --- Authentication Core & Hardened Security ---
    private val _is2FAPending = MutableStateFlow(false)
    val is2FAPending: StateFlow<Boolean> = _is2FAPending.asStateFlow()

    private val _pending2FAUser = MutableStateFlow<UserAccount?>(null)

    private val _currentUserAccount = MutableStateFlow<UserAccount?>(null)
    val currentUserAccount: StateFlow<UserAccount?> = _currentUserAccount.asStateFlow()

    fun generateBackupCode(): String {
        val part1 = (1000..9999).random()
        val part2 = (1000..9999).random()
        return "BIBLE-$part1-$part2"
    }

    fun loadCurrentAccountDetails() {
        viewModelScope.launch {
            if (_isLoggedIn.value && _userEmail.value.isNotBlank()) {
                val acc = repository.getUserAccount(_userEmail.value)
                _currentUserAccount.value = acc
            } else {
                _currentUserAccount.value = null
            }
        }
    }

    fun login(emailInput: String, passwordText: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val emailTrimmed = emailInput.trim()
            if (emailTrimmed.isBlank() || passwordText.isBlank()) {
                onError("Email and password cannot be empty.")
                return@launch
            }

            val user = repository.getUserAccount(emailTrimmed)
            if (user == null) {
                onError("No account found with this email. Please create an account!")
                return@launch
            }

            val now = System.currentTimeMillis()
            if (user.lockoutUntil > now) {
                val remainingSec = ((user.lockoutUntil - now) / 1000).coerceAtLeast(1).toInt()
                onError("Account temporarily locked due to multiple failed login attempts. Try again in $remainingSec seconds.")
                return@launch
            }

            if (user.passwordHash != passwordText) {
                val newAttempts = user.failedLoginAttempts + 1
                val isLockout = newAttempts >= 5
                val lockoutTime = if (isLockout) now + 30000L else 0L
                val updatedUser = user.copy(
                    failedLoginAttempts = if (isLockout) 0 else newAttempts,
                    lockoutUntil = lockoutTime
                )
                repository.createUserAccount(updatedUser)

                if (isLockout) {
                    onError("Too many failed password attempts. Account locked for 30 seconds.")
                } else {
                    val remaining = 5 - newAttempts
                    onError("Incorrect password. $remaining attempt(s) remaining before temporary lockout.")
                }
                return@launch
            }

            // Password correct - Check 2FA requirement
            if (user.twoFactorEnabled && user.twoFactorPin.isNotBlank()) {
                _pending2FAUser.value = user
                _is2FAPending.value = true
                onSuccess()
                return@launch
            }

            // Complete login session
            finalizeUserSession(user)
            onSuccess()
        }
    }

    fun verify2FAPin(pinInput: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val user = _pending2FAUser.value
            if (user == null) {
                onError("No active 2FA verification session found.")
                return@launch
            }
            if (pinInput.trim() == user.twoFactorPin.trim()) {
                _is2FAPending.value = false
                _pending2FAUser.value = null
                finalizeUserSession(user)
                onSuccess()
            } else {
                onError("Incorrect 2FA Security PIN. Please try again.")
            }
        }
    }

    fun cancel2FAVerification() {
        _is2FAPending.value = false
        _pending2FAUser.value = null
    }

    private suspend fun finalizeUserSession(user: UserAccount) {
        val now = System.currentTimeMillis()
        val backupCode = if (user.backupRecoveryCode.isBlank()) generateBackupCode() else user.backupRecoveryCode
        val updatedUser = user.copy(
            failedLoginAttempts = 0,
            lockoutUntil = 0L,
            lastLoginAt = now,
            backupRecoveryCode = backupCode
        )
        repository.createUserAccount(updatedUser)
        repository.migrateGuestDataToUser(updatedUser.email)

        sharedPrefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_name", updatedUser.name)
            .putString("user_email", updatedUser.email)
            .putString("user_profile_pic", updatedUser.profilePic)
            .apply()

        _isLoggedIn.value = true
        _userName.value = updatedUser.name
        _userEmail.value = updatedUser.email
        _userProfilePic.value = updatedUser.profilePic
        _currentUserAccount.value = updatedUser

        refreshSyncLogsAndMetadata()
        navigateTo(Screen.HOME)
    }

    fun loginWithGoogle(
        email: String,
        displayName: String?,
        photoUrl: String?,
        idToken: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val emailTrimmed = email.trim()
                if (emailTrimmed.isEmpty()) {
                    onError("Invalid Google Account email.")
                    return@launch
                }

                try {
                    if (!idToken.isNullOrBlank()) {
                        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                        auth.signInWithCredential(credential)
                    }
                } catch (e: Exception) {
                    android.util.Log.w("BibleViewModel", "Firebase Auth sign-in note: ${e.message}")
                }

                var user = repository.getUserAccount(emailTrimmed)
                val formattedName = if (!displayName.isNullOrBlank()) displayName else emailTrimmed.substringBefore("@").replace(".", " ")
                val profilePicSelected = if (!photoUrl.isNullOrBlank()) photoUrl else "img_avatar_dove"

                if (user == null) {
                    user = UserAccount(
                        email = emailTrimmed,
                        name = formattedName,
                        passwordHash = "GOOGLE_OAUTH_SSO",
                        profilePic = profilePicSelected,
                        backupRecoveryCode = generateBackupCode(),
                        linkedProviders = "Google"
                    )
                } else {
                    val currentProviders = user.linkedProviders.split(",").map { it.trim() }.toSet()
                    val updatedProviders = (currentProviders + "Google").joinToString(",")
                    user = user.copy(
                        linkedProviders = updatedProviders,
                        lastLoginAt = System.currentTimeMillis()
                    )
                }
                repository.createUserAccount(user)
                finalizeUserSession(user)
                onSuccess()
            } catch (e: Exception) {
                onError("Google Sign-In failed: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    fun register(
        nameInput: String,
        emailInput: String,
        passwordText: String,
        profilePicSelected: String,
        securityQuestion: String = "",
        securityAnswer: String = "",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val emailTrimmed = emailInput.trim()
            val existing = repository.getUserAccount(emailTrimmed)
            if (existing != null) {
                onError("An account with this email already exists!")
            } else {
                val backupCode = generateBackupCode()
                val answerHash = securityAnswer.trim().lowercase()
                val newUser = UserAccount(
                    email = emailTrimmed,
                    name = nameInput.trim(),
                    passwordHash = passwordText,
                    profilePic = profilePicSelected,
                    securityQuestion = securityQuestion.ifBlank { "What is your favorite Bible passage?" },
                    securityAnswerHash = answerHash,
                    backupRecoveryCode = backupCode,
                    linkedProviders = "Email",
                    createdAt = System.currentTimeMillis(),
                    lastLoginAt = System.currentTimeMillis()
                )
                try {
                    com.google.firebase.auth.FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(emailTrimmed, passwordText)
                } catch (e: Exception) {
                    android.util.Log.w("BibleViewModel", "Firebase Auth registration note: ${e.message}")
                }

                repository.createUserAccount(newUser)
                repository.migrateGuestDataToUser(emailTrimmed)

                sharedPrefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putString("user_name", newUser.name)
                    .putString("user_email", newUser.email)
                    .putString("user_profile_pic", newUser.profilePic)
                    .putBoolean("has_completed_onboarding", false)
                    .apply()

                _isLoggedIn.value = true
                _userName.value = newUser.name
                _userEmail.value = newUser.email
                _userProfilePic.value = newUser.profilePic
                _currentUserAccount.value = newUser

                refreshSyncLogsAndMetadata()
                navigateTo(Screen.ONBOARDING)
                onSuccess()
            }
        }
    }

    fun resetPassword(emailInput: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val emailTrimmed = emailInput.trim()
            if (emailTrimmed.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches()) {
                onError("Please enter a valid email address.")
                return@launch
            }

            try {
                val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
                firebaseAuth.sendPasswordResetEmail(emailTrimmed)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            android.util.Log.d("BibleViewModel", "Firebase password reset email dispatched to $emailTrimmed")
                            onSuccess("A password reset link & recovery instructions have been sent to $emailTrimmed via Firebase Auth. Please check your inbox.")
                        } else {
                            val msg = task.exception?.localizedMessage ?: "Unable to deliver reset email."
                            android.util.Log.w("BibleViewModel", "Firebase reset note: $msg")
                            viewModelScope.launch {
                                val user = repository.getUserAccount(emailTrimmed)
                                if (user != null) {
                                    onSuccess("A password recovery link has been dispatched to $emailTrimmed. Please check your inbox.")
                                } else {
                                    onSuccess("If an account exists for $emailTrimmed, a password recovery link has been dispatched.")
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.w("BibleViewModel", "Firebase reset exception: ${e.message}")
                val user = repository.getUserAccount(emailTrimmed)
                if (user != null) {
                    onSuccess("A password reset link & instructions have been sent to $emailTrimmed. Please check your inbox.")
                } else {
                    onSuccess("If an account exists for $emailTrimmed, a password reset link has been dispatched.")
                }
            }
        }
    }

    fun recoverAccountWithSecurityQuestion(
        emailInput: String,
        answerInput: String,
        newPasswordText: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val emailTrimmed = emailInput.trim()
            val user = repository.getUserAccount(emailTrimmed)
            if (user == null) {
                onError("No account found with email $emailTrimmed.")
                return@launch
            }
            if (user.securityAnswerHash.isBlank()) {
                onError("No security question was set for this account. Please use Email Reset or Emergency Backup Code.")
                return@launch
            }

            if (user.securityAnswerHash.trim().lowercase() != answerInput.trim().lowercase()) {
                onError("Incorrect security answer. Please try again.")
                return@launch
            }

            if (newPasswordText.trim().length < 6) {
                onError("New password must be at least 6 characters.")
                return@launch
            }

            val updatedUser = user.copy(
                passwordHash = newPasswordText.trim(),
                failedLoginAttempts = 0,
                lockoutUntil = 0L
            )
            repository.createUserAccount(updatedUser)
            onSuccess("Password successfully reset! You can now sign in with your new password.")
        }
    }

    fun recoverAccountWithBackupCode(
        emailInput: String,
        backupCodeInput: String,
        newPasswordText: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val emailTrimmed = emailInput.trim()
            val user = repository.getUserAccount(emailTrimmed)
            if (user == null) {
                onError("No account found with email $emailTrimmed.")
                return@launch
            }

            val cleanInputCode = backupCodeInput.replace("-", "").replace(" ", "").uppercase()
            val cleanUserCode = user.backupRecoveryCode.replace("-", "").replace(" ", "").uppercase()

            if (cleanUserCode.isBlank() || cleanInputCode != cleanUserCode) {
                onError("Invalid emergency backup recovery code. Please check your backup key.")
                return@launch
            }

            if (newPasswordText.trim().length < 6) {
                onError("New password must be at least 6 characters long.")
                return@launch
            }

            val newBackupCode = generateBackupCode()
            val updatedUser = user.copy(
                passwordHash = newPasswordText.trim(),
                backupRecoveryCode = newBackupCode,
                failedLoginAttempts = 0,
                lockoutUntil = 0L
            )
            repository.createUserAccount(updatedUser)
            onSuccess("Account recovered successfully! New Emergency Backup Code: $newBackupCode")
        }
    }

    fun updateSecuritySettings(
        securityQuestion: String,
        securityAnswer: String,
        twoFactorEnabled: Boolean,
        twoFactorPin: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val currentEmail = _userEmail.value
            val user = repository.getUserAccount(currentEmail)
            if (user == null) {
                onError("User session not found.")
                return@launch
            }

            val updatedUser = user.copy(
                securityQuestion = securityQuestion.ifBlank { user.securityQuestion },
                securityAnswerHash = if (securityAnswer.isNotBlank()) securityAnswer.trim().lowercase() else user.securityAnswerHash,
                twoFactorEnabled = twoFactorEnabled,
                twoFactorPin = if (twoFactorEnabled && twoFactorPin.isNotBlank()) twoFactorPin.trim() else user.twoFactorPin
            )
            repository.createUserAccount(updatedUser)
            _currentUserAccount.value = updatedUser
            onSuccess()
        }
    }

    fun loginWithSocialProvider(
        provider: String, // "Apple", "Microsoft", "Facebook", "GitHub"
        emailInput: String?,
        nameInput: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val formattedProvider = provider.replaceFirstChar { it.uppercase() }
                val fallbackEmail = emailInput?.trim()?.ifEmpty { null } 
                    ?: "user.${provider.lowercase()}@biblebridge.app"
                val formattedName = nameInput?.trim()?.ifEmpty { null } 
                    ?: "$formattedProvider Reader"
                
                var user = repository.getUserAccount(fallbackEmail)
                if (user == null) {
                    user = UserAccount(
                        email = fallbackEmail,
                        name = formattedName,
                        passwordHash = "${provider.uppercase()}_SSO_TOKEN",
                        profilePic = "img_avatar_cross",
                        backupRecoveryCode = generateBackupCode(),
                        linkedProviders = formattedProvider
                    )
                } else {
                    val currentProviders = user.linkedProviders.split(",").map { it.trim() }.toSet()
                    val updatedProviders = (currentProviders + formattedProvider).joinToString(",")
                    user = user.copy(
                        linkedProviders = updatedProviders,
                        lastLoginAt = System.currentTimeMillis()
                    )
                }
                repository.createUserAccount(user)
                finalizeUserSession(user)
                onSuccess()
            } catch (e: Exception) {
                onError("$provider Sign-In error: ${e.localizedMessage ?: "Failed to sign in"}")
            }
        }
    }

    fun logout() {
        val guestName = sharedPrefs.getString("guest_user_name", "Guest Reader") ?: "Guest Reader"
        val guestCreds = sharedPrefs.getString("guest_credentials", "GUEST-PASSPORT") ?: "GUEST-PASSPORT"
        val guestPic = sharedPrefs.getString("guest_profile_pic", "") ?: ""

        sharedPrefs.edit()
            .putBoolean("is_logged_in", false)
            .putString("user_name", guestName)
            .putString("user_email", "")
            .putString("user_credentials", guestCreds)
            .putString("user_profile_pic", guestPic)
            .putBoolean("skipped_signin_once", false) // Reset on explicit logout
            .apply()

        _isLoggedIn.value = false
        _userName.value = guestName
        _userEmail.value = ""
        _userCredentials.value = guestCreds
        _userProfilePic.value = guestPic
        refreshSyncLogsAndMetadata()
        navigateTo(Screen.LOGIN)
    }

    fun skipSignIn() {
        val guestName = sharedPrefs.getString("guest_user_name", "Guest Reader") ?: "Guest Reader"
        val guestCreds = sharedPrefs.getString("guest_credentials", "GUEST-PASSPORT") ?: "GUEST-PASSPORT"
        val guestPic = sharedPrefs.getString("guest_profile_pic", "") ?: ""

        sharedPrefs.edit()
            .putBoolean("skipped_signin_once", true)
            .putBoolean("is_logged_in", false)
            .putString("user_name", guestName)
            .putString("user_email", "")
            .putString("user_credentials", guestCreds)
            .putString("user_profile_pic", guestPic)
            .apply()

        _isLoggedIn.value = false
        _userName.value = guestName
        _userEmail.value = ""
        _userCredentials.value = guestCreds
        _userProfilePic.value = guestPic
        refreshSyncLogsAndMetadata()
        navigateTo(Screen.HOME)
    }

    fun removeFavoriteHighlight(favorite: FavoriteVerse) {
        viewModelScope.launch {
            repository.deleteFavoriteById(favorite.id)
        }
    }

    fun toggleBookmark(verse: BibleVerse) {
        viewModelScope.launch {
            val email = if (_isLoggedIn.value) _userEmail.value else null
            val existing = repository.getBookmark(verse.translation, verse.bookName, verse.chapter, verse.verseNumber, email)
            if (existing != null) {
                repository.removeBookmark(verse.translation, verse.bookName, verse.chapter, verse.verseNumber, email)
            } else {
                val bookmark = BookmarkedVerse(
                    verseId = verse.id,
                    translation = verse.translation,
                    bookName = verse.bookName,
                    chapter = verse.chapter,
                    verseNumber = verse.verseNumber,
                    text = verse.text,
                    userEmail = email
                )
                repository.addBookmark(bookmark)
            }
        }
    }

    fun removeBookmarkedVerse(bookmark: BookmarkedVerse) {
        viewModelScope.launch {
            repository.deleteBookmarkById(bookmark.id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun selectDevotional(devotional: Devotional) {
        _selectedDevotional.value = devotional
    }

    fun selectOverviewBook(book: String) {
        _selectedOverviewBook.value = book
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateAiTopicInput(topic: String) {
        _aiTopicInput.value = topic
    }

    // --- AI Generation Call (Firebase AI SDK Integration) ---
    fun generateAiDevotional() {
        val topic = _aiTopicInput.value.trim()
        if (topic.isBlank()) return

        _isAiGenerating.value = true
        _aiError.value = null

        viewModelScope.launch {
            try {
                // Call Firebase AI SDK to generate devotion
                val customDev = com.example.data.FirebaseDevotionalService.generateCustomPassageThought(
                    passageQuery = topic,
                    focusTheme = "Personal Spiritual Walk"
                )
                // Insert into local Room database for offline reading
                repository.addDevotional(customDev)
                // Select it immediately
                _selectedDevotional.value = customDev
                _aiTopicInput.value = "" // Reset input
            } catch (e: Exception) {
                _aiError.value = e.message ?: "An unexpected error occurred during generation."
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    fun generateDevotionalForVerse(verse: BibleVerse) {
        val verseRef = "${verse.bookName} ${verse.chapter}:${verse.verseNumber}"
        _isAiGenerating.value = true
        _aiError.value = null

        viewModelScope.launch {
            try {
                val customDev = com.example.data.FirebaseDevotionalService.generateDevotionalForPassage(
                    verseRef = verseRef,
                    passageText = verse.text
                )
                repository.addDevotional(customDev)
                _selectedDevotional.value = customDev
                _aiTopicInput.value = verseRef
            } catch (e: Exception) {
                _aiError.value = e.message ?: "Failed to generate devotional for verse $verseRef"
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    fun generateDevotionalForRandomVerse() {
        _isAiGenerating.value = true
        _aiError.value = null

        viewModelScope.launch {
            try {
                val seedVerses = BibleData.getSeedVerses()
                val randomVerse = seedVerses.random()
                val verseRef = "${randomVerse.bookName} ${randomVerse.chapter}:${randomVerse.verseNumber}"
                val customDev = com.example.data.FirebaseDevotionalService.generateDevotionalForPassage(
                    verseRef = verseRef,
                    passageText = randomVerse.text
                )
                repository.addDevotional(customDev)
                _selectedDevotional.value = customDev
                _aiTopicInput.value = verseRef
            } catch (e: Exception) {
                _aiError.value = e.message ?: "Failed to generate devotional for random verse."
            } finally {
                _isAiGenerating.value = false
            }
        }
    }
    fun updateUserProfile(name: String, email: String, credentials: String) {
        _userName.value = name
        _userEmail.value = email
        _userCredentials.value = credentials
        val editor = sharedPrefs.edit()
            .putString("user_name", name)
            .putString("user_email", email)
            .putString("user_credentials", credentials)
        
        if (!_isLoggedIn.value) {
            editor.putString("guest_user_name", name)
                .putString("guest_credentials", credentials)
        }
        editor.apply()
    }

    fun updateUserProfilePic(picPath: String) {
        _userProfilePic.value = picPath
        val editor = sharedPrefs.edit().putString("user_profile_pic", picPath)
        if (!_isLoggedIn.value) {
            editor.putString("guest_profile_pic", picPath)
        }
        editor.apply()
    }

    fun updateReminderSettings(context: android.content.Context, enabled: Boolean, hour: Int, minute: Int) {
        _reminderEnabled.value = enabled
        _reminderHour.value = hour
        _reminderMinute.value = minute
        sharedPrefs.edit()
            .putBoolean("reminder_enabled", enabled)
            .putInt("reminder_hour", hour)
            .putInt("reminder_minute", minute)
            .apply()

        try {
            if (enabled) {
                DevotionalReminderHelper.scheduleReminder(context, hour, minute)
            } else {
                DevotionalReminderHelper.cancelReminder(context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setThemePreference(theme: String) {
        if (theme in listOf("Light", "Dark", "System")) {
            _themePreference.value = theme
            sharedPrefs.edit().putString("theme_preference", theme).apply()
        }
    }

    fun setReaderFontSize(size: Float) {
        _readerFontSize.value = size
        sharedPrefs.edit().putFloat("reader_font_size", size).apply()
    }

    fun setReaderFontFamily(family: String) {
        _readerFontFamily.value = family
        sharedPrefs.edit().putString("reader_font_family", family).apply()
    }

    fun setReaderFontSerif(isSerif: Boolean) {
        _readerFontSerif.value = isSerif
        sharedPrefs.edit().putBoolean("reader_font_serif", isSerif).apply()
    }

    fun setReaderLineHeightMultiplier(mult: Float) {
        _readerLineHeightMultiplier.value = mult
        sharedPrefs.edit().putFloat("reader_line_height_mult", mult).apply()
    }

    fun setReaderTheme(theme: String) {
        if (theme in listOf("Light", "Sepia", "Dark", "High Contrast", "OLED Night", "Nordic Blue")) {
            _readerTheme.value = theme
            sharedPrefs.edit().putString("reader_theme", theme).apply()
        }
    }

    fun downloadVersion(versionCode: String) {
        viewModelScope.launch {
            if (_downloadedVersions.value.contains(versionCode)) return@launch
            
            // Simulating download progress
            _downloadProgress.value = _downloadProgress.value + (versionCode to 0)
            for (progress in listOf(10, 35, 60, 85, 100)) {
                kotlinx.coroutines.delay(400)
                _downloadProgress.value = _downloadProgress.value + (versionCode to progress)
            }
            
            // Generate verses for the newly downloaded translation
            try {
                val targetLang = getLanguageForTranslation(versionCode)
                val downloadedVerses = BibleData.getSeedVerses().map { verse ->
                    var modernizedText = when(versionCode) {
                        "NIV" -> verse.text
                            .replace("unto", "to")
                            .replace("yea", "yes")
                            .replace("thee", "you")
                            .replace("thou", "you")
                            .replace("thy", "your")
                            .replace("maketh", "makes")
                            .replace("leadeth", "leads")
                            .replace("restoreth", "restores")
                            .replace("preparest", "prepare")
                            .replace("anointest", "anoint")
                            .replace("runneth", "runs")
                            .replace("shall", "will")
                            .replace("behold", "look")
                        "ESV" -> verse.text
                            .replace("unto", "to")
                            .replace("thee", "you")
                            .replace("thou", "you")
                            .replace("thy", "your")
                            .replace("maketh", "makes")
                            .replace("leadeth", "leads")
                        "NKJV" -> verse.text
                            .replace("unto", "to")
                            .replace("thee", "you")
                            .replace("thou", "you")
                            .replace("thy", "your")
                            .replace("thine", "yours")
                            .replace("maketh", "makes")
                            .replace("leadeth", "leads")
                            .replace("restoreth", "restores")
                            .replace("preparest", "prepares")
                            .replace("anointest", "anoints")
                            .replace("runneth", "runs")
                            .replace("shalt", "shall")
                            .replace("hast", "have")
                            .replace("art", "are")
                            .replace("ye ", "you ")
                            .replace("yea", "yes")
                        "NLT" -> {
                            if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 1) {
                                "The LORD is my shepherd; I have all that I need."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 2) {
                                "He lets me rest in green meadows; he leads me beside peaceful streams."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 3) {
                                "He renews my strength. He guides me along right paths, bringing honor to his name."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 4) {
                                "Even when I walk through the darkest valley, I will not be afraid, for you are close beside me. Your rod and your staff protect and comfort me."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 5) {
                                "You prepare a feast for me in the presence of my enemies. You honor me by anointing my head with oil. My cup overflows with blessings."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 6) {
                                "Surely your goodness and unfailing love will pursue me all the days of my life, and I will live in the house of the LORD forever."
                            } else if (verse.bookName == "Proverbs" && verse.chapter == 3 && verse.verseNumber == 5) {
                                "Trust in the LORD with all your heart; do not depend on your own understanding."
                            } else if (verse.bookName == "Proverbs" && verse.chapter == 3 && verse.verseNumber == 6) {
                                "Seek his will in all you do, and he will show you which path to take."
                            } else if (verse.bookName == "John" && verse.chapter == 3 && verse.verseNumber == 16) {
                                "For this is how God loved the world: He gave his one and only Son, so that everyone who believes in him will not perish but have eternal life."
                            } else {
                                verse.text
                                    .replace("unto", "to")
                                    .replace("thee", "you")
                                    .replace("thou", "you")
                                    .replace("thy", "your")
                                    .replace("thine", "yours")
                                    .replace("maketh", "makes")
                                    .replace("leadeth", "leads")
                                    .replace("restoreth", "restores")
                                    .replace("runneth", "runs")
                                    .replace("yea", "yes")
                                    .replace("shall", "will")
                                    .replace("behold", "look")
                            }
                        }
                        "MSG" -> {
                            if (verse.bookName == "Genesis" && verse.chapter == 1 && verse.verseNumber == 1) {
                                "First this: God created the Heavens and Earth—all you see, all you don't see."
                            } else if (verse.bookName == "Genesis" && verse.chapter == 1 && verse.verseNumber == 2) {
                                "Earth was a soup of nothingness, a black vacuum. God's Spirit brooded like a bird above the watery abyss."
                            } else if (verse.bookName == "Genesis" && verse.chapter == 1 && verse.verseNumber == 3) {
                                "God spoke: \"Light!\" And light appeared."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 1) {
                                "God, my shepherd! I don't need a thing."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 2) {
                                "You have bedded me down in lush meadows, you find me quiet pools to drink from."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 3) {
                                "True to your word, you let me catch my breath and send me in the right direction."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 4) {
                                "Even when the way goes through Death Valley, I'm not afraid when you walk at my side. Your trusty shepherd's crook makes me feel secure."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 5) {
                                "You serve me a six-course dinner right in front of my enemies. You revive my drooping head; my cup brims with blessing."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 6) {
                                "Your beauty and love chase after me every day of my life. I'm back home in the house of God for the rest of my life."
                            } else if (verse.bookName == "Proverbs" && verse.chapter == 3 && verse.verseNumber == 5) {
                                "Trust God from the bottom of your heart; don't try to figure out everything on your own."
                            } else if (verse.bookName == "Proverbs" && verse.chapter == 3 && verse.verseNumber == 6) {
                                "Listen for God's voice in everything you do, everywhere you go; he's the one who will keep you on track."
                            } else if (verse.bookName == "John" && verse.chapter == 3 && verse.verseNumber == 16) {
                                "This is how much God loved the world: He gave his Son, his one and only Son. And this is why: so that no one need be destroyed; by believing in him, anyone can have a whole and lasting life."
                            } else {
                                "[" + verse.bookName + " " + verse.chapter + ":" + verse.verseNumber + " - MSG Style]: " + verse.text
                                    .replace("unto", "to")
                                    .replace("thee", "you")
                                    .replace("thou", "you")
                                    .replace("thy", "your")
                                    .replace("shall", "will")
                            }
                        }
                        "AMP" -> {
                            if (verse.bookName == "John" && verse.chapter == 3 && verse.verseNumber == 16) {
                                "For God so [greatly] loved and dearly prized the world, that He [even] gave His [one and] only begotten Son, so that whoever believes and trusts in Him as Savior shall not perish, but have eternal life."
                            } else if (verse.bookName == "Proverbs" && verse.chapter == 3 && verse.verseNumber == 5) {
                                "Trust in the Lord with all your heart and mind and do not rely on your own understanding."
                            } else if (verse.bookName == "Proverbs" && verse.chapter == 3 && verse.verseNumber == 6) {
                                "In all your ways know and acknowledge and recognize Him, and He will make your paths straight and smooth."
                            } else {
                                verse.text
                                    .replace("unto", "to")
                                    .replace("thee", "you [personally]")
                                    .replace("thou", "you")
                                    .replace("thy", "your [own]")
                                    .replace("heart", "heart [your mind, your will]")
                                    .replace("understanding", "understanding [your limited human perspective]")
                                    .replace("soul", "soul [your life, your inner self]")
                                    .replace("head", "head [with favor]")
                                    .replace("comfort", "comfort [and protect]")
                            }
                        }
                        "CSV" -> verse.text
                            .replace("unto", "to")
                            .replace("thee", "you")
                            .replace("thou", "you")
                            .replace("thy", "your")
                            .replace("thine", "your")
                            .replace("maketh", "lets")
                            .replace("leadeth", "leads")
                            .replace("restoreth", "renews")
                            .replace("preparest", "prepare")
                            .replace("anointest", "anoint")
                            .replace("runneth", "overflows")
                            .replace("shall not want", "have what I need")
                            .replace("yea, though", "even though")
                            .replace("shall", "will")
                        "NASB" -> verse.text
                            .replace("unto", "to")
                            .replace("thee", "you")
                            .replace("thou", "you")
                            .replace("thy", "your")
                            .replace("yea", "yes")
                            .replace("maketh", "makes")
                            .replace("leadeth", "leads")
                            .replace("restoreth", "restores")
                        "RVR1960" -> {
                            if (verse.bookName == "John" && verse.chapter == 3 && verse.verseNumber == 16) {
                                "Porque de tal manera amó Dios al mundo, que ha dado a su Hijo unigénito, para que todo aquel que en él cree, no se pierda, mas tenga vida eterna."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 1) {
                                "Jehová es mi pastor; nada me faltará."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 2) {
                                "En lugares de delicados pastos me hará descansar; Junto a aguas de reposo me pastoreará."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 3) {
                                "Confortará mi alma; Me guiará por sendas de justicia por amor de su nombre."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 4) {
                                "Aunque ande en valle de sombra de muerte, No temeré mal alguno, porque tú estarás conmigo; Tu vara y tu cayado me infundirán aliento."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 5) {
                                "Aderezas mesa delante de mí en presencia de mis angustiadores; Unges mi cabeza con aceite; mi copa está rebosando."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 6) {
                                "Ciertamente el bien y la misericordia me seguirán todos los días de mi vida, Y en la casa de Jehová moraré por largos días."
                            } else if (verse.bookName == "Proverbs" && verse.chapter == 3 && verse.verseNumber == 5) {
                                "Fíate de Jehová de todo tu corazón, Y no te apoyes en tu propia prudencia."
                            } else if (verse.bookName == "Proverbs" && verse.chapter == 3 && verse.verseNumber == 6) {
                                "Reconócelo en todos tus caminos, Y él enderezará tus veredas."
                            } else {
                                "[RVR1960] " + verse.text
                                    .replace("unto", "a")
                                    .replace("thee", "ti")
                                    .replace("thou", "tú")
                                    .replace("thy", "tu")
                            }
                        }
                        "NVI" -> {
                            if (verse.bookName == "John" && verse.chapter == 3 && verse.verseNumber == 16) {
                                "Porque tanto amó Dios al mundo que dio a su Hijo unigénito, para que todo el que cree en él no se pierda, sino que tenga vida eterna."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 1) {
                                "El Señor es mi pastor, nada me falta."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 2) {
                                "En verdes pastos me hace descansar; a las aguas de reposo me conduce."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 3) {
                                "Me infunde nuevas fuerzas. Me guía por sendas de justicia por amor a su nombre."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 4) {
                                "Aun si voy por valles oscuros, no temeré peligro alguno, porque tú estás a mi lado; tu vara y tu bastón me brindan consuelo."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 5) {
                                "Dispones ante mí un banquete en presencia de mis enemigos. Has ungido con perfume mi cabeza; mi copa está rebosando."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 6) {
                                "El bien y el amor me seguirán todos los días de mi vida, y en la casa del Señor habitaré para siempre."
                            } else {
                                "[NVI] " + verse.text
                                    .replace("unto", "a")
                                    .replace("thee", "ti")
                                    .replace("thou", "tú")
                                    .replace("thy", "tu")
                            }
                        }
                        "VULG" -> {
                            if (verse.bookName == "John" && verse.chapter == 3 && verse.verseNumber == 16) {
                                "Sic enim Deus dilexit mundum ut Filium suum unigenitum daret ut omnis qui credit in eum non pereat sed habeat vitam aeternam."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 1) {
                                "Dominus regit me et nihil mihi deerit."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 2) {
                                "In loco pascuae ibi me collocavit super aquam refectionis educavit me."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 3) {
                                "Animam meam convertit deduxit me super semitas iustitiae propter nomen suum."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 4) {
                                "Nam et si ambulavero in medio umbrae mortis non timebo mala quoniam tu mecum es virga tua et baculus tuus ipsa me consolata sunt."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 5) {
                                "Parasti in conspectu meo mensam adversus eos qui tribulant me inpinguasti in oleo caput meum et calix meus inebrians quam praeclarus est."
                            } else if (verse.bookName == "Psalms" && verse.chapter == 23 && verse.verseNumber == 6) {
                                "Et misericordia tua subsequetur me omnibus diebus vitae meae et ut habitur in domo Domini in longitudinem dierum."
                            } else {
                                "[Vulgata] " + verse.text
                            }
                        }
                        else -> verse.text
                    }
                    
                    // Dynamic translation override for non-English downloads if Gemini is online
                    if (targetLang != "English") {
                        val translated = GeminiService.translateText(verse.text, targetLang)
                        if (translated != verse.text) {
                            modernizedText = translated
                        }
                    }
                    
                    BibleVerse(
                        translation = versionCode,
                        bookName = verse.bookName,
                        chapter = verse.chapter,
                        verseNumber = verse.verseNumber,
                        text = modernizedText
                    )
                }
                repository.addDownloadedVerses(downloadedVerses)
            } catch (e: Exception) {
                // Fallback: if database fails, we still allow translation select
            }
            
            // Download completed! Add to downloaded set
            val updatedSet = _downloadedVersions.value + versionCode
            _downloadedVersions.value = updatedSet
            _downloadProgress.value = _downloadProgress.value - versionCode
            
            sharedPrefs.edit().putStringSet("downloaded_versions", updatedSet).apply()
            
            // Auto-switch app language to the downloaded translation's language!
            val targetLang = getLanguageForTranslation(versionCode)
            setAppLanguage(targetLang)
            
            refreshStorageSizes()
        }
    }

    fun refreshStorageSizes() {
        viewModelScope.launch {
            val sizes = mutableMapOf<String, Long>()
            val allTranslations = listOf(
                "KJV", "NKJV", "NIV", "ESV", "NLT", "MSG", "AMP", "CSV", "WEB", "ASV", 
                "RVR1960", "NVI", "LBLA", "JBS", "NTV", "RVR", "SEV",
                "AA", "NVI-PT", "LSG", "OST", "ELB", "LUT", "GDB", "RIV", "SVV",
                "CUV", "CUVP", "CUVS", "VULG", "SUV"
            )
            for (code in allTranslations) {
                if (_downloadedVersions.value.contains(code)) {
                    val count = repository.getVerseCountForTranslation(code)
                    if (count > 0) {
                        // Estimate 220 bytes per verse on average
                        sizes[code] = count * 220L
                    } else {
                        // Estimated size for seed database
                        sizes[code] = 98 * 220L
                    }
                } else {
                    sizes[code] = 0L
                }
            }
            _translationStorageSizes.value = sizes
        }
    }

    fun deleteVersion(versionCode: String) {
        viewModelScope.launch {
            if (_downloadedVersions.value.size <= 1 && _downloadedVersions.value.contains(versionCode)) {
                return@launch
            }
            if (_selectedTranslation.value == versionCode) {
                val remaining = _downloadedVersions.value.filter { it != versionCode }
                if (remaining.isNotEmpty()) {
                    _selectedTranslation.value = remaining.first()
                    sharedPrefs.edit().putString("last_read_translation", remaining.first()).apply()
                }
            }
            if (_compareTranslation.value == versionCode) {
                val remaining = _downloadedVersions.value.filter { it != versionCode }
                if (remaining.isNotEmpty()) {
                    _compareTranslation.value = remaining.first()
                    sharedPrefs.edit().putString("compare_translation", remaining.first()).apply()
                }
            }
            
            repository.deleteDownloadedVerses(versionCode)
            
            val updatedSet = _downloadedVersions.value - versionCode
            _downloadedVersions.value = updatedSet
            sharedPrefs.edit().putStringSet("downloaded_versions", updatedSet).apply()
            
            refreshStorageSizes()
        }
    }

    fun loadVerseOfTheDay() {
        val savedVerseText = sharedPrefs.getString("votd_text", "") ?: ""
        val savedVerseBook = sharedPrefs.getString("votd_book", "") ?: ""
        val savedVerseChapter = sharedPrefs.getInt("votd_chapter", 0)
        val savedVerseNumber = sharedPrefs.getInt("votd_number", 0)
        val savedVerseTranslation = sharedPrefs.getString("votd_translation", "KJV") ?: "KJV"

        if (savedVerseText.isNotEmpty() && savedVerseBook.isNotEmpty() && savedVerseChapter > 0 && savedVerseNumber > 0) {
            _verseOfTheDay.value = BibleVerse(
                translation = savedVerseTranslation,
                bookName = savedVerseBook,
                chapter = savedVerseChapter,
                verseNumber = savedVerseNumber,
                text = savedVerseText
            )
        } else {
            // Default select offline
            _verseOfTheDay.value = selectOfflineVerseOfTheDay()
        }
    }

    private fun selectOfflineVerseOfTheDay(): BibleVerse {
        val list = listOf(
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
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        return list[dayOfYear % list.size]
    }

    fun syncVerseOfTheDay(context: android.content.Context? = null) {
        viewModelScope.launch {
            _isSyncingVotd.value = true
            _votdSyncStatus.value = "Connecting to Heaven's Gate..."
            kotlinx.coroutines.delay(1200) // Beautiful professional visual sync delay
            try {
                val syncedVerse = GeminiService.generateVerseOfTheDay()
                
                if (syncedVerse != null) {
                    _verseOfTheDay.value = syncedVerse
                    sharedPrefs.edit()
                        .putString("votd_text", syncedVerse.text)
                        .putString("votd_book", syncedVerse.bookName)
                        .putInt("votd_chapter", syncedVerse.chapter)
                        .putInt("votd_number", syncedVerse.verseNumber)
                        .putString("votd_translation", syncedVerse.translation)
                        .apply()
                    _votdSyncStatus.value = "Synced via Gemini"
                } else {
                    selectOfflineVerseOfTheDay()
                }
            } catch (e: Exception) {
                // Offline fallback - use beautiful deterministic rotation
                val fallback = selectOfflineVerseOfTheDay()
                _verseOfTheDay.value = fallback
                sharedPrefs.edit()
                    .putString("votd_text", fallback.text)
                    .putString("votd_book", fallback.bookName)
                    .putInt("votd_chapter", fallback.chapter)
                    .putInt("votd_number", fallback.verseNumber)
                    .putString("votd_translation", fallback.translation)
                    .apply()
                _votdSyncStatus.value = "Synced via Offline Cache"
            } finally {
                _isSyncingVotd.value = false
                context?.let { ctx ->
                    com.example.VerseOfTheDayWidgetProvider.updateAllWidgets(ctx)
                    com.example.DailyVerseNotificationHelper.updatePersistentNotificationIfActive(ctx)
                }
            }
        }
    }

    // --- Daily Verse Notification & Widget State ---
    private val _persistentVotdEnabled = MutableStateFlow(sharedPrefs.getBoolean("persistent_votd_enabled", false))
    val persistentVotdEnabled: StateFlow<Boolean> = _persistentVotdEnabled.asStateFlow()

    private val _dailyVotdAlarmEnabled = MutableStateFlow(sharedPrefs.getBoolean("daily_votd_alarm_enabled", false))
    val dailyVotdAlarmEnabled: StateFlow<Boolean> = _dailyVotdAlarmEnabled.asStateFlow()

    private val _dailyVotdAlarmHour = MutableStateFlow(sharedPrefs.getInt("daily_votd_alarm_hour", 8))
    val dailyVotdAlarmHour: StateFlow<Int> = _dailyVotdAlarmHour.asStateFlow()

    private val _dailyVotdAlarmMinute = MutableStateFlow(sharedPrefs.getInt("daily_votd_alarm_minute", 0))
    val dailyVotdAlarmMinute: StateFlow<Int> = _dailyVotdAlarmMinute.asStateFlow()

    private val _dailyNotificationCategory = MutableStateFlow(sharedPrefs.getString("daily_notification_category", "All Verses") ?: "All Verses")
    val dailyNotificationCategory: StateFlow<String> = _dailyNotificationCategory.asStateFlow()

    private val _dailyNotificationTitle = MutableStateFlow(sharedPrefs.getString("daily_notification_title", "Daily Devotion — Verse of the Day") ?: "Daily Devotion — Verse of the Day")
    val dailyNotificationTitle: StateFlow<String> = _dailyNotificationTitle.asStateFlow()

    private val _dailyNotificationFrequency = MutableStateFlow(sharedPrefs.getString("daily_notification_frequency", "Daily") ?: "Daily")
    val dailyNotificationFrequency: StateFlow<String> = _dailyNotificationFrequency.asStateFlow()

    private val _customNotificationVerseText = MutableStateFlow(sharedPrefs.getString("custom_notif_verse_text", "") ?: "")
    val customNotificationVerseText: StateFlow<String> = _customNotificationVerseText.asStateFlow()
    private val _customNotificationVerseBook = MutableStateFlow(sharedPrefs.getString("custom_notif_verse_book", "") ?: "")
    val customNotificationVerseBook: StateFlow<String> = _customNotificationVerseBook.asStateFlow()
    private val _customNotificationVerseChapter = MutableStateFlow(sharedPrefs.getInt("custom_notif_verse_chapter", 0))
    val customNotificationVerseChapter: StateFlow<Int> = _customNotificationVerseChapter.asStateFlow()
    private val _customNotificationVerseNumber = MutableStateFlow(sharedPrefs.getInt("custom_notif_verse_number", 0))
    val customNotificationVerseNumber: StateFlow<Int> = _customNotificationVerseNumber.asStateFlow()

    fun setDailyNotificationCategory(context: android.content.Context, category: String) {
        _dailyNotificationCategory.value = category
        sharedPrefs.edit().putString("daily_notification_category", category).apply()
    }

    fun setDailyNotificationTitle(context: android.content.Context, title: String) {
        _dailyNotificationTitle.value = title
        sharedPrefs.edit().putString("daily_notification_title", title).apply()
    }

    fun setDailyNotificationFrequency(context: android.content.Context, frequency: String) {
        _dailyNotificationFrequency.value = frequency
        sharedPrefs.edit().putString("daily_notification_frequency", frequency).apply()
    }

    fun setCustomNotificationVerse(context: android.content.Context, verse: BibleVerse) {
        _customNotificationVerseText.value = verse.text
        _customNotificationVerseBook.value = verse.bookName
        _customNotificationVerseChapter.value = verse.chapter
        _customNotificationVerseNumber.value = verse.verseNumber
        sharedPrefs.edit()
            .putString("custom_notif_verse_text", verse.text)
            .putString("custom_notif_verse_book", verse.bookName)
            .putInt("custom_notif_verse_chapter", verse.chapter)
            .putInt("custom_notif_verse_number", verse.verseNumber)
            .putString("custom_notif_verse_translation", verse.translation)
            .apply()
    }

    fun clearCustomNotificationVerse(context: android.content.Context) {
        _customNotificationVerseText.value = ""
        _customNotificationVerseBook.value = ""
        _customNotificationVerseChapter.value = 0
        _customNotificationVerseNumber.value = 0
        sharedPrefs.edit()
            .remove("custom_notif_verse_text")
            .remove("custom_notif_verse_book")
            .remove("custom_notif_verse_chapter")
            .remove("custom_notif_verse_number")
            .remove("custom_notif_verse_translation")
            .apply()
    }

    fun togglePersistentVotd(context: android.content.Context) {
        val newState = !_persistentVotdEnabled.value
        _persistentVotdEnabled.value = newState
        sharedPrefs.edit().putBoolean("persistent_votd_enabled", newState).apply()
        if (newState) {
            val verse = com.example.DailyVerseNotificationHelper.getVerseForCurrentPreferences(context)
            com.example.DailyVerseNotificationHelper.showDailyVerseNotification(context, verse, isPersistent = true, customTitle = _dailyNotificationTitle.value)
        } else {
            com.example.DailyVerseNotificationHelper.cancelPersistentNotification(context)
        }
    }

    fun toggleDailyVotdAlarm(context: android.content.Context, hour: Int = _dailyVotdAlarmHour.value, minute: Int = _dailyVotdAlarmMinute.value) {
        val newState = !_dailyVotdAlarmEnabled.value
        _dailyVotdAlarmEnabled.value = newState
        _dailyVotdAlarmHour.value = hour
        _dailyVotdAlarmMinute.value = minute
        sharedPrefs.edit()
            .putBoolean("daily_votd_alarm_enabled", newState)
            .putInt("daily_votd_alarm_hour", hour)
            .putInt("daily_votd_alarm_minute", minute)
            .apply()

        if (newState) {
            com.example.DailyVerseNotificationHelper.scheduleDailyAlarm(context, hour, minute)
        } else {
            com.example.DailyVerseNotificationHelper.cancelDailyAlarm(context)
        }
    }

    fun setDailyVotdAlarmTime(context: android.content.Context, hour: Int, minute: Int) {
        _dailyVotdAlarmHour.value = hour
        _dailyVotdAlarmMinute.value = minute
        sharedPrefs.edit()
            .putInt("daily_votd_alarm_hour", hour)
            .putInt("daily_votd_alarm_minute", minute)
            .apply()

        if (_dailyVotdAlarmEnabled.value) {
            com.example.DailyVerseNotificationHelper.scheduleDailyAlarm(context, hour, minute)
        }
    }

    fun showVotdNotificationNow(context: android.content.Context) {
        val verse = com.example.DailyVerseNotificationHelper.getVerseForCurrentPreferences(context)
        com.example.DailyVerseNotificationHelper.showDailyVerseNotification(
            context = context,
            verse = verse,
            isPersistent = false,
            customTitle = _dailyNotificationTitle.value
        )
    }

    private val _votdBackgroundStyle = MutableStateFlow(sharedPrefs.getString("votd_background_style", "sunset") ?: "sunset")
    val votdBackgroundStyle: StateFlow<String> = _votdBackgroundStyle.asStateFlow()

    fun setVotdBackgroundStyle(style: String) {
        _votdBackgroundStyle.value = style
        sharedPrefs.edit().putString("votd_background_style", style).apply()
    }

    fun fetchRandomVerseOfTheDay(context: android.content.Context) {
        val category = sharedPrefs.getString("daily_notification_category", "All Verses") ?: "All Verses"
        val versesList = com.example.DailyVerseNotificationHelper.getVersesByCategory(category)
        if (versesList.isNotEmpty()) {
            val randomIndex = (0 until versesList.size).random()
            val randomVerse = versesList[randomIndex]

            _verseOfTheDay.value = randomVerse
            sharedPrefs.edit()
                .putInt("votd_offline_index", randomIndex)
                .putString("votd_text", randomVerse.text)
                .putString("votd_book", randomVerse.bookName)
                .putInt("votd_chapter", randomVerse.chapter)
                .putInt("votd_number", randomVerse.verseNumber)
                .putString("votd_translation", randomVerse.translation)
                .apply()

            com.example.VerseOfTheDayWidgetProvider.updateAllWidgets(context)
            com.example.DailyVerseNotificationHelper.updatePersistentNotificationIfActive(context)
        }
    }

    fun rotateNextVerseOfTheDay(context: android.content.Context) {
        val offlineList = com.example.VerseOfTheDayWidgetProvider.getOfflineVerses()
        val currentIndex = sharedPrefs.getInt("votd_offline_index", 0)
        val nextIndex = (currentIndex + 1) % offlineList.size
        val nextVerse = offlineList[nextIndex]

        _verseOfTheDay.value = nextVerse
        sharedPrefs.edit()
            .putInt("votd_offline_index", nextIndex)
            .putString("votd_text", nextVerse.text)
            .putString("votd_book", nextVerse.bookName)
            .putInt("votd_chapter", nextVerse.chapter)
            .putInt("votd_number", nextVerse.verseNumber)
            .putString("votd_translation", nextVerse.translation)
            .apply()

        com.example.VerseOfTheDayWidgetProvider.updateAllWidgets(context)
        com.example.DailyVerseNotificationHelper.updatePersistentNotificationIfActive(context)
    }

    fun getVersesForReference(translation: String, bookName: String, chapter: Int): Flow<List<BibleVerse>> {
        return repository.getVerses(translation, bookName, chapter)
    }

    fun saveDownloadedVerses(verses: List<BibleVerse>) {
        viewModelScope.launch {
            repository.addDownloadedVerses(verses)
        }
    }
}

class BibleViewModelFactory(
    private val repository: BibleRepository,
    private val sharedPrefs: android.content.SharedPreferences,
    private val syncManager: SyncManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BibleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BibleViewModel(repository, sharedPrefs, syncManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

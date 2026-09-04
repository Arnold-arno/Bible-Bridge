import 'package:flutter/material.dart';
import 'dart:async';
import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter_tts/flutter_tts.dart';
import '../models/bible_verse.dart';
import '../models/book_overview.dart';
import '../models/timeline_event.dart';
import '../models/favorite_verse.dart';
import '../models/reading_history.dart';
import '../models/devotional.dart';
import '../services/database_service.dart';
import '../services/bible_text_generator.dart';
import '../services/sync_manager.dart';

enum ActiveTab { home, read, devotions, timeline, settings }

class BibleViewModel with ChangeNotifier {
  final DatabaseService _db = DatabaseService.instance;

  // Active State
  ActiveTab _activeTab = ActiveTab.home;
  String _selectedTranslation = 'KJV';
  String _selectedBook = 'Genesis';
  int _selectedChapter = 1;
  int _selectedVerseNumber = 1;
  double _fontSize = 16.0;
  String _fontFamily = 'Serif';
  String _themeMode = 'dark'; // 'light', 'dark', or 'sepia'

  // App Language and Translation Download State
  String _appLanguage = 'English';
  List<String> _downloadedTranslations = ['KJV', 'WEB', 'ASV'];
  bool _isDownloadingTranslation = false;
  double _translationDownloadProgress = 0.0;
  String _downloadingTranslationId = '';
  String? _suggestedTranslationId;

  List<BibleVerse> _currentVerses = [];
  List<FavoriteVerse> _favorites = [];
  List<ReadingHistory> _history = [];
  List<Devotional> _devotionals = [];
  
  // Search History & Results State
  List<String> _recentSearches = [];
  List<BibleVerse> _searchResults = [];
  bool _isSearching = false;

  // Offline Search Index & Background Indexing State
  double _indexingProgress = 0.0;
  bool _isIndexing = false;
  bool _msgOfflineError = false;
  bool _isGeneratingAiDevotional = false;
  
  // Highlight Popup state
  BibleVerse? _selectedVerseForHighlight;

  // Audio Player/Reader State
  final FlutterTts _flutterTts = FlutterTts();
  bool _isAudioActive = false;
  bool _isAudioPlaying = false;
  String _audioTitle = '';
  String _audioText = '';
  double _audioProgress = 0.0;
  Duration _audioElapsed = Duration.zero;
  Duration _audioTotal = Duration.zero;
  Timer? _audioTimer;

  List<String> _audioSentences = [];
  int _currentSentenceIndex = 0;

  // Voice Customization & Optimization State
  double _ttsRate = 0.5; // range: 0.0 to 1.0 (default 0.5 is normal on Android)
  double _ttsPitch = 1.0; // range: 0.5 to 2.0 (default 1.0)
  String _ttsLanguage = 'en-US'; // standard accents for voice change

  // Comparison State
  bool _compareMode = false;
  String _compareTranslation = 'MSG';

  // Getters
  ActiveTab get activeTab => _activeTab;
  String get selectedTranslation => _selectedTranslation;
  String get selectedBook => _selectedBook;
  int get selectedChapter => _selectedChapter;
  int get selectedVerseNumber => _selectedVerseNumber;
  double get fontSize => _fontSize;
  String get fontFamily => _fontFamily;
  String get themeMode => _themeMode;
  List<BibleVerse> get currentVerses => _currentVerses;
  List<FavoriteVerse> get favorites => _favorites;
  List<ReadingHistory> get history => _history;
  List<Devotional> get devotionals => _devotionals;
  List<String> get recentSearches => _recentSearches;
  List<BibleVerse> get searchResults => _searchResults;
  bool get isSearching => _isSearching;
  BibleVerse? get selectedVerseForHighlight => _selectedVerseForHighlight;

  bool get isAudioActive => _isAudioActive;
  bool get isAudioPlaying => _isAudioPlaying;
  String get audioTitle => _audioTitle;
  String get audioText => _audioText;
  double get audioProgress => _audioProgress;
  Duration get audioElapsed => _audioElapsed;
  Duration get audioTotal => _audioTotal;
  int get currentSentenceIndex => _currentSentenceIndex;
  List<String> get audioSentences => _audioSentences;

  double get ttsRate => _ttsRate;
  double get ttsPitch => _ttsPitch;
  String get ttsLanguage => _ttsLanguage;

  bool get compareMode => _compareMode;
  String get compareTranslation => _compareTranslation;

  double get indexingProgress => _indexingProgress;
  bool get isIndexing => _isIndexing;
  bool get msgOfflineError => _msgOfflineError;
  bool get isGeneratingAiDevotional => _isGeneratingAiDevotional;

  // App Language and Translation Download Getters
  String get appLanguage => _appLanguage;
  List<String> get downloadedTranslations => _downloadedTranslations;
  bool get isDownloadingTranslation => _isDownloadingTranslation;
  double get translationDownloadProgress => _translationDownloadProgress;
  String get downloadingTranslationId => _downloadingTranslationId;
  String? get suggestedTranslationId => _suggestedTranslationId;

  // Curated Daily Verses List
  static const List<Map<String, String>> _dailyVerses = [
    {
      'book': 'John',
      'chapter': '3',
      'verse': '16',
      'text': 'For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life.',
      'title': 'The Gift of Eternal Love',
    },
    {
      'book': 'Psalms',
      'chapter': '23',
      'verse': '1',
      'text': 'The Lord is my shepherd; I shall not want.',
      'title': 'The Great Shepherd\'s Care',
    },
    {
      'book': 'Romans',
      'chapter': '12',
      'verse': '2',
      'text': 'And be not conformed to this world: but be ye transformed by the renewing of your mind, that ye may prove what is that good, and acceptable, and perfect, will of God.',
      'title': 'Renewed in Truth',
    },
    {
      'book': 'Matthew',
      'chapter': '6',
      'verse': '33',
      'text': 'But seek ye first the kingdom of God, and his righteousness; and all these things shall be added unto you.',
      'title': 'Priority of the Kingdom',
    },
    {
      'book': 'Genesis',
      'chapter': '1',
      'verse': '1',
      'text': 'In the beginning God created the heaven and the earth.',
      'title': 'A Glorious Beginning',
    },
    {
      'book': 'Joshua',
      'chapter': '1',
      'verse': '9',
      'text': 'Have not I commanded thee? Be strong and of a good courage; be not afraid, neither be thou dismayed: for the Lord thy God is with thee whithersoever thou goest.',
      'title': 'Courage and Presence',
    },
    {
      'book': 'Proverbs',
      'chapter': '3',
      'verse': '5',
      'text': 'Trust in the Lord with all thine heart; and lean not unto thine own understanding.',
      'title': 'Heartfelt Trust',
    },
    {
      'book': 'Isaiah',
      'chapter': '40',
      'verse': '31',
      'text': 'But they that wait upon the Lord shall renew their strength; they shall mount up with wings as eagles; they shall run, and not be weary; and they shall walk, and not faint.',
      'title': 'Strength in Waiting',
    },
    {
      'book': 'Romans',
      'chapter': '8',
      'verse': '28',
      'text': 'And we know that all things work together for good to them that love God, to them who are the called according to his purpose.',
      'title': 'Working for Our Good',
    },
    {
      'book': 'Philippians',
      'chapter': '4',
      'verse': '13',
      'text': 'I can do all things through Christ which strengtheneth me.',
      'title': 'Strength through Him',
    },
  ];

  Map<String, String> get dailyVerse {
    final now = DateTime.now();
    final dayOfYear = now.difference(DateTime(now.year, 1, 1)).inDays;
    final index = dayOfYear % _dailyVerses.length;
    return _dailyVerses[index];
  }

  String get dailyVerseText {
    final verse = dailyVerse;
    final book = verse['book']!;
    final chapter = int.parse(verse['chapter']!);
    final verseNum = int.parse(verse['verse']!);
    final baseText = verse['text']!;
    
    final verses = BibleTextGenerator.generateVerses(book, chapter, _selectedTranslation);
    if (verseNum - 1 < verses.length) {
      return verses[verseNum - 1].text;
    }
    return baseText;
  }

  // Dynamic Multi-Language Bible Translations Map
  final Map<String, List<Map<String, String>>> translationsByLanguage = {
    'English': [
      {'id': 'KJV', 'name': 'King James Version (KJV)'},
      {'id': 'NKJV', 'name': 'New King James Version (NKJV)'},
      {'id': 'NIV', 'name': 'New International Version (NIV)'},
      {'id': 'ESV', 'name': 'English Standard Version (ESV)'},
      {'id': 'NLT', 'name': 'New Living Translation (NLT)'},
      {'id': 'NASB', 'name': 'New American Standard Bible (NASB)'},
      {'id': 'AMP', 'name': 'Amplified Bible (AMP)'},
      {'id': 'WEB', 'name': 'World English Bible (WEB)'},
      {'id': 'ASV', 'name': 'American Standard Version (ASV)'},
      {'id': 'MSG', 'name': 'The Message (MSG)'},
    ],
    'Spanish': [
      {'id': 'RVR1960', 'name': 'Reina Valera 1960'},
      {'id': 'NVI_ES', 'name': 'Nueva Versión Internacional (NVI)'},
      {'id': 'DHH', 'name': 'Dios Habla Hoy'},
      {'id': 'LBLA', 'name': 'La Biblia de las Américas (LBLA)'},
      {'id': 'JBS', 'name': 'La Biblia del Jubileo 2000 (JBS)'},
      {'id': 'NTV', 'name': 'Nueva Traducción Viviente (NTV)'},
      {'id': 'RVR', 'name': 'La Biblia Reina-Valera (RVR)'},
      {'id': 'SEV', 'name': 'Sagradas Escrituras (1569) (SEV)'},
    ],
    'Portuguese': [
      {'id': 'AA', 'name': 'Almeida Atualizada (AA)'},
      {'id': 'NVI_PT', 'name': 'Nova Versão Internacional (NVI-PT)'},
    ],
    'French': [
      {'id': 'LSG', 'name': 'Louis Segond'},
      {'id': 'SEM', 'name': 'Semeur'},
      {'id': 'LSG1910', 'name': 'Louis Segond 1910 (LSG)'},
      {'id': 'OST', 'name': 'Ostervald'},
    ],
    'German': [
      {'id': 'ELB', 'name': 'Elberfelder 1905 (ELB)'},
      {'id': 'LUT', 'name': 'Luther Bible 1912 (LUT)'},
    ],
    'Italian': [
      {'id': 'GDB', 'name': 'Giovanni Diodati 1649 (GDB)'},
      {'id': 'RIV', 'name': 'Riveduta 1927 (RIV)'},
    ],
    'Dutch': [
      {'id': 'SVV', 'name': 'Statenvertaling (SVV)'},
    ],
    'Swahili': [
      {'id': 'BHN', 'name': 'Biblia Habari Njema (BHN)'},
    ],
    'Luganda': [
      {'id': 'EE', 'name': 'Endagaano Enkadde n\'Empya (EE)'},
    ],
    'Chinese': [
      {'id': 'CUV', 'name': 'Chinese Union Version (CUV)'},
      {'id': 'CCB', 'name': 'Chinese Contemporary Bible (CCB)'},
      {'id': 'CUV_TR', 'name': 'Chinese Union Version - Traditional (CUV-TR)'},
      {'id': 'CUVP', 'name': 'Chinese Union Version - Pinyin (CUVP)'},
      {'id': 'CUVS', 'name': 'Chinese Union Version - Simplified (CUVS)'},
    ],
    'Arabic': [
      {'id': 'SVD', 'name': 'Smith & Van Dyke (SVD)'},
    ],
  };

  List<String> get translations {
    final List<String> list = [];
    translationsByLanguage.values.forEach((langList) {
      for (var trans in langList) {
        list.add(trans['id']!);
      }
    });
    return list;
  }

  static const Map<String, int> bookChapters = {
    'Genesis': 50, 'Exodus': 40, 'Leviticus': 27, 'Numbers': 36, 'Deuteronomy': 34,
    'Joshua': 24, 'Judges': 21, 'Ruth': 4, '1 Samuel': 31, '2 Samuel': 24,
    '1 Kings': 22, '2 Kings': 25, '1 Chronicles': 29, '2 Chronicles': 36,
    'Ezra': 10, 'Nehemiah': 13, 'Esther': 10, 'Job': 42, 'Psalms': 150,
    'Proverbs': 31, 'Ecclesiastes': 12, 'Song of Solomon': 8, 'Isaiah': 66,
    'Jeremiah': 52, 'Lamentations': 5, 'Ezekiel': 48, 'Daniel': 12, 'Hosea': 14,
    'Joel': 3, 'Amos': 9, 'Obadiah': 1, 'Jonah': 4, 'Micah': 7, 'Nahum': 3,
    'Habakkuk': 3, 'Zephaniah': 3, 'Haggai': 2, 'Zechariah': 14, 'Malachi': 4,
    'Matthew': 28, 'Mark': 16, 'Luke': 24, 'John': 21, 'Acts': 28, 'Romans': 16,
    '1 Corinthians': 16, '2 Corinthians': 13, 'Galatians': 6, 'Ephesians': 6,
    'Philippians': 4, 'Colossians': 4, '1 Thessalonians': 5, '2 Thessalonians': 3,
    '1 Timothy': 6, '2 Timothy': 4, 'Titus': 3, 'Philemon': 1, 'Hebrews': 13,
    'James': 5, '1 Peter': 5, '2 Peter': 3, '1 John': 5, '2 John': 1, '3 John': 1,
    'Jude': 1, 'Revelation': 22
  };

  static const List<String> oldTestamentBooks = [
    'Genesis', 'Exodus', 'Leviticus', 'Numbers', 'Deuteronomy', 'Joshua', 'Judges', 'Ruth',
    '1 Samuel', '2 Samuel', '1 Kings', '2 Kings', '1 Chronicles', '2 Chronicles', 'Ezra',
    'Nehemiah', 'Esther', 'Job', 'Psalms', 'Proverbs', 'Ecclesiastes', 'Song of Solomon',
    'Isaiah', 'Jeremiah', 'Lamentations', 'Ezekiel', 'Daniel', 'Hosea', 'Joel', 'Amos',
    'Obadiah', 'Jonah', 'Micah', 'Nahum', 'Habakkuk', 'Zephaniah', 'Haggai', 'Zechariah', 'Malachi'
  ];

  static const List<String> newTestamentBooks = [
    'Matthew', 'Mark', 'Luke', 'John', 'Acts', 'Romans', '1 Corinthians', '2 Corinthians',
    'Galatians', 'Ephesians', 'Philippians', 'Colossians', '1 Thessalonians', '2 Thessalonians',
    '1 Timothy', '2 Timothy', 'Titus', 'Philemon', 'Hebrews', 'James', '1 Peter', '2 Peter',
    '1 John', '2 John', '3 John', 'Jude', 'Revelation'
  ];

  List<String> get books => [...oldTestamentBooks, ...newTestamentBooks];

  final List<TimelineEvent> timelineEvents = [
    TimelineEvent(
      title: 'Creation & The Fall',
      period: 'c. 4000 BC',
      description: 'The creation of the universe, Adam and Eve, and the introduction of sin into the world.',
      scriptureRef: 'Genesis 1-3',
      iconName: 'landscape',
    ),
    TimelineEvent(
      title: 'The Great Flood & Covenant',
      period: 'c. 2500 BC',
      description: 'Noah builds the Ark; God cleanses the earth of wickedness and establishes the rainbow covenant.',
      scriptureRef: 'Genesis 6-9',
      iconName: 'tsunami',
    ),
    TimelineEvent(
      title: 'The Call of Abraham',
      period: 'c. 2100 BC',
      description: 'God establishes a covenant with Abraham, promising to make him a great nation of blessing.',
      scriptureRef: 'Genesis 12',
      iconName: 'star',
    ),
    TimelineEvent(
      title: 'The Exodus from Egypt',
      period: 'c. 1446 BC',
      description: 'Moses leads the Hebrews out of slavery through the parted Red Sea, escaping Pharaoh\'s army.',
      scriptureRef: 'Exodus 12-14',
      iconName: 'waves',
    ),
    TimelineEvent(
      title: 'The Reign of King David',
      period: 'c. 1010 BC',
      description: 'David rules as Israel\'s greatest king, writing Psalms and receiving the eternal covenant promise.',
      scriptureRef: '2 Samuel 7',
      iconName: 'crown',
    ),
    TimelineEvent(
      title: 'Babylonian Exile',
      period: 'c. 586 BC',
      description: 'Jerusalem falls to King Nebuchadnezzar; the Temple is destroyed and Jews are exiled to Babylon.',
      scriptureRef: '2 Kings 25',
      iconName: 'gavel',
    ),
    TimelineEvent(
      title: 'Birth of Jesus Christ',
      period: 'c. 4 BC',
      description: 'The Messiah is born in Bethlehem to the virgin Mary, fulfilling centuries of prophecies.',
      scriptureRef: 'Matthew 1-2',
      iconName: 'child_care',
    ),
    TimelineEvent(
      title: 'The Sermon on the Mount',
      period: 'c. 28 AD',
      description: 'Jesus preaches the Beatitudes, outlining the radical principles of the Kingdom of Heaven.',
      scriptureRef: 'Matthew 5-7',
      iconName: 'nature_people',
    ),
    TimelineEvent(
      title: 'Crucifixion, Resurrection & Ascension',
      period: 'c. 30 AD',
      description: 'Jesus dies on the cross, rises victorious on the third day, and ascends to heaven.',
      scriptureRef: 'John 19-21',
      iconName: 'church',
    ),
    TimelineEvent(
      title: 'Pentecost & The Early Church',
      period: 'c. 30 AD',
      description: 'The Holy Spirit descends upon the disciples in Jerusalem; 3,000 believers are baptized in a day.',
      scriptureRef: 'Acts 2',
      iconName: 'fireplace',
    ),
  ];

  final Map<String, BookOverview> bookOverviews = {
    'Genesis': BookOverview(
      name: 'Genesis',
      category: 'Pentateuch',
      author: 'Moses',
      dateWritten: 'c. 1440-1400 BC',
      theme: 'Beginnings, Covenant, and Sovereignty',
      keyVerse: 'Genesis 1:1',
      summary: 'The book of beginnings. Covers the creation of the universe, the fall of man, and God\'s selection of Abraham\'s lineage.',
    ),
    'Exodus': BookOverview(
      name: 'Exodus',
      category: 'Pentateuch',
      author: 'Moses',
      dateWritten: 'c. 1440-1400 BC',
      theme: 'Redemption, Law, and God\'s Presence',
      keyVerse: 'Exodus 3:14',
      summary: 'Israel\'s miraculous escape from Egyptian slavery under Moses\' leadership, followed by the giving of the Law at Mount Sinai.',
    ),
    'Psalms': BookOverview(
      name: 'Psalms',
      category: 'Wisdom Literature',
      author: 'David, Asaph, Solomon, Moses, etc.',
      dateWritten: 'c. 1000-450 BC',
      theme: 'Worship, Lament, Prayer, and Thanksgiving',
      keyVerse: 'Psalm 23:1',
      summary: 'The inspired prayer book and hymnal of Israel, expressing every shade of human emotion before God.',
    ),
    'John': BookOverview(
      name: 'John',
      category: 'Gospels',
      author: 'John the Apostle',
      dateWritten: 'c. 85-90 AD',
      theme: 'Jesus is the divine Son of God',
      keyVerse: 'John 3:16',
      summary: 'A theological Gospel designed to show that Jesus is the Word made flesh, encouraging readers to believe and receive eternal life.',
    ),
  };

  BibleViewModel() {
    _initTts();
    loadDatabaseData();
    indexBibleDatabase();

    // Automatically reload bookmarks and history if sync is successful
    SyncManager.instance.addListener(() async {
      if (SyncManager.instance.status == SyncStatus.success) {
        _favorites = await _db.getFavorites();
        _history = await _db.getHistory();
        notifyListeners();
      }
    });
  }

  void _initTts() {
    _flutterTts.setLanguage(_ttsLanguage);
    _flutterTts.setSpeechRate(_ttsRate);
    _flutterTts.setVolume(1.0);
    _flutterTts.setPitch(_ttsPitch);

    _flutterTts.setStartHandler(() {
      _isAudioPlaying = true;
      _startTimer();
      notifyListeners();
    });

    _flutterTts.setCompletionHandler(() {
      if (!_isAudioPlaying) return;

      _currentSentenceIndex++;
      if (_currentSentenceIndex < _audioSentences.length) {
        _speakCurrentSentence();
      } else {
        stopAudio();
      }
    });

    _flutterTts.setPauseHandler(() {
      _isAudioPlaying = false;
      _audioTimer?.cancel();
      notifyListeners();
    });

    _flutterTts.setContinueHandler(() {
      _isAudioPlaying = true;
      _startTimer();
      notifyListeners();
    });

    _flutterTts.setErrorHandler((msg) {
      debugPrint("TTS Error: $msg");
      stopAudio();
    });
  }

  // Initial Data Load
  Future<void> loadDatabaseData() async {
    final prefs = await SharedPreferences.getInstance();
    _appLanguage = prefs.getString('app_language') ?? 'English';
    _downloadedTranslations = prefs.getStringList('downloaded_translations') ?? ['KJV', 'WEB', 'ASV'];
    
    // Ensure default offline translations are always present
    if (!_downloadedTranslations.contains('KJV')) _downloadedTranslations.add('KJV');
    if (!_downloadedTranslations.contains('WEB')) _downloadedTranslations.add('WEB');
    if (!_downloadedTranslations.contains('ASV')) _downloadedTranslations.add('ASV');

    _favorites = await _db.getFavorites();
    _history = await _db.getHistory();
    _devotionals = await _db.getDevotionals();
    _recentSearches = await _db.getRecentSearches();
    await fetchVerses();
    notifyListeners();

    // Trigger background sync on startup
    if (SyncManager.instance.isAutoSyncEnabled) {
      SyncManager.instance.syncNow();
    }
  }

  Future<void> fetchVerses() async {
    _msgOfflineError = false;

    final isDownloaded = _downloadedTranslations.contains(_selectedTranslation);
    final isOnline = await SyncManager.instance.checkOnlineStatus();

    _currentVerses = await _db.getVerses(_selectedBook, _selectedChapter, _selectedTranslation);

    if (_currentVerses.isEmpty) {
      // If complete offline and not downloaded, fail gracefully
      if (!isDownloaded && !isOnline) {
        _msgOfflineError = true;
        _currentVerses = [];
        notifyListeners();
        return;
      }

      // If online and not downloaded, simulate network fetch delay
      if (!isDownloaded && isOnline) {
        await Future.delayed(const Duration(milliseconds: 500));
      }

      _currentVerses = BibleTextGenerator.generateVerses(_selectedBook, _selectedChapter, _selectedTranslation);
      // Persist to local SQLite database so it's cached for offline reading!
      await _db.saveVerses(_currentVerses);
    }
    notifyListeners();
  }

  // --- Translation Download & App Language Methods ---

  void clearSuggestedTranslation() {
    _suggestedTranslationId = null;
    notifyListeners();
  }

  Future<void> setAppLanguage(String language) async {
    _appLanguage = language;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('app_language', language);
    
    // Suggest a matching Bible translation when the app language changes, but never force it.
    final primaryTranslation = _getPrimaryTranslationForLanguage(language);
    if (primaryTranslation != null && primaryTranslation != _selectedTranslation) {
      _suggestedTranslationId = primaryTranslation;
    } else {
      _suggestedTranslationId = null;
    }
    
    notifyListeners();
  }

  String? _getPrimaryTranslationForLanguage(String language) {
    switch (language) {
      case 'Spanish': return 'RVR1960';
      case 'Portuguese': return 'AA';
      case 'French': return 'LSG';
      case 'German': return 'LUT';
      case 'Italian': return 'GDB';
      case 'Dutch': return 'SVV';
      case 'Swahili': return 'BHN';
      case 'Luganda': return 'EE';
      case 'Chinese': return 'CUV';
      case 'Arabic': return 'SVD';
      case 'English':
      default:
        return 'KJV';
    }
  }

  Future<void> downloadTranslation(String translationId) async {
    if (_downloadedTranslations.contains(translationId)) return;
    _isDownloadingTranslation = true;
    _downloadingTranslationId = translationId;
    _translationDownloadProgress = 0.0;
    notifyListeners();

    try {
      // Simulate highly responsive progress of download and database indexing
      for (int i = 1; i <= 10; i++) {
        await Future.delayed(const Duration(milliseconds: 150));
        _translationDownloadProgress = i / 10.0;
        notifyListeners();
      }

      // Pre-populate key chapters in SQLite database for this translation
      final keyChapters = [
        {'book': 'Genesis', 'chapter': 1},
        {'book': 'Psalms', 'chapter': 23},
        {'book': 'John', 'chapter': 1},
        {'book': 'John', 'chapter': 3},
        {'book': 'Matthew', 'chapter': 6},
        {'book': 'Romans', 'chapter': 12},
      ];
      for (var ch in keyChapters) {
        final verses = BibleTextGenerator.generateVerses(ch['book'] as String, ch['chapter'] as int, translationId);
        await _db.saveVerses(verses);
      }

      _downloadedTranslations.add(translationId);
      final prefs = await SharedPreferences.getInstance();
      await prefs.setStringList('downloaded_translations', _downloadedTranslations);
    } catch (e) {
      debugPrint('Error downloading translation: $e');
    } finally {
      _isDownloadingTranslation = false;
      _downloadingTranslationId = '';
      notifyListeners();
    }
  }

  Future<void> removeTranslation(String translationId) async {
    // KJV, WEB, ASV are default offline and cannot be removed to prevent empty states
    if (['KJV', 'WEB', 'ASV'].contains(translationId)) return;
    
    _downloadedTranslations.remove(translationId);
    final prefs = await SharedPreferences.getInstance();
    await prefs.setStringList('downloaded_translations', _downloadedTranslations);
    
    // Clear database cache for this translation
    await _db.clearVersesForTranslation(translationId);
    
    if (_selectedTranslation == translationId) {
      _selectedTranslation = 'KJV';
      await fetchVerses();
    }
    notifyListeners();
  }

  // State Mutators
  void selectTab(ActiveTab tab) {
    _activeTab = tab;
    notifyListeners();
  }

  void selectTranslation(String translation) {
    if (translations.contains(translation)) {
      _selectedTranslation = translation;
      fetchVerses();
    }
  }

  void selectBook(String book) {
    if (books.contains(book)) {
      _selectedBook = book;
      _selectedChapter = 1;
      _selectedVerseNumber = 1;
      fetchVerses();
      addHistoryEntry();
    }
  }

  void selectChapter(int chapter) {
    _selectedChapter = chapter;
    _selectedVerseNumber = 1;
    fetchVerses();
    addHistoryEntry();
  }

  void selectVerseNumber(int verseNum) {
    _selectedVerseNumber = verseNum;
    notifyListeners();
  }

  void setTtsRate(double rate) {
    _ttsRate = rate;
    _flutterTts.setSpeechRate(rate);
    notifyListeners();
  }

  void setTtsPitch(double pitch) {
    _ttsPitch = pitch;
    _flutterTts.setPitch(pitch);
    notifyListeners();
  }

  void setTtsLanguage(String language) {
    _ttsLanguage = language;
    _flutterTts.setLanguage(language);
    notifyListeners();
  }

  void toggleCompareMode(bool value) {
    _compareMode = value;
    notifyListeners();
  }

  void setCompareTranslation(String translation) {
    _compareTranslation = translation;
    notifyListeners();
  }

  void changeFontSize(double step) {
    _fontSize = (_fontSize + step).clamp(12.0, 32.0);
    notifyListeners();
  }

  void setFontSize(double size) {
    _fontSize = size.clamp(12.0, 32.0);
    notifyListeners();
  }

  void setThemeMode(String mode) {
    if (mode == 'light' || mode == 'dark' || mode == 'sepia') {
      _themeMode = mode;
      notifyListeners();
    }
  }

  void changeFontFamily(String family) {
    _fontFamily = family;
    notifyListeners();
  }

  // --- Search Operations ---

  Future<void> performSearch(String query) async {
    final trimmed = query.trim();
    if (trimmed.isEmpty) return;
    _isSearching = true;
    notifyListeners();

    // 1. Save query in SQLite database
    await _db.addSearchQuery(trimmed);
    _recentSearches = await _db.getRecentSearches();

    // 2. Perform SQLite search in cached verses
    var results = await _db.searchVerses(trimmed);

    // 3. Fallback: Search in pre-defined chapters of BibleTextGenerator to make search feel alive even on first run
    if (results.length < 5) {
      final realKeys = ['Genesis_1', 'Psalms_23', 'John_1', 'John_3', 'Matthew_6', 'Romans_12'];
      final lowercaseQuery = trimmed.toLowerCase();
      
      for (var key in realKeys) {
        final parts = key.split('_');
        final book = parts[0];
        final chapter = int.parse(parts[1]);
        
        // Generate the high-quality chapters and search them
        final generated = BibleTextGenerator.generateVerses(book, chapter, _selectedTranslation);
        for (var v in generated) {
          if (v.text.toLowerCase().contains(lowercaseQuery) || v.bookName.toLowerCase().contains(lowercaseQuery)) {
            // Avoid adding duplicate verses to the results list
            if (!results.any((res) => res.bookName == v.bookName && res.chapter == v.chapter && res.verseNumber == v.verseNumber)) {
              results.add(v);
              // Save to database so it's fully cached locally
              await _db.saveVerses([v]);
            }
          }
        }
      }
    }

    _searchResults = results;
    _isSearching = false;
    notifyListeners();
  }

  Future<void> deleteRecentSearch(String query) async {
    await _db.deleteSearchQuery(query);
    _recentSearches = await _db.getRecentSearches();
    notifyListeners();
  }

  Future<void> clearSearchAndReadingHistory() async {
    await _db.clearSearchHistory();
    await _db.clearReadingHistory();
    _recentSearches = [];
    _history = [];
    notifyListeners();
  }

  void selectVerseForHighlight(BibleVerse? verse) {
    _selectedVerseForHighlight = verse;
    notifyListeners();
  }

  // Bookmark / Highlighting Handlers
  Future<void> highlightVerse(BibleVerse verse, String colorHex) async {
    // Check if already favorited
    final exists = _favorites.any((f) => 
      f.bookName == verse.bookName && 
      f.chapter == verse.chapter && 
      f.verseNumber == verse.verseNumber && 
      f.translation == verse.translation
    );

    if (exists) return;

    final favorite = FavoriteVerse(
      verseId: verse.id ?? 0,
      translation: verse.translation,
      bookName: verse.bookName,
      chapter: verse.chapter,
      verseNumber: verse.verseNumber,
      text: verse.text,
      colorHex: colorHex,
    );

    await _db.addFavorite(favorite);
    _favorites = await _db.getFavorites();
    _selectedVerseForHighlight = null;
    notifyListeners();

    // Trigger background auto-sync
    if (SyncManager.instance.isAutoSyncEnabled) {
      SyncManager.instance.syncNow();
    }
  }

  Future<void> removeFavoriteHighlight(FavoriteVerse favorite) async {
    if (favorite.id != null) {
      await _db.removeFavorite(favorite.id!);
      _favorites = await _db.getFavorites();
      notifyListeners();

      // Trigger background auto-sync
      if (SyncManager.instance.isAutoSyncEnabled) {
        SyncManager.instance.syncNow();
      }
    }
  }

  // History Helper
  Future<void> addHistoryEntry() async {
    final entry = ReadingHistory(
      bookName: _selectedBook,
      chapter: _selectedChapter,
      timestamp: DateTime.now(),
    );
    await _db.addHistoryEntry(entry);
    _history = await _db.getHistory();
    notifyListeners();

    // Trigger background auto-sync
    if (SyncManager.instance.isAutoSyncEnabled) {
      SyncManager.instance.syncNow();
    }
  }

  // Devotions Helper
  Future<void> addCustomDevotional(String title, String content, String prayer, String scripture) async {
    final dev = Devotional(
      title: title,
      date: 'Today',
      scripture: scripture,
      content: content,
      prayer: prayer,
      isCustom: true,
    );
    await _db.addDevotional(dev);
    _devotionals = await _db.getDevotionals();
    notifyListeners();
  }

  void _speakCurrentSentence() async {
    if (_currentSentenceIndex >= _audioSentences.length) {
      stopAudio();
      return;
    }
    final textToSpeak = _audioSentences[_currentSentenceIndex];
    await _flutterTts.speak(textToSpeak);
  }

  // Audio Player Control Methods
  void startAudioReader(String title, String text) async {
    _audioTimer?.cancel();
    _audioTitle = title;
    _audioText = text;
    _isAudioActive = true;
    _isAudioPlaying = true;
    _audioProgress = 0.0;
    _audioElapsed = Duration.zero;

    // Clean text and split it into clean sentences
    final cleanText = text.replaceAll(RegExp(r'\s+'), ' ').trim();
    _audioSentences = cleanText.split(RegExp(r'(?<=[.!?])\s+')).where((s) => s.trim().isNotEmpty).toList();
    if (_audioSentences.isEmpty) {
      _audioSentences = [cleanText];
    }
    _currentSentenceIndex = 0;

    // Estimate reading time: ~150 words per minute -> 2.5 words per second
    final wordCount = cleanText.split(RegExp(r'\s+')).where((w) => w.isNotEmpty).length;
    final seconds = (wordCount / 2.5).clamp(5.0, 300.0).toInt();
    _audioTotal = Duration(seconds: seconds);

    await _flutterTts.stop();
    _speakCurrentSentence();
    _startTimer();
    notifyListeners();
  }

  void playAudio() async {
    if (!_isAudioActive || _isAudioPlaying) return;
    _isAudioPlaying = true;
    _startTimer();
    _speakCurrentSentence();
    notifyListeners();
  }

  void pauseAudio() async {
    if (!_isAudioPlaying) return;
    _isAudioPlaying = false;
    _audioTimer?.cancel();
    await _flutterTts.stop();
    notifyListeners();
  }

  void stopAudio() async {
    _isAudioActive = false;
    _isAudioPlaying = false;
    _audioTimer?.cancel();
    _audioProgress = 0.0;
    _audioElapsed = Duration.zero;
    _currentSentenceIndex = 0;
    _audioSentences = [];
    await _flutterTts.stop();
    notifyListeners();
  }

  void seekAudio(double value) async {
    _audioProgress = value;
    final totalSecs = _audioTotal.inSeconds;
    _audioElapsed = Duration(seconds: (totalSecs * value).toInt());

    if (_audioSentences.isNotEmpty) {
      final newIndex = (value * _audioSentences.length).floor().clamp(0, _audioSentences.length - 1);
      if (newIndex != _currentSentenceIndex) {
        _currentSentenceIndex = newIndex;
        if (_isAudioPlaying) {
          await _flutterTts.stop();
          _speakCurrentSentence();
        }
      }
    }
    notifyListeners();
  }

  void _startTimer() {
    _audioTimer?.cancel();
    _audioTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (!_isAudioPlaying) {
        timer.cancel();
        return;
      }
      final totalSecs = _audioTotal.inSeconds;
      var elapsedSecs = _audioElapsed.inSeconds + 1;
      if (elapsedSecs >= totalSecs) {
        elapsedSecs = totalSecs;
        _isAudioPlaying = false;
        timer.cancel();
      }
      _audioElapsed = Duration(seconds: elapsedSecs);
      _audioProgress = totalSecs > 0 ? elapsedSecs / totalSecs : 0.0;
      notifyListeners();
    });
  }

  Future<void> indexBibleDatabase() async {
    final prefs = await SharedPreferences.getInstance();
    final isIndexed = prefs.getBool('bible_search_indexed_v1') ?? false;
    if (isIndexed) return;

    _isIndexing = true;
    _indexingProgress = 0.0;
    notifyListeners();

    try {
      final allBooks = books;
      int totalBooks = allBooks.length;
      
      for (int i = 0; i < totalBooks; i++) {
        final book = allBooks[i];
        final chaptersCount = bookChapters[book] ?? 0;
        final List<BibleVerse> versesToSave = [];
        
        for (int ch = 1; ch <= chaptersCount; ch++) {
          for (final trans in ['KJV', 'WEB', 'ASV']) {
            versesToSave.addAll(BibleTextGenerator.generateVerses(book, ch, trans));
          }
        }
        
        if (versesToSave.isNotEmpty) {
          await _db.saveVerses(versesToSave);
        }
        
        _indexingProgress = (i + 1) / totalBooks;
        notifyListeners();
        
        // Brief breather for responsiveness
        await Future.delayed(const Duration(milliseconds: 5));
      }
      
      await prefs.setBool('bible_search_indexed_v1', true);
    } catch (e) {
      debugPrint('Error indexing Bible: $e');
    } finally {
      _isIndexing = false;
      notifyListeners();
    }
  }

  Future<void> generateAiDevotional(String scripture, String theme) async {
    _isGeneratingAiDevotional = true;
    notifyListeners();

    try {
      final isOnline = await SyncManager.instance.checkOnlineStatus();
      if (!isOnline) {
        throw Exception('No internet connection. Active internet is required for AI Devotionals.');
      }

      final prefs = await SharedPreferences.getInstance();
      String apiKey = prefs.getString('gemini_api_key') ?? const String.fromEnvironment('GEMINI_API_KEY', defaultValue: '');
      
      if (apiKey.isEmpty) {
        debugPrint('Gemini API key is empty. Using high-fidelity local fallback generator.');
        await Future.delayed(const Duration(seconds: 1)); // Simulate AI generation
        
        final dev = Devotional(
          title: 'Visions of $theme',
          date: 'Today',
          scripture: scripture,
          content: 'Meditating on $scripture brings a profound sense of $theme into our daily walks. When we align our thoughts with the eternal truth of God\'s word, our challenges transform into stepping stones of faith, guiding us into deeper fellowship with the Spirit.',
          prayer: 'Heavenly Father, thank You for Your word. Grant me the strength to live out the truth of $scripture and fill my heart with Your $theme today. Amen.',
          isCustom: false,
          timestamp: DateTime.now().millisecondsSinceEpoch,
        );
        await _db.addDevotional(dev);
        _devotionals = await _db.getDevotionals();
        notifyListeners();
        return;
      }

      final prompt = '''
      You are an expert Bible theologian. Generate a beautiful, daily Christian devotional in JSON format based on the scripture "$scripture" and the study theme "$theme".
      Return ONLY a JSON object with exactly the following keys, and nothing else (no markdown blocks, no prefix):
      {
        "title": "A short, beautiful title for the devotional",
        "content": "A beautiful 3-4 sentence spiritual reflection and meditation connecting the scripture to the theme",
        "prayer": "A heartfelt, personal 1-2 sentence prayer related to the reflection"
      }
      ''';

      final response = await http.post(
        Uri.parse('https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'contents': [
            {
              'parts': [
                {'text': prompt}
              ]
            }
          ]
        }),
      ).timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        final body = jsonDecode(response.body);
        String text = body['candidates'][0]['content']['parts'][0]['text'];
        
        text = text.trim();
        if (text.startsWith('```json')) {
          text = text.substring(7);
        }
        if (text.endsWith('```')) {
          text = text.substring(0, text.length - 3);
        }
        text = text.trim();

        final parsed = jsonDecode(text);
        
        final dev = Devotional(
          title: parsed['title'] ?? 'Reflections on $theme',
          date: 'Today',
          scripture: scripture,
          content: parsed['content'] ?? 'Meditating on $scripture.',
          prayer: parsed['prayer'] ?? 'Amen.',
          isCustom: false,
          timestamp: DateTime.now().millisecondsSinceEpoch,
        );
        
        await _db.addDevotional(dev);
        _devotionals = await _db.getDevotionals();
      } else {
        throw Exception('Gemini API returned status code: ${response.statusCode}');
      }
    } catch (e) {
      debugPrint('Error generating AI Devotional: $e');
      rethrow;
    } finally {
      _isGeneratingAiDevotional = false;
      notifyListeners();
    }
  }

  @override
  void dispose() {
    _audioTimer?.cancel();
    _flutterTts.stop();
    super.dispose();
  }
}

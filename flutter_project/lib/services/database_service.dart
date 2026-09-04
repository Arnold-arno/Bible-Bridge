import 'dart:async';
import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';
import '../models/bible_verse.dart';
import '../models/favorite_verse.dart';
import '../models/reading_history.dart';
import '../models/devotional.dart';

class DatabaseService {
  static final DatabaseService instance = DatabaseService._init();
  static Database? _database;

  DatabaseService._init();

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDB('bible_companion.db');
    return _database!;
  }

  Future<Database> _initDB(String filePath) async {
    final dbPath = await getDatabasesPath();
    final path = join(dbPath, filePath);

    final db = await openDatabase(
      path,
      version: 1,
      onCreate: _createDB,
    );

    // Ensure search_history table exists (safest for upgrades!)
    await db.execute('''
      CREATE TABLE IF NOT EXISTS search_history (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        query TEXT UNIQUE,
        timestamp INTEGER
      )
    ''');

    // Ensure sync_tombstones table exists
    await db.execute('''
      CREATE TABLE IF NOT EXISTS sync_tombstones (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        item_type TEXT NOT NULL,
        unique_key TEXT NOT NULL,
        timestamp INTEGER NOT NULL
      )
    ''');

    return db;
  }

  Future<void> _createDB(Database db, int version) async {
    const idType = 'INTEGER PRIMARY KEY AUTOINCREMENT';
    const textType = 'TEXT NOT NULL';
    const textNullableType = 'TEXT';
    const intType = 'INTEGER NOT NULL';
    const intNullableType = 'INTEGER';

    // 1. Bible Verses table
    await db.execute('''
      CREATE TABLE bible_verses (
        id $idType,
        translation $textType,
        bookName $textType,
        chapter $intType,
        verseNumber $intType,
        text $textType
      )
    ''');

    // 2. Favorite Verses table (Bookmarks and Highlights)
    await db.execute('''
      CREATE TABLE favorite_verses (
        id $idType,
        verseId $intType,
        translation $textType,
        bookName $textType,
        chapter $intType,
        verseNumber $intType,
        text $textType,
        colorHex $textType,
        timestamp $intType
      )
    ''');

    // 3. Reading History table
    await db.execute('''
      CREATE TABLE reading_history (
        id $idType,
        bookName $textType,
        chapter $intType,
        verseNumber $intNullableType,
        verseText $textNullableType,
        timestamp $intType
      )
    ''');

    // 4. Devotionals table
    await db.execute('''
      CREATE TABLE devotionals (
        id $idType,
        title $textType,
        date $textType,
        scripture $textType,
        content $textType,
        prayer $textType,
        isCustom $intType,
        timestamp $intType
      )
    ''');

    // 5. Search History table
    await db.execute('''
      CREATE TABLE search_history (
        id $idType,
        query $textType UNIQUE,
        timestamp $intType
      )
    ''');

    // 6. Sync Tombstones table
    await db.execute('''
      CREATE TABLE sync_tombstones (
        id $idType,
        item_type $textType,
        unique_key $textType,
        timestamp $intType
      )
    ''');

    // Seed some initial offline Bible verses for demonstration
    await _seedInitialData(db);
  }

  Future<void> _seedInitialData(Database db) async {
    final verses = [
      BibleVerse(
        translation: 'KJV',
        bookName: 'Genesis',
        chapter: 1,
        verseNumber: 1,
        text: 'In the beginning God created the heaven and the earth.',
      ),
      BibleVerse(
        translation: 'KJV',
        bookName: 'John',
        chapter: 1,
        verseNumber: 1,
        text: 'In the beginning was the Word, and the Word was with God, and the Word was God.',
      ),
      BibleVerse(
        translation: 'KJV',
        bookName: 'John',
        chapter: 3,
        verseNumber: 16,
        text: 'For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life.',
      ),
      BibleVerse(
        translation: 'KJV',
        bookName: 'Romans',
        chapter: 12,
        verseNumber: 1,
        text: 'I beseech you therefore, brethren, by the mercies of God, that ye present your bodies a living sacrifice, holy, acceptable unto God, which is your reasonable service.',
      ),
    ];

    for (var verse in verses) {
      await db.insert('bible_verses', verse.toJson());
    }

    // Seed an initial devotional
    await db.insert('devotionals', {
      'title': 'The Sovereign Creator',
      'date': 'Today',
      'scripture': 'Genesis 1:1',
      'content': 'Taking time to acknowledge God as Creator reorients our perspective. When we view our lives through the lens of His unmatched power, our worries become small and our faith grows deep.',
      'prayer': 'Lord, thank You for creating me with purpose. Direct my steps today and remind me of Your absolute sovereignty. Amen.',
      'isCustom': 0,
      'timestamp': DateTime.now().millisecondsSinceEpoch,
    });
  }

  // --- CRUD operations for Bible Verses ---

  Future<void> saveVerses(List<BibleVerse> verses) async {
    final db = await instance.database;
    final batch = db.batch();
    for (var verse in verses) {
      batch.insert(
        'bible_verses',
        verse.toJson(),
        conflictAlgorithm: ConflictAlgorithm.replace,
      );
    }
    await batch.commit(noResult: true);
  }

  Future<List<BibleVerse>> getVerses(String book, int chapter, String translation) async {
    final db = await instance.database;
    final maps = await db.query(
      'bible_verses',
      where: 'bookName = ? AND chapter = ? AND translation = ?',
      whereArgs: [book, chapter, translation],
    );

    return maps.map((json) => BibleVerse.fromJson(json)).toList();
  }

  // --- CRUD operations for Favorite/Bookmarked Verses ---

  Future<int> addFavorite(FavoriteVerse favorite) async {
    final db = await instance.database;
    return await db.insert('favorite_verses', favorite.toJson());
  }

  Future<int> removeFavorite(int id) async {
    final db = await instance.database;
    
    // Fetch the item first to record a tombstone
    final maps = await db.query(
      'favorite_verses',
      where: 'id = ?',
      whereArgs: [id],
    );
    
    if (maps.isNotEmpty) {
      try {
        final item = FavoriteVerse.fromJson(maps.first);
        final uniqueKey = '${item.bookName}|${item.chapter}|${item.verseNumber}|${item.translation}';
        
        await db.insert('sync_tombstones', {
          'item_type': 'favorite',
          'unique_key': uniqueKey,
          'timestamp': DateTime.now().millisecondsSinceEpoch,
        });
      } catch (_) {}
    }

    return await db.delete(
      'favorite_verses',
      where: 'id = ?',
      whereArgs: [id],
    );
  }

  Future<List<Map<String, dynamic>>> getTombstones() async {
    final db = await instance.database;
    return await db.query('sync_tombstones');
  }

  Future<int> deleteTombstone(int id) async {
    final db = await instance.database;
    return await db.delete(
      'sync_tombstones',
      where: 'id = ?',
      whereArgs: [id],
    );
  }

  Future<int> clearTombstones() async {
    final db = await instance.database;
    return await db.delete('sync_tombstones');
  }

  // Support upserting FavoriteVerse locally (from remote PostgreSQL)
  Future<void> upsertLocalFavorite(FavoriteVerse item) async {
    final db = await instance.database;
    final maps = await db.query(
      'favorite_verses',
      where: 'bookName = ? AND chapter = ? AND verseNumber = ? AND translation = ?',
      whereArgs: [item.bookName, item.chapter, item.verseNumber, item.translation],
    );

    if (maps.isEmpty) {
      await db.insert('favorite_verses', item.toJson());
    } else {
      final existingId = maps.first['id'] as int;
      await db.update(
        'favorite_verses',
        item.toJson(),
        where: 'id = ?',
        whereArgs: [existingId],
      );
    }
  }

  // Support upserting ReadingHistory locally (from remote PostgreSQL)
  Future<void> upsertLocalHistory(ReadingHistory item) async {
    final db = await instance.database;
    final maps = await db.query(
      'reading_history',
      where: 'bookName = ? AND chapter = ? AND timestamp = ?',
      whereArgs: [item.bookName, item.chapter, item.timestamp.millisecondsSinceEpoch],
    );

    if (maps.isEmpty) {
      await db.insert('reading_history', item.toJson());
    }
  }

  Future<List<FavoriteVerse>> getFavorites() async {
    final db = await instance.database;
    final maps = await db.query('favorite_verses', orderBy: 'timestamp DESC');
    return maps.map((json) => FavoriteVerse.fromJson(json)).toList();
  }

  // --- CRUD operations for Reading History ---

  Future<int> addHistoryEntry(ReadingHistory history) async {
    final db = await instance.database;
    return await db.insert('reading_history', history.toJson());
  }

  Future<List<ReadingHistory>> getHistory() async {
    final db = await instance.database;
    final maps = await db.query('reading_history', orderBy: 'timestamp DESC', limit: 30);
    return maps.map((json) => ReadingHistory.fromJson(json)).toList();
  }

  // --- CRUD operations for Devotionals ---

  Future<int> addDevotional(Devotional devotional) async {
    final db = await instance.database;
    return await db.insert('devotionals', devotional.toJson());
  }

  Future<List<Devotional>> getDevotionals() async {
    final db = await instance.database;
    final maps = await db.query('devotionals', orderBy: 'timestamp DESC');
    return maps.map((json) => Devotional.fromJson(json)).toList();
  }

  // --- CRUD operations for Search History ---

  Future<void> addSearchQuery(String query) async {
    final db = await instance.database;
    final trimmed = query.trim();
    if (trimmed.isEmpty) return;

    await db.insert(
      'search_history',
      {
        'query': trimmed,
        'timestamp': DateTime.now().millisecondsSinceEpoch,
      },
      conflictAlgorithm: ConflictAlgorithm.replace,
    );

    // Keep only the top 10 most recent search queries
    final queries = await db.query('search_history', orderBy: 'timestamp DESC');
    if (queries.length > 10) {
      final limitTimestamp = queries[9]['timestamp'] as int;
      await db.delete(
        'search_history',
        where: 'timestamp < ?',
        whereArgs: [limitTimestamp],
      );
    }
  }

  Future<List<String>> getRecentSearches() async {
    final db = await instance.database;
    final maps = await db.query(
      'search_history',
      orderBy: 'timestamp DESC',
      limit: 10,
    );
    return maps.map((m) => m['query'] as String).toList();
  }

  Future<int> deleteSearchQuery(String query) async {
    final db = await instance.database;
    return await db.delete(
      'search_history',
      where: 'query = ?',
      whereArgs: [query.trim()],
    );
  }

  Future<int> clearSearchHistory() async {
    final db = await instance.database;
    return await db.delete('search_history');
  }

  Future<int> clearReadingHistory() async {
    final db = await instance.database;
    return await db.delete('reading_history');
  }

  // --- Verse Search operation ---

  Future<List<BibleVerse>> searchVerses(String query) async {
    final db = await instance.database;
    final trimmed = query.trim();
    if (trimmed.isEmpty) return [];

    final maps = await db.query(
      'bible_verses',
      where: 'text LIKE ? OR bookName LIKE ?',
      whereArgs: ['%$trimmed%', '%$trimmed%'],
      limit: 100,
    );

    return maps.map((json) => BibleVerse.fromJson(json)).toList();
  }

  Future<int> clearVersesForTranslation(String translation) async {
    final db = await instance.database;
    return await db.delete(
      'bible_verses',
      where: 'translation = ?',
      whereArgs: [translation],
    );
  }
}

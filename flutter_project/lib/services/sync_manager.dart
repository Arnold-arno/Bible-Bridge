import 'dart:async';
import 'dart:io';
import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:postgres/postgres.dart';
import 'database_service.dart';
import '../models/favorite_verse.dart';
import '../models/reading_history.dart';

enum SyncStatus { idle, syncing, success, error }

class SyncManager extends ChangeNotifier {
  static final SyncManager instance = SyncManager._init();

  SyncStatus _status = SyncStatus.idle;
  String _message = 'No synchronization performed yet.';
  DateTime? _lastSyncTime;
  bool _isAutoSyncEnabled = true;
  Timer? _autoSyncTimer;

  // PostgreSQL Connection settings (with defaults)
  String _host = 'localhost';
  int _port = 5432;
  String _databaseName = 'bible_sync';
  String _username = 'postgres';
  String _password = 'password';
  bool _useSsl = false;

  SyncStatus get status => _status;
  String get message => _message;
  DateTime? get lastSyncTime => _lastSyncTime;
  bool get isAutoSyncEnabled => _isAutoSyncEnabled;

  String get host => _host;
  int get port => _port;
  String get databaseName => _databaseName;
  String get username => _username;
  String get password => _password;
  bool get useSsl => _useSsl;

  SyncManager._init() {
    _loadSettings();
    _startAutoSyncTimer();
  }

  Future<void> _loadSettings() async {
    final prefs = await SharedPreferences.getInstance();
    _isAutoSyncEnabled = prefs.getBool('sync_auto_enabled') ?? true;
    _host = prefs.getString('sync_pg_host') ?? 'postgres-instance.ctyofvdwvsqq.eu-west-1.rds.amazonaws.com';
    _port = prefs.getInt('sync_pg_port') ?? 5432;
    _databaseName = prefs.getString('sync_pg_db') ?? 'bible_db';
    _username = prefs.getString('sync_pg_user') ?? 'bible_admin';
    _password = prefs.getString('sync_pg_password') ?? 'SecurePassword123!';
    _useSsl = prefs.getBool('sync_pg_ssl') ?? false;

    final lastSyncMs = prefs.getInt('sync_last_time');
    if (lastSyncMs != null) {
      _lastSyncTime = DateTime.fromMillisecondsSinceEpoch(lastSyncMs);
    }
    notifyListeners();
  }

  Future<void> saveSettings({
    required String host,
    required int port,
    required String databaseName,
    required String username,
    required String password,
    required bool useSsl,
    required bool isAutoSyncEnabled,
  }) async {
    _host = host;
    _port = port;
    _databaseName = databaseName;
    _username = username;
    _password = password;
    _useSsl = useSsl;
    _isAutoSyncEnabled = isAutoSyncEnabled;

    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('sync_pg_host', host);
    await prefs.setInt('sync_pg_port', port);
    await prefs.setString('sync_pg_db', databaseName);
    await prefs.setString('sync_pg_user', username);
    await prefs.setString('sync_pg_password', password);
    await prefs.setBool('sync_pg_ssl', useSsl);
    await prefs.setBool('sync_auto_enabled', isAutoSyncEnabled);

    _startAutoSyncTimer();
    notifyListeners();
  }

  void _startAutoSyncTimer() {
    _autoSyncTimer?.cancel();
    if (_isAutoSyncEnabled) {
      // Auto sync every 60 seconds when background or running
      _autoSyncTimer = Timer.periodic(const Duration(seconds: 60), (timer) {
        syncNow();
      });
    }
  }

  // Check true internet / server online status
  Future<bool> checkOnlineStatus() async {
    try {
      // First, perform a quick DNS lookup to check general internet access
      final lookupResult = await InternetAddress.lookup('google.com').timeout(const Duration(seconds: 3));
      if (lookupResult.isEmpty || lookupResult.first.rawAddress.isEmpty) {
        return false;
      }
      return true;
    } catch (_) {
      return false;
    }
  }

  Future<void> syncNow({String userEmail = 'arnowirtz5@gmail.com'}) async {
    if (_status == SyncStatus.syncing) return;

    _status = SyncStatus.syncing;
    _message = 'Checking server availability...';
    notifyListeners();

    final isOnline = await checkOnlineStatus();
    if (!isOnline) {
      _status = SyncStatus.error;
      _message = 'Offline. Synchronization postponed until connection is active.';
      notifyListeners();
      return;
    }

    Connection? conn;
    try {
      _message = 'Connecting to PostgreSQL Database...';
      notifyListeners();

      conn = await Connection.open(
        Endpoint(
          host: _host,
          port: _port,
          database: _databaseName,
          username: _username,
          password: _password,
        ),
        settings: ConnectionSettings(
          sslMode: _useSsl ? SslMode.require : SslMode.disable,
        ),
      ).timeout(const Duration(seconds: 8));

      _message = 'Initializing PostgreSQL Database Schemas...';
      notifyListeners();

      // Ensure remote tables exist
      await conn.execute('''
        CREATE TABLE IF NOT EXISTS favorite_verses (
          id SERIAL PRIMARY KEY,
          user_email VARCHAR(255) NOT NULL,
          verse_id INT NOT NULL,
          translation VARCHAR(50) NOT NULL,
          book_name VARCHAR(100) NOT NULL,
          chapter INT NOT NULL,
          verse_number INT NOT NULL,
          text TEXT NOT NULL,
          color_hex VARCHAR(50) NOT NULL,
          timestamp BIGINT NOT NULL,
          CONSTRAINT unique_fav_verse UNIQUE (user_email, book_name, chapter, verse_number, translation)
        )
      ''');

      await conn.execute('''
        CREATE TABLE IF NOT EXISTS reading_history (
          id SERIAL PRIMARY KEY,
          user_email VARCHAR(255) NOT NULL,
          book_name VARCHAR(100) NOT NULL,
          chapter INT NOT NULL,
          verse_number INT,
          verse_text TEXT,
          timestamp BIGINT NOT NULL,
          CONSTRAINT unique_history UNIQUE (user_email, book_name, chapter, timestamp)
        )
      ''');

      final db = DatabaseService.instance;

      // ----------------------------------------------------
      // PHASE 1: PROCESS DELETIONS (TOMBSTONES) ON REMOTE
      // ----------------------------------------------------
      _message = 'Processing local deletions on remote server...';
      notifyListeners();

      final tombstones = await db.getTombstones();
      for (var t in tombstones) {
        final id = t['id'] as int;
        final type = t['item_type'] as String;
        final uniqueKey = t['unique_key'] as String;
        final timestamp = t['timestamp'] as int;

        if (type == 'favorite') {
          // uniqueKey is bookName|chapter|verseNumber|translation
          final parts = uniqueKey.split('|');
          if (parts.length == 4) {
            final book = parts[0];
            final chapter = int.tryParse(parts[1]) ?? 0;
            final verseNum = int.tryParse(parts[2]) ?? 0;
            final trans = parts[3];

            await conn.execute(
              'DELETE FROM favorite_verses WHERE user_email = \$1 AND book_name = \$2 AND chapter = \$3 AND verse_number = \$4 AND translation = \$5 AND timestamp <= \$6',
              parameters: [userEmail, book, chapter, verseNum, trans, timestamp],
            );
          }
        }
        // Remove local tombstone since it is successfully executed on PostgreSQL
        await db.deleteTombstone(id);
      }

      // ----------------------------------------------------
      // PHASE 2: SYNC FAVORITES & HIGHLIGHTS (BIDIRECTIONAL)
      // ----------------------------------------------------
      _message = 'Synchronizing bookmarks & highlights...';
      notifyListeners();

      // Get local favorites
      final localFavorites = await db.getFavorites();

      // Push local favorites to remote PostgreSQL with ON CONFLICT resolution
      for (var fav in localFavorites) {
        await conn.execute(
          '''
          INSERT INTO favorite_verses (user_email, verse_id, translation, book_name, chapter, verse_number, text, color_hex, timestamp)
          VALUES (\$1, \$2, \$3, \$4, \$5, \$6, \$7, \$8, \$9)
          ON CONFLICT (user_email, book_name, chapter, verse_number, translation)
          DO UPDATE SET
            color_hex = CASE WHEN EXCLUDED.timestamp > favorite_verses.timestamp THEN EXCLUDED.color_hex ELSE favorite_verses.color_hex END,
            timestamp = CASE WHEN EXCLUDED.timestamp > favorite_verses.timestamp THEN EXCLUDED.timestamp ELSE favorite_verses.timestamp END
          ''',
          parameters: [
            userEmail,
            fav.verseId,
            fav.translation,
            fav.bookName,
            fav.chapter,
            fav.verseNumber,
            fav.text,
            fav.colorHex,
            fav.timestamp.millisecondsSinceEpoch,
          ],
        );
      }

      // Pull remote favorites from PostgreSQL
      final remoteFavsResult = await conn.execute(
        'SELECT verse_id, translation, book_name, chapter, verse_number, text, color_hex, timestamp FROM favorite_verses WHERE user_email = \$1',
        parameters: [userEmail],
      );

      for (var row in remoteFavsResult) {
        final verseId = row[0] as int;
        final translation = row[1] as String;
        final bookName = row[2] as String;
        final chapter = row[3] as int;
        final verseNumber = row[4] as int;
        final text = row[5] as String;
        final colorHex = row[6] as String;
        final timestampMs = row[7] as int;

        final remoteTimestamp = DateTime.fromMillisecondsSinceEpoch(timestampMs);

        // Check if there is a local tombstone for this item that is newer than remote timestamp
        final tombstonesForThis = tombstones.where((t) =>
            t['item_type'] == 'favorite' &&
            t['unique_key'] == '$bookName|$chapter|$verseNumber|$translation' &&
            (t['timestamp'] as int) >= timestampMs);

        if (tombstonesForThis.isNotEmpty) {
          // Skip pulling this item as it was deleted locally and should be deleted on remote
          continue;
        }

        // Upsert locally
        final remoteFav = FavoriteVerse(
          verseId: verseId,
          translation: translation,
          bookName: bookName,
          chapter: chapter,
          verseNumber: verseNumber,
          text: text,
          colorHex: colorHex,
          timestamp: remoteTimestamp,
        );

        // Fetch local representation if exists
        final localMatch = localFavorites.firstWhere(
          (lf) =>
              lf.bookName == bookName &&
              lf.chapter == chapter &&
              lf.verseNumber == verseNumber &&
              lf.translation == translation,
          orElse: () => FavoriteVerse(
            verseId: -1,
            translation: '',
            bookName: '',
            chapter: -1,
            verseNumber: -1,
            text: '',
            colorHex: '',
          ),
        );

        if (localMatch.verseId == -1) {
          // Doesn't exist locally: pull it
          await db.upsertLocalFavorite(remoteFav);
        } else {
          // Exists locally: Resolve conflict with Last-Write-Wins
          if (remoteTimestamp.isAfter(localMatch.timestamp)) {
            await db.upsertLocalFavorite(remoteFav);
          }
        }
      }

      // ----------------------------------------------------
      // PHASE 3: SYNC READING HISTORY (BIDIRECTIONAL)
      // ----------------------------------------------------
      _message = 'Synchronizing reading history...';
      notifyListeners();

      // Get local history
      final localHistory = await db.getHistory();

      // Push local history to remote PostgreSQL with conflict resolution
      for (var hist in localHistory) {
        await conn.execute(
          '''
          INSERT INTO reading_history (user_email, book_name, chapter, verse_number, verse_text, timestamp)
          VALUES (\$1, \$2, \$3, \$4, \$5, \$6)
          ON CONFLICT (user_email, book_name, chapter, timestamp)
          DO NOTHING
          ''',
          parameters: [
            userEmail,
            hist.bookName,
            hist.chapter,
            hist.verseNumber,
            hist.verseText,
            hist.timestamp.millisecondsSinceEpoch,
          ],
        );
      }

      // Pull remote history from PostgreSQL
      final remoteHistoryResult = await conn.execute(
        'SELECT book_name, chapter, verse_number, verse_text, timestamp FROM reading_history WHERE user_email = \$1',
        parameters: [userEmail],
      );

      for (var row in remoteHistoryResult) {
        final bookName = row[0] as String;
        final chapter = row[1] as int;
        final verseNumber = row[2] as int?;
        final verseText = row[3] as String?;
        final timestampMs = row[4] as int;

        final remoteTimestamp = DateTime.fromMillisecondsSinceEpoch(timestampMs);

        final remoteHist = ReadingHistory(
          bookName: bookName,
          chapter: chapter,
          verseNumber: verseNumber,
          verseText: verseText,
          timestamp: remoteTimestamp,
        );

        // Check if exists locally
        final existsLocally = localHistory.any((lh) =>
            lh.bookName == bookName &&
            lh.chapter == chapter &&
            lh.timestamp.millisecondsSinceEpoch == timestampMs);

        if (!existsLocally) {
          await db.upsertLocalHistory(remoteHist);
        }
      }

      // Complete Sync
      _lastSyncTime = DateTime.now();
      final prefs = await SharedPreferences.getInstance();
      await prefs.setInt('sync_last_time', _lastSyncTime!.millisecondsSinceEpoch);

      _status = SyncStatus.success;
      _message = 'Synchronization complete! Pushed and merged changes successfully.';
      notifyListeners();
    } catch (e) {
      _status = SyncStatus.error;
      _message = 'Could not sync with PostgreSQL: $e. Your changes are saved locally and will auto-sync when online.';
      notifyListeners();
    } finally {
      if (conn != null) {
        await conn.close();
      }
    }
  }

  @override
  void dispose() {
    _autoSyncTimer?.cancel();
    super.dispose();
  }
}

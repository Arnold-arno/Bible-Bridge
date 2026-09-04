class ReadingHistory {
  final int? id;
  final String bookName;
  final int chapter;
  final int? verseNumber;
  final String? verseText;
  final DateTime timestamp;

  ReadingHistory({
    this.id,
    required this.bookName,
    required this.chapter,
    this.verseNumber,
    this.verseText,
    DateTime? timestamp,
  }) : this.timestamp = timestamp ?? DateTime.now();

  factory ReadingHistory.fromJson(Map<String, dynamic> json) {
    return ReadingHistory(
      id: json['id'] as int?,
      bookName: json['bookName'] as String,
      chapter: json['chapter'] as int,
      verseNumber: json['verseNumber'] as int?,
      verseText: json['verseText'] as String?,
      timestamp: json['timestamp'] != null
          ? DateTime.fromMillisecondsSinceEpoch(json['timestamp'] as int)
          : DateTime.now(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      'bookName': bookName,
      'chapter': chapter,
      'verseNumber': verseNumber,
      'verseText': verseText,
      'timestamp': timestamp.millisecondsSinceEpoch,
    };
  }
}

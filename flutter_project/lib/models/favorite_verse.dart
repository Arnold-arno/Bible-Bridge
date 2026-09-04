class FavoriteVerse {
  final int? id;
  final int verseId;
  final String translation;
  final String bookName;
  final int chapter;
  final int verseNumber;
  final String text;
  final String colorHex; // e.g., "#FF1744"
  final DateTime timestamp;

  FavoriteVerse({
    this.id,
    required this.verseId,
    required this.translation,
    required this.bookName,
    required this.chapter,
    required this.verseNumber,
    required this.text,
    required this.colorHex,
    DateTime? timestamp,
  }) : this.timestamp = timestamp ?? DateTime.now();

  factory FavoriteVerse.fromJson(Map<String, dynamic> json) {
    return FavoriteVerse(
      id: json['id'] as int?,
      verseId: json['verseId'] as int? ?? 0,
      translation: json['translation'] as String,
      bookName: json['bookName'] as String,
      chapter: json['chapter'] as int,
      verseNumber: json['verseNumber'] as int,
      text: json['text'] as String,
      colorHex: json['colorHex'] as String,
      timestamp: json['timestamp'] != null
          ? DateTime.fromMillisecondsSinceEpoch(json['timestamp'] as int)
          : DateTime.now(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      'verseId': verseId,
      'translation': translation,
      'bookName': bookName,
      'chapter': chapter,
      'verseNumber': verseNumber,
      'text': text,
      'colorHex': colorHex,
      'timestamp': timestamp.millisecondsSinceEpoch,
    };
  }
}

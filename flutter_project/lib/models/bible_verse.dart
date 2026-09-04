class BibleVerse {
  final int? id;
  final String translation; // KJV, WEB, ASV
  final String bookName;
  final int chapter;
  final int verseNumber;
  final String text;

  BibleVerse({
    this.id,
    required this.translation,
    required this.bookName,
    required this.chapter,
    required this.verseNumber,
    required this.text,
  });

  // Convert a Map (SQLite row / JSON) into a BibleVerse
  factory BibleVerse.fromJson(Map<String, dynamic> json) {
    return BibleVerse(
      id: json['id'] as int?,
      translation: json['translation'] as String,
      bookName: json['bookName'] as String,
      chapter: json['chapter'] as int,
      verseNumber: json['verseNumber'] as int,
      text: json['text'] as String,
    );
  }

  // Convert a BibleVerse into a Map
  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      'translation': translation,
      'bookName': bookName,
      'chapter': chapter,
      'verseNumber': verseNumber,
      'text': text,
    };
  }

  // CopyWith helper for updating properties
  BibleVerse copyWith({
    int? id,
    String? translation,
    String? bookName,
    int? chapter,
    int? verseNumber,
    String? text,
  }) {
    return BibleVerse(
      id: id ?? this.id,
      translation: translation ?? this.translation,
      bookName: bookName ?? this.bookName,
      chapter: chapter ?? this.chapter,
      verseNumber: verseNumber ?? this.verseNumber,
      text: text ?? this.text,
    );
  }

  @override
  String toString() {
    return '$bookName $chapter:$verseNumber ($translation)';
  }
}

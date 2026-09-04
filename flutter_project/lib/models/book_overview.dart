class BookOverview {
  final String name;
  final String category; // e.g., "Pentateuch", "Gospels", "Epistles"
  final String author;
  final String dateWritten;
  final String theme;
  final String keyVerse;
  final String summary;
  final String characters;
  final String lessons;
  final String majorScenes;
  final List<String> keyCharacters;
  final List<String> centralLessons;

  BookOverview({
    required this.name,
    required this.category,
    required this.author,
    required this.dateWritten,
    required this.theme,
    required this.keyVerse,
    required this.summary,
    this.characters = '',
    this.lessons = '',
    this.majorScenes = '',
    this.keyCharacters = const [],
    this.centralLessons = const [],
  });

  factory BookOverview.fromJson(Map<String, dynamic> json) {
    return BookOverview(
      name: json['name'] as String,
      category: json['category'] as String,
      author: json['author'] as String,
      dateWritten: json['dateWritten'] as String,
      theme: json['theme'] as String,
      keyVerse: json['keyVerse'] as String,
      summary: json['summary'] as String,
      characters: json['characters'] as String? ?? '',
      lessons: json['lessons'] as String? ?? '',
      majorScenes: json['majorScenes'] as String? ?? '',
      keyCharacters: (json['keyCharacters'] as List<dynamic>?)
              ?.map((e) => e as String)
              .toList() ??
          const [],
      centralLessons: (json['centralLessons'] as List<dynamic>?)
              ?.map((e) => e as String)
              .toList() ??
          const [],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'category': category,
      'author': author,
      'dateWritten': dateWritten,
      'theme': theme,
      'keyVerse': keyVerse,
      'summary': summary,
      'characters': characters,
      'lessons': lessons,
      'majorScenes': majorScenes,
      'keyCharacters': keyCharacters,
      'centralLessons': centralLessons,
    };
  }
}

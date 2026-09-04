class Devotional {
  final int? id;
  final String title;
  final String date; // "Monday", "Tuesday", etc., or specific date
  final String scripture;
  final String content;
  final String prayer;
  final bool isCustom;
  final DateTime timestamp;

  Devotional({
    this.id,
    required this.title,
    required this.date,
    required this.scripture,
    required this.content,
    required this.prayer,
    this.isCustom = false,
    DateTime? timestamp,
  }) : this.timestamp = timestamp ?? DateTime.now();

  factory Devotional.fromJson(Map<String, dynamic> json) {
    return Devotional(
      id: json['id'] as int?,
      title: json['title'] as String,
      date: json['date'] as String,
      scripture: json['scripture'] as String,
      content: json['content'] as String,
      prayer: json['prayer'] as String,
      isCustom: (json['isCustom'] as int? ?? 0) == 1 || (json['isCustom'] as bool? ?? false),
      timestamp: json['timestamp'] != null
          ? DateTime.fromMillisecondsSinceEpoch(json['timestamp'] as int)
          : DateTime.now(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      'title': title,
      'date': date,
      'scripture': scripture,
      'content': content,
      'prayer': prayer,
      'isCustom': isCustom ? 1 : 0,
      'timestamp': timestamp.millisecondsSinceEpoch,
    };
  }
}

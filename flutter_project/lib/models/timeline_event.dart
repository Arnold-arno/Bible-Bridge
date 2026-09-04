class TimelineEvent {
  final String title;
  final String period; // e.g., "c. 4000 BC"
  final String description;
  final String scriptureRef;
  final String iconName; // Maps to matching graphic icons in Flutter UI

  TimelineEvent({
    required this.title,
    required this.period,
    required this.description,
    required this.scriptureRef,
    required this.iconName,
  });

  factory TimelineEvent.fromJson(Map<String, dynamic> json) {
    return TimelineEvent(
      title: json['title'] as String,
      period: json['period'] as String,
      description: json['description'] as String,
      scriptureRef: json['scriptureRef'] as String,
      iconName: json['iconName'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'title': title,
      'period': period,
      'description': description,
      'scriptureRef': scriptureRef,
      'iconName': iconName,
    };
  }
}

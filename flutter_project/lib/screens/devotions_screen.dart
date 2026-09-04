import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart';
import 'package:provider/provider.dart';
import '../viewmodels/bible_viewmodel.dart';

class DevotionsScreen extends StatelessWidget {
  const DevotionsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final viewModel = Provider.of<BibleViewModel>(context);
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Daily Devotionals'),
        actions: [
          if (viewModel.devotionals.isNotEmpty) ...[
            IconButton(
              icon: Icon(
                viewModel.isAudioPlaying && viewModel.audioTitle == 'All Devotionals' ? Icons.pause : Icons.volume_up,
                color: viewModel.isAudioPlaying && viewModel.audioTitle == 'All Devotionals' ? Colors.greenAccent : null,
              ),
              onPressed: () {
                if (viewModel.isAudioActive && viewModel.audioTitle == 'All Devotionals') {
                  if (viewModel.isAudioPlaying) {
                    viewModel.pauseAudio();
                  } else {
                    viewModel.playAudio();
                  }
                } else {
                  final allText = viewModel.devotionals.map((d) => 'Devotional: ${d.title}. Scripture context: ${d.scripture}. Reflection: ${d.content}. Prayer: ${d.prayer}').join(' ');
                  viewModel.startAudioReader('All Devotionals', allText);
                }
              },
              tooltip: 'Read All Devotionals',
            ),
            IconButton(
              icon: const Icon(Icons.file_download_outlined),
              onPressed: () {
                _exportAllDevotionals(context, viewModel.devotionals);
              },
              tooltip: 'Export All Devotionals',
            ),
          ],
          IconButton(
            icon: const Icon(Icons.auto_awesome, color: Colors.amber),
            onPressed: () {
              _showAiDevotionalDialog(context, viewModel);
            },
            tooltip: 'Generate AI Devotional',
          ),
          IconButton(
            icon: const Icon(Icons.add),
            onPressed: () {
              _showAddDevotionalDialog(context, viewModel);
            },
            tooltip: 'Add Custom Devotional',
          ),
        ],
      ),
      body: viewModel.devotionals.isEmpty
          ? const Center(
              child: Padding(
                padding: EdgeInsets.all(24.0),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(Icons.edit_note, size: 64, color: Colors.grey),
                    SizedBox(height: 12),
                    Text(
                      'No devotionals loaded yet.\nTap the + button above to write your own, or select a Bible passage to start reading.',
                      textAlign: TextAlign.center,
                      style: TextStyle(color: Colors.grey),
                    ),
                  ],
                ),
              ),
            )
          : ListView.builder(
              padding: const EdgeInsets.all(16.0),
              itemCount: viewModel.devotionals.length,
              itemBuilder: (context, index) {
                final dev = viewModel.devotionals[index];

                return Card(
                  margin: const EdgeInsets.only(bottom: 16.0),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
                  child: ExpansionTile(
                    leading: Icon(
                      dev.isCustom ? Icons.edit_note : Icons.auto_awesome,
                      color: theme.colorScheme.primary,
                    ),
                    title: Text(
                      dev.title,
                      style: const TextStyle(fontWeight: FontWeight.bold),
                    ),
                    subtitle: Text('Scripture: ${dev.scripture}'),
                    children: [
                      Padding(
                        padding: const EdgeInsets.all(16.0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'Meditation',
                              style: theme.textTheme.titleSmall?.copyWith(
                                color: theme.colorScheme.primary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            const SizedBox(height: 6.0),
                            Text(
                              dev.content,
                              style: const TextStyle(height: 1.4),
                            ),
                            const SizedBox(height: 16.0),
                            Text(
                              'Prayer Reflection',
                              style: theme.textTheme.titleSmall?.copyWith(
                                color: theme.colorScheme.secondary,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            const SizedBox(height: 6.0),
                            Text(
                              dev.prayer,
                              style: const TextStyle(fontStyle: FontStyle.Italic, height: 1.4),
                            ),
                            const SizedBox(height: 16.0),
                            Row(
                              mainAxisAlignment: MainAxisAlignment.end,
                              children: [
                                ElevatedButton.icon(
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: viewModel.isAudioPlaying && viewModel.audioTitle == dev.title ? Colors.green : null,
                                    foregroundColor: viewModel.isAudioPlaying && viewModel.audioTitle == dev.title ? Colors.white : null,
                                    padding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 8.0),
                                  ),
                                  onPressed: () {
                                    if (viewModel.isAudioActive && viewModel.audioTitle == dev.title) {
                                      if (viewModel.isAudioPlaying) {
                                        viewModel.pauseAudio();
                                      } else {
                                        viewModel.playAudio();
                                      }
                                    } else {
                                      viewModel.startAudioReader(
                                        dev.title,
                                        'Devotional: ${dev.title}. Scripture context: ${dev.scripture}. Reflection: ${dev.content}. Prayer: ${dev.prayer}',
                                      );
                                    }
                                  },
                                  icon: Icon(
                                    viewModel.isAudioPlaying && viewModel.audioTitle == dev.title ? Icons.pause : Icons.volume_up,
                                    size: 18.0,
                                  ),
                                  label: Text(viewModel.isAudioPlaying && viewModel.audioTitle == dev.title ? 'Pause' : 'Read Aloud'),
                                ),
                                const SizedBox(width: 8.0),
                                ElevatedButton.icon(
                                  onPressed: () {
                                    _exportSingleDevotional(context, dev);
                                  },
                                  icon: const Icon(Icons.file_download, size: 18.0),
                                  label: const Text('Export'),
                                  style: ElevatedButton.styleFrom(
                                    backgroundColor: theme.colorScheme.secondaryContainer,
                                    foregroundColor: theme.colorScheme.onSecondaryContainer,
                                    padding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 8.0),
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ),
                      )
                    ],
                  ),
                );
              },
            ),
    );
  }

  void _showAiDevotionalDialog(BuildContext context, BibleViewModel viewModel) {
    final scriptureController = TextEditingController(
      text: '${viewModel.selectedBook} ${viewModel.selectedChapter}:${viewModel.selectedVerseNumber}'
    );
    String selectedTheme = 'Peace';
    final List<String> themes = ['Faith', 'Grace', 'Peace', 'Love', 'Hope', 'Wisdom', 'Strength', 'Comfort', 'Joy', 'Patience'];

    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setDialogState) {
            final isGenerating = viewModel.isGeneratingAiDevotional;

            return AlertDialog(
              title: Row(
                children: [
                  Icon(Icons.auto_awesome, color: Colors.amber[700]),
                  const SizedBox(width: 8),
                  const Text('AI Devotional'),
                ],
              ),
              content: isGenerating
                  ? Container(
                      padding: const EdgeInsets.symmetric(vertical: 24.0),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const CircularProgressIndicator(color: Colors.greenAccent),
                          const SizedBox(height: 16),
                          Text(
                            'Gemini is crafting your devotional reflection...',
                            textAlign: TextAlign.center,
                            style: TextStyle(fontWeight: FontWeight.bold, color: Theme.of(context).colorScheme.primary),
                          ),
                          const SizedBox(height: 8),
                          const Text(
                            'Connecting to Gemini 1.5 Flash via REST API...',
                            style: TextStyle(fontSize: 11, color: Colors.grey),
                          ),
                        ],
                      ),
                    )
                  : SingleChildScrollView(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text(
                            'Generate a personalized devotional using Gemini AI. Works online; falls back offline gracefully.',
                            style: TextStyle(fontSize: 12, color: Colors.grey),
                          ),
                          const SizedBox(height: 16),
                          TextField(
                            controller: scriptureController,
                            decoration: const InputDecoration(
                              labelText: 'Scripture Reference Context',
                              border: OutlineInputBorder(),
                            ),
                          ),
                          const SizedBox(height: 16),
                          Text(
                            'Select Study Theme',
                            style: TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                              color: Theme.of(context).colorScheme.primary,
                            ),
                          ),
                          const SizedBox(height: 8),
                          DropdownButtonFormField<String>(
                            value: selectedTheme,
                            decoration: const InputDecoration(
                              border: OutlineInputBorder(),
                              contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                            ),
                            onChanged: (val) {
                              if (val != null) {
                                setDialogState(() {
                                  selectedTheme = val;
                                });
                              }
                            },
                            items: themes.map((theme) {
                              return DropdownMenuItem(
                                value: theme,
                                child: Text(theme),
                              );
                            }).toList(),
                          ),
                        ],
                      ),
                    ),
              actions: isGenerating
                  ? null
                  : [
                      TextButton(
                        onPressed: () => Navigator.pop(context),
                        child: const Text('Cancel'),
                      ),
                      ElevatedButton.icon(
                        icon: const Icon(Icons.auto_awesome, size: 16),
                        onPressed: () async {
                          final scripture = scriptureController.text.trim();
                          if (scripture.isNotEmpty) {
                            setDialogState(() {});
                            try {
                              await viewModel.generateAiDevotional(scripture, selectedTheme);
                              Navigator.pop(context);
                              ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(
                                  content: Text('AI Devotional generated successfully with Gemini!'),
                                  backgroundColor: Colors.green,
                                ),
                              );
                            } catch (e) {
                              ScaffoldMessenger.of(context).showSnackBar(
                                SnackBar(
                                  content: Text('Generation failed: ${e.toString()}'),
                                  backgroundColor: Colors.red,
                                ),
                              );
                            }
                          }
                        },
                        label: const Text('Generate'),
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Theme.of(context).colorScheme.primary,
                          foregroundColor: Colors.white,
                        ),
                      ),
                    ],
            );
          },
        );
      },
    );
  }

  void _showAddDevotionalDialog(BuildContext context, BibleViewModel viewModel) {
    final titleController = TextEditingController();
    final scriptureController = TextEditingController();
    final contentController = TextEditingController();
    final prayerController = TextEditingController();

    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text('Write Devotional Note'),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: titleController,
                  decoration: const InputDecoration(labelText: 'Devotional Title'),
                ),
                const SizedBox(height: 8.0),
                TextField(
                  controller: scriptureController,
                  decoration: const InputDecoration(labelText: 'Scripture Reference'),
                ),
                const SizedBox(height: 8.0),
                TextField(
                  controller: contentController,
                  maxLines: 4,
                  decoration: const InputDecoration(labelText: 'Meditation Reflection'),
                ),
                const SizedBox(height: 8.0),
                TextField(
                  controller: prayerController,
                  maxLines: 3,
                  decoration: const InputDecoration(labelText: 'Prayer Note'),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () {
                if (titleController.text.isNotEmpty && contentController.text.isNotEmpty) {
                  viewModel.addCustomDevotional(
                    titleController.text,
                    contentController.text,
                    prayerController.text,
                    scriptureController.text,
                  );
                  Navigator.pop(context);
                }
              },
              child: const Text('Save'),
            ),
          ],
        );
      },
    );
  }

  Future<void> _exportSingleDevotional(BuildContext context, dynamic dev) async {
    try {
      final textContent = _formatDevotionalText(dev);
      final directory = await getApplicationDocumentsDirectory();
      // Safe filename from title
      final safeTitle = dev.title.replaceAll(RegExp(r'[^\w\s-]'), '').replaceAll(RegExp(r'\s+'), '_').toLowerCase();
      final filePath = '${directory.path}/devotional_$safeTitle.txt';
      final file = File(filePath);
      await file.writeAsString(textContent);

      if (context.mounted) {
        _showExportSuccessDialog(context, 'Devotional Exported', filePath, textContent);
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Failed to export devotional: $e'), backgroundColor: Colors.redAccent),
        );
      }
    }
  }

  Future<void> _exportAllDevotionals(BuildContext context, List<dynamic> devotionals) async {
    if (devotionals.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('No devotionals available to export.')),
      );
      return;
    }

    try {
      final buffer = StringBuffer();
      buffer.writeln('==================================================');
      buffer.writeln('BIBLE CHRONOLOGY COMPANION - EXPORTED DEVOTIONALS');
      buffer.writeln('Export Date: ${DateTime.now().toLocal().toString().split('.')[0]}');
      buffer.writeln('Total Devotionals: ${devotionals.length}');
      buffer.writeln('==================================================\n\n');

      for (var i = 0; i < devotionals.length; i++) {
        buffer.writeln(_formatDevotionalText(devotionals[i]));
        if (i < devotionals.length - 1) {
          buffer.writeln('\n\n${'=' * 50}\n\n');
        }
      }

      final textContent = buffer.toString();
      final directory = await getApplicationDocumentsDirectory();
      final filePath = '${directory.path}/all_devotionals_export.txt';
      final file = File(filePath);
      await file.writeAsString(textContent);

      if (context.mounted) {
        _showExportSuccessDialog(context, 'All Devotionals Exported', filePath, textContent);
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Failed to export devotionals: $e'), backgroundColor: Colors.redAccent),
        );
      }
    }
  }

  String _formatDevotionalText(dynamic dev) {
    final source = dev.isCustom ? 'Custom Reflection Note' : 'AI Generated Devotional';
    return '''
==================================================
Daily Devotional: ${dev.title}
Date/Theme: ${dev.date}
Scripture Context: ${dev.scripture}
Source: $source
Timestamp: ${dev.timestamp.toLocal().toString().split('.')[0]}
==================================================

MEDITATION:
${dev.content}

PRAYER REFLECTION:
${dev.prayer}

==================================================
''';
  }

  void _showExportSuccessDialog(BuildContext context, String title, String filePath, String textContent) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          title: Row(
            children: [
              const Icon(Icons.check_circle, color: Colors.greenAccent, size: 28),
              const SizedBox(width: 10),
              Text(
                title,
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
              ),
            ],
          ),
          content: Container(
            width: double.maxFinite,
            constraints: const BoxConstraints(maxHeight: 400),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  'The text file has been successfully generated and saved to your device.',
                  style: TextStyle(fontSize: 13, height: 1.3),
                ),
                const SizedBox(height: 12),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  decoration: BoxDecoration(
                    color: isDark ? const Color(0xFF1E2124) : Colors.grey.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(8),
                    border: Border.all(color: Colors.grey.withOpacity(0.3)),
                  ),
                  child: Row(
                    children: [
                      const Icon(Icons.folder_open, size: 18, color: Colors.grey),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          filePath,
                          style: const TextStyle(fontSize: 11, fontFamily: 'monospace'),
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),
                Text(
                  'PREVIEW:',
                  style: theme.textTheme.titleSmall?.copyWith(
                    fontWeight: FontWeight.bold,
                    fontSize: 11,
                    color: theme.colorScheme.primary,
                  ),
                ),
                const SizedBox(height: 6),
                Expanded(
                  child: Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: isDark ? Colors.black54 : Colors.grey.withOpacity(0.05),
                      borderRadius: BorderRadius.circular(10),
                      border: Border.all(color: Colors.grey.withOpacity(0.2)),
                    ),
                    child: SingleChildScrollView(
                      physics: const BouncingScrollPhysics(),
                      child: Text(
                        textContent,
                        style: TextStyle(
                          fontSize: 11.5,
                          fontFamily: 'monospace',
                          height: 1.3,
                          color: isDark ? Colors.white70 : Colors.black87,
                        ),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Close'),
            ),
            ElevatedButton.icon(
              style: ElevatedButton.styleFrom(
                backgroundColor: theme.colorScheme.primary,
                foregroundColor: theme.colorScheme.onPrimary,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
              ),
              icon: const Icon(Icons.copy, size: 16),
              label: const Text('Copy Text'),
              onPressed: () {
                Clipboard.setData(ClipboardData(text: textContent));
                Navigator.pop(context);
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Copied devotional content to clipboard!'),
                    behavior: SnackBarBehavior.floating,
                  ),
                );
              },
            ),
          ],
        );
      },
    );
  }
}

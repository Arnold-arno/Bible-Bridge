import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart';
import 'package:provider/provider.dart';
import '../viewmodels/bible_viewmodel.dart';
import 'search_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  Future<void> _shareDailyVerse(BuildContext context, BibleViewModel viewModel, String format) async {
    final theme = Theme.of(context);
    final verse = viewModel.dailyVerse;
    final verseText = viewModel.dailyVerseText;
    final ref = "${verse['book']} ${verse['chapter']}:${verse['verse']} (${viewModel.selectedTranslation})";
    final title = verse['title'] ?? 'Daily Scripture';

    if (format == 'document') {
      try {
        final textContent = "========================================\n"
            "        BIBLE CHRONOLOGY COMPANION      \n"
            "             VERSE OF THE DAY           \n"
            "========================================\n\n"
            "Title: $title\n"
            "Reference: $ref\n"
            "Date: ${DateTime.now().toLocal().toString().split(' ')[0]}\n\n"
            "Scripture:\n"
            "\"$verseText\"\n\n"
            "========================================\n"
            "Shared via Bible Chronology Companion\n";

        final directory = await getApplicationDocumentsDirectory();
        final safeTitle = title.replaceAll(RegExp(r'[^\w\s-]'), '').replaceAll(RegExp(r'\s+'), '_').toLowerCase();
        final filePath = '${directory.path}/verse_$safeTitle.txt';
        final file = File(filePath);
        await file.writeAsString(textContent);

        await Clipboard.setData(ClipboardData(text: textContent));

        if (context.mounted) {
          showDialog(
            context: context,
            builder: (context) {
              return AlertDialog(
                title: const Row(
                  children: [
                    Icon(Icons.check_circle, color: Colors.green),
                    SizedBox(width: 8),
                    Text('Saved as Document'),
                  ],
                ),
                content: Column(
                  mainAxisSize: MainAxisSize.min,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('The verse document has been saved successfully and the text has been copied to your clipboard!\n', style: TextStyle(fontSize: 14)),
                    const Text('File Location:', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12)),
                    SelectableText(
                      filePath,
                      style: const TextStyle(fontFamily: 'monospace', fontSize: 11, color: Colors.blueAccent),
                    ),
                    const SizedBox(height: 12),
                    const Text('Preview:', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 12)),
                    Container(
                      maxHeight: 120,
                      width: double.infinity,
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: theme.brightness == Brightness.dark ? Colors.grey[900] : Colors.grey[100],
                        borderRadius: BorderRadius.circular(6),
                      ),
                      child: SingleChildScrollView(
                        child: Text(
                          textContent,
                          style: const TextStyle(fontFamily: 'monospace', fontSize: 11),
                        ),
                      ),
                    ),
                  ],
                ),
                actions: [
                  TextButton(
                    onPressed: () => Navigator.pop(context),
                    child: const Text('Close'),
                  ),
                ],
              );
            },
          );
        }
      } catch (e) {
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Failed to save document: $e'), backgroundColor: Colors.redAccent),
          );
        }
      }
    } else if (format == 'card') {
      showDialog(
        context: context,
        builder: (context) {
          return Dialog(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
            backgroundColor: Colors.transparent,
            child: Container(
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(20),
                gradient: const LinearGradient(
                  colors: [Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                boxShadow: const [
                  BoxShadow(color: Colors.black45, blurRadius: 10, offset: Offset(0, 5)),
                ],
              ),
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Icon(Icons.star_purple500_sharp, color: Colors.amber, size: 36),
                  const SizedBox(height: 12),
                  Text(
                    title.toUpperCase(),
                    style: const TextStyle(
                      color: Colors.white70,
                      fontSize: 12,
                      letterSpacing: 2,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 16),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 16),
                    decoration: BoxDecoration(
                      color: Colors.white.withOpacity(0.05),
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: Colors.white12),
                    ),
                    child: Column(
                      children: [
                        const Text(
                          '“',
                          style: TextStyle(
                            color: Colors.amber,
                            fontSize: 40,
                            fontFamily: 'serif',
                            height: 0.5,
                          ),
                        ),
                        Text(
                          verseText,
                          textAlign: TextAlign.center,
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 16,
                            fontStyle: FontStyle.Italic,
                            fontFamily: 'serif',
                            height: 1.4,
                          ),
                        ),
                        const Text(
                          '”',
                          style: TextStyle(
                            color: Colors.amber,
                            fontSize: 40,
                            fontFamily: 'serif',
                            height: 0.5,
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 16),
                  Text(
                    ref,
                    style: const TextStyle(
                      color: Colors.amber,
                      fontWeight: FontWeight.bold,
                      fontSize: 15,
                    ),
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'Bible Chronology Companion',
                    style: TextStyle(
                      color: Colors.white38,
                      fontSize: 11,
                    ),
                  ),
                  const SizedBox(height: 24),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    children: [
                      ElevatedButton.icon(
                        onPressed: () async {
                          final clipboardText = '"$verseText"\n— $ref\n\nShared via Bible Chronology Companion';
                          await Clipboard.setData(ClipboardData(text: clipboardText));
                          if (context.mounted) {
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(content: Text('Visual Card content copied to clipboard!')),
                            );
                            Navigator.pop(context);
                          }
                        },
                        icon: const Icon(Icons.copy, size: 16),
                        label: const Text('Copy Card'),
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.amber,
                          foregroundColor: Colors.black,
                        ),
                      ),
                      TextButton(
                        onPressed: () => Navigator.pop(context),
                        child: const Text('Close', style: TextStyle(color: Colors.white70)),
                      ),
                    ],
                  )
                ],
              ),
            ),
          );
        },
      );
    }
  }

  void _showShareOptions(BuildContext context, BibleViewModel viewModel) {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) {
        return Container(
          padding: const EdgeInsets.symmetric(vertical: 24, horizontal: 16),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Padding(
                padding: EdgeInsets.symmetric(horizontal: 12.0),
                child: Text(
                  'Share Verse of the Day',
                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
                ),
              ),
              const SizedBox(height: 16),
              ListTile(
                leading: const Icon(Icons.image, color: Colors.blueAccent),
                title: const Text('Share as Stylized Visual Card'),
                subtitle: const Text('View and copy a beautiful card layout with ambient colors'),
                onTap: () {
                  Navigator.pop(context);
                  _shareDailyVerse(context, viewModel, 'card');
                },
              ),
              const Divider(),
              ListTile(
                leading: const Icon(Icons.description, color: Colors.green),
                title: const Text('Share as Text Document'),
                subtitle: const Text('Saves a formatted text file and copies to clipboard'),
                onTap: () {
                  Navigator.pop(context);
                  _shareDailyVerse(context, viewModel, 'document');
                },
              ),
            ],
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final viewModel = Provider.of<BibleViewModel>(context);
    final theme = Theme.of(context);
    final dailyV = viewModel.dailyVerse;
    final dailyVText = viewModel.dailyVerseText;
    final dailyVRef = "${dailyV['book']} ${dailyV['chapter']}:${dailyV['verse']} (${viewModel.selectedTranslation})";

    return Scaffold(
      appBar: AppBar(
        title: const Text('Bible Chronology Companion'),
        actions: [
          IconButton(
            icon: const Icon(Icons.search),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const SearchScreen()),
              );
            },
            tooltip: 'Search Bible',
          ),
          IconButton(
            icon: const Icon(Icons.info_outline),
            onPressed: () {
              showAboutDialog(
                context: context,
                applicationName: 'Bible Chronology Companion',
                applicationVersion: '1.0.0',
                children: [
                  const Text('A cross-platform app leveraging timelines, book overviews, and cross-references directly inside the scripture reading pane.'),
                ],
              );
            },
          )
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Image-derived Greeting & Offline Status (Not Sticky)
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Good Morning, Faithful Reader! 🌟',
                  style: theme.textTheme.headlineSmall?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: theme.brightness == Brightness.dark
                        ? const Color(0xFF64B5F6)
                        : theme.colorScheme.primary,
                  ),
                ),
                const SizedBox(height: 4.0),
                Text(
                  'Welcome to Bible Bridge — Connecting Every Verse to Its Story.',
                  style: theme.textTheme.bodyMedium?.copyWith(
                    color: theme.brightness == Brightness.dark
                        ? Colors.white70
                        : Colors.black87,
                  ),
                ),
                const SizedBox(height: 8.0),
                Row(
                  children: [
                    Container(
                      width: 8.0,
                      height: 8.0,
                      decoration: const BoxDecoration(
                        color: Color(0xFF00FF00),
                        shape: BoxShape.circle,
                      ),
                    ),
                    const SizedBox(width: 8.0),
                    const Text(
                      'Offline Reading Mode Active',
                      style: TextStyle(
                        color: Color(0xFF4CAF50),
                        fontWeight: FontWeight.bold,
                        fontSize: 12.0,
                      ),
                    ),
                  ],
                ),
              ],
            ),
            const SizedBox(height: 24.0),

            // 1. Beautiful Hero Banner Card
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(20.0),
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [theme.colorScheme.primary, theme.colorScheme.primary.withOpacity(0.75)],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                borderRadius: BorderRadius.circular(16.0),
                boxShadow: [
                  BoxShadow(
                    color: theme.colorScheme.primary.withOpacity(0.2),
                    blurRadius: 8.0,
                    offset: const Offset(0, 4),
                  )
                ],
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Chronological Scripture Study',
                    style: theme.textTheme.titleMedium?.copyWith(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 8.0),
                  Text(
                    'Study the Word through historical timelines, book overviews, and cross references in a unified study view.',
                    style: theme.textTheme.bodyMedium?.copyWith(
                      color: Colors.white.withOpacity(0.9),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24.0),

            // 2. Verse of the Day
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Verse of the Day',
                  style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold),
                ),
                IconButton(
                  icon: const Icon(Icons.volume_up, color: Colors.blue),
                  onPressed: () {
                    viewModel.startAudioReader(
                      'Verse of the Day: ${dailyV['title'] ?? 'Scripture'}',
                      dailyVText,
                    );
                  },
                  tooltip: 'Read Aloud',
                ),
              ],
            ),
            const SizedBox(height: 8.0),
            Card(
              elevation: 0,
              color: theme.colorScheme.secondaryContainer.withOpacity(0.4),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  children: [
                    Text(
                      '"$dailyVText"',
                      textAlign: TextAlign.center,
                      style: const TextStyle(fontStyle: FontStyle.Italic, fontSize: 15.0),
                    ),
                    const SizedBox(height: 12.0),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Expanded(
                          child: Text(
                            dailyVRef,
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              color: theme.colorScheme.primary,
                            ),
                          ),
                        ),
                        Row(
                          children: [
                            IconButton(
                              icon: const Icon(Icons.share, color: Colors.teal),
                              onPressed: () => _showShareOptions(context, viewModel),
                              tooltip: 'Share Verse',
                            ),
                            const SizedBox(width: 8.0),
                            ElevatedButton.icon(
                              onPressed: () {
                                viewModel.selectBook(dailyV['book']!);
                                viewModel.selectChapter(int.parse(dailyV['chapter']!));
                                viewModel.selectTab(ActiveTab.read);
                              },
                              icon: const Icon(Icons.chrome_reader_mode, size: 16.0),
                              label: const Text('Read Now'),
                              style: ElevatedButton.styleFrom(
                                padding: const EdgeInsets.symmetric(horizontal: 12.0, vertical: 8.0),
                              ),
                            ),
                          ],
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24.0),

            // 3. Historical Timeline Sneak Peek
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Historical Timeline Highlights',
                  style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold),
                ),
                TextButton(
                  onPressed: () => viewModel.selectTab(ActiveTab.timeline),
                  child: const Text('See All'),
                )
              ],
            ),
            const SizedBox(height: 8.0),
            SizedBox(
              height: 140.0,
              child: ListView.builder(
                scrollDirection: Axis.horizontal,
                itemCount: 4,
                itemBuilder: (context, index) {
                  final event = viewModel.timelineEvents[index];
                  return Card(
                    margin: const EdgeInsets.only(right: 12.0),
                    child: Container(
                      width: 200.0,
                      padding: const EdgeInsets.all(12.0),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Expanded(
                                child: Text(
                                  event.title,
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                  style: const TextStyle(fontWeight: FontWeight.bold),
                                ),
                              ),
                              Text(
                                event.period,
                                style: TextStyle(
                                  fontSize: 10.0,
                                  color: theme.colorScheme.primary,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 6.0),
                          Expanded(
                            child: Text(
                              event.description,
                              maxLines: 3,
                              overflow: TextOverflow.ellipsis,
                              style: theme.textTheme.bodySmall?.copyWith(height: 1.3),
                            ),
                          ),
                          const SizedBox(height: 4.0),
                          Text(
                            event.scriptureRef,
                            style: TextStyle(
                              fontSize: 10.0,
                              fontWeight: FontWeight.bold,
                              color: theme.colorScheme.secondary,
                            ),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
            const SizedBox(height: 24.0),

            // 4. Reading History
            if (viewModel.history.isNotEmpty) ...[
              Text(
                'Recent Reading Progress',
                style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 8.0),
              ListView.builder(
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                itemCount: viewModel.history.take(3).length,
                itemBuilder: (context, index) {
                  final hist = viewModel.history[index];
                  return ListTile(
                    leading: const Icon(Icons.history_toggle_off),
                    title: Text('${hist.bookName} Chapter ${hist.chapter}'),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: () {
                      viewModel.selectBook(hist.bookName);
                      viewModel.selectChapter(hist.chapter);
                      viewModel.selectTab(ActiveTab.read);
                    },
                  );
                },
              ),
            ],
          ],
        ),
      ),
    );
  }
}

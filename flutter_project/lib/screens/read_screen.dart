import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../viewmodels/bible_viewmodel.dart';
import '../models/bible_verse.dart';
import '../services/bible_text_generator.dart';
import 'search_screen.dart';

class ReadScreen extends StatefulWidget {
  const ReadScreen({super.key});

  @override
  State<ReadScreen> createState() => _ReadScreenState();
}

class _ReadScreenState extends State<ReadScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;
  final ScrollController _scriptureScrollController = ScrollController();
  BibleViewModel? _viewModel;
  int _lastSentenceIndex = -1;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 4, vsync: this);
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    final newViewModel = Provider.of<BibleViewModel>(context);
    if (_viewModel != newViewModel) {
      _viewModel?.removeListener(_onViewModelChanged);
      _viewModel = newViewModel;
      _viewModel?.addListener(_onViewModelChanged);
    }
  }

  void _onViewModelChanged() {
    if (_viewModel == null) return;
    
    // Auto scroll when playing audio
    if (_viewModel!.isAudioPlaying && _viewModel!.isAudioActive) {
      final currentIdx = _viewModel!.currentSentenceIndex;
      final totalSentences = _viewModel!.audioSentences.length;
      if (currentIdx != _lastSentenceIndex && totalSentences > 0) {
        _lastSentenceIndex = currentIdx;
        
        if (_scriptureScrollController.hasClients) {
          final maxScroll = _scriptureScrollController.position.maxScrollExtent;
          final ratio = currentIdx / totalSentences;
          final targetOffset = (maxScroll * ratio).clamp(0.0, maxScroll);
          
          _scriptureScrollController.animateTo(
            targetOffset,
            duration: const Duration(milliseconds: 1000),
            curve: Curves.easeInOut,
          );
        }
      }
    }
  }

  @override
  void dispose() {
    _viewModel?.removeListener(_onViewModelChanged);
    _scriptureScrollController.dispose();
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final viewModel = Provider.of<BibleViewModel>(context);
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Bible Reader'),
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
            icon: const Icon(Icons.format_size),
            onPressed: () {
              _showFontSettingsDialog(context, viewModel);
            },
            tooltip: 'Reader Settings',
          ),
        ],
      ),
      body: LayoutBuilder(
        builder: (context, constraints) {
          final isWide = constraints.maxWidth >= 800;

          return Column(
            children: [
              // Beautiful 3-Row Arrow Navigation Panel for Testament-Book, Chapter, and Verse
              _buildArrowNavigationPanel(context, viewModel),

              // 3. Comparison Mode Controller row
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 2.0),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Row(
                      children: [
                        const Icon(Icons.compare, color: Colors.blueAccent, size: 18),
                        const SizedBox(width: 8),
                        const Text('Compare Versions', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13)),
                        const SizedBox(width: 6),
                        Text(
                          '[With ${viewModel.compareTranslation}]',
                          style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.greenAccent, fontSize: 13),
                        ),
                      ],
                    ),
                    Row(
                      children: [
                        if (viewModel.compareMode) ...[
                          DropdownButton<String>(
                            value: viewModel.compareTranslation,
                            underline: const SizedBox(),
                            icon: const Icon(Icons.arrow_drop_down, color: Colors.blueAccent),
                            onChanged: (val) {
                              if (val != null) {
                                viewModel.setCompareTranslation(val);
                              }
                            },
                            items: viewModel.translations
                                .where((t) => t != viewModel.selectedTranslation)
                                .map((t) => DropdownMenuItem(value: t, child: Text(t, style: const TextStyle(fontSize: 12, color: Colors.blueAccent))))
                                .toList(),
                          ),
                          const SizedBox(width: 8),
                        ],
                        Switch(
                          value: viewModel.compareMode,
                          activeColor: Colors.greenAccent,
                          onChanged: (val) {
                            viewModel.toggleCompareMode(val);
                          },
                        ),
                      ],
                    ),
                  ],
                ),
              ),
              const Divider(height: 1),

              // Responsive reader body
              Expanded(
                child: isWide
                    ? Row(
                        children: [
                          Expanded(
                            flex: 3,
                            child: _buildScriptureList(viewModel, theme),
                          ),
                          const VerticalDivider(width: 1),
                          Expanded(
                            flex: 2,
                            child: _buildStudyPanelTabs(viewModel, isWide: true),
                          ),
                        ],
                      )
                    : Column(
                        children: [
                          Expanded(
                            child: _buildScriptureList(viewModel, theme),
                          ),
                          // Floating HUD or integrated panel for Seekable Audio
                          _buildAudioControlPanel(viewModel),
                          const Divider(height: 1),
                          Container(
                            height: 220,
                            decoration: BoxDecoration(
                              color: theme.colorScheme.surface,
                              boxShadow: [
                                BoxShadow(
                                  color: Colors.black.withOpacity(0.05),
                                  blurRadius: 4.0,
                                  offset: const Offset(0, -2),
                                ),
                              ],
                            ),
                            child: _buildStudyPanelTabs(viewModel, isWide: false),
                          ),
                        ],
                      ),
              ),
            ],
          );
        },
      ),
    );
  }

  String _getTestamentOfBook(String book) {
    if (BibleViewModel.oldTestamentBooks.contains(book)) {
      return 'Old Testament';
    } else {
      return 'New Testament';
    }
  }

  Widget _buildArrowNavigationPanel(BuildContext context, BibleViewModel viewModel) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final textStyle = const TextStyle(
      fontSize: 16,
      fontWeight: FontWeight.bold,
      letterSpacing: 1.1,
    );

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 12.0),
      margin: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
      decoration: BoxDecoration(
        color: isDark ? const Color(0xFF1E2124) : Colors.green.withOpacity(0.04),
        borderRadius: BorderRadius.circular(16.0),
        border: Border.all(
          color: Colors.greenAccent.withOpacity(0.3),
          width: 1.5,
        ),
      ),
      child: Column(
        children: [
          // Translation & Read control bar
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              // Translation selector dropdown
              InkWell(
                onTap: () => _showTranslationSelectionSheet(context, viewModel),
                borderRadius: BorderRadius.circular(8),
                child: Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                  decoration: BoxDecoration(
                    border: Border.all(color: Colors.greenAccent.withOpacity(0.5)),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Row(
                    children: [
                      const Icon(Icons.translate, size: 14, color: Colors.greenAccent),
                      const SizedBox(width: 6),
                      Text(
                        viewModel.selectedTranslation,
                        style: TextStyle(fontWeight: FontWeight.bold, color: isDark ? Colors.white : Colors.black87, fontSize: 12),
                      ),
                      const Icon(Icons.arrow_drop_down, color: Colors.greenAccent, size: 14),
                    ],
                  ),
                ),
              ),
              // Reader Settings Button
              IconButton(
                icon: const Icon(Icons.text_format, color: Colors.greenAccent, size: 22),
                onPressed: () => _showFontSettingsDialog(context, viewModel),
                tooltip: 'Reader Settings (Font & Theme)',
              ),
              // Read Aloud play/pause button
              ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                  backgroundColor: viewModel.isAudioPlaying ? Colors.green : Colors.transparent,
                  shadowColor: Colors.transparent,
                  side: const BorderSide(color: Colors.greenAccent, width: 1.0),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                ),
                onPressed: () {
                  if (viewModel.isAudioActive) {
                    if (viewModel.isAudioPlaying) {
                      viewModel.pauseAudio();
                    } else {
                      viewModel.playAudio();
                    }
                  } else {
                    final chapterText = viewModel.currentVerses.map((v) => '${v.verseNumber}. ${v.text}').join(' ');
                    viewModel.startAudioReader(
                      '${viewModel.selectedBook} ${viewModel.selectedChapter} (${viewModel.selectedTranslation})',
                      chapterText,
                    );
                  }
                },
                icon: Icon(
                  viewModel.isAudioPlaying ? Icons.pause : Icons.volume_up,
                  color: viewModel.isAudioPlaying ? Colors.white : Colors.greenAccent,
                  size: 16,
                ),
                label: Text(
                  viewModel.isAudioPlaying ? 'Pause' : 'Read Aloud',
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    color: viewModel.isAudioPlaying ? Colors.white : (isDark ? Colors.white : Colors.black87),
                    fontSize: 12,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          const Divider(height: 1),
          const SizedBox(height: 12),

          // 1. Testament & Book Selector
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              IconButton(
                icon: const Icon(Icons.chevron_left, color: Colors.greenAccent, size: 28),
                onPressed: () {
                  final allBooks = viewModel.books;
                  final currentIdx = allBooks.indexOf(viewModel.selectedBook);
                  if (currentIdx > 0) {
                    viewModel.selectBook(allBooks[currentIdx - 1]);
                  }
                },
                tooltip: 'Previous Book',
              ),
              Expanded(
                child: InkWell(
                  onTap: () => _showBookSelectionSheet(context, viewModel),
                  child: Column(
                    children: [
                      Text(
                        _getTestamentOfBook(viewModel.selectedBook).toUpperCase(),
                        style: TextStyle(
                          fontSize: 10,
                          fontWeight: FontWeight.w600,
                          color: Colors.greenAccent.withOpacity(0.8),
                          letterSpacing: 1.2,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        viewModel.selectedBook,
                        style: textStyle.copyWith(color: isDark ? Colors.white : Colors.black87, fontSize: 18),
                        textAlign: TextAlign.center,
                      ),
                    ],
                  ),
                ),
              ),
              IconButton(
                icon: const Icon(Icons.chevron_right, color: Colors.greenAccent, size: 28),
                onPressed: () {
                  final allBooks = viewModel.books;
                  final currentIdx = allBooks.indexOf(viewModel.selectedBook);
                  if (currentIdx < allBooks.length - 1) {
                    viewModel.selectBook(allBooks[currentIdx + 1]);
                  }
                },
                tooltip: 'Next Book',
              ),
            ],
          ),
          const SizedBox(height: 10),

          // 2. Chapter Selector
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              IconButton(
                icon: const Icon(Icons.chevron_left, color: Colors.greenAccent, size: 28),
                onPressed: () {
                  if (viewModel.selectedChapter > 1) {
                    viewModel.selectChapter(viewModel.selectedChapter - 1);
                  } else {
                    // Wrap to last chapter of previous book
                    final allBooks = viewModel.books;
                    final currentIdx = allBooks.indexOf(viewModel.selectedBook);
                    if (currentIdx > 0) {
                      final prevBook = allBooks[currentIdx - 1];
                      viewModel.selectBook(prevBook);
                      final maxChapters = BibleViewModel.bookChapters[prevBook] ?? 20;
                      viewModel.selectChapter(maxChapters);
                    }
                  }
                },
                tooltip: 'Previous Chapter',
              ),
              Expanded(
                child: InkWell(
                  onTap: () => _showChapterSelectionSheet(context, viewModel),
                  child: Text(
                    'Chapter ${viewModel.selectedChapter}',
                    style: textStyle.copyWith(color: isDark ? Colors.white : Colors.black87),
                    textAlign: TextAlign.center,
                  ),
                ),
              ),
              IconButton(
                icon: const Icon(Icons.chevron_right, color: Colors.greenAccent, size: 28),
                onPressed: () {
                  final maxChapters = BibleViewModel.bookChapters[viewModel.selectedBook] ?? 20;
                  if (viewModel.selectedChapter < maxChapters) {
                    viewModel.selectChapter(viewModel.selectedChapter + 1);
                  } else {
                    // Wrap to first chapter of next book
                    final allBooks = viewModel.books;
                    final currentIdx = allBooks.indexOf(viewModel.selectedBook);
                    if (currentIdx < allBooks.length - 1) {
                      viewModel.selectBook(allBooks[currentIdx + 1]);
                      viewModel.selectChapter(1);
                    }
                  }
                },
                tooltip: 'Next Chapter',
              ),
            ],
          ),
          const SizedBox(height: 10),

          // 3. Verse Selector
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              IconButton(
                icon: const Icon(Icons.chevron_left, color: Colors.greenAccent, size: 28),
                onPressed: () {
                  if (viewModel.selectedVerseNumber > 1) {
                    viewModel.selectVerseNumber(viewModel.selectedVerseNumber - 1);
                  }
                },
                tooltip: 'Previous Verse',
              ),
              Expanded(
                child: InkWell(
                  onTap: () => _showVerseSelectionSheet(context, viewModel),
                  child: Text(
                    'Verse ${viewModel.selectedVerseNumber}',
                    style: textStyle.copyWith(color: isDark ? Colors.white : Colors.black87),
                    textAlign: TextAlign.center,
                  ),
                ),
              ),
              IconButton(
                icon: const Icon(Icons.chevron_right, color: Colors.greenAccent, size: 28),
                onPressed: () {
                  final totalVerses = viewModel.currentVerses.length;
                  if (viewModel.selectedVerseNumber < totalVerses) {
                    viewModel.selectVerseNumber(viewModel.selectedVerseNumber + 1);
                  }
                },
                tooltip: 'Next Verse',
              ),
            ],
          ),
        ],
      ),
    );
  }

  // Capsule Button Widget builder
  Widget _buildCapsuleButton({
    required Widget child,
    required VoidCallback onTap,
    bool isActive = false,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(20),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 14.0, vertical: 8.0),
        decoration: BoxDecoration(
          color: isActive ? Colors.green.withOpacity(0.12) : Colors.black87,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(
            color: isActive ? Colors.greenAccent : Colors.green.withOpacity(0.5),
            width: 1.5,
          ),
          boxShadow: isActive
              ? [
                  BoxShadow(
                    color: Colors.greenAccent.withOpacity(0.2),
                    blurRadius: 6,
                    spreadRadius: 1,
                  )
                ]
              : [],
        ),
        child: child,
      ),
    );
  }

  // Scripture Reading Pane List Helper
  Widget _buildScriptureList(BibleViewModel viewModel, ThemeData theme) {
    if (viewModel.msgOfflineError) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              const Icon(
                Icons.wifi_off,
                size: 64,
                color: Colors.amber,
              ),
              const SizedBox(height: 16),
              Text(
                'Translation Offline',
                style: theme.textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: theme.colorScheme.error,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 12),
              Text(
                'The MSG (The Message) translation is not downloaded on this device and requires an active internet connection to load online.\n\nPlease connect to the internet to read MSG, or switch to an offline-ready translation below:',
                style: theme.textTheme.bodyMedium?.copyWith(
                  color: Colors.grey,
                  height: 1.4,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 24),
              Wrap(
                spacing: 12,
                runSpacing: 12,
                children: ['KJV', 'WEB', 'ASV'].map((trans) {
                  return ElevatedButton(
                    onPressed: () {
                      viewModel.selectTranslation(trans);
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: theme.colorScheme.secondaryContainer,
                      foregroundColor: theme.colorScheme.onSecondaryContainer,
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                    ),
                    child: Text(trans),
                  );
                }).toList(),
              ),
            ],
          ),
        ),
      );
    }

    return ListView.builder(
      controller: _scriptureScrollController,
      padding: const EdgeInsets.all(16.0),
      itemCount: viewModel.currentVerses.length,
      itemBuilder: (context, index) {
        final verse = viewModel.currentVerses[index];
        final isSelectedForPopup = viewModel.selectedVerseForHighlight?.verseNumber == verse.verseNumber;
        final isCurrentSelectedVerseNum = viewModel.selectedVerseNumber == verse.verseNumber;

        // Fetch comparative verse dynamically if Compare Mode is enabled
        BibleVerse? compareVerse;
        if (viewModel.compareMode) {
          // If we are offline and trying to compare with MSG, we shouldn't show it
          if (!(viewModel.compareTranslation == 'MSG' && viewModel.msgOfflineError)) {
            final cmpVerses = BibleTextGenerator.generateVerses(verse.bookName, verse.chapter, viewModel.compareTranslation);
            if (index < cmpVerses.length) {
              compareVerse = cmpVerses[index];
            }
          }
        }

        return InkWell(
          onLongPress: () {
            viewModel.selectVerseForHighlight(verse);
            viewModel.selectVerseNumber(verse.verseNumber);
            _showContextStudyPanel(context, viewModel, verse);
          },
          onTap: () {
            viewModel.selectVerseNumber(verse.verseNumber);
            viewModel.selectVerseForHighlight(verse);
            _showContextStudyPanel(context, viewModel, verse);
          },
          child: Container(
            padding: const EdgeInsets.symmetric(vertical: 10.0, horizontal: 8.0),
            margin: const EdgeInsets.symmetric(vertical: 4.0),
            decoration: BoxDecoration(
              color: isCurrentSelectedVerseNum
                  ? Colors.green.withOpacity(0.08)
                  : isSelectedForPopup
                      ? theme.colorScheme.primaryContainer.withOpacity(0.2)
                      : Colors.transparent,
              border: isCurrentSelectedVerseNum
                  ? Border.all(color: Colors.greenAccent.withOpacity(0.25), width: 1.0)
                  : null,
              borderRadius: BorderRadius.circular(8.0),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                RichText(
                  text: TextSpan(
                    children: [
                      TextSpan(
                        text: '${verse.verseNumber}  ',
                        style: TextStyle(
                          fontWeight: FontWeight.bold,
                          fontSize: viewModel.fontSize - 1,
                          color: isCurrentSelectedVerseNum ? Colors.greenAccent : theme.colorScheme.primary,
                        ),
                      ),
                      TextSpan(
                        text: verse.text,
                        style: TextStyle(
                          fontSize: viewModel.fontSize,
                          fontFamily: viewModel.fontFamily == 'Serif' ? 'Georgia' : 'Arial',
                          color: theme.colorScheme.onBackground,
                          height: 1.6,
                        ),
                      ),
                    ],
                  ),
                ),
                // Comparative Verse representation
                if (viewModel.compareMode && compareVerse != null) ...[
                  const SizedBox(height: 6),
                  Container(
                    padding: const EdgeInsets.all(8.0),
                    width: double.infinity,
                    decoration: BoxDecoration(
                      color: Colors.blueGrey.withOpacity(0.12),
                      borderRadius: BorderRadius.circular(6.0),
                      border: Border.all(color: Colors.blue.withOpacity(0.2), width: 0.5),
                    ),
                    child: RichText(
                      text: TextSpan(
                        children: [
                          TextSpan(
                            text: '[${viewModel.compareTranslation}]  ',
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              fontSize: viewModel.fontSize - 3,
                              color: Colors.blueAccent,
                            ),
                          ),
                          TextSpan(
                            text: compareVerse.text,
                            style: TextStyle(
                              fontSize: viewModel.fontSize - 1,
                              fontStyle: FontStyle.italic,
                              fontFamily: viewModel.fontFamily == 'Serif' ? 'Georgia' : 'Arial',
                              color: theme.colorScheme.onBackground.withOpacity(0.85),
                              height: 1.5,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ],
            ),
          ),
        );
      },
    );
  }

  // Seekable Seek Bar & Audio Control Dashboard HUD
  Widget _buildAudioControlPanel(BibleViewModel viewModel) {
    if (!viewModel.isAudioActive) return const SizedBox.shrink();

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
      elevation: 8.0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16.0),
        side: const BorderSide(color: Colors.greenAccent, width: 1.0),
      ),
      color: Colors.black90,
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Row(
              children: [
                const Icon(Icons.volume_up, color: Colors.greenAccent, size: 20),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    viewModel.audioTitle,
                    style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.white, fontSize: 13),
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.close, color: Colors.grey, size: 20),
                  onPressed: () => viewModel.stopAudio(),
                  padding: EdgeInsets.zero,
                  constraints: const BoxConstraints(),
                ),
              ],
            ),
            const SizedBox(height: 4),
            Row(
              children: [
                Text(
                  _formatDuration(viewModel.audioElapsed),
                  style: const TextStyle(color: Colors.grey, fontSize: 10),
                ),
                Expanded(
                  child: SliderTheme(
                    data: SliderTheme.of(context).copyWith(
                      activeTrackColor: Colors.greenAccent,
                      inactiveTrackColor: Colors.grey[800],
                      thumbColor: Colors.greenAccent,
                      trackHeight: 3.0,
                      thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 6.0),
                    ),
                    child: Slider(
                      value: viewModel.audioProgress.clamp(0.0, 1.0),
                      onChanged: (value) => viewModel.seekAudio(value),
                    ),
                  ),
                ),
                Text(
                  _formatDuration(viewModel.audioTotal),
                  style: const TextStyle(color: Colors.grey, fontSize: 10),
                ),
              ],
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                IconButton(
                  icon: const Icon(Icons.replay, color: Colors.white),
                  onPressed: () => viewModel.seekAudio(0.0),
                  tooltip: 'Restart from beginning',
                ),
                const SizedBox(width: 24),
                GestureDetector(
                  onTap: () {
                    if (viewModel.isAudioPlaying) {
                      viewModel.pauseAudio();
                    } else {
                      viewModel.playAudio();
                    }
                  },
                  child: Container(
                    padding: const EdgeInsets.all(10),
                    decoration: const BoxDecoration(
                      color: Colors.greenAccent,
                      shape: BoxShape.circle,
                    ),
                    child: Icon(
                      viewModel.isAudioPlaying ? Icons.pause : Icons.play_arrow,
                      color: Colors.black,
                      size: 28,
                    ),
                  ),
                ),
                const SizedBox(width: 24),
                IconButton(
                  icon: const Icon(Icons.fast_forward, color: Colors.white),
                  onPressed: () {
                    final newVal = (viewModel.audioProgress + 0.1).clamp(0.0, 1.0);
                    viewModel.seekAudio(newVal);
                  },
                  tooltip: 'Forward 10%',
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  String _formatDuration(Duration d) {
    final minutes = d.inMinutes.remainder(60).toString().padLeft(2, '0');
    final seconds = d.inSeconds.remainder(60).toString().padLeft(2, '0');
    return '$minutes:$seconds';
  }

  // Translation Selection Bottom Sheet
  void _showTranslationSelectionSheet(BuildContext context, BibleViewModel viewModel) {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(20))),
      isScrollControlled: true, // Allow it to expand nicely since we have many translations!
      builder: (context) {
        return DraggableScrollableSheet(
          initialChildSize: 0.6,
          minChildSize: 0.4,
          maxChildSize: 0.9,
          expand: false,
          builder: (context, scrollController) {
            return Container(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text(
                        'Select Bible Translation',
                        style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Colors.white),
                      ),
                      IconButton(
                        icon: const Icon(Icons.close, color: Colors.white70),
                        onPressed: () => Navigator.pop(context),
                      ),
                    ],
                  ),
                  const Divider(),
                  Expanded(
                    child: ListView(
                      controller: scrollController,
                      children: viewModel.translationsByLanguage.entries.map((entry) {
                        final language = entry.key;
                        final transList = entry.value;

                        return Theme(
                          data: Theme.of(context).copyWith(dividerColor: Colors.transparent),
                          child: ExpansionTile(
                            initiallyExpanded: language == 'English' || language == viewModel.appLanguage,
                            title: Text(
                              language,
                              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: Colors.white),
                            ),
                            children: transList.map((trans) {
                              final id = trans['id']!;
                              final name = trans['name']!;
                              final isSelected = viewModel.selectedTranslation == id;
                              final isDownloaded = viewModel.downloadedTranslations.contains(id);

                              return ListTile(
                                dense: true,
                                title: Text(
                                  name,
                                  style: TextStyle(
                                    fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                                    color: isSelected ? Colors.greenAccent : Colors.white70,
                                    fontSize: 14,
                                  ),
                                ),
                                subtitle: Text(
                                  isDownloaded ? 'Offline Ready' : 'Available Online (Tap to use or download)',
                                  style: TextStyle(
                                    color: isDownloaded ? Colors.green[300] : Colors.white30,
                                    fontSize: 11,
                                  ),
                                ),
                                leading: isSelected 
                                    ? const Icon(Icons.check_circle, color: Colors.greenAccent) 
                                    : (isDownloaded 
                                        ? const Icon(Icons.offline_pin, color: Colors.green, size: 20) 
                                        : const Icon(Icons.cloud_queue, color: Colors.white24, size: 20)),
                                trailing: isSelected 
                                    ? null 
                                    : (!isDownloaded 
                                        ? IconButton(
                                            icon: const Icon(Icons.download, color: Colors.blueAccent, size: 20),
                                            onPressed: () async {
                                              Navigator.pop(context); // Close sheet
                                              ScaffoldMessenger.of(context).showSnackBar(
                                                SnackBar(
                                                  content: Text('Downloading $id for offline use...'),
                                                  duration: const Duration(seconds: 2),
                                                ),
                                              );
                                              await viewModel.downloadTranslation(id);
                                              if (context.mounted) {
                                                ScaffoldMessenger.of(context).showSnackBar(
                                                  SnackBar(
                                                    content: Text('$id successfully downloaded!'),
                                                    backgroundColor: Colors.green,
                                                  ),
                                                );
                                              }
                                            },
                                          )
                                        : null),
                                onTap: () {
                                  viewModel.selectTranslation(id);
                                  Navigator.pop(context);
                                },
                              );
                            }).toList(),
                          ),
                        );
                      }).toList(),
                    ),
                  ),
                ],
              ),
            );
          },
        );
      },
    );
  }

  // Testament and Book Selector Sheet (testament-book!)
  void _showBookSelectionSheet(BuildContext context, BibleViewModel viewModel) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(20))),
      builder: (context) {
        return DefaultTabController(
          length: 2,
          child: Container(
            height: MediaQuery.of(context).size.height * 0.75,
            padding: const EdgeInsets.only(top: 12),
            child: Column(
              children: [
                Container(
                  width: 45,
                  height: 5,
                  decoration: BoxDecoration(color: Colors.grey[400], borderRadius: BorderRadius.circular(10)),
                ),
                const SizedBox(height: 8),
                const TabBar(
                  tabs: [
                    Tab(text: 'Old Testament'),
                    Tab(text: 'New Testament'),
                  ],
                  labelStyle: TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                ),
                Expanded(
                  child: TabBarView(
                    children: [
                      // Old Testament
                      GridView.builder(
                        padding: const EdgeInsets.all(16),
                        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                          crossAxisCount: 3,
                          childAspectRatio: 2.2,
                          crossAxisSpacing: 10,
                          mainAxisSpacing: 10,
                        ),
                        itemCount: BibleViewModel.oldTestamentBooks.length,
                        itemBuilder: (context, index) {
                          final bName = BibleViewModel.oldTestamentBooks[index];
                          final isSelected = viewModel.selectedBook == bName;
                          return ElevatedButton(
                            style: ElevatedButton.styleFrom(
                              backgroundColor: isSelected ? Colors.green : Colors.blueGrey[900],
                              foregroundColor: Colors.white,
                              padding: EdgeInsets.zero,
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                            ),
                            onPressed: () {
                              viewModel.selectBook(bName);
                              Navigator.pop(context);
                            },
                            child: Text(bName, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold), textAlign: TextAlign.center),
                          );
                        },
                      ),
                      // New Testament
                      GridView.builder(
                        padding: const EdgeInsets.all(16),
                        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                          crossAxisCount: 3,
                          childAspectRatio: 2.2,
                          crossAxisSpacing: 10,
                          mainAxisSpacing: 10,
                        ),
                        itemCount: BibleViewModel.newTestamentBooks.length,
                        itemBuilder: (context, index) {
                          final bName = BibleViewModel.newTestamentBooks[index];
                          final isSelected = viewModel.selectedBook == bName;
                          return ElevatedButton(
                            style: ElevatedButton.styleFrom(
                              backgroundColor: isSelected ? Colors.green : Colors.blueGrey[900],
                              foregroundColor: Colors.white,
                              padding: EdgeInsets.zero,
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                            ),
                            onPressed: () {
                              viewModel.selectBook(bName);
                              Navigator.pop(context);
                            },
                            child: Text(bName, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold), textAlign: TextAlign.center),
                          );
                        },
                      ),
                    ],
                  ),
                )
              ],
            ),
          ),
        );
      },
    );
  }

  // Chapter Selection bottom grid sheet
  void _showChapterSelectionSheet(BuildContext context, BibleViewModel viewModel) {
    final totalChapters = BibleViewModel.bookChapters[viewModel.selectedBook] ?? 20;
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(20))),
      builder: (context) {
        return Container(
          height: 350,
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Select Chapter (${viewModel.selectedBook})', style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
              const Divider(),
              const SizedBox(height: 8),
              Expanded(
                child: GridView.builder(
                  gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: 5,
                    crossAxisSpacing: 10,
                    mainAxisSpacing: 10,
                  ),
                  itemCount: totalChapters,
                  itemBuilder: (context, index) {
                    final chNum = index + 1;
                    final isSelected = viewModel.selectedChapter == chNum;
                    return ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: isSelected ? Colors.green : Colors.blueGrey[900],
                        foregroundColor: Colors.white,
                        padding: EdgeInsets.zero,
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                      ),
                      onPressed: () {
                        viewModel.selectChapter(chNum);
                        Navigator.pop(context);
                      },
                      child: Text('$chNum', style: const TextStyle(fontWeight: FontWeight.bold)),
                    );
                  },
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  // Verse Selection bottom grid sheet
  void _showVerseSelectionSheet(BuildContext context, BibleViewModel viewModel) {
    final totalVerses = viewModel.currentVerses.length;
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(20))),
      builder: (context) {
        return Container(
          height: 350,
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Select Verse (${viewModel.selectedBook} ${viewModel.selectedChapter})', style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
              const Divider(),
              const SizedBox(height: 8),
              Expanded(
                child: GridView.builder(
                  gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: 5,
                    crossAxisSpacing: 10,
                    mainAxisSpacing: 10,
                  ),
                  itemCount: totalVerses > 0 ? totalVerses : 12,
                  itemBuilder: (context, index) {
                    final verseNum = index + 1;
                    final isSelected = viewModel.selectedVerseNumber == verseNum;
                    return ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: isSelected ? Colors.green : Colors.blueGrey[900],
                        foregroundColor: Colors.white,
                        padding: EdgeInsets.zero,
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                      ),
                      onPressed: () {
                        viewModel.selectVerseNumber(verseNum);
                        Navigator.pop(context);
                      },
                      child: Text('$verseNum', style: const TextStyle(fontWeight: FontWeight.bold)),
                    );
                  },
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  // Voice Settings Bottom Sheet
  void _showVoiceSettingsSheet(BuildContext context, BibleViewModel viewModel) {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(20))),
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setModalState) {
            return Container(
              padding: const EdgeInsets.all(20),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text(
                        'Voice Optimization Settings',
                        style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18.0),
                      ),
                      IconButton(
                        icon: const Icon(Icons.close),
                        onPressed: () => Navigator.pop(context),
                      )
                    ],
                  ),
                  const Divider(),
                  const SizedBox(height: 12),
                  const Text('Change Voice Style / Accent', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                  const SizedBox(height: 8),
                  DropdownButtonFormField<String>(
                    value: viewModel.ttsLanguage,
                    dropdownColor: Colors.blueGrey[900],
                    decoration: InputDecoration(
                      filled: true,
                      fillColor: Colors.blueGrey[900],
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
                      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    ),
                    onChanged: (val) {
                      if (val != null) {
                        viewModel.setTtsLanguage(val);
                        setModalState(() {});
                      }
                    },
                    items: const [
                      DropdownMenuItem(value: 'en-US', child: Text('English (US) - Standard Voice A', style: TextStyle(color: Colors.white))),
                      DropdownMenuItem(value: 'en-GB', child: Text('English (UK) - Standard Voice B', style: TextStyle(color: Colors.white))),
                      DropdownMenuItem(value: 'en-AU', child: Text('English (AU) - Warm Accent', style: TextStyle(color: Colors.white))),
                      DropdownMenuItem(value: 'en-IN', child: Text('English (IN) - Clear Voice', style: TextStyle(color: Colors.white))),
                    ],
                  ),
                  const SizedBox(height: 20),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text('Voice Speed (Speech Rate)', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                      Text('${(viewModel.ttsRate * 2.0).toStringAsFixed(2)}x', style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.blueAccent)),
                    ],
                  ),
                  Slider(
                    value: viewModel.ttsRate,
                    min: 0.2,
                    max: 1.0,
                    divisions: 8,
                    onChanged: (val) {
                      viewModel.setTtsRate(val);
                      setModalState(() {});
                    },
                  ),
                  const SizedBox(height: 12),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text('Voice Tone (Pitch)', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                      Text(
                        viewModel.ttsPitch == 1.0
                            ? 'Normal'
                            : viewModel.ttsPitch < 1.0
                                ? 'Deeper'
                                : 'Higher',
                        style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.blueAccent),
                      ),
                    ],
                  ),
                  Slider(
                    value: viewModel.ttsPitch,
                    min: 0.5,
                    max: 1.5,
                    divisions: 10,
                    onChanged: (val) {
                      viewModel.setTtsPitch(val);
                      setModalState(() {});
                    },
                  ),
                  const SizedBox(height: 12),
                ],
              ),
            );
          },
        );
      },
    );
  }

  // Study Panel Content Tabs Helper
  Widget _buildStudyPanelTabs(BibleViewModel viewModel, {required bool isWide}) {
    final theme = Theme.of(context);
    final verse = viewModel.selectedVerseForHighlight ?? (viewModel.currentVerses.isNotEmpty ? viewModel.currentVerses.first : null);

    if (verse == null) {
      return const Center(child: Text('Select a verse to load contextual study resources.'));
    }

    return Column(
      children: [
        TabBar(
          controller: _tabController,
          isScrollable: !isWide,
          tabAlignment: !isWide ? TabAlignment.start : TabAlignment.fill,
          labelStyle: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13.0),
          tabs: const [
            Tab(text: 'Timeline'),
            Tab(text: 'Cross Refs'),
            Tab(text: 'Overview'),
            Tab(text: 'People'),
          ],
        ),
        Expanded(
          child: TabBarView(
            controller: _tabController,
            children: [
              _buildTimelineTab(viewModel, verse),
              _buildCrossRefsTab(viewModel, verse),
              _buildOverviewTab(viewModel, verse),
              _buildPeopleTab(viewModel, verse),
            ],
          ),
        ),
      ],
    );
  }

  // Tab 1: Timeline Context
  Widget _buildTimelineTab(BibleViewModel viewModel, BibleVerse verse) {
    final events = viewModel.timelineEvents.where((e) {
      final book = verse.bookName.toLowerCase();
      if (book == 'genesis') return e.title.contains('Creation') || e.title.contains('Abraham');
      if (book == 'john') return e.title.contains('Birth') || e.title.contains('Sermon') || e.title.contains('Crucifixion');
      return true;
    }).toList();

    return ListView.builder(
      padding: const EdgeInsets.all(12.0),
      itemCount: events.length,
      itemBuilder: (context, index) {
        final ev = events[index];
        return Card(
          elevation: 0.5,
          margin: const EdgeInsets.symmetric(vertical: 6.0),
          child: ListTile(
            leading: const CircleAvatar(child: Icon(Icons.explore)),
            title: Text(ev.title, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13.0)),
            subtitle: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const SizedBox(height: 4),
                Text(ev.description, style: const TextStyle(fontSize: 12.0)),
                const SizedBox(height: 4),
                Text(ev.period, style: TextStyle(color: Theme.of(context).colorScheme.primary, fontSize: 11.0, fontWeight: FontWeight.bold)),
              ],
            ),
          ),
        );
      },
    );
  }

  // Tab 2: Cross References
  Widget _buildCrossRefsTab(BibleViewModel viewModel, BibleVerse verse) {
    return ListView(
      padding: const EdgeInsets.all(12.0),
      children: [
        const Text('Interactive Cross References', style: TextStyle(fontWeight: FontWeight.bold)),
        const SizedBox(height: 8.0),
        ListTile(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8.0)),
          tileColor: Theme.of(context).colorScheme.secondaryContainer.withOpacity(0.3),
          title: const Text('John 1:1-3', style: TextStyle(fontWeight: FontWeight.bold)),
          subtitle: const Text('"In the beginning was the Word, and the Word was with God..."'),
          trailing: const Icon(Icons.arrow_forward),
          onTap: () {
            viewModel.selectBook('John');
            viewModel.selectChapter(1);
          },
        ),
      ],
    );
  }

  // Tab 3: Book Overview Metadata
  Widget _buildOverviewTab(BibleViewModel viewModel, BibleVerse verse) {
    final overview = viewModel.bookOverviews[verse.bookName];
    if (overview == null) {
      return const Center(child: Text('Overview not available for this book.'));
    }

    return SingleChildScrollView(
      padding: const EdgeInsets.all(12.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(
                child: Text(overview.name, style: Theme.of(context).textTheme.titleLarge),
              ),
              ElevatedButton.icon(
                style: ElevatedButton.styleFrom(
                  backgroundColor: viewModel.isAudioPlaying && viewModel.audioTitle == 'Overview: ${overview.name}' ? Colors.green : Colors.transparent,
                  shadowColor: Colors.transparent,
                  side: const BorderSide(color: Colors.greenAccent, width: 1.0),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                ),
                onPressed: () {
                  final text = 'Book Overview of ${overview.name}. Author: ${overview.author}. Date written: ${overview.dateWritten}. Category: ${overview.category}. Central Theme: ${overview.theme}. Summary: ${overview.summary}';
                  if (viewModel.isAudioActive && viewModel.audioTitle == 'Overview: ${overview.name}') {
                    if (viewModel.isAudioPlaying) {
                      viewModel.pauseAudio();
                    } else {
                      viewModel.playAudio();
                    }
                  } else {
                    viewModel.startAudioReader('Overview: ${overview.name}', text);
                  }
                },
                icon: Icon(
                  viewModel.isAudioPlaying && viewModel.audioTitle == 'Overview: ${overview.name}' ? Icons.pause : Icons.volume_up,
                  color: viewModel.isAudioPlaying && viewModel.audioTitle == 'Overview: ${overview.name}' ? Colors.white : Colors.greenAccent,
                  size: 14,
                ),
                label: Text(
                  viewModel.isAudioPlaying && viewModel.audioTitle == 'Overview: ${overview.name}' ? 'Pause' : 'Listen',
                  style: TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.bold,
                    color: viewModel.isAudioPlaying && viewModel.audioTitle == 'Overview: ${overview.name}' ? Colors.white : (Theme.of(context).brightness == Brightness.dark ? Colors.white : Colors.black87),
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 4.0),
          Text('Author: ${overview.author}', style: const TextStyle(fontWeight: FontWeight.bold)),
          Text('Date Written: ${overview.dateWritten}'),
          Text('Category: ${overview.category}'),
          const Divider(),
          const Text('Central Theme', style: TextStyle(fontWeight: FontWeight.bold)),
          Text(overview.theme),
          const SizedBox(height: 12.0),
          const Text('Summary', style: TextStyle(fontWeight: FontWeight.bold)),
          Text(overview.summary),
        ],
      ),
    );
  }

  // Tab 4: People & Places
  Widget _buildPeopleTab(BibleViewModel viewModel, BibleVerse verse) {
    return ListView(
      padding: const EdgeInsets.all(12.0),
      children: const [
        ListTile(
          title: Text('Moses', style: TextStyle(fontWeight: FontWeight.bold)),
          subtitle: Text('Prophet and deliverer who led Israel out of Egypt and received the Law on Mount Sinai.'),
        ),
        ListTile(
          title: Text('Abraham', style: TextStyle(fontWeight: FontWeight.bold)),
          subtitle: Text('The father of faith who entered into a covenant with Yahweh.'),
        ),
      ],
    );
  }

  // Context Modal Bottom Sheet for smaller screen devices
  void _showContextStudyPanel(BuildContext context, BibleViewModel viewModel, BibleVerse verse) {
    if (MediaQuery.of(context).size.width >= 800) return;

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Theme.of(context).colorScheme.surface,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(16.0))),
      builder: (context) {
        return DraggableScrollableSheet(
          initialChildSize: 0.6,
          maxChildSize: 0.9,
          expand: false,
          builder: (context, scrollController) {
            return Column(
              children: [
                Padding(
                  padding: const EdgeInsets.all(12.0),
                  child: Container(width: 40, height: 4, decoration: BoxDecoration(color: Colors.grey, borderRadius: BorderRadius.circular(2))),
                ),
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16.0),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text('${verse.bookName} ${verse.chapter}:${verse.verseNumber}', style: Theme.of(context).textTheme.titleMedium),
                      IconButton(
                        icon: const Icon(Icons.bookmark_border),
                        onPressed: () {
                          viewModel.highlightVerse(verse, '#FF1744');
                          Navigator.pop(context);
                          ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Added to Bookmarks & Favorites!')));
                        },
                      ),
                    ],
                  ),
                ),
                Expanded(child: _buildStudyPanelTabs(viewModel, isWide: false)),
              ],
            );
          },
        );
      },
    );
  }

  // Font and reader settings
  void _showFontSettingsDialog(BuildContext context, BibleViewModel viewModel) {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(20))),
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setModalState) {
            final theme = Theme.of(context);
            final isDark = theme.brightness == Brightness.dark;

            return Container(
              padding: const EdgeInsets.all(20),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text(
                        'Reader Settings',
                        style: TextStyle(fontWeight: FontWeight.bold, fontSize: 18.0),
                      ),
                      IconButton(
                        icon: const Icon(Icons.close),
                        onPressed: () => Navigator.pop(context),
                      )
                    ],
                  ),
                  const Divider(),
                  const SizedBox(height: 12),
                  const Text('Reader Theme', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                  const SizedBox(height: 10),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      // Light Theme Choice
                      Expanded(
                        child: _buildThemeOption(
                          context,
                          label: 'Light',
                          isSelected: viewModel.themeMode == 'light',
                          backgroundColor: const Color(0xFFFAF9F6),
                          textColor: Colors.black87,
                          borderColor: Colors.green,
                          onTap: () {
                            viewModel.setThemeMode('light');
                            setModalState(() {});
                          },
                        ),
                      ),
                      const SizedBox(width: 8),
                      // Dark Theme Choice
                      Expanded(
                        child: _buildThemeOption(
                          context,
                          label: 'Dark',
                          isSelected: viewModel.themeMode == 'dark',
                          backgroundColor: const Color(0xFF1E2124),
                          textColor: Colors.white,
                          borderColor: Colors.greenAccent,
                          onTap: () {
                            viewModel.setThemeMode('dark');
                            setModalState(() {});
                          },
                        ),
                      ),
                      const SizedBox(width: 8),
                      // Sepia Theme Choice
                      Expanded(
                        child: _buildThemeOption(
                          context,
                          label: 'Sepia',
                          isSelected: viewModel.themeMode == 'sepia',
                          backgroundColor: const Color(0xFFF4ECD8),
                          textColor: const Color(0xFF5B4636),
                          borderColor: const Color(0xFF8E6F54),
                          onTap: () {
                            viewModel.setThemeMode('sepia');
                            setModalState(() {});
                          },
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 20),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text(
                        'Reader Font Size',
                        style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14.0),
                      ),
                      Text(
                        '${viewModel.fontSize.round()} sp',
                        style: TextStyle(
                          fontWeight: FontWeight.bold,
                          color: Colors.greenAccent,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Row(
                    children: [
                      const Icon(Icons.format_size, size: 16, color: Colors.grey),
                      Expanded(
                        child: Slider(
                          value: viewModel.fontSize,
                          min: 12.0,
                          max: 32.0,
                          divisions: 20,
                          label: '${viewModel.fontSize.round()}',
                          activeColor: Colors.greenAccent,
                          onChanged: (val) {
                            viewModel.setFontSize(val);
                            setModalState(() {});
                          },
                        ),
                      ),
                      const Icon(Icons.format_size, size: 24, color: Colors.grey),
                    ],
                  ),
                  const SizedBox(height: 12),
                  const Text('Font Style', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14)),
                  const SizedBox(height: 10),
                  Row(
                    children: [
                      Expanded(
                        child: ChoiceChip(
                          label: const Text('Elegant Serif'),
                          selected: viewModel.fontFamily == 'Serif',
                          onSelected: (selected) {
                            if (selected) {
                              viewModel.changeFontFamily('Serif');
                              setModalState(() {});
                            }
                          },
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: ChoiceChip(
                          label: const Text('Modern Sans'),
                          selected: viewModel.fontFamily == 'Sans-Serif',
                          onSelected: (selected) {
                            if (selected) {
                              viewModel.changeFontFamily('Sans-Serif');
                              setModalState(() {});
                            }
                          },
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            );
          },
        );
      },
    );
  }

  Widget _buildThemeOption(
    BuildContext context, {
    required String label,
    required bool isSelected,
    required Color backgroundColor,
    required Color textColor,
    required Color borderColor,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(10),
      child: Container(
        height: 44,
        alignment: Alignment.center,
        decoration: BoxDecoration(
          color: backgroundColor,
          borderRadius: BorderRadius.circular(10),
          border: Border.all(
            color: isSelected ? borderColor : Colors.grey.withOpacity(0.3),
            width: isSelected ? 2.5 : 1.0,
          ),
          boxShadow: isSelected
              ? [
                  BoxShadow(
                    color: borderColor.withOpacity(0.3),
                    blurRadius: 4,
                    offset: const Offset(0, 2),
                  )
                ]
              : [],
        ),
        child: Text(
          label,
          style: TextStyle(
            color: textColor,
            fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
            fontSize: 13,
          ),
        ),
      ),
    );
  }
}

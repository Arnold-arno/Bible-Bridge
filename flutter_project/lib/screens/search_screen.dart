import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../viewmodels/bible_viewmodel.dart';
import '../models/bible_verse.dart';

class SearchScreen extends StatefulWidget {
  const SearchScreen({super.key});

  @override
  State<SearchScreen> createState() => _SearchScreenState();
}

class _SearchScreenState extends State<SearchScreen> {
  final TextEditingController _searchController = TextEditingController();
  final FocusNode _focusNode = FocusNode();
  bool _isFocused = false;
  String _submittedQuery = '';

  @override
  void initState() {
    super.initState();
    _focusNode.addListener(() {
      setState(() {
        _isFocused = _focusNode.hasFocus;
      });
    });
    // Auto-focus search bar on screen open
    WidgetsBinding.instance.addPostFrameCallback((_) {
      FocusScope.of(context).requestFocus(_focusNode);
    });
  }

  @override
  void dispose() {
    _searchController.dispose();
    _focusNode.dispose();
    super.dispose();
  }

  void _onSearchSubmitted(String query, BibleViewModel viewModel) {
    final trimmed = query.trim();
    if (trimmed.isNotEmpty) {
      setState(() {
        _submittedQuery = trimmed;
      });
      viewModel.performSearch(trimmed);
      _focusNode.unfocus();
    }
  }

  // Highlight helper to stylize the matched search query in text
  Widget _buildHighlightedText(String text, String query, TextStyle baseStyle, Color highlightColor) {
    if (query.isEmpty) return Text(text, style: baseStyle);

    final lowercaseText = text.toLowerCase();
    final lowercaseQuery = query.toLowerCase();
    final List<TextSpan> spans = [];

    int start = 0;
    int indexOfQuery = lowercaseText.indexOf(lowercaseQuery, start);

    while (indexOfQuery != -1) {
      // Add text before match
      if (indexOfQuery > start) {
        spans.add(TextSpan(text: text.substring(start, indexOfQuery)));
      }

      // Add highlighted match
      final matchText = text.substring(indexOfQuery, indexOfQuery + query.length);
      spans.add(TextSpan(
        text: matchText,
        style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.greenAccent),
      ));

      start = indexOfQuery + query.length;
      indexOfQuery = lowercaseText.indexOf(lowercaseQuery, start);
    }

    // Add remaining text
    if (start < text.length) {
      spans.add(TextSpan(text: text.substring(start)));
    }

    return RichText(
      text: TextSpan(
        style: baseStyle,
        children: spans,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final viewModel = Provider.of<BibleViewModel>(context);
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    // Show recent searches when search bar is focused OR query input is empty
    final showRecentSearches = _isFocused || _searchController.text.trim().isEmpty;

    return Scaffold(
      appBar: AppBar(
        titleSpacing: 0,
        title: Padding(
          padding: const EdgeInsets.only(right: 16.0),
          child: Container(
            height: 44,
            decoration: BoxDecoration(
              color: isDark ? const Color(0xFF1E2124) : Colors.green.withOpacity(0.06),
              borderRadius: BorderRadius.circular(24),
              border: Border.all(
                color: _isFocused ? Colors.greenAccent : Colors.grey.withOpacity(0.3),
                width: 1.5,
              ),
            ),
            child: TextField(
              controller: _searchController,
              focusNode: _focusNode,
              textInputAction: TextInputAction.search,
              onSubmitted: (value) => _onSearchSubmitted(value, viewModel),
              decoration: InputDecoration(
                hintText: 'Search verses or books...',
                prefixIcon: const Icon(Icons.search, size: 20, color: Colors.grey),
                suffixIcon: _searchController.text.isNotEmpty
                    ? IconButton(
                        icon: const Icon(Icons.clear, size: 18, color: Colors.grey),
                        onPressed: () {
                          _searchController.clear();
                          setState(() {
                            _submittedQuery = '';
                          });
                        },
                      )
                    : null,
                border: InputBorder.none,
                contentPadding: const EdgeInsets.symmetric(vertical: 10),
              ),
              style: TextStyle(color: isDark ? Colors.white : Colors.black87),
              onChanged: (text) {
                setState(() {}); // Trigger refresh to show/hide close button
              },
            ),
          ),
        ),
      ),
      body: Column(
        children: [
          if (viewModel.isIndexing)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 10.0),
              color: theme.colorScheme.primaryContainer,
              child: Row(
                children: [
                  const SizedBox(
                    width: 16,
                    height: 16,
                    child: CircularProgressIndicator(strokeWidth: 2, color: Colors.greenAccent),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      'Indexing Bible for Offline Search... ${(viewModel.indexingProgress * 100).toStringAsFixed(0)}%',
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onPrimaryContainer,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ],
              ),
            )
          else
            Container(
              width: double.infinity,
              padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 6.0),
              color: theme.brightness == Brightness.dark
                  ? const Color(0xFF1E2E20)
                  : Colors.green.withOpacity(0.08),
              child: Row(
                children: [
                  const Icon(Icons.offline_bolt, size: 16, color: Colors.greenAccent),
                  const SizedBox(width: 8),
                  Text(
                    'Full Bible Offline Search Index: Active',
                    style: TextStyle(
                      fontSize: 11.0,
                      fontWeight: FontWeight.bold,
                      color: theme.brightness == Brightness.dark
                          ? Colors.greenAccent
                          : Colors.green[800],
                    ),
                  ),
                ],
              ),
            ),
          Expanded(
            child: AnimatedSwitcher(
              duration: const Duration(milliseconds: 200),
              child: showRecentSearches
                  ? _buildRecentSearchesSection(viewModel, theme)
                  : _buildSearchResultsSection(viewModel, theme),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildRecentSearchesSection(BibleViewModel viewModel, ThemeData theme) {
    final queries = viewModel.recentSearches;

    return ListView(
      key: const ValueKey('recent_searches_list'),
      padding: const EdgeInsets.all(16.0),
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'Recent Searches',
              style: theme.textTheme.titleMedium?.copyWith(
                fontWeight: FontWeight.bold,
                color: theme.brightness == Brightness.dark
                    ? Colors.greenAccent
                    : theme.colorScheme.primary,
              ),
            ),
            if (queries.isNotEmpty)
              TextButton.icon(
                onPressed: () {
                  viewModel.clearSearchAndReadingHistory();
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Search history cleared!')),
                  );
                },
                icon: const Icon(Icons.delete_sweep_outlined, size: 16),
                label: const Text('Clear All', style: TextStyle(fontSize: 12)),
              ),
          ],
        ),
        const SizedBox(height: 8.0),
        if (queries.isEmpty)
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 40.0),
            child: Column(
              children: [
                Icon(Icons.search_off_outlined, size: 48, color: Colors.grey.withOpacity(0.5)),
                const SizedBox(height: 12),
                Text(
                  'No recent searches yet',
                  style: TextStyle(color: Colors.grey[500], fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 6),
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 32.0),
                  child: Text(
                    'Search for keywords like "beginning", "shepherd", "love", or "faith" to find scripture verses.',
                    textAlign: TextAlign.center,
                    style: TextStyle(color: Colors.grey[600], fontSize: 12),
                  ),
                ),
              ],
            ),
          )
        else
          ...queries.map((q) => Card(
                elevation: 0,
                color: theme.brightness == Brightness.dark
                    ? const Color(0xFF1E2124).withOpacity(0.6)
                    : Colors.green.withOpacity(0.03),
                margin: const EdgeInsets.symmetric(vertical: 4.0),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10.0)),
                child: ListTile(
                  dense: true,
                  leading: const Icon(Icons.history, color: Colors.grey, size: 18),
                  title: Text(
                    q,
                    style: TextStyle(
                      fontSize: 14.0,
                      fontWeight: FontWeight.w500,
                      color: theme.brightness == Brightness.dark ? Colors.white70 : Colors.black87,
                    ),
                  ),
                  trailing: IconButton(
                    icon: const Icon(Icons.close, size: 16, color: Colors.grey),
                    onPressed: () {
                      viewModel.deleteRecentSearch(q);
                    },
                    tooltip: 'Delete',
                  ),
                  onTap: () {
                    _searchController.text = q;
                    _onSearchSubmitted(q, viewModel);
                  },
                ),
              )),
      ],
    );
  }

  Widget _buildSearchResultsSection(BibleViewModel viewModel, ThemeData theme) {
    if (viewModel.isSearching) {
      return const Center(
        key: ValueKey('searching_loader'),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            CircularProgressIndicator(color: Colors.greenAccent),
            SizedBox(height: 16),
            Text('Searching Holy Scripture...', style: TextStyle(fontStyle: FontStyle.Italic, color: Colors.grey)),
          ],
        ),
      );
    }

    final results = viewModel.searchResults;

    if (results.isEmpty) {
      return Center(
        key: const ValueKey('no_results'),
        child: Padding(
          padding: const EdgeInsets.all(32.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(Icons.find_in_page_outlined, size: 56, color: Colors.grey.withOpacity(0.5)),
              const SizedBox(height: 16),
              Text(
                'No matching verses found',
                style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 8),
              Text(
                'We couldn\'t find any verses matching "$_submittedQuery" in the cached translations. Try another term or word.',
                textAlign: TextAlign.center,
                style: const TextStyle(color: Colors.grey, fontSize: 13),
              ),
            ],
          ),
        ),
      );
    }

    return Column(
      key: const ValueKey('search_results_content'),
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 12.0),
          child: Text(
            'Found ${results.length} verses matching "$_submittedQuery"',
            style: TextStyle(
              fontWeight: FontWeight.bold,
              fontSize: 13,
              color: theme.brightness == Brightness.dark ? Colors.grey[400] : Colors.grey[700],
            ),
          ),
        ),
        Expanded(
          child: ListView.builder(
            padding: const EdgeInsets.symmetric(horizontal: 16.0),
            itemCount: results.length,
            itemBuilder: (context, index) {
              final verse = results[index];
              return Card(
                elevation: 0.5,
                margin: const EdgeInsets.symmetric(vertical: 6.0),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                child: InkWell(
                  borderRadius: BorderRadius.circular(12),
                  onTap: () {
                    // Navigate to the reading pane containing this verse
                    viewModel.selectBook(verse.bookName);
                    viewModel.selectChapter(verse.chapter);
                    viewModel.selectVerseNumber(verse.verseNumber);
                    viewModel.selectTab(ActiveTab.read);
                    Navigator.pop(context); // Pop search screen
                  },
                  child: Padding(
                    padding: const EdgeInsets.all(14.0),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(
                              '${verse.bookName} ${verse.chapter}:${verse.verseNumber}',
                              style: TextStyle(
                                fontWeight: FontWeight.bold,
                                fontSize: 14,
                                color: theme.colorScheme.primary,
                              ),
                            ),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                              decoration: BoxDecoration(
                                color: theme.colorScheme.primary.withOpacity(0.1),
                                borderRadius: BorderRadius.circular(6),
                              ),
                              child: Text(
                                verse.translation,
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  fontSize: 10,
                                  color: theme.colorScheme.primary,
                                ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 8.0),
                        _buildHighlightedText(
                          verse.text,
                          _submittedQuery,
                          TextStyle(
                            fontSize: 14.5,
                            height: 1.4,
                            color: theme.brightness == Brightness.dark ? Colors.white80 : Colors.black87,
                            fontFamily: viewModel.fontFamily == 'Serif' ? 'Georgia' : 'Arial',
                          ),
                          Colors.greenAccent,
                        ),
                      ],
                    ),
                  ),
                ),
              );
            },
          ),
        ),
      ],
    );
  }
}

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../viewmodels/bible_viewmodel.dart';
import 'home_screen.dart';
import 'read_screen.dart';
import 'timeline_screen.dart';
import 'devotions_screen.dart';
import 'settings_screen.dart';

class MainNavigationScreen extends StatelessWidget {
  const MainNavigationScreen({super.key});

  String _formatDuration(Duration duration) {
    String twoDigits(int n) => n.toString().padLeft(2, '0');
    final minutes = twoDigits(duration.inMinutes.remainder(60));
    final seconds = twoDigits(duration.inSeconds.remainder(60));
    return '$minutes:$seconds';
  }

  @override
  Widget build(BuildContext context) {
    final viewModel = Provider.of<BibleViewModel>(context);
    final width = MediaQuery.of(context).size.width;
    final isWide = width >= 600;

    final List<Widget> screens = [
      const HomeScreen(),
      const ReadScreen(),
      const DevotionsScreen(),
      const TimelineScreen(),
      const SettingsScreen(),
    ];

    return Scaffold(
      body: Column(
        children: [
          Expanded(
            child: Row(
              children: [
                if (isWide) ...[
                  NavigationRail(
                    selectedIndex: viewModel.activeTab.index,
                    onDestinationSelected: (index) {
                      viewModel.selectTab(ActiveTab.values[index]);
                    },
                    labelType: NavigationRailLabelType.all,
                    leading: Padding(
                      padding: const EdgeInsets.symmetric(vertical: 24.0),
                      child: Icon(
                        Icons.auto_stories,
                        color: Theme.of(context).colorScheme.primary,
                        size: 32.0,
                      ),
                    ),
                    destinations: const [
                      NavigationRailDestination(
                        icon: Icon(Icons.dashboard_outlined),
                        selectedIcon: Icon(Icons.dashboard),
                        label: Text('Home'),
                      ),
                      NavigationRailDestination(
                        icon: Icon(Icons.menu_book_outlined),
                        selectedIcon: Icon(Icons.menu_book),
                        label: Text('Read'),
                      ),
                      NavigationRailDestination(
                        icon: Icon(Icons.favorite_border),
                        selectedIcon: Icon(Icons.favorite),
                        label: Text('Devotions'),
                      ),
                      NavigationRailDestination(
                        icon: Icon(Icons.timeline_outlined),
                        selectedIcon: Icon(Icons.timeline),
                        label: Text('Timeline'),
                      ),
                      NavigationRailDestination(
                        icon: Icon(Icons.settings_outlined),
                        selectedIcon: Icon(Icons.settings),
                        label: Text('Settings'),
                      ),
                    ],
                  ),
                  const VerticalDivider(thickness: 1, width: 1),
                ],
                Expanded(
                  child: screens[viewModel.activeTab.index],
                ),
              ],
            ),
          ),
          if (viewModel.isAudioActive) ...[
            _buildAudioPlayerBar(context, viewModel),
          ],
        ],
      ),
      bottomNavigationBar: isWide
          ? null
          : NavigationBar(
              selectedIndex: viewModel.activeTab.index,
              onDestinationSelected: (index) {
                viewModel.selectTab(ActiveTab.values[index]);
              },
              destinations: const [
                NavigationDestination(
                  icon: Icon(Icons.dashboard_outlined),
                  selectedIcon: Icon(Icons.dashboard),
                  label: 'Home',
                ),
                NavigationDestination(
                  icon: Icon(Icons.menu_book_outlined),
                  selectedIcon: Icon(Icons.menu_book),
                  label: 'Read',
                ),
                NavigationDestination(
                  icon: Icon(Icons.favorite_border),
                  selectedIcon: Icon(Icons.favorite),
                  label: 'Devotions',
                ),
                NavigationDestination(
                  icon: Icon(Icons.timeline_outlined),
                  selectedIcon: Icon(Icons.timeline),
                  label: 'Timeline',
                ),
                NavigationDestination(
                  icon: Icon(Icons.settings_outlined),
                  selectedIcon: Icon(Icons.settings),
                  label: 'Settings',
                ),
              ],
            ),
    );
  }

  Widget _buildAudioPlayerBar(BuildContext context, BibleViewModel viewModel) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
      decoration: BoxDecoration(
        color: isDark ? const Color(0xFF1E2124) : const Color(0xFFF0FDF4),
        border: Border(
          top: BorderSide(color: Colors.greenAccent.withOpacity(0.3), width: 1.5),
        ),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.1),
            blurRadius: 10.0,
            offset: const Offset(0, -4),
          )
        ],
      ),
      child: SafeArea(
        top: false,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Row(
              children: [
                const Icon(
                  Icons.volume_up,
                  color: Colors.greenAccent,
                  size: 24.0,
                ),
                const SizedBox(width: 12.0),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        viewModel.audioTitle,
                        style: theme.textTheme.bodyMedium?.copyWith(
                          fontWeight: FontWeight.bold,
                          color: isDark ? Colors.white : Colors.black87,
                        ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                      Text(
                        viewModel.audioText,
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: isDark ? Colors.grey[400] : Colors.grey[700],
                        ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: 8.0),
                // Replay/Restart Button
                IconButton(
                  icon: const Icon(Icons.replay, size: 22.0),
                  color: isDark ? Colors.white70 : Colors.black54,
                  tooltip: 'Restart',
                  onPressed: () {
                    viewModel.seekAudio(0.0);
                  },
                ),
                // Play/Pause button
                IconButton(
                  icon: Icon(
                    viewModel.isAudioPlaying ? Icons.pause_circle_filled : Icons.play_circle_filled,
                    size: 36.0,
                    color: Colors.greenAccent,
                  ),
                  onPressed: () {
                    if (viewModel.isAudioPlaying) {
                      viewModel.pauseAudio();
                    } else {
                      viewModel.playAudio();
                    }
                  },
                ),
                // Forward Button
                IconButton(
                  icon: const Icon(Icons.fast_forward, size: 22.0),
                  color: isDark ? Colors.white70 : Colors.black54,
                  tooltip: 'Forward 10%',
                  onPressed: () {
                    final newVal = (viewModel.audioProgress + 0.1).clamp(0.0, 1.0);
                    viewModel.seekAudio(newVal);
                  },
                ),
                // Close button
                IconButton(
                  icon: const Icon(Icons.close, size: 22.0),
                  color: isDark ? Colors.white54 : Colors.black54,
                  onPressed: () {
                    viewModel.stopAudio();
                  },
                ),
              ],
            ),
            Row(
              children: [
                Text(
                  _formatDuration(viewModel.audioElapsed),
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: isDark ? Colors.white70 : Colors.black87,
                  ),
                ),
                Expanded(
                  child: SliderTheme(
                    data: SliderTheme.of(context).copyWith(
                      activeTrackColor: Colors.greenAccent,
                      inactiveTrackColor: isDark ? Colors.grey[800] : Colors.grey[300],
                      thumbColor: Colors.greenAccent,
                      trackHeight: 3.0,
                      thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 6.0),
                    ),
                    child: Slider(
                      value: viewModel.audioProgress.clamp(0.0, 1.0),
                      onChanged: (val) {
                        viewModel.seekAudio(val);
                      },
                    ),
                  ),
                ),
                Text(
                  _formatDuration(viewModel.audioTotal),
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: isDark ? Colors.white70 : Colors.black87,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

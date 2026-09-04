import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../viewmodels/bible_viewmodel.dart';

class TimelineScreen extends StatelessWidget {
  const TimelineScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final viewModel = Provider.of<BibleViewModel>(context);
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Chronological Timeline'),
        actions: [
          IconButton(
            icon: Icon(
              viewModel.isAudioPlaying && viewModel.audioTitle == 'Entire Timeline' ? Icons.pause : Icons.volume_up,
              color: viewModel.isAudioPlaying && viewModel.audioTitle == 'Entire Timeline' ? Colors.greenAccent : null,
            ),
            tooltip: 'Read Entire Timeline',
            onPressed: () {
              if (viewModel.isAudioActive && viewModel.audioTitle == 'Entire Timeline') {
                if (viewModel.isAudioPlaying) {
                  viewModel.pauseAudio();
                } else {
                  viewModel.playAudio();
                }
              } else {
                final allEventsText = viewModel.timelineEvents.map((e) => 'Event: ${e.title}. Period: ${e.period}. Description: ${e.description}.').join(' ');
                viewModel.startAudioReader(
                  'Entire Timeline',
                  allEventsText,
                );
              }
            },
          ),
        ],
      ),
      body: ListView.builder(
        padding: const EdgeInsets.all(16.0),
        itemCount: viewModel.timelineEvents.length,
        itemBuilder: (context, index) {
          final event = viewModel.timelineEvents[index];
          final isEven = index % 2 == 0;

          return IntrinsicHeight(
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // Chronological Timeline Left Indicator
                Column(
                  children: [
                    Container(
                      width: 12.0,
                      height: 12.0,
                      decoration: BoxDecoration(
                        color: theme.colorScheme.primary,
                        shape: BoxShape.circle,
                      ),
                    ),
                    Expanded(
                      child: Container(
                        width: 2.0,
                        color: theme.colorScheme.primary.withOpacity(0.4),
                      ),
                    ),
                  ],
                ),
                const SizedBox(width: 16.0),

                // Timeline Event Content Card
                Expanded(
                  child: Card(
                    elevation: 1,
                    margin: const EdgeInsets.only(bottom: 20.0),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12.0)),
                    child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Expanded(
                                child: Text(
                                  event.title,
                                  style: theme.textTheme.titleMedium?.copyWith(
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                              ),
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 4.0),
                                decoration: BoxDecoration(
                                  color: theme.colorScheme.primaryContainer,
                                  borderRadius: BorderRadius.circular(20.0),
                                ),
                                child: Text(
                                  event.period,
                                  style: TextStyle(
                                    fontSize: 11.0,
                                    fontWeight: FontWeight.bold,
                                    color: theme.colorScheme.onPrimaryContainer,
                                  ),
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 8.0),
                          Text(
                            event.description,
                            style: theme.textTheme.bodyMedium?.copyWith(
                              color: theme.colorScheme.onSurfaceVariant,
                            ),
                          ),
                          const SizedBox(height: 12.0),
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Chip(
                                label: Text(
                                  event.scriptureRef,
                                  style: const TextStyle(fontSize: 11.0, fontWeight: FontWeight.bold),
                                ),
                                backgroundColor: theme.colorScheme.surfaceVariant,
                              ),
                              Row(
                                children: [
                                  IconButton(
                                    icon: Icon(
                                      viewModel.isAudioPlaying && viewModel.audioTitle == event.title ? Icons.pause : Icons.volume_up,
                                      color: viewModel.isAudioPlaying && viewModel.audioTitle == event.title ? Colors.greenAccent : null,
                                      size: 20.0,
                                    ),
                                    tooltip: 'Listen to Event',
                                    onPressed: () {
                                      if (viewModel.isAudioActive && viewModel.audioTitle == event.title) {
                                        if (viewModel.isAudioPlaying) {
                                          viewModel.pauseAudio();
                                        } else {
                                          viewModel.playAudio();
                                        }
                                      } else {
                                        viewModel.startAudioReader(
                                          event.title,
                                          'Timeline event: ${event.title}. Period: ${event.period}. Description: ${event.description}. Scripture reference: ${event.scriptureRef}',
                                        );
                                      }
                                    },
                                  ),
                                  TextButton.icon(
                                    onPressed: () {
                                      // Parse book reference
                                      final ref = event.scriptureRef;
                                      final spaceIdx = ref.lastIndexOf(' ');
                                      if (spaceIdx != -1) {
                                        final book = ref.substring(0, spaceIdx).trim();
                                        viewModel.selectBook(book);
                                        viewModel.selectChapter(1);
                                        viewModel.selectTab(ActiveTab.read);
                                      }
                                    },
                                    icon: const Icon(Icons.chrome_reader_mode_outlined, size: 16.0),
                                    label: const Text('Go to Reading'),
                                  ),
                                ],
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}

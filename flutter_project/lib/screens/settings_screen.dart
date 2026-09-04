import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../viewmodels/bible_viewmodel.dart';
import '../services/sync_manager.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final viewModel = Provider.of<BibleViewModel>(context);
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Settings'),
      ),
      body: ListView(
        padding: const EdgeInsets.all(16.0),
        children: [
          // 0. Display & Theme Customization Section
          _buildSectionHeader(context, 'Display & Typography Settings'),
          Card(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'Reader Theme',
                    style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13.0),
                  ),
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
                          onTap: () => viewModel.setThemeMode('light'),
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
                          onTap: () => viewModel.setThemeMode('dark'),
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
                          onTap: () => viewModel.setThemeMode('sepia'),
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
                        style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13.0),
                      ),
                      Text(
                        '${viewModel.fontSize.round()} sp',
                        style: TextStyle(
                          fontWeight: FontWeight.bold,
                          color: Theme.of(context).colorScheme.primary,
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
                          },
                        ),
                      ),
                      const Icon(Icons.format_size, size: 24, color: Colors.grey),
                    ],
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24.0),

          // App Language & Translation Manager Section
          _buildSectionHeader(context, 'App Language & Translation Manager'),
          Card(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    'App Interface Language',
                    style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13.0),
                  ),
                  const SizedBox(height: 8),
                  DropdownButtonFormField<String>(
                    value: viewModel.appLanguage,
                    dropdownColor: theme.brightness == Brightness.dark ? const Color(0xFF1E2124) : Colors.white,
                    decoration: InputDecoration(
                      filled: true,
                      fillColor: theme.brightness == Brightness.dark ? Colors.blueGrey[900] : Colors.grey[100],
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
                      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    ),
                    onChanged: (val) {
                      if (val != null) {
                        viewModel.setAppLanguage(val);
                        // Suggest translation
                        if (viewModel.suggestedTranslationId != null) {
                          _showTranslationSuggestionDialog(context, viewModel);
                        }
                      }
                    },
                    items: const [
                      DropdownMenuItem(value: 'English', child: Text('English')),
                      DropdownMenuItem(value: 'Spanish', child: Text('Español (Spanish)')),
                      DropdownMenuItem(value: 'Portuguese', child: Text('Português (Portuguese)')),
                      DropdownMenuItem(value: 'French', child: Text('Français (French)')),
                      DropdownMenuItem(value: 'German', child: Text('Deutsch (German)')),
                      DropdownMenuItem(value: 'Italian', child: Text('Italiano (Italian)')),
                      DropdownMenuItem(value: 'Dutch', child: Text('Nederlands (Dutch)')),
                      DropdownMenuItem(value: 'Swahili', child: Text('Kiswahili (Swahili)')),
                      DropdownMenuItem(value: 'Luganda', child: Text('Oluganda (Luganda)')),
                      DropdownMenuItem(value: 'Chinese', child: Text('中文 (Chinese)')),
                      DropdownMenuItem(value: 'Arabic', child: Text('العربية (Arabic)')),
                    ],
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'The app interface language runs independently from Bible translations, allowing you to study scripture in any language while keeping menus familiar.',
                    style: TextStyle(color: Colors.white38, fontSize: 11.0),
                  ),
                  const SizedBox(height: 16),
                  const Divider(),
                  const SizedBox(height: 8),
                  const Row(
                    children: [
                      Icon(Icons.download_for_offline, size: 20, color: Colors.greenAccent),
                      SizedBox(width: 8),
                      Text(
                        'Offline Translation Manager',
                        style: TextStyle(fontWeight: FontWeight.bold, fontSize: 14.0),
                      ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  const Text(
                    'Download translations for offline study. Undownloaded versions will stream dynamically online.',
                    style: TextStyle(color: Colors.white38, fontSize: 11.0),
                  ),
                  const SizedBox(height: 12),
                  // If a download is currently active
                  if (viewModel.isDownloadingTranslation) ...[
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: Colors.blue.withOpacity(0.1),
                        borderRadius: BorderRadius.circular(8),
                        border: Border.all(color: Colors.blue.withOpacity(0.3)),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text(
                                'Downloading ${viewModel.downloadingTranslationId}...',
                                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 12, color: Colors.blueAccent),
                              ),
                              Text(
                                '${(viewModel.translationDownloadProgress * 100).toStringAsFixed(0)}%',
                                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 12, color: Colors.blueAccent),
                              ),
                            ],
                          ),
                          const SizedBox(height: 8),
                          LinearProgressIndicator(
                            value: viewModel.translationDownloadProgress,
                            color: Colors.blueAccent,
                            backgroundColor: Colors.blue.withOpacity(0.2),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 12),
                  ],
                  // List of translations grouped by language
                  ListView(
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    children: viewModel.translationsByLanguage.entries.map((entry) {
                      final language = entry.key;
                      final transList = entry.value;

                      return Theme(
                        data: Theme.of(context).copyWith(dividerColor: Colors.transparent),
                        child: ExpansionTile(
                          title: Text(
                            language,
                            style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13, color: Colors.white),
                          ),
                          children: transList.map((trans) {
                            final id = trans['id']!;
                            final name = trans['name']!;
                            final isDownloaded = viewModel.downloadedTranslations.contains(id);
                            final isDefault = ['KJV', 'WEB', 'ASV'].contains(id);

                            return ListTile(
                              dense: true,
                              title: Text(name, style: const TextStyle(fontSize: 12, color: Colors.white70)),
                              subtitle: Text(
                                isDefault
                                    ? 'Pre-installed Offline'
                                    : (isDownloaded ? 'Downloaded Offline' : 'Online Stream Only'),
                                style: TextStyle(
                                  color: isDownloaded ? Colors.greenAccent : Colors.white30,
                                  fontSize: 10,
                                ),
                              ),
                              leading: Icon(
                                isDownloaded ? Icons.offline_pin : Icons.cloud_outlined,
                                size: 18,
                                color: isDownloaded ? Colors.greenAccent : Colors.grey,
                              ),
                              trailing: isDefault
                                  ? const Icon(Icons.lock, size: 14, color: Colors.white24)
                                  : (isDownloaded
                                      ? IconButton(
                                          icon: const Icon(Icons.delete, size: 16, color: Colors.redAccent),
                                          onPressed: () => viewModel.removeTranslation(id),
                                          tooltip: 'Remove from offline cache',
                                        )
                                      : IconButton(
                                          icon: const Icon(Icons.download, size: 16, color: Colors.blueAccent),
                                          onPressed: () => viewModel.downloadTranslation(id),
                                          tooltip: 'Download translation',
                                        )),
                            );
                          }).toList(),
                        ),
                      );
                    }).toList(),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24.0),

          // 1. Storage & Offline Data Section
          _buildSectionHeader(context, 'Offline Database & Storage'),
          Card(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: Column(
              children: [
                ListTile(
                  leading: const Icon(Icons.storage_outlined),
                  title: const Text('Bible Translations Cache'),
                  subtitle: const Text('KJV, WEB, ASV fully available offline'),
                  trailing: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(color: theme.colorScheme.primary.withOpacity(0.1), borderRadius: BorderRadius.circular(8)),
                    child: Text('24 MB', style: TextStyle(fontWeight: FontWeight.bold, color: theme.colorScheme.primary, fontSize: 12)),
                  ),
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.cleaning_services_outlined),
                  title: const Text('Clear Search & Reading History'),
                  subtitle: const Text('Removes recent reading paths locally'),
                  onTap: () {
                    viewModel.clearSearchAndReadingHistory();
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Search and reading history cleared successfully!')),
                    );
                  },
                ),
              ],
            ),
          ),
          const SizedBox(height: 24.0),

          // 2. Cloud Sync & Account
          _buildSectionHeader(context, 'Cloud Synchronization'),
          AnimatedBuilder(
            animation: SyncManager.instance,
            builder: (context, _) {
              final syncMgr = SyncManager.instance;
              
              Color statusColor;
              IconData statusIcon;
              switch (syncMgr.status) {
                case SyncStatus.syncing:
                  statusColor = Colors.orange;
                  statusIcon = Icons.hourglass_empty;
                  break;
                case SyncStatus.success:
                  statusColor = Colors.green;
                  statusIcon = Icons.check_circle_outline;
                  break;
                case SyncStatus.error:
                  statusColor = Colors.red;
                  statusIcon = Icons.error_outline;
                  break;
                case SyncStatus.idle:
                default:
                  statusColor = Colors.grey;
                  statusIcon = Icons.cloud_queue_outlined;
                  break;
              }

              return Card(
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                child: Padding(
                  padding: const EdgeInsets.symmetric(vertical: 8.0),
                  child: Column(
                    children: [
                      ListTile(
                        leading: Icon(statusIcon, color: statusColor, size: 28),
                        title: const Text('PostgreSQL Sync Status', style: TextStyle(fontWeight: FontWeight.bold)),
                        subtitle: Text(
                          syncMgr.message,
                          style: TextStyle(
                            fontSize: 13,
                            color: theme.brightness == Brightness.dark ? Colors.white70 : Colors.black87,
                          ),
                        ),
                      ),
                      
                      if (syncMgr.lastSyncTime != null)
                        Padding(
                          padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 4.0),
                          child: Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              const Text('Last Synced', style: TextStyle(fontSize: 12, color: Colors.grey)),
                              Text(
                                '${syncMgr.lastSyncTime!.toLocal().toString().split('.')[0]}',
                                style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: Colors.grey),
                              ),
                            ],
                          ),
                        ),
                      
                      const Divider(),
                      
                      SwitchListTile(
                        secondary: const Icon(Icons.sync),
                        title: const Text('Auto Sync in Background'),
                        subtitle: const Text('Automatically syncs local modifications online'),
                        value: syncMgr.isAutoSyncEnabled,
                        onChanged: (val) {
                          syncMgr.saveSettings(
                            host: syncMgr.host,
                            port: syncMgr.port,
                            databaseName: syncMgr.databaseName,
                            username: syncMgr.username,
                            password: syncMgr.password,
                            useSsl: syncMgr.useSsl,
                            isAutoSyncEnabled: val,
                          );
                        },
                      ),
                      
                      const Divider(),

                      ListTile(
                        leading: const Icon(Icons.settings_input_component_outlined),
                        title: const Text('Configure PostgreSQL Database'),
                        subtitle: Text('${syncMgr.username}@${syncMgr.host}:${syncMgr.port}/${syncMgr.databaseName}'),
                        trailing: const Icon(Icons.chevron_right, size: 18),
                        onTap: () {
                          _showPgConfigDialog(context, syncMgr);
                        },
                      ),

                      const Divider(),

                      Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
                        child: SizedBox(
                          width: double.infinity,
                          child: ElevatedButton.icon(
                            style: ElevatedButton.styleFrom(
                              backgroundColor: statusColor.withOpacity(0.15),
                              foregroundColor: statusColor,
                              elevation: 0,
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                              padding: const EdgeInsets.symmetric(vertical: 12),
                            ),
                            icon: syncMgr.status == SyncStatus.syncing
                                ? const SizedBox(
                                    width: 18,
                                    height: 18,
                                    child: CircularProgressIndicator(strokeWidth: 2, color: Colors.orange),
                                  )
                                : const Icon(Icons.sync_alt, size: 18),
                            label: Text(
                              syncMgr.status == SyncStatus.syncing
                                  ? 'Synchronizing...'
                                  : 'Sync Now',
                              style: const TextStyle(fontWeight: FontWeight.bold),
                            ),
                            onPressed: syncMgr.status == SyncStatus.syncing
                                ? null
                                : () => syncMgr.syncNow(),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              );
            },
          ),
          const SizedBox(height: 24.0),

          // 3. Voice Optimization Settings
          _buildSectionHeader(context, 'Voice Optimization Settings'),
          Card(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 12.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text('Change Voice Style / Accent', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13.0)),
                  const SizedBox(height: 8),
                  DropdownButtonFormField<String>(
                    value: viewModel.ttsLanguage,
                    dropdownColor: theme.brightness == Brightness.dark ? const Color(0xFF1E2124) : Colors.white,
                    decoration: InputDecoration(
                      filled: true,
                      fillColor: theme.brightness == Brightness.dark ? Colors.blueGrey[900] : Colors.grey[100],
                      border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
                      contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    ),
                    onChanged: (val) {
                      if (val != null) {
                        viewModel.setTtsLanguage(val);
                      }
                    },
                    items: const [
                      DropdownMenuItem(value: 'en-US', child: Text('English (US) - Standard Voice A')),
                      DropdownMenuItem(value: 'en-GB', child: Text('English (UK) - Standard Voice B')),
                      DropdownMenuItem(value: 'en-AU', child: Text('English (AU) - Warm Accent')),
                      DropdownMenuItem(value: 'en-IN', child: Text('English (IN) - Clear Voice')),
                    ],
                  ),
                  const SizedBox(height: 16),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text('Voice Speed (Speech Rate)', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13.0)),
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
                    },
                  ),
                  const SizedBox(height: 12),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text('Voice Tone (Pitch)', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13.0)),
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
                    },
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24.0),

          // 4. AI & Search Indexing Settings
          _buildSectionHeader(context, 'AI & Offline Index Settings'),
          Card(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text(
                        'Offline Search Index Status',
                        style: TextStyle(fontWeight: FontWeight.bold),
                      ),
                      viewModel.isIndexing
                          ? const Text(
                              'Indexing...',
                              style: TextStyle(fontWeight: FontWeight.bold, color: Colors.orange),
                            )
                          : const Text(
                              'Ready (Fully Offline)',
                              style: TextStyle(fontWeight: FontWeight.bold, color: Colors.green),
                            ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  if (viewModel.isIndexing) ...[
                    LinearProgressIndicator(
                      value: viewModel.indexingProgress,
                      backgroundColor: Colors.grey.withOpacity(0.2),
                      color: Colors.greenAccent,
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Indexing Bible books: ${(viewModel.indexingProgress * 100).toStringAsFixed(0)}% complete',
                      style: const TextStyle(fontSize: 11, color: Colors.grey),
                    ),
                  ] else ...[
                    const Text(
                      'All 1,189 chapters of KJV, WEB, and ASV are fully indexed inside SQLite. Offline keyword searches work instantly without any active network connection.',
                      style: TextStyle(fontSize: 12, color: Colors.grey, height: 1.3),
                    ),
                    const SizedBox(height: 12),
                    ElevatedButton.icon(
                      onPressed: () {
                        viewModel.indexBibleDatabase();
                      },
                      icon: const Icon(Icons.refresh, size: 16),
                      label: const Text('Re-Build Search Index'),
                      style: ElevatedButton.styleFrom(
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                        textStyle: const TextStyle(fontSize: 12),
                      ),
                    ),
                  ],
                  const Divider(height: 24),
                  const Text(
                    'Gemini API Key (Optional Override)',
                    style: TextStyle(fontWeight: FontWeight.bold),
                  ),
                  const SizedBox(height: 6),
                  const Text(
                    'AI Devotions automatically run via server-side credentials. If you wish to use your own personal API key, input it below. Leave blank to use defaults.',
                    style: TextStyle(fontSize: 12, color: Colors.grey, height: 1.3),
                  ),
                  const SizedBox(height: 12),
                  ElevatedButton.icon(
                    onPressed: () {
                      _showApiKeyInputDialog(context);
                    },
                    icon: const Icon(Icons.key, size: 16),
                    label: const Text('Configure Gemini API Key'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: theme.colorScheme.secondaryContainer,
                      foregroundColor: theme.colorScheme.onSecondaryContainer,
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      textStyle: const TextStyle(fontSize: 12),
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24.0),

          // 5. System Specification
          _buildSectionHeader(context, 'System Specification'),
          Card(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: const Column(
              children: [
                ListTile(
                  leading: Icon(Icons.info_outline),
                  title: Text('App Version'),
                  trailing: Text('1.4.2-Release', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.grey)),
                ),
                Divider(height: 1),
                ListTile(
                  leading: Icon(Icons.build_circle_outlined),
                  title: Text('Engine Build'),
                  trailing: Text('SQLite Room v2.6.1', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.grey)),
                ),
                Divider(height: 1),
                ListTile(
                  leading: Icon(Icons.tag),
                  title: Text('Schema Hash'),
                  trailing: Text('3b6a0dcd', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.grey)),
                ),
                Divider(height: 1),
                ListTile(
                  leading: Icon(Icons.psychology_outlined),
                  title: Text('AI Model'),
                  trailing: Text('Gemini 1.5 Flash REST Server-Side', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.grey)),
                ),
                Divider(height: 1),
                ListTile(
                  leading: Icon(Icons.swap_calls),
                  title: Text('Connectivity'),
                  trailing: Text('Dual Mode (Offline/Cloud Hybrid)', style: TextStyle(fontWeight: FontWeight.bold, color: Colors.grey)),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  void _showTranslationSuggestionDialog(BuildContext context, BibleViewModel viewModel) {
    final transId = viewModel.suggestedTranslationId;
    if (transId == null) return;

    // Retrieve full name of the suggested translation
    String fullName = transId;
    for (var langList in viewModel.translationsByLanguage.values) {
      for (var trans in langList) {
        if (trans['id'] == transId) {
          fullName = trans['name']!;
          break;
        }
      }
    }

    // Delay briefly to allow the dropdown close transition to finish
    Future.microtask(() {
      if (!context.mounted) return;
      showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            title: const Row(
              children: [
                Icon(Icons.language_outlined, color: Colors.amber),
                SizedBox(width: 8),
                Text('Suggested Translation'),
              ],
            ),
            content: Text(
              'We noticed you set your app language to ${viewModel.appLanguage}. Would you like to switch your active Bible translation to $fullName for a matching study experience?\n\n(This will not affect your offline downloads.)',
              style: const TextStyle(fontSize: 14),
            ),
            actions: [
              TextButton(
                onPressed: () {
                  viewModel.clearSuggestedTranslation();
                  Navigator.pop(context);
                },
                child: const Text('Keep Current'),
              ),
              ElevatedButton(
                onPressed: () {
                  viewModel.selectTranslation(transId);
                  viewModel.clearSuggestedTranslation();
                  Navigator.pop(context);
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(
                      content: Text('Switched translation to $fullName!'),
                      backgroundColor: Colors.green,
                    ),
                  );
                },
                child: const Text('Switch'),
              ),
            ],
          );
        },
      );
    });
  }

  Widget _buildSectionHeader(BuildContext context, String title) {
    return Padding(
      padding: const EdgeInsets.only(left: 4.0, bottom: 8.0),
      child: Text(
        title,
        style: Theme.of(context).textTheme.titleSmall?.copyWith(
              fontWeight: FontWeight.bold,
              color: Theme.of(context).colorScheme.primary,
            ),
      ),
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

  void _showPgConfigDialog(BuildContext context, SyncManager syncMgr) {
    final hostController = TextEditingController(text: syncMgr.host);
    final portController = TextEditingController(text: syncMgr.port.toString());
    final dbController = TextEditingController(text: syncMgr.databaseName);
    final userController = TextEditingController(text: syncMgr.username);
    final passwordController = TextEditingController(text: syncMgr.password);
    bool useSslVal = syncMgr.useSsl;

    showDialog(
      context: context,
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setState) {
            final theme = Theme.of(context);
            return AlertDialog(
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
              title: Row(
                children: [
                  Icon(Icons.storage, color: theme.colorScheme.primary),
                  const SizedBox(width: 8),
                  const Text('PostgreSQL Settings', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                ],
              ),
              content: SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Text(
                      'Sync your highlights, bookmarks, and history with your PostgreSQL database.',
                      style: TextStyle(fontSize: 12, color: Colors.grey),
                    ),
                    const SizedBox(height: 16),
                    TextField(
                      controller: hostController,
                      decoration: const InputDecoration(
                        labelText: 'Host / IP Address',
                        border: OutlineInputBorder(),
                        isDense: true,
                      ),
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: portController,
                      keyboardType: TextInputType.number,
                      decoration: const InputDecoration(
                        labelText: 'Port',
                        border: OutlineInputBorder(),
                        isDense: true,
                      ),
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: dbController,
                      decoration: const InputDecoration(
                        labelText: 'Database Name',
                        border: OutlineInputBorder(),
                        isDense: true,
                      ),
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: userController,
                      decoration: const InputDecoration(
                        labelText: 'Username',
                        border: OutlineInputBorder(),
                        isDense: true,
                      ),
                    ),
                    const SizedBox(height: 12),
                    TextField(
                      controller: passwordController,
                      obscureText: true,
                      decoration: const InputDecoration(
                        labelText: 'Password',
                        border: OutlineInputBorder(),
                        isDense: true,
                      ),
                    ),
                    const SizedBox(height: 8),
                    SwitchListTile(
                      contentPadding: EdgeInsets.zero,
                      title: const Text('Use SSL / TLS Encryption', style: TextStyle(fontSize: 13)),
                      value: useSslVal,
                      onChanged: (val) {
                        setState(() {
                          useSslVal = val;
                        });
                      },
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
                  style: ElevatedButton.styleFrom(
                    backgroundColor: theme.colorScheme.primary,
                    foregroundColor: theme.colorScheme.onPrimary,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                  ),
                  onPressed: () {
                    final portVal = int.tryParse(portController.text.trim()) ?? 5432;
                    syncMgr.saveSettings(
                      host: hostController.text.trim(),
                      port: portVal,
                      databaseName: dbController.text.trim(),
                      username: userController.text.trim(),
                      password: passwordController.text.trim(),
                      useSsl: useSslVal,
                      isAutoSyncEnabled: syncMgr.isAutoSyncEnabled,
                    );
                    Navigator.pop(context);
                    
                    // Trigger instant sync with new settings
                    syncMgr.syncNow();
                  },
                  child: const Text('Save & Sync'),
                ),
              ],
            );
          },
        );
      },
    );
  }

  void _showApiKeyInputDialog(BuildContext context) async {
    final prefs = await SharedPreferences.getInstance();
    final currentKey = prefs.getString('gemini_api_key') ?? '';
    final controller = TextEditingController(text: currentKey);

    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Row(
            children: [
              Icon(Icons.key, color: Colors.blueAccent),
              SizedBox(width: 8),
              Text('Gemini API Key'),
            ],
          ),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Enter your custom Gemini API key. This will be securely saved locally to your device\'s local storage and used for Devotional generation.',
                style: TextStyle(fontSize: 12, color: Colors.grey, height: 1.3),
              ),
              const SizedBox(height: 16),
              TextField(
                controller: controller,
                obscureText: true,
                decoration: const InputDecoration(
                  labelText: 'Gemini API Key',
                  border: OutlineInputBorder(),
                  hintText: 'AIzaSy...',
                ),
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.pop(context);
              },
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () async {
                final key = controller.text.trim();
                if (key.isEmpty) {
                  await prefs.remove('gemini_api_key');
                } else {
                  await prefs.setString('gemini_api_key', key);
                }
                Navigator.pop(context);
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                    content: Text('Gemini API key updated successfully!'),
                    backgroundColor: Colors.green,
                  ),
                );
              },
              child: const Text('Save Key'),
            ),
          ],
        );
      },
    );
  }
}

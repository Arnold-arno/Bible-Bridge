import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'viewmodels/bible_viewmodel.dart';
import 'screens/main_navigation_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const BibleTimelineApp());
}

class BibleTimelineApp extends StatelessWidget {
  const BibleTimelineApp({super.key});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (_) => BibleViewModel(),
      child: Consumer<BibleViewModel>(
        builder: (context, viewModel, child) {
          final lightTheme = ThemeData(
            useMaterial3: true,
            brightness: Brightness.light,
            colorScheme: ColorScheme.fromSeed(
              seedColor: const Color(0xFF2E7D32), // Forest Green
              brightness: Brightness.light,
              background: const Color(0xFFFAF9F6), // Warm off-white
              surface: const Color(0xFFFFFFFF),
              primary: const Color(0xFF2E7D32),
              secondary: const Color(0xFF1565C0), // Royal Blue
            ),
            textTheme: const TextTheme(
              titleLarge: TextStyle(fontWeight: FontWeight.bold, fontSize: 22.0),
              bodyMedium: TextStyle(fontSize: 16.0, height: 1.5),
            ),
          );

          final darkTheme = ThemeData(
            useMaterial3: true,
            brightness: Brightness.dark,
            colorScheme: ColorScheme.fromSeed(
              seedColor: const Color(0xFF81C784), // Light Sage Green
              brightness: Brightness.dark,
              background: const Color(0xFF121416), // Dark Slate
              surface: const Color(0xFF1E2124),
              primary: const Color(0xFF81C784),
              secondary: const Color(0xFF64B5F6),
            ),
            textTheme: const TextTheme(
              titleLarge: TextStyle(fontWeight: FontWeight.bold, fontSize: 22.0),
              bodyMedium: TextStyle(fontSize: 16.0, height: 1.5),
            ),
          );

          final sepiaTheme = ThemeData(
            useMaterial3: true,
            brightness: Brightness.light,
            colorScheme: const ColorScheme(
              brightness: Brightness.light,
              primary: Color(0xFF5B4636), // Deep Sepia Brown
              onPrimary: Color(0xFFFFFFFF),
              secondary: Color(0xFF8E6F54), // Medium Warm Sepia
              onSecondary: Color(0xFFFFFFFF),
              error: Color(0xFFBA1A1A),
              onError: Color(0xFFFFFFFF),
              background: Color(0xFFF4ECD8), // Soft Warm Sepia canvas
              onBackground: Color(0xFF423225), // Elegant dark brown
              surface: Color(0xFFFDF6E3), // Warm paper
              onSurface: Color(0xFF423225),
              surfaceVariant: Color(0xFFEFE6D0),
              onSurfaceVariant: Color(0xFF5D4037),
              outline: Color(0xFF8D6E63),
            ),
            textTheme: const TextTheme(
              titleLarge: TextStyle(fontWeight: FontWeight.bold, fontSize: 22.0, color: Color(0xFF423225)),
              bodyMedium: TextStyle(fontSize: 16.0, height: 1.5, color: Color(0xFF423225)),
            ),
          );

          ThemeData activeTheme;
          if (viewModel.themeMode == 'dark') {
            activeTheme = darkTheme;
          } else if (viewModel.themeMode == 'sepia') {
            activeTheme = sepiaTheme;
          } else {
            activeTheme = lightTheme;
          }

          return MaterialApp(
            title: 'Scripture Chronology Companion',
            debugShowCheckedModeBanner: false,
            theme: activeTheme,
            darkTheme: darkTheme,
            themeMode: viewModel.themeMode == 'dark' ? ThemeMode.dark : ThemeMode.light,
            home: const MainNavigationScreen(),
          );
        },
      ),
    );
  }
}

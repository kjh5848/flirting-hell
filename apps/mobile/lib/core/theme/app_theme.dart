import 'package:flutter/material.dart';

class AppTheme {
  static const _background = Color(0xFFFBF4F1);
  static const _surface = Color(0xFFFFFFFF);
  static const _surfaceSoft = Color(0xFFFFF8F5);
  static const _textPrimary = Color(0xFF2A2024);
  static const _textSecondary = Color(0xFF6E5C62);
  static const _accent = Color(0xFFC65F77);
  static const _border = Color(0xFFEFE0E3);

  /// 화면에서 재사용하는 공개 토큰 (차분한 로즈 방향).
  static const accent = _accent;
  static const ink = _textPrimary;
  static const heroSurface = Color(0xFFF4E3E6); // 블러시 — 히어로/강조 면
  static const heroBorder = Color(0xFFF0DCE0);
  static const textSecondary = _textSecondary;

  static ThemeData get light {
    final colorScheme = ColorScheme.fromSeed(
      seedColor: _accent,
      brightness: Brightness.light,
      primary: _accent,
      surface: _surface,
      onSurface: _textPrimary,
    );

    return ThemeData(
      useMaterial3: true,
      colorScheme: colorScheme,
      scaffoldBackgroundColor: _background,
      fontFamily: 'Pretendard',
      progressIndicatorTheme: const ProgressIndicatorThemeData(
        color: _accent,
      ),
      appBarTheme: const AppBarTheme(
        centerTitle: false,
        elevation: 0,
        scrolledUnderElevation: 0,
        backgroundColor: _background,
        foregroundColor: _textPrimary,
        titleTextStyle: TextStyle(
          color: _textPrimary,
          fontSize: 22,
          fontWeight: FontWeight.w900,
          height: 1.1,
        ),
      ),
      bottomNavigationBarTheme: const BottomNavigationBarThemeData(
        type: BottomNavigationBarType.fixed,
        backgroundColor: _surface,
        selectedItemColor: _accent,
        unselectedItemColor: _textSecondary,
        selectedLabelStyle: TextStyle(
          fontSize: 11,
          fontWeight: FontWeight.w900,
        ),
        unselectedLabelStyle: TextStyle(
          fontSize: 11,
          fontWeight: FontWeight.w700,
        ),
      ),
      cardTheme: CardTheme(
        color: _surface,
        elevation: 0,
        margin: EdgeInsets.zero,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(24),
          side: const BorderSide(color: _border),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: _surfaceSoft,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: _border),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: _border),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(color: _accent, width: 1.4),
        ),
        labelStyle: const TextStyle(
          color: _textSecondary,
          fontSize: 13,
          fontWeight: FontWeight.w800,
        ),
        helperStyle: const TextStyle(
          color: _textSecondary,
          fontSize: 12,
          fontWeight: FontWeight.w600,
          height: 1.35,
        ),
      ),
      chipTheme: ChipThemeData(
        backgroundColor: const Color(0xFFF7EAED),
        selectedColor: _accent,
        disabledColor: _surfaceSoft,
        side: BorderSide.none,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(999),
        ),
        labelStyle: const TextStyle(
          color: _accent,
          fontSize: 12,
          fontWeight: FontWeight.w800,
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          backgroundColor: _textPrimary,
          foregroundColor: Colors.white,
          minimumSize: const Size.fromHeight(56),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(20),
          ),
          textStyle: const TextStyle(
            fontSize: 15,
            fontWeight: FontWeight.w900,
          ),
        ),
      ),
      textTheme: const TextTheme(
        headlineLarge: TextStyle(
          color: _textPrimary,
          fontSize: 30,
          fontWeight: FontWeight.w900,
          height: 1.05,
          letterSpacing: -1.2,
        ),
        titleLarge: TextStyle(
          color: _textPrimary,
          fontSize: 20,
          fontWeight: FontWeight.w900,
          height: 1.15,
          letterSpacing: -0.5,
        ),
        titleMedium: TextStyle(
          color: _textPrimary,
          fontSize: 16,
          fontWeight: FontWeight.w900,
          height: 1.2,
        ),
        bodyMedium: TextStyle(
          color: _textSecondary,
          fontSize: 14,
          fontWeight: FontWeight.w700,
          height: 1.45,
        ),
        labelSmall: TextStyle(
          color: _accent,
          fontSize: 11,
          fontWeight: FontWeight.w900,
          letterSpacing: 1.4,
        ),
      ),
    );
  }
}

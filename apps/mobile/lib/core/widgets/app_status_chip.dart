import 'package:flutter/material.dart';

enum AppStatusChipTone {
  accent,
  warm,
  success,
  warning,
  neutral,
}

class AppStatusChip extends StatelessWidget {
  const AppStatusChip({
    required this.label,
    this.tone = AppStatusChipTone.accent,
    super.key,
  });

  final String label;
  final AppStatusChipTone tone;

  @override
  Widget build(BuildContext context) {
    final colors = _colors(tone);

    return DecoratedBox(
      decoration: BoxDecoration(
        color: colors.background,
        borderRadius: BorderRadius.circular(999),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
        child: Text(
          label,
          style: TextStyle(
            color: colors.foreground,
            fontSize: 11,
            fontWeight: FontWeight.w900,
            height: 1,
          ),
        ),
      ),
    );
  }
}

({Color background, Color foreground}) _colors(AppStatusChipTone tone) {
  return switch (tone) {
    AppStatusChipTone.accent => (
        background: const Color(0xFFF7EAED),
        foreground: const Color(0xFFC65F77),
      ),
    AppStatusChipTone.warm => (
        background: const Color(0xFFFFF7ED),
        foreground: const Color(0xFFFF7A59),
      ),
    AppStatusChipTone.success => (
        background: const Color(0xFFECFDF5),
        foreground: const Color(0xFF047857),
      ),
    AppStatusChipTone.warning => (
        background: const Color(0xFFFFF7ED),
        foreground: const Color(0xFFB45309),
      ),
    AppStatusChipTone.neutral => (
        background: const Color(0xFFFFF8F4),
        foreground: const Color(0xFF6E5C62),
      ),
  };
}

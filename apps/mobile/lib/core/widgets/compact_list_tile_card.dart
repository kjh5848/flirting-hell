import 'package:flutter/material.dart';

import 'app_status_chip.dart';
import 'section_card.dart';

class CompactListTileCard extends StatelessWidget {
  const CompactListTileCard({
    required this.title,
    required this.subtitle,
    this.leadingText,
    this.trailingLabel,
    this.trailingTone = AppStatusChipTone.accent,
    this.trailingText,
    this.showChevron = true,
    this.onTap,
    super.key,
  });

  final String title;
  final String subtitle;
  final String? leadingText;
  final String? trailingLabel;
  final AppStatusChipTone trailingTone;

  /// Rose-colored trailing value (e.g. a package price). Replaces the status
  /// chip when set.
  final String? trailingText;

  /// Hide the chevron for non-navigational rows (e.g. billing packages).
  final bool showChevron;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final content = SectionCard(
      padding: const EdgeInsets.all(14),
      radius: 20,
      child: Row(
        children: [
          _LeadingBadge(text: leadingText ?? title.characters.first),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: Theme.of(context).textTheme.titleMedium),
                const SizedBox(height: 4),
                Text(
                  subtitle,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: Theme.of(context).textTheme.bodyMedium,
                ),
              ],
            ),
          ),
          if (trailingText != null) ...[
            const SizedBox(width: 8),
            Text(
              trailingText!,
              style: TextStyle(
                color: Theme.of(context).colorScheme.primary,
                fontSize: 14,
                fontWeight: FontWeight.w900,
              ),
            ),
          ] else if (trailingLabel != null) ...[
            const SizedBox(width: 8),
            AppStatusChip(label: trailingLabel!, tone: trailingTone),
          ],
          if (showChevron) ...[
            const SizedBox(width: 6),
            const Icon(
              Icons.chevron_right_rounded,
              size: 22,
              color: Color(0xFF9B8A8E),
            ),
          ],
        ],
      ),
    );

    if (onTap == null) {
      return content;
    }

    return InkWell(
      borderRadius: BorderRadius.circular(20),
      onTap: onTap,
      child: content,
    );
  }
}

class _LeadingBadge extends StatelessWidget {
  const _LeadingBadge({required this.text});

  final String text;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: const Color(0xFFFFF1F2),
        borderRadius: BorderRadius.circular(15),
      ),
      child: SizedBox(
        width: 40,
        height: 40,
        child: Center(
          child: Text(
            text.characters.first,
            style: TextStyle(
              color: Theme.of(context).colorScheme.primary,
              fontSize: 16,
              fontWeight: FontWeight.w900,
            ),
          ),
        ),
      ),
    );
  }
}

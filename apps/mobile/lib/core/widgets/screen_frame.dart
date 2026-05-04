import 'package:flutter/material.dart';

class ScreenFrame extends StatelessWidget {
  const ScreenFrame({
    required this.title,
    required this.children,
    this.subtitle,
    super.key,
  });

  final String title;
  final String? subtitle;
  final List<Widget> children;

  @override
  Widget build(BuildContext context) {
    return CustomScrollView(
      slivers: [
        SliverAppBar(
          pinned: true,
          title: Text(title),
        ),
        SliverPadding(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 28),
          sliver: SliverList.list(
            children: [
              if (subtitle != null) ...[
                Text(
                  subtitle!,
                  style: Theme.of(context).textTheme.bodyMedium,
                ),
                const SizedBox(height: 18),
              ],
              ...children,
            ],
          ),
        ),
      ],
    );
  }
}

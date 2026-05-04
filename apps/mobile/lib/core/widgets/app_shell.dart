import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

class AppShell extends StatelessWidget {
  const AppShell({
    required this.currentPath,
    required this.child,
    super.key,
  });

  final String currentPath;
  final Widget child;

  static const _destinations = [
    _ShellDestination('/home', '홈', Icons.home_rounded),
    _ShellDestination('/rooms', '상담방', Icons.chat_bubble_rounded),
    _ShellDestination('/saved', '저장', Icons.bookmark_rounded),
    _ShellDestination('/profile', '내 정보', Icons.person_rounded),
    _ShellDestination('/billing', '분석권', Icons.local_activity_rounded),
  ];

  @override
  Widget build(BuildContext context) {
    final currentIndex = _destinations.indexWhere(
      (destination) => currentPath.startsWith(destination.path),
    );

    return Scaffold(
      body: SafeArea(
        child: child,
      ),
      bottomNavigationBar: DecoratedBox(
        decoration: BoxDecoration(
          color: Theme.of(context).colorScheme.surface,
          border: const Border(
            top: BorderSide(color: Color(0xFFEDE3DF)),
          ),
        ),
        child: BottomNavigationBar(
          currentIndex: currentIndex < 0 ? 0 : currentIndex,
          onTap: (index) => context.go(_destinations[index].path),
          items: [
            for (final destination in _destinations)
              BottomNavigationBarItem(
                icon: Icon(destination.icon),
                label: destination.label,
              ),
          ],
        ),
      ),
    );
  }
}

class _ShellDestination {
  const _ShellDestination(this.path, this.label, this.icon);

  final String path;
  final String label;
  final IconData icon;
}

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

  // 분석권(/billing)은 탭이 아니라 홈 상단 잔여량 칩과 내 정보 화면에서 진입한다.
  static const _destinations = [
    _ShellDestination('/home', '홈', Icons.home_rounded),
    _ShellDestination('/rooms', '상담방', Icons.chat_bubble_rounded),
    _ShellDestination('/saved', '저장', Icons.bookmark_rounded),
    _ShellDestination('/profile', '내 정보', Icons.person_rounded),
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
      bottomNavigationBar: SafeArea(
        top: false,
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 8, 20, 14),
          child: DecoratedBox(
            decoration: BoxDecoration(
              color: Theme.of(context).colorScheme.surface,
              borderRadius: BorderRadius.circular(26),
              border: Border.all(color: const Color(0xFFEDE3DF)),
              boxShadow: [
                BoxShadow(
                  color: const Color(0xFF2A141B).withOpacity(0.07),
                  blurRadius: 24,
                  offset: const Offset(0, 10),
                ),
              ],
            ),
            child: ClipRRect(
              borderRadius: BorderRadius.circular(26),
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
          ),
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

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/widgets/screen_frame.dart';
import '../../../core/widgets/section_card.dart';
import '../application/bootstrap_provider.dart';

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final bootstrap = ref.watch(bootstrapProvider);
    final nickname = bootstrap?.user.profile.nickname ?? '사용자';

    return ScreenFrame(
      title: '플러팅지옥',
      subtitle: '$nickname님, 상대방과 나눈 대화나 지금 처한 상황을 정리하고 다음 답장과 전략을 고르세요.',
      children: [
        SectionCard(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('오늘 이어갈 상담방', style: Theme.of(context).textTheme.labelSmall),
              const SizedBox(height: 10),
              Text('지금 고민되는 대화를 먼저 고르세요',
                  style: Theme.of(context).textTheme.headlineLarge),
              const SizedBox(height: 12),
              Text(
                '전략을 먼저 고르지 않습니다. 대화나 상황을 붙여넣으면 앱이 흐름을 요약한 뒤 필요한 전략을 제안합니다.',
                style: Theme.of(context).textTheme.bodyMedium,
              ),
              const SizedBox(height: 18),
              FilledButton(
                onPressed: () => context.go('/rooms'),
                child: const Text('상담방 보기'),
              ),
            ],
          ),
        ),
        const SizedBox(height: 16),
        if (bootstrap != null)
          for (final room in bootstrap.recentRooms) ...[
            _RecentRoomTile(
              name: room.alias,
              summary: room.currentConcern,
              time: '최근',
            ),
            const SizedBox(height: 10),
          ],
      ],
    );
  }
}

class _RecentRoomTile extends StatelessWidget {
  const _RecentRoomTile({
    required this.name,
    required this.summary,
    required this.time,
  });

  final String name;
  final String summary;
  final String time;

  @override
  Widget build(BuildContext context) {
    return SectionCard(
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          CircleAvatar(
            backgroundColor: const Color(0xFFFFF1F2),
            foregroundColor: Theme.of(context).colorScheme.primary,
            child: Text(name.characters.first),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(name, style: Theme.of(context).textTheme.titleMedium),
                const SizedBox(height: 4),
                Text(summary, style: Theme.of(context).textTheme.bodyMedium),
              ],
            ),
          ),
          Text(time, style: Theme.of(context).textTheme.bodyMedium),
        ],
      ),
    );
  }
}

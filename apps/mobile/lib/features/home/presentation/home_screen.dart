import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/widgets/app_status_chip.dart';
import '../../../core/widgets/compact_list_tile_card.dart';
import '../../../core/widgets/screen_frame.dart';
import '../../../core/widgets/section_card.dart';
import '../application/bootstrap_provider.dart';

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final bootstrapAsync = ref.watch(bootstrapProvider);
    final bootstrap = bootstrapAsync.valueOrNull;
    final nickname = bootstrap?.user.profile.nickname ?? '사용자';
    final freeRemaining = bootstrap?.usage.freeRemaining;

    return ScreenFrame(
      title: '플러팅지옥',
      subtitle: '$nickname님, 상대방과 나눈 대화나 지금 처한 상황을 정리하고 다음 답장과 전략을 고르세요.',
      trailing: freeRemaining == null
          ? null
          : InkWell(
              borderRadius: BorderRadius.circular(999),
              onTap: () => context.go('/billing'),
              child: AppStatusChip(
                label: '$freeRemaining회 남음',
                tone: AppStatusChipTone.neutral,
              ),
            ),
      children: [
        SectionCard(
          backgroundColor: const Color(0xFF1D1719),
          borderColor: const Color(0xFF1D1719),
          radius: 28,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const AppStatusChip(label: '대화 붙여넣기'),
              const SizedBox(height: 10),
              const Text(
                '새 분석 시작',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 28,
                  fontWeight: FontWeight.w900,
                  height: 1.08,
                  letterSpacing: -1,
                ),
              ),
              const SizedBox(height: 12),
              Text(
                '전략을 먼저 고르지 않습니다. 상담방을 고르고 대화나 상황을 붙여넣으면 흐름을 요약한 뒤 필요한 전략을 제안합니다.',
                style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      color: const Color(0xFFF6EDEE),
                    ),
              ),
              const SizedBox(height: 18),
              FilledButton(
                onPressed: () => context.go('/rooms'),
                style: FilledButton.styleFrom(
                  backgroundColor: const Color(0xFFE43F5A),
                ),
                child: const Text('상담방에서 시작'),
              ),
            ],
          ),
        ),
        const SizedBox(height: 16),
        bootstrapAsync.when(
          data: (snapshot) {
            final rooms = snapshot?.recentRooms ?? const [];
            if (rooms.isEmpty) {
              return const _BootstrapStateCard(
                title: '아직 상담방이 없어요',
                body: '첫 상담방을 만들면 상대별 요약과 추천 답장을 이곳에서 확인합니다.',
              );
            }

            return Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('최근 상담방', style: Theme.of(context).textTheme.titleLarge),
                const SizedBox(height: 10),
                for (final room in rooms) ...[
                  CompactListTileCard(
                    title: room.alias,
                    subtitle: room.lastTurnSummary.isEmpty
                        ? room.currentConcern
                        : room.lastTurnSummary,
                    trailingLabel: '최근',
                    trailingTone: AppStatusChipTone.accent,
                    onTap: () => context.go('/rooms/${room.roomId}'),
                  ),
                  const SizedBox(height: 10),
                ],
                const SizedBox(height: 4),
                SectionCard(
                  radius: 22,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('오늘의 기준',
                          style: Theme.of(context).textTheme.titleLarge),
                      const SizedBox(height: 12),
                      Wrap(
                        spacing: 8,
                        runSpacing: 8,
                        children: [
                          AppStatusChip(
                            label: snapshot?.user.profile.speechStyle ?? '내 말투',
                          ),
                          AppStatusChip(
                            label: _guidanceLabel(
                              snapshot?.user.profile.guidanceLevel ??
                                  'BALANCED',
                            ),
                            tone: AppStatusChipTone.warning,
                          ),
                        ],
                      ),
                      const SizedBox(height: 12),
                      Text(
                        '상대 반응을 단정하지 않고, 부담 없는 다음 말을 우선 추천합니다.',
                        style: Theme.of(context).textTheme.bodyMedium,
                      ),
                    ],
                  ),
                ),
              ],
            );
          },
          error: (error, stackTrace) => const _BootstrapStateCard(
            title: '서버 연결이 필요해요',
            body: 'Spring 백엔드를 실행하면 Firebase 사용자 기준 bootstrap 데이터를 불러옵니다.',
          ),
          loading: () => const _BootstrapStateCard(
            title: '내 정보를 불러오는 중',
            body: '사용자 설정, 분석권, 최근 상담방을 확인하고 있습니다.',
          ),
        ),
      ],
    );
  }
}

class _BootstrapStateCard extends StatelessWidget {
  const _BootstrapStateCard({
    required this.title,
    required this.body,
  });

  final String title;
  final String body;

  @override
  Widget build(BuildContext context) {
    return SectionCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 8),
          Text(body, style: Theme.of(context).textTheme.bodyMedium),
        ],
      ),
    );
  }
}

String _guidanceLabel(String value) {
  return switch (value) {
    'SUPPORTIVE' => '부드럽게',
    'REALITY_CHECK' => '현실 체크',
    _ => '균형 조언',
  };
}

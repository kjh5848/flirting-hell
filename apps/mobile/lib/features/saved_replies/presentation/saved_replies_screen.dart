import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/widgets/app_status_chip.dart';
import '../../../core/widgets/compact_list_tile_card.dart';
import '../../../core/widgets/screen_frame.dart';
import '../../../core/widgets/section_card.dart';
import '../../../data/models/room_models.dart';
import '../../../data/remote/rooms_api.dart';

final savedRepliesProvider =
    FutureProvider.autoDispose<List<SavedReply>>((ref) {
  return ref.watch(roomsApiProvider).fetchSavedReplies();
});

class SavedRepliesScreen extends ConsumerWidget {
  const SavedRepliesScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final savedAsync = ref.watch(savedRepliesProvider);

    return ScreenFrame(
      title: '저장',
      subtitle: '원문 전문이 아니라 상담방별 요약, 선택 이유, 추천 답장을 중심으로 보관합니다.',
      children: [
        SectionCard(
          backgroundColor: const Color(0xFFFFF8F5),
          radius: 24,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const AppStatusChip(label: '저장 원칙'),
              const SizedBox(height: 10),
              Text('답장은 상담방별로 보관',
                  style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 8),
              Text(
                '분석 결과에서 북마크한 답장만 모아둡니다. 나중에 왜 이 답장을 골랐는지 다시 볼 수 있어요.',
                style: Theme.of(context).textTheme.bodyMedium,
              ),
            ],
          ),
        ),
        const SizedBox(height: 14),
        savedAsync.when(
          data: (replies) {
            if (replies.isEmpty) {
              return const _StateCard(
                title: '아직 저장한 답장이 없어요',
                body: '상담방 분석 결과에서 북마크를 누르면 여기에 모입니다.',
              );
            }
            return Column(
              children: [
                for (final reply in replies) ...[
                  CompactListTileCard(
                    leadingText: reply.roomAlias,
                    title: reply.roomAlias,
                    subtitle: reply.primaryReply,
                    trailingLabel: _strategyLabel(reply.recommendedStrategyId),
                    trailingTone: AppStatusChipTone.accent,
                    onTap: () => context.go('/rooms/${reply.roomId}'),
                  ),
                  const SizedBox(height: 10),
                ],
              ],
            );
          },
          error: (error, stackTrace) => const _StateCard(
            title: '저장 목록을 불러오지 못했어요',
            body: '서버 실행 상태와 로그인 토큰을 확인해야 합니다.',
          ),
          loading: () => const SectionCard(
            child: Center(child: CircularProgressIndicator()),
          ),
        ),
      ],
    );
  }
}

class _StateCard extends StatelessWidget {
  const _StateCard({required this.title, required this.body});

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

String _strategyLabel(String value) {
  return switch (value) {
    'DEVELOP_ROMANCE' => '연애로 발전',
    'CHECK_RELATIONSHIP_STATUS' => '여친/남친 확인',
    'MAKE_PLAN' => '약속 잡기',
    'MARRIAGE_VALUES' => '결혼 가치관',
    'SLOW_DOWN' => '속도 조절',
    _ => '전략',
  };
}

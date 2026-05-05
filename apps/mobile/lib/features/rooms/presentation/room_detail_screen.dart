import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/widgets/screen_frame.dart';
import '../../../core/widgets/section_card.dart';
import '../application/rooms_provider.dart';

class RoomDetailScreen extends ConsumerWidget {
  const RoomDetailScreen({
    required this.roomId,
    super.key,
  });

  final String roomId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final detailAsync = ref.watch(roomDetailProvider(roomId));

    return detailAsync.when(
      data: (detail) => ScreenFrame(
        title: detail.room.alias,
        subtitle: '상대별 설정과 이후 분석 히스토리를 이 상담방에 쌓습니다.',
        children: [
          SectionCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('현재 고민', style: Theme.of(context).textTheme.labelSmall),
                const SizedBox(height: 10),
                Text(
                  detail.room.currentConcern ?? '아직 입력된 고민이 없어요',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                const SizedBox(height: 12),
                Text(
                  '관계 단계: ${_relationshipStageLabel(detail.room.relationshipStage)}',
                  style: Theme.of(context).textTheme.bodyMedium,
                ),
                if (detail.room.cautionNotes != null) ...[
                  const SizedBox(height: 8),
                  Text(
                    '조심할 점: ${detail.room.cautionNotes}',
                    style: Theme.of(context).textTheme.bodyMedium,
                  ),
                ],
              ],
            ),
          ),
          const SizedBox(height: 12),
          SectionCard(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('다음 단계', style: Theme.of(context).textTheme.labelSmall),
                const SizedBox(height: 10),
                Text(
                  '대화나 상황 붙여넣기',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                const SizedBox(height: 8),
                Text(
                  '다음 구현에서 이 상담방 안에 카톡/DM/상황 설명을 붙여넣고 자동 요약과 전략 추천을 연결합니다.',
                  style: Theme.of(context).textTheme.bodyMedium,
                ),
              ],
            ),
          ),
        ],
      ),
      error: (error, stackTrace) => ScreenFrame(
        title: '상담방',
        children: [
          SectionCard(
            child: Text(
              '상담방을 불러오지 못했습니다.',
              style: Theme.of(context).textTheme.titleMedium,
            ),
          ),
        ],
      ),
      loading: () => const ScreenFrame(
        title: '상담방',
        children: [
          SectionCard(
            child: Center(child: CircularProgressIndicator()),
          ),
        ],
      ),
    );
  }
}

String _relationshipStageLabel(String value) {
  return switch (value) {
    'FIRST_CONTACT' => '처음 연락',
    'TALKING' => '썸',
    'BEFORE_DATE' => '데이트 전',
    'AFTER_DATE' => '데이트 후',
    'DATING' => '연애 중',
    'RECOVERY' => '관계 회복',
    _ => '아직 모르겠음',
  };
}

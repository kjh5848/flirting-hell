import 'package:flutter/material.dart';

import '../../../core/widgets/app_status_chip.dart';
import '../../../core/widgets/compact_list_tile_card.dart';
import '../../../core/widgets/screen_frame.dart';
import '../../../core/widgets/section_card.dart';

class SavedRepliesScreen extends StatelessWidget {
  const SavedRepliesScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ScreenFrame(
      title: '저장',
      subtitle: '원문 전문이 아니라 상담방별 요약, 선택 이유, 추천 답장을 중심으로 보관합니다.',
      children: [
        SectionCard(
          backgroundColor: const Color(0xFFFFF8F4),
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
                '나중에 왜 이 답장을 골랐는지 다시 볼 수 있도록 요약과 선택 이유를 같이 남깁니다.',
                style: Theme.of(context).textTheme.bodyMedium,
              ),
            ],
          ),
        ),
        const SizedBox(height: 14),
        const CompactListTileCard(
          title: '지우',
          subtitle: '오늘은 그냥 네 얘기 좀 더 듣고 싶었어.',
          trailingLabel: '약속 잡기',
          trailingTone: AppStatusChipTone.accent,
        ),
        const SizedBox(height: 10),
        const CompactListTileCard(
          title: '민서',
          subtitle: '그럼 우리 너무 급하게 말고 편하게 얘기해보자.',
          trailingLabel: '속도 조절',
          trailingTone: AppStatusChipTone.warning,
        ),
      ],
    );
  }
}

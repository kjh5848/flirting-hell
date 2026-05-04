import 'package:flutter/material.dart';

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
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('지우', style: Theme.of(context).textTheme.labelSmall),
              const SizedBox(height: 8),
              Text('“오늘은 그냥 네 얘기 좀 더 듣고 싶었어.”',
                  style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 10),
              Text('이유: 바로 확인하거나 압박하지 않고 대화를 이어가기 위한 답장입니다.',
                  style: Theme.of(context).textTheme.bodyMedium),
            ],
          ),
        ),
      ],
    );
  }
}

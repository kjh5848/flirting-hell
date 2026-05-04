import 'package:flutter/material.dart';

import '../../../core/widgets/screen_frame.dart';
import '../../../core/widgets/section_card.dart';

class RoomsScreen extends StatelessWidget {
  const RoomsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ScreenFrame(
      title: '상담방',
      subtitle: '상대별로 붙여넣은 대화, 상황 요약, 추천 답장을 분리해서 보관합니다.',
      children: [
        FilledButton.icon(
          onPressed: () {},
          icon: const Icon(Icons.add_rounded),
          label: const Text('새 상담방 만들기'),
        ),
        const SizedBox(height: 16),
        const _RoomCard(
          name: '지우',
          state: '호감 가능성은 있지만 정보가 더 필요해요',
          lastTurn: '오늘 22:12',
          reply: '가볍게 이어가는 답장 1개 저장됨',
        ),
        const SizedBox(height: 10),
        const _RoomCard(
          name: '하린',
          state: '약속 제안 타이밍을 보는 중',
          lastTurn: '어제 18:40',
          reply: '다른 톤 답장 2개 저장됨',
        ),
        const SizedBox(height: 10),
        const _RoomCard(
          name: '도윤',
          state: '대화 흐름 유지가 우선',
          lastTurn: '4월 30일',
          reply: '피해야 할 말 3개 저장됨',
        ),
      ],
    );
  }
}

class _RoomCard extends StatelessWidget {
  const _RoomCard({
    required this.name,
    required this.state,
    required this.lastTurn,
    required this.reply,
  });

  final String name;
  final String state;
  final String lastTurn;
  final String reply;

  @override
  Widget build(BuildContext context) {
    return SectionCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              CircleAvatar(
                backgroundColor: const Color(0xFFFFF1F2),
                foregroundColor: Theme.of(context).colorScheme.primary,
                child: Text(name.characters.first),
              ),
              const SizedBox(width: 12),
              Expanded(
                child:
                    Text(name, style: Theme.of(context).textTheme.titleLarge),
              ),
              Text(lastTurn, style: Theme.of(context).textTheme.bodyMedium),
            ],
          ),
          const SizedBox(height: 14),
          Text(state, style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 8),
          Text(reply, style: Theme.of(context).textTheme.bodyMedium),
        ],
      ),
    );
  }
}

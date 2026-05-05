import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/widgets/screen_frame.dart';
import '../../../core/widgets/section_card.dart';
import '../../../data/models/room_models.dart';
import '../../../data/remote/rooms_api.dart';
import '../../home/application/bootstrap_provider.dart';
import '../application/rooms_provider.dart';

class RoomDetailScreen extends ConsumerStatefulWidget {
  const RoomDetailScreen({
    required this.roomId,
    super.key,
  });

  final String roomId;

  @override
  ConsumerState<RoomDetailScreen> createState() => _RoomDetailScreenState();
}

class _RoomDetailScreenState extends ConsumerState<RoomDetailScreen> {
  final _inputController = TextEditingController();

  String _strategyId = 'MAKE_PLAN';
  bool _isSubmitting = false;

  @override
  void dispose() {
    _inputController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final detailAsync = ref.watch(roomDetailProvider(widget.roomId));

    return detailAsync.when(
      data: (detail) => ScreenFrame(
        title: detail.room.alias,
        subtitle: '대화 전문은 길게 저장하지 않고, 요약과 추천 답장만 이 상담방에 남깁니다.',
        children: [
          _RoomProfileCard(room: detail.room),
          const SizedBox(height: 12),
          _AnalysisInputCard(
            controller: _inputController,
            strategyId: _strategyId,
            isSubmitting: _isSubmitting,
            onStrategyChanged: (value) => setState(() => _strategyId = value),
            onSubmit: () => _submitAnalysis(detail.room.roomId),
          ),
          const SizedBox(height: 12),
          if (detail.recentTurns.isEmpty)
            const _EmptyHistoryCard()
          else
            for (final turn in detail.recentTurns) ...[
              _AnalysisTurnCard(turn: turn),
              const SizedBox(height: 12),
            ],
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

  Future<void> _submitAnalysis(String roomId) async {
    final rawInput = _inputController.text.trim();
    if (rawInput.isEmpty || _isSubmitting) {
      return;
    }

    setState(() => _isSubmitting = true);
    try {
      await ref.read(roomsApiProvider).createAnalysis(
            roomId,
            CreateAnalysisPayload(
              rawInput: rawInput,
              requestedStrategyId: _strategyId,
            ),
          );
      _inputController.clear();
      ref.invalidate(roomDetailProvider(roomId));
      ref.invalidate(roomsProvider);
      ref.invalidate(bootstrapProvider);
    } finally {
      if (mounted) {
        setState(() => _isSubmitting = false);
      }
    }
  }
}

class _RoomProfileCard extends StatelessWidget {
  const _RoomProfileCard({
    required this.room,
  });

  final Room room;

  @override
  Widget build(BuildContext context) {
    return SectionCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  room.currentConcern ?? '아직 입력된 고민이 없어요',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
              ),
              const SizedBox(width: 12),
              Chip(label: Text(_relationshipStageLabel(room.relationshipStage))),
            ],
          ),
          if (room.cautionNotes != null) ...[
            const SizedBox(height: 10),
            Text(
              '조심할 점: ${room.cautionNotes}',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
          ],
        ],
      ),
    );
  }
}

class _AnalysisInputCard extends StatelessWidget {
  const _AnalysisInputCard({
    required this.controller,
    required this.strategyId,
    required this.isSubmitting,
    required this.onStrategyChanged,
    required this.onSubmit,
  });

  final TextEditingController controller;
  final String strategyId;
  final bool isSubmitting;
  final ValueChanged<String> onStrategyChanged;
  final VoidCallback onSubmit;

  @override
  Widget build(BuildContext context) {
    return SectionCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('대화나 상황 붙여넣기', style: Theme.of(context).textTheme.titleLarge),
          const SizedBox(height: 8),
          Text(
            '카톡, DM, 문자, 상황 설명을 그대로 붙여넣으면 입력 종류와 흐름을 먼저 요약합니다.',
            style: Theme.of(context).textTheme.bodyMedium,
          ),
          const SizedBox(height: 14),
          TextField(
            controller: controller,
            minLines: 5,
            maxLines: 9,
            textInputAction: TextInputAction.newline,
            decoration: const InputDecoration(
              hintText: '예) 나: 오늘 뭐해?\n상대: 그냥 집에 있어 ㅋㅋ\n나: 오 쉬는 날이네. 뭐하면서 쉬어?',
              alignLabelWithHint: true,
              labelText: '붙여넣을 내용',
            ),
          ),
          const SizedBox(height: 12),
          DropdownButtonFormField<String>(
            value: strategyId,
            decoration: const InputDecoration(labelText: '이번에 필요한 전략'),
            items: const [
              DropdownMenuItem(value: 'DEVELOP_ROMANCE', child: Text('연애로 발전')),
              DropdownMenuItem(value: 'CHECK_RELATIONSHIP_STATUS', child: Text('여친/남친 여부 확인')),
              DropdownMenuItem(value: 'MAKE_PLAN', child: Text('약속 잡기')),
              DropdownMenuItem(value: 'MARRIAGE_VALUES', child: Text('결혼 가치관')),
              DropdownMenuItem(value: 'SLOW_DOWN', child: Text('속도 조절')),
            ],
            onChanged: isSubmitting
                ? null
                : (value) {
                    if (value != null) {
                      onStrategyChanged(value);
                    }
                  },
          ),
          const SizedBox(height: 12),
          Text(
            '개인정보, 실명, 전화번호, 주소는 붙여넣기 전에 지우세요.',
            style: Theme.of(context).textTheme.bodySmall,
          ),
          const SizedBox(height: 14),
          SizedBox(
            width: double.infinity,
            child: FilledButton(
              onPressed: isSubmitting ? null : onSubmit,
              child: isSubmitting
                  ? const SizedBox.square(
                      dimension: 18,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                  : const Text('요약하고 답장 받기'),
            ),
          ),
        ],
      ),
    );
  }
}

class _EmptyHistoryCard extends StatelessWidget {
  const _EmptyHistoryCard();

  @override
  Widget build(BuildContext context) {
    return SectionCard(
      child: Text(
        '아직 저장된 분석이 없어요. 위에 대화나 상황을 붙여넣으면 첫 답장 카드가 생깁니다.',
        style: Theme.of(context).textTheme.bodyMedium,
      ),
    );
  }
}

class _AnalysisTurnCard extends StatelessWidget {
  const _AnalysisTurnCard({
    required this.turn,
  });

  final AnalysisTurn turn;

  @override
  Widget build(BuildContext context) {
    return SectionCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              Chip(label: Text(_sourceTypeLabel(turn.sourceType))),
              Chip(label: Text(_strategyLabel(turn.recommendedStrategyId))),
            ],
          ),
          const SizedBox(height: 12),
          Text(turn.summary, style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 8),
          Text(turn.currentState, style: Theme.of(context).textTheme.bodyMedium),
          const SizedBox(height: 16),
          Text('추천 답장', style: Theme.of(context).textTheme.labelSmall),
          const SizedBox(height: 8),
          Text(turn.primaryReply, style: Theme.of(context).textTheme.titleLarge),
          const SizedBox(height: 14),
          Text('왜 이 답장인가', style: Theme.of(context).textTheme.labelSmall),
          const SizedBox(height: 6),
          Text(turn.replyReason, style: Theme.of(context).textTheme.bodyMedium),
          const SizedBox(height: 14),
          Text('피해야 할 말', style: Theme.of(context).textTheme.labelSmall),
          const SizedBox(height: 6),
          for (final warning in turn.warnings)
            Padding(
              padding: const EdgeInsets.only(bottom: 4),
              child: Text('• $warning', style: Theme.of(context).textTheme.bodyMedium),
            ),
          const SizedBox(height: 10),
          Text('다음 행동', style: Theme.of(context).textTheme.labelSmall),
          const SizedBox(height: 6),
          Text(turn.nextAction, style: Theme.of(context).textTheme.bodyMedium),
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

String _sourceTypeLabel(String value) {
  return switch (value) {
    'KAKAO' => '카톡으로 보여요',
    'DM' => 'DM으로 보여요',
    'TELEGRAM' => '텔레그램으로 보여요',
    'SMS' => '문자로 보여요',
    'SITUATION' => '상황 설명이에요',
    _ => '형식이 애매해요',
  };
}

String _strategyLabel(String value) {
  return switch (value) {
    'DEVELOP_ROMANCE' => '연애로 발전',
    'CHECK_RELATIONSHIP_STATUS' => '여친/남친 여부 확인',
    'MAKE_PLAN' => '약속 잡기',
    'MARRIAGE_VALUES' => '결혼 가치관',
    'SLOW_DOWN' => '속도 조절',
    _ => '전략 미정',
  };
}

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/widgets/app_status_chip.dart';
import '../../../core/widgets/screen_frame.dart';
import '../../../core/widgets/section_card.dart';
import '../../../data/models/room_models.dart';
import '../../../data/remote/rooms_api.dart';
import '../../home/application/bootstrap_provider.dart';
import '../../personality/domain/personality_models.dart';
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
    final idealRaw =
        ref.watch(bootstrapProvider).valueOrNull?.user.profile.personalityIdeal;
    final idealAxes = (idealRaw == null || idealRaw.trim().isEmpty)
        ? null
        : PersonalityProfile.fromStored(ideal: idealRaw).ideal;

    return detailAsync.when(
      data: (detail) => ScreenFrame(
        title: detail.room.alias,
        subtitle: '대화 전문은 길게 저장하지 않고, 요약과 추천 답장만 이 상담방에 남깁니다.',
        trailing: AppStatusChip(
          label: _relationshipStageLabel(detail.room.relationshipStage),
          tone: AppStatusChipTone.neutral,
        ),
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
          _CoachEntryCard(roomId: detail.room.roomId),
          const SizedBox(height: 10),
          _ActionEntryCard(
            icon: Icons.map_rounded,
            title: '데이트 플랜',
            subtitle: '코스 + 상대 식습관·취향으로 궁합까지 확인',
            onTap: () => context.push('/rooms/${detail.room.roomId}/plan'),
          ),
          const SizedBox(height: 12),
          if (detail.recentTurns.isEmpty)
            const _EmptyHistoryCard()
          else
            for (final turn in detail.recentTurns) ...[
              _AnalysisTurnCard(
                turn: turn,
                idealAxes: idealAxes,
                roomId: widget.roomId,
              ),
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
      radius: 24,
      backgroundColor: const Color(0xFFFFF8F4),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              AppStatusChip(label: _strategyLabel(room.preferredStrategyId)),
              const AppStatusChip(
                label: '원문 미저장',
                tone: AppStatusChipTone.neutral,
              ),
            ],
          ),
          const SizedBox(height: 12),
          if (room.currentConcern != null)
            Text(
              room.currentConcern!,
              style: Theme.of(context).textTheme.titleLarge,
            )
          else
            Text(
              '아직 고민을 안 적었어도 괜찮아요. 바로 대화를 붙여넣어 보세요.',
              style: Theme.of(context).textTheme.bodyMedium,
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
      radius: 26,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const AppStatusChip(label: '분석 입력'),
          const SizedBox(height: 10),
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
          Text('원하면 이번 목적을 먼저 고를 수 있어요',
              style: Theme.of(context).textTheme.labelSmall),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              for (final strategy in _strategyOptions)
                ChoiceChip(
                  label: Text(strategy.label),
                  selected: strategy.id == strategyId,
                  onSelected: isSubmitting
                      ? null
                      : (_) => onStrategyChanged(strategy.id),
                ),
            ],
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

class _CoachEntryCard extends StatelessWidget {
  const _CoachEntryCard({required this.roomId});

  final String roomId;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(24),
      onTap: () => context.push('/rooms/$roomId/coach'),
      child: SectionCard(
        backgroundColor: const Color(0xFFFFF8F4),
        child: Row(
          children: [
            const Icon(Icons.forum_rounded, color: Color(0xFFC65F77)),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('코치와 대화', style: Theme.of(context).textTheme.titleMedium),
                  const SizedBox(height: 4),
                  Text(
                    '붙여넣기 말고, 지금 고민을 편하게 털어놓고 싶을 때',
                    style: Theme.of(context).textTheme.bodyMedium,
                  ),
                ],
              ),
            ),
            const Icon(Icons.chevron_right_rounded, color: Color(0xFF9B8A8E)),
          ],
        ),
      ),
    );
  }
}

class _ActionEntryCard extends StatelessWidget {
  const _ActionEntryCard({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.onTap,
  });

  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(24),
      onTap: onTap,
      child: SectionCard(
        backgroundColor: const Color(0xFFFFF8F4),
        child: Row(
          children: [
            Icon(icon, color: const Color(0xFFC65F77)),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(title, style: Theme.of(context).textTheme.titleMedium),
                  const SizedBox(height: 4),
                  Text(subtitle, style: Theme.of(context).textTheme.bodyMedium),
                ],
              ),
            ),
            const Icon(Icons.chevron_right_rounded, color: Color(0xFF9B8A8E)),
          ],
        ),
      ),
    );
  }
}

class _EmptyHistoryCard extends StatelessWidget {
  const _EmptyHistoryCard();

  @override
  Widget build(BuildContext context) {
    return SectionCard(
      backgroundColor: const Color(0xFFFFF8F4),
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
    required this.roomId,
    this.idealAxes,
  });

  final AnalysisTurn turn;
  final String roomId;

  /// 내 이상형 5축. 설정돼 있으면 상대 유형과의 적합도를 계산한다.
  final Map<String, int>? idealAxes;

  @override
  Widget build(BuildContext context) {
    final partnerType = PartnerType.fromStored(turn.partnerType);

    return SectionCard(
      radius: 26,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              AppStatusChip(label: _sourceTypeLabel(turn.sourceType)),
              AppStatusChip(
                label: _strategyLabel(turn.recommendedStrategyId),
                tone: AppStatusChipTone.warning,
              ),
            ],
          ),
          const SizedBox(height: 12),
          Text(turn.summary, style: Theme.of(context).textTheme.titleLarge),
          const SizedBox(height: 12),
          _InsightGrid(turn: turn),
          if (partnerType != null) ...[
            const SizedBox(height: 12),
            _PartnerTypeCard(partnerType: partnerType, idealAxes: idealAxes),
          ],
          const SizedBox(height: 14),
          _PrimaryReplyCard(reply: turn.primaryReply, roomId: roomId),
          if (turn.alternativeReplies.isNotEmpty) ...[
            const SizedBox(height: 14),
            Text('다른 톤', style: Theme.of(context).textTheme.labelSmall),
            const SizedBox(height: 8),
            for (final reply in turn.alternativeReplies.take(2)) ...[
              SectionCard(
                padding: const EdgeInsets.all(14),
                radius: 18,
                backgroundColor: const Color(0xFFFFF8F4),
                child:
                    Text(reply, style: Theme.of(context).textTheme.bodyMedium),
              ),
              const SizedBox(height: 8),
            ],
          ],
          const SizedBox(height: 14),
          _MiniInfoCard(
            label: '왜 이 답장인가',
            body: turn.replyReason,
          ),
          const SizedBox(height: 10),
          if (turn.warnings.isNotEmpty)
            _MiniInfoCard(
              label: '피해야 할 말',
              body: turn.warnings.map((warning) => '• $warning').join('\n'),
            ),
          const SizedBox(height: 10),
          _MiniInfoCard(label: '다음 행동', body: turn.nextAction),
        ],
      ),
    );
  }
}

class _PartnerTypeCard extends StatelessWidget {
  const _PartnerTypeCard({
    required this.partnerType,
    this.idealAxes,
  });

  final PartnerType partnerType;
  final Map<String, int>? idealAxes;

  @override
  Widget build(BuildContext context) {
    final ideal = idealAxes;
    final compatibility = ideal == null
        ? null
        : computeCompatibility(ideal: ideal, partner: partnerType.axes);

    return SectionCard(
      padding: const EdgeInsets.all(16),
      radius: 18,
      backgroundColor: const Color(0xFFFFF8F4),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const AppStatusChip(
                label: '상대 유형',
                tone: AppStatusChipTone.neutral,
              ),
              const Spacer(),
              if (compatibility != null)
                Text(
                  '${compatibility.score}%',
                  style: Theme.of(context).textTheme.bodySmall,
                ),
            ],
          ),
          const SizedBox(height: 10),
          if (compatibility != null) ...[
            Text(
              _matchLabel(compatibility.score),
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 6),
            Text(
              '이상형과 ${_gapPhrase(compatibility.biggestGap)}. 잘 맞는 축은 ${compatibility.bestMatch.axis.label}.',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
          ] else
            Text(
              '내 정보 > 연애 성향에서 이상형을 설정하면 이 상대와의 적합도까지 보여드려요.',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
          if (partnerType.summary != null) ...[
            const SizedBox(height: 8),
            Text(
              partnerType.summary!,
              style: Theme.of(context).textTheme.bodyMedium,
            ),
          ],
        ],
      ),
    );
  }
}

/// 적합도 점수를 정성 라벨로(숫자 단정 대신 부드러운 표현 — 안전 규칙).
String _matchLabel(int score) {
  if (score >= 75) return '잘 맞는 편이에요';
  if (score >= 50) return '어느 정도 맞아요';
  if (score >= 25) return '차이가 있는 편이에요';
  return '차이가 큰 편이에요';
}

String _gapPhrase(AxisGap gap) {
  if (gap.gap <= 1) return '대부분 잘 맞아요';
  return '${gap.axis.label}에서 차이가 보여요';
}

class _InsightGrid extends StatelessWidget {
  const _InsightGrid({required this.turn});

  final AnalysisTurn turn;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: _MiniInfoCard(
            label: '현재 상태',
            body: turn.currentState,
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: _MiniInfoCard(
            label: '분류',
            body: turn.participantSummary,
          ),
        ),
      ],
    );
  }
}

class _PrimaryReplyCard extends ConsumerStatefulWidget {
  const _PrimaryReplyCard({required this.reply, required this.roomId});

  final String reply;
  final String roomId;

  @override
  ConsumerState<_PrimaryReplyCard> createState() => _PrimaryReplyCardState();
}

class _PrimaryReplyCardState extends ConsumerState<_PrimaryReplyCard> {
  String? _refined;
  bool _loading = false;

  static const _directions = [
    ('LIGHTER', '더 가볍게'),
    ('MORE_SERIOUS', '더 진지하게'),
    ('SLOWER', '천천히'),
    ('BOLDER', '적극적으로'),
  ];

  Future<void> _refine(String direction) async {
    if (_loading) return;
    setState(() => _loading = true);
    try {
      final reply = await ref
          .read(roomsApiProvider)
          .refineReply(widget.roomId, widget.reply, direction);
      if (mounted) setState(() => _refined = reply);
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('다시 쓰지 못했어요. 잠시 후 다시 시도하세요.')),
        );
      }
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final shown = _refined ?? widget.reply;
    return SectionCard(
      backgroundColor: const Color(0xFF2A2024),
      borderColor: const Color(0xFF2A2024),
      radius: 22,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              AppStatusChip(label: _refined == null ? '1순위 답장' : '다시 쓴 답장'),
              const Spacer(),
              if (_loading)
                const SizedBox.square(
                  dimension: 16,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    color: Colors.white,
                  ),
                )
              else if (_refined != null)
                GestureDetector(
                  onTap: () => setState(() => _refined = null),
                  child: const Text(
                    '원래대로',
                    style: TextStyle(
                      color: Color(0xFFF6EDEE),
                      fontSize: 12,
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                ),
            ],
          ),
          const SizedBox(height: 12),
          Text(
            shown,
            style: const TextStyle(
              color: Colors.white,
              fontSize: 21,
              fontWeight: FontWeight.w900,
              height: 1.25,
              letterSpacing: -0.6,
            ),
          ),
          const SizedBox(height: 14),
          const Text(
            '다른 톤으로 다시',
            style: TextStyle(
              color: Color(0xFFB9A9AD),
              fontSize: 11,
              fontWeight: FontWeight.w900,
              letterSpacing: 1.2,
            ),
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              for (final (value, label) in _directions)
                GestureDetector(
                  onTap: _loading ? null : () => _refine(value),
                  child: DecoratedBox(
                    decoration: BoxDecoration(
                      color: const Color(0xFF2A2125),
                      borderRadius: BorderRadius.circular(999),
                    ),
                    child: Padding(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 12,
                        vertical: 8,
                      ),
                      child: Text(
                        label,
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 12,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                    ),
                  ),
                ),
            ],
          ),
        ],
      ),
    );
  }
}

class _MiniInfoCard extends StatelessWidget {
  const _MiniInfoCard({
    required this.label,
    required this.body,
  });

  final String label;
  final String body;

  @override
  Widget build(BuildContext context) {
    return SectionCard(
      padding: const EdgeInsets.all(14),
      radius: 18,
      backgroundColor: const Color(0xFFFFF8F4),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: Theme.of(context).textTheme.labelSmall),
          const SizedBox(height: 8),
          Text(body, style: Theme.of(context).textTheme.bodyMedium),
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

String _strategyLabel(String? value) {
  return switch (value) {
    'DEVELOP_ROMANCE' => '연애로 발전',
    'CHECK_RELATIONSHIP_STATUS' => '여친/남친 여부 확인',
    'MAKE_PLAN' => '약속 잡기',
    'MARRIAGE_VALUES' => '결혼 가치관',
    'SLOW_DOWN' => '속도 조절',
    _ => '전략 미정',
  };
}

const _strategyOptions = [
  _StrategyOption('DEVELOP_ROMANCE', '연애로 발전'),
  _StrategyOption('CHECK_RELATIONSHIP_STATUS', '여친/남친 확인'),
  _StrategyOption('MAKE_PLAN', '약속 잡기'),
  _StrategyOption('MARRIAGE_VALUES', '결혼 가치관'),
  _StrategyOption('SLOW_DOWN', '속도 조절'),
];

class _StrategyOption {
  const _StrategyOption(this.id, this.label);

  final String id;
  final String label;
}

import 'package:flutter/material.dart';

import '../../../core/widgets/app_status_chip.dart';
import '../../../core/widgets/screen_frame.dart';
import '../../../core/widgets/section_card.dart';
import '../domain/personality_axes.dart';
import '../domain/personality_models.dart';

/// 내 성향 + 이상형을 설정하는 화면.
///
/// 이번 단계는 **로컬 상태만** 유지한다. 백엔드 영속화와 대화 기반 상대 유형
/// 추론은 사용자 확인 후 다음 단계에서 붙인다(스펙 §5 정지선).
class PersonalitySetupScreen extends StatefulWidget {
  const PersonalitySetupScreen({super.key});

  @override
  State<PersonalitySetupScreen> createState() => _PersonalitySetupScreenState();
}

class _PersonalitySetupScreenState extends State<PersonalitySetupScreen> {
  late PersonalityProfile _profile = PersonalityProfile.neutral();

  /// 궁합 미리보기에 쓰는 예시 상대(실데이터 아님 — 대화 추론은 다음 단계).
  static const _examplePartner = {
    'expression': 4,
    'pace': 2,
    'contact': 4,
    'emotion': 3,
    'values': 5,
  };

  @override
  Widget build(BuildContext context) {
    final compatibility = computeCompatibility(
      ideal: _profile.ideal,
      partner: _examplePartner,
    );

    return ScreenFrame(
      title: '연애 성향',
      subtitle: '내 성향과 이상형을 정해두면 상대 유형과의 적합도를 보여주고, 추천 답장에도 반영합니다.',
      children: [
        _AxisGroup(
          title: '내 성향',
          caption: '나를 어떻게 표현하는지',
          values: _profile.self,
          onChanged: (axisId, value) {
            setState(() {
              _profile = _profile.copyWith(
                self: {..._profile.self, axisId: value},
              );
            });
          },
        ),
        const SizedBox(height: 12),
        _AxisGroup(
          title: '내 이상형',
          caption: '어떤 상대가 잘 맞는지',
          values: _profile.ideal,
          onChanged: (axisId, value) {
            setState(() {
              _profile = _profile.copyWith(
                ideal: {..._profile.ideal, axisId: value},
              );
            });
          },
        ),
        const SizedBox(height: 12),
        _CompatibilityPreviewCard(compatibility: compatibility),
        const SizedBox(height: 18),
        FilledButton(
          onPressed: () {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(
                content: Text('성향을 임시 저장했어요. 계정 저장 연동은 다음 단계예요.'),
              ),
            );
          },
          child: const Text('성향 저장'),
        ),
      ],
    );
  }
}

class _AxisGroup extends StatelessWidget {
  const _AxisGroup({
    required this.title,
    required this.caption,
    required this.values,
    required this.onChanged,
  });

  final String title;
  final String caption;
  final Map<String, int> values;
  final void Function(String axisId, int value) onChanged;

  @override
  Widget build(BuildContext context) {
    return SectionCard(
      radius: 26,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: Theme.of(context).textTheme.titleLarge),
          const SizedBox(height: 4),
          Text(caption, style: Theme.of(context).textTheme.bodyMedium),
          const SizedBox(height: 8),
          for (final axis in personalityAxes)
            _AxisSlider(
              axis: axis,
              value: values[axis.id] ?? 3,
              onChanged: (value) => onChanged(axis.id, value),
            ),
        ],
      ),
    );
  }
}

class _AxisSlider extends StatelessWidget {
  const _AxisSlider({
    required this.axis,
    required this.value,
    required this.onChanged,
  });

  final PersonalityAxis axis;
  final int value;
  final ValueChanged<int> onChanged;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Text(axis.label, style: Theme.of(context).textTheme.titleMedium),
              const Spacer(),
              AppStatusChip(label: poleLabelFor(axis, value)),
            ],
          ),
          Slider(
            value: value.toDouble(),
            min: personalityScoreMin.toDouble(),
            max: personalityScoreMax.toDouble(),
            divisions: personalityScoreMax - personalityScoreMin,
            label: poleLabelFor(axis, value),
            onChanged: (v) => onChanged(v.round()),
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(axis.lowLabel,
                  style: Theme.of(context).textTheme.bodySmall),
              Text(axis.highLabel,
                  style: Theme.of(context).textTheme.bodySmall),
            ],
          ),
        ],
      ),
    );
  }
}

class _CompatibilityPreviewCard extends StatelessWidget {
  const _CompatibilityPreviewCard({required this.compatibility});

  final Compatibility compatibility;

  @override
  Widget build(BuildContext context) {
    final best = compatibility.bestMatch;
    final gap = compatibility.biggestGap;

    return SectionCard(
      radius: 26,
      backgroundColor: const Color(0xFFFFF8F4),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const AppStatusChip(
                label: '예시 상대',
                tone: AppStatusChipTone.neutral,
              ),
              const Spacer(),
              Text(
                '${compatibility.score}%',
                style: const TextStyle(
                  color: Color(0xFFE43F5A),
                  fontSize: 30,
                  fontWeight: FontWeight.w900,
                  letterSpacing: -1,
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Text('이상형 적합도 미리보기',
              style: Theme.of(context).textTheme.titleLarge),
          const SizedBox(height: 8),
          Text(
            '실제 상대 유형은 상담방 대화 분석에서 추론합니다. 아래는 예시 상대 기준 미리보기예요.',
            style: Theme.of(context).textTheme.bodyMedium,
          ),
          const SizedBox(height: 12),
          _AxisHighlight(
            label: '잘 맞는 축',
            axisLabel: best.axis.label,
            tone: AppStatusChipTone.success,
          ),
          const SizedBox(height: 8),
          _AxisHighlight(
            label: '주의할 축',
            axisLabel: gap.axis.label,
            tone: AppStatusChipTone.warning,
          ),
          const SizedBox(height: 14),
          for (final axisGap in compatibility.perAxis)
            Padding(
              padding: const EdgeInsets.only(top: 10),
              child: _AxisGapBar(axisGap: axisGap),
            ),
        ],
      ),
    );
  }
}

/// 한 축에서 이상형(rose 테두리)과 상대(채움) 위치를 5칸 트랙으로 보여준다.
class _AxisGapBar extends StatelessWidget {
  const _AxisGapBar({required this.axisGap});

  final AxisGap axisGap;

  @override
  Widget build(BuildContext context) {
    final accent = Theme.of(context).colorScheme.primary;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Text(axisGap.axis.label,
                style: Theme.of(context).textTheme.bodyMedium),
            const Spacer(),
            Text(
              axisGap.gap == 0 ? '딱 맞음' : '차이 ${axisGap.gap}',
              style: Theme.of(context).textTheme.bodySmall,
            ),
          ],
        ),
        const SizedBox(height: 6),
        Row(
          children: [
            for (var score = personalityScoreMin;
                score <= personalityScoreMax;
                score++)
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 2),
                  child: _TrackCell(
                    isPartner: score == axisGap.partnerValue,
                    isIdeal: score == axisGap.idealValue,
                    accent: accent,
                  ),
                ),
              ),
          ],
        ),
      ],
    );
  }
}

class _TrackCell extends StatelessWidget {
  const _TrackCell({
    required this.isPartner,
    required this.isIdeal,
    required this.accent,
  });

  final bool isPartner;
  final bool isIdeal;
  final Color accent;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 10,
      decoration: BoxDecoration(
        color: isPartner ? accent : const Color(0xFFF1E4E0),
        borderRadius: BorderRadius.circular(999),
        border: isIdeal ? Border.all(color: accent, width: 2) : null,
      ),
    );
  }
}

class _AxisHighlight extends StatelessWidget {
  const _AxisHighlight({
    required this.label,
    required this.axisLabel,
    required this.tone,
  });

  final String label;
  final String axisLabel;
  final AppStatusChipTone tone;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        AppStatusChip(label: label, tone: tone),
        const SizedBox(width: 10),
        Text(axisLabel, style: Theme.of(context).textTheme.titleMedium),
      ],
    );
  }
}

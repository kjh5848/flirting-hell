import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/widgets/app_status_chip.dart';
import '../../../core/widgets/section_card.dart';
import '../../../data/models/room_models.dart';
import '../../../data/remote/rooms_api.dart';

final _planProvider =
    FutureProvider.family.autoDispose<DatePlan, String>((ref, roomId) {
  return ref.watch(roomsApiProvider).fetchPlan(roomId);
});

/// 데이트 플랜: 코스 + 식습관·취향으로 궁합을 가늠하는 확인 포인트.
class DatePlanScreen extends ConsumerWidget {
  const DatePlanScreen({required this.roomId, super.key});

  final String roomId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final planAsync = ref.watch(_planProvider(roomId));

    return Scaffold(
      appBar: AppBar(title: const Text('데이트 플랜')),
      body: SafeArea(
        child: planAsync.when(
          data: (plan) => _PlanView(plan: plan),
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (error, _) => _ErrorView(
            onRetry: () => ref.invalidate(_planProvider(roomId)),
          ),
        ),
      ),
    );
  }
}

class _PlanView extends StatelessWidget {
  const _PlanView({required this.plan});

  final DatePlan plan;

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 28),
      children: [
        const AppStatusChip(label: '추천 플랜'),
        const SizedBox(height: 10),
        Text(plan.theme, style: Theme.of(context).textTheme.headlineLarge),
        const SizedBox(height: 18),
        for (var i = 0; i < plan.steps.length; i++) ...[
          _StepCard(index: i + 1, step: plan.steps[i]),
          const SizedBox(height: 10),
        ],
        const SizedBox(height: 8),
        _ListCard(
          chipLabel: '궁합 확인',
          chipTone: AppStatusChipTone.success,
          title: '이걸 자연스럽게 확인해봐요',
          caption: '상대 식습관·취향을 알면 다음 약속도, 우리 궁합도 같이 보여요.',
          items: plan.checkPoints,
        ),
        const SizedBox(height: 10),
        _ListCard(
          chipLabel: '주의',
          chipTone: AppStatusChipTone.warning,
          title: '이건 피하기',
          items: plan.cautions,
        ),
      ],
    );
  }
}

class _StepCard extends StatelessWidget {
  const _StepCard({required this.index, required this.step});

  final int index;
  final PlanStep step;

  @override
  Widget build(BuildContext context) {
    return SectionCard(
      padding: const EdgeInsets.all(16),
      radius: 20,
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          DecoratedBox(
            decoration: BoxDecoration(
              color: const Color(0xFFF7EAED),
              borderRadius: BorderRadius.circular(12),
            ),
            child: SizedBox(
              width: 30,
              height: 30,
              child: Center(
                child: Text(
                  '$index',
                  style: TextStyle(
                    color: Theme.of(context).colorScheme.primary,
                    fontWeight: FontWeight.w900,
                  ),
                ),
              ),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(step.title, style: Theme.of(context).textTheme.titleMedium),
                const SizedBox(height: 4),
                Text(step.detail, style: Theme.of(context).textTheme.bodyMedium),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _ListCard extends StatelessWidget {
  const _ListCard({
    required this.chipLabel,
    required this.chipTone,
    required this.title,
    required this.items,
    this.caption,
  });

  final String chipLabel;
  final AppStatusChipTone chipTone;
  final String title;
  final String? caption;
  final List<String> items;

  @override
  Widget build(BuildContext context) {
    return SectionCard(
      radius: 22,
      backgroundColor: const Color(0xFFFFF8F4),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          AppStatusChip(label: chipLabel, tone: chipTone),
          const SizedBox(height: 10),
          Text(title, style: Theme.of(context).textTheme.titleLarge),
          if (caption != null) ...[
            const SizedBox(height: 6),
            Text(caption!, style: Theme.of(context).textTheme.bodyMedium),
          ],
          const SizedBox(height: 12),
          for (final item in items) ...[
            Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Padding(
                  padding: EdgeInsets.only(top: 7, right: 8),
                  child: Icon(Icons.circle, size: 5, color: Color(0xFFC65F77)),
                ),
                Expanded(
                  child: Text(item,
                      style: Theme.of(context).textTheme.bodyMedium),
                ),
              ],
            ),
            const SizedBox(height: 8),
          ],
        ],
      ),
    );
  }
}

class _ErrorView extends StatelessWidget {
  const _ErrorView({required this.onRetry});

  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(28),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('플랜을 불러오지 못했어요.',
                style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 12),
            OutlinedButton(onPressed: onRetry, child: const Text('다시 시도')),
          ],
        ),
      ),
    );
  }
}

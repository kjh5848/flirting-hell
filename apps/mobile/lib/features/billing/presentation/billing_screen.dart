import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/widgets/app_status_chip.dart';
import '../../../core/widgets/compact_list_tile_card.dart';
import '../../../core/widgets/screen_frame.dart';
import '../../../core/widgets/section_card.dart';
import '../../home/application/bootstrap_provider.dart';

class BillingScreen extends ConsumerWidget {
  const BillingScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final usage = ref.watch(bootstrapProvider).valueOrNull?.usage;
    final freeRemaining = usage?.freeRemaining;
    final creditBalance = usage?.creditBalance;
    final rewardAdRemaining = usage?.rewardAdRemaining;

    return ScreenFrame(
      title: '분석권',
      subtitle: '결제와 리워드 광고는 RevenueCat, AdMob 연동 전까지 화면 구조만 둡니다.',
      trailing: creditBalance == null
          ? null
          : AppStatusChip(
              label: '$creditBalance회',
              tone: AppStatusChipTone.neutral,
            ),
      children: [
        SectionCard(
          backgroundColor: const Color(0xFFFFF1F2),
          borderColor: const Color(0xFFFFE0E4),
          radius: 26,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('오늘 무료 분석', style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 12),
              Text(
                freeRemaining == null ? '— 회 남음' : '$freeRemaining회 남음',
                style: const TextStyle(
                  color: Color(0xFFE43F5A),
                  fontSize: 34,
                  fontWeight: FontWeight.w900,
                  height: 1,
                  letterSpacing: -1.2,
                ),
              ),
              const SizedBox(height: 10),
              Text(
                creditBalance == null
                    ? '실제 차감과 지급은 서버 ledger 기준으로 계산합니다.'
                    : '무료 분석을 모두 쓰면 보유 분석권 $creditBalance회에서 차감합니다.',
                style: Theme.of(context).textTheme.bodyMedium,
              ),
            ],
          ),
        ),
        const SizedBox(height: 14),
        const CompactListTileCard(
          leadingText: 'S',
          title: '스타터 분석권',
          subtitle: '가볍게 테스트하는 10회',
          trailingText: '₩3,900',
          showChevron: false,
        ),
        const SizedBox(height: 10),
        const CompactListTileCard(
          leadingText: 'F',
          title: '집중 분석권',
          subtitle: '여러 상담방을 관리하는 50회',
          trailingText: '₩14,900',
          showChevron: false,
        ),
        const SizedBox(height: 10),
        CompactListTileCard(
          leadingText: 'A',
          title: '리워드 광고',
          subtitle: rewardAdRemaining == null
              ? '광고 보고 1회 추가'
              : '광고 보고 1회 추가 · 오늘 $rewardAdRemaining회 가능',
          trailingText: '무료',
          showChevron: false,
        ),
        const SizedBox(height: 18),
        FilledButton(
          onPressed: () {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('결제 연동은 RevenueCat 연결 후 활성화됩니다.')),
            );
          },
          child: const Text('분석권 충전하기'),
        ),
      ],
    );
  }
}

import 'package:flutter/material.dart';

import '../../../core/widgets/screen_frame.dart';
import '../../../core/widgets/section_card.dart';

class BillingScreen extends StatelessWidget {
  const BillingScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return ScreenFrame(
      title: '분석권',
      subtitle: '결제와 리워드 광고는 RevenueCat, AdMob 연동 전까지 화면 구조만 둡니다.',
      children: [
        SectionCard(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('보유 분석권', style: Theme.of(context).textTheme.labelSmall),
              const SizedBox(height: 8),
              Text('3회', style: Theme.of(context).textTheme.headlineLarge),
              const SizedBox(height: 10),
              Text(
                '실제 차감과 지급은 서버 ledger 기준으로 계산합니다.',
                style: Theme.of(context).textTheme.bodyMedium,
              ),
            ],
          ),
        ),
      ],
    );
  }
}

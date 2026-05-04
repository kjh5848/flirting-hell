import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/widgets/screen_frame.dart';
import '../../../core/widgets/section_card.dart';
import '../../auth/application/auth_controller.dart';
import '../../home/application/bootstrap_provider.dart';

class ProfileScreen extends ConsumerWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final bootstrap = ref.watch(bootstrapProvider).valueOrNull;
    final profile = bootstrap?.user.profile;

    return ScreenFrame(
      title: '내 정보',
      subtitle: '내 말투, 연애 스타일, 조언 수위는 전체 상담방에 기본값으로 적용합니다.',
      children: [
        _ProfileSetting(title: '내 말투', value: profile?.speechStyle ?? '-'),
        const SizedBox(height: 10),
        _ProfileSetting(title: '연애 스타일', value: profile?.datingStyle ?? '-'),
        const SizedBox(height: 10),
        _ProfileSetting(title: '조언 수위', value: profile?.guidanceLevel ?? '-'),
        const SizedBox(height: 18),
        OutlinedButton(
          onPressed: () => ref.read(authControllerProvider).signOut(),
          child: const Text('로그아웃'),
        ),
      ],
    );
  }
}

class _ProfileSetting extends StatelessWidget {
  const _ProfileSetting({
    required this.title,
    required this.value,
  });

  final String title;
  final String value;

  @override
  Widget build(BuildContext context) {
    return SectionCard(
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: Theme.of(context).textTheme.labelSmall),
                const SizedBox(height: 6),
                Text(value, style: Theme.of(context).textTheme.titleMedium),
              ],
            ),
          ),
          const Icon(Icons.chevron_right_rounded),
        ],
      ),
    );
  }
}

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/widgets/screen_frame.dart';
import '../../../core/widgets/section_card.dart';
import '../application/auth_controller.dart';

class AuthScreen extends ConsumerWidget {
  const AuthScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return ScreenFrame(
      title: '로그인',
      subtitle: 'Phase 2에서는 실제 Firebase 연결 전, 보호 화면과 bootstrap 흐름을 먼저 검증합니다.',
      children: [
        SectionCard(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('개발용 로그인', style: Theme.of(context).textTheme.labelSmall),
              const SizedBox(height: 10),
              Text('앱 진입 흐름 확인',
                  style: Theme.of(context).textTheme.headlineLarge),
              const SizedBox(height: 12),
              Text(
                '실제 Apple, Google, Kakao 로그인은 Firebase 설정이 들어온 뒤 연결합니다.',
                style: Theme.of(context).textTheme.bodyMedium,
              ),
              const SizedBox(height: 18),
              FilledButton(
                onPressed: () =>
                    ref.read(authControllerProvider).signInDevelopment(),
                child: const Text('개발용 로그인'),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

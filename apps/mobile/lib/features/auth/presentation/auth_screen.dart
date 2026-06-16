import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/widgets/app_status_chip.dart';
import '../../../core/widgets/screen_frame.dart';
import '../../../core/widgets/section_card.dart';
import '../application/auth_controller.dart';

class AuthScreen extends ConsumerWidget {
  const AuthScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final auth = ref.watch(authControllerProvider);

    return ScreenFrame(
      title: '시작하기',
      subtitle: '먼저 익명으로 시작하고, 앱 흐름이 안정되면 Apple/Google/Kakao 로그인을 붙입니다.',
      children: [
        SectionCard(
          radius: 26,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const AppStatusChip(label: '익명 시작'),
              const SizedBox(height: 10),
              Text('개인정보 없이 먼저 써보기',
                  style: Theme.of(context).textTheme.headlineLarge),
              const SizedBox(height: 12),
              Text(
                'MVP에서는 Firebase 익명 인증으로 상담방과 분석 흐름을 먼저 검증합니다.',
                style: Theme.of(context).textTheme.bodyMedium,
              ),
              if (auth.errorMessage != null) ...[
                const SizedBox(height: 12),
                Text(
                  auth.errorMessage!,
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                        color: Theme.of(context).colorScheme.error,
                      ),
                ),
              ],
              const SizedBox(height: 18),
              FilledButton(
                onPressed: auth.isSigningIn
                    ? null
                    : () =>
                        ref.read(authControllerProvider).signInAnonymously(),
                child: Text(auth.isSigningIn ? '로그인 중' : 'Firebase로 시작'),
              ),
              const SizedBox(height: 10),
              OutlinedButton(
                onPressed: auth.isSigningIn
                    ? null
                    : () =>
                        ref.read(authControllerProvider).signInDevelopment(),
                child: const Text('로컬 개발용 로그인'),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

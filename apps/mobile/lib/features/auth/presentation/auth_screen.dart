import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/widgets/screen_frame.dart';
import '../../../core/widgets/section_card.dart';
import '../application/auth_controller.dart';

class AuthScreen extends ConsumerWidget {
  const AuthScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final auth = ref.watch(authControllerProvider);

    return ScreenFrame(
      title: '로그인',
      subtitle:
          'Firebase Auth로 사용자를 식별하고, 서버 bootstrap API로 내 설정과 상담방 상태를 불러옵니다.',
      children: [
        SectionCard(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('Firebase Auth',
                  style: Theme.of(context).textTheme.labelSmall),
              const SizedBox(height: 10),
              Text('익명 로그인으로 먼저 시작',
                  style: Theme.of(context).textTheme.headlineLarge),
              const SizedBox(height: 12),
              Text(
                'Apple, Google, Kakao 로그인 전에는 Firebase 익명 로그인을 사용해 서버 인증 흐름을 검증합니다.',
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

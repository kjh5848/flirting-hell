import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

class SplashScreen extends StatelessWidget {
  const SplashScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Spacer(),
              Text('플러팅지옥', style: Theme.of(context).textTheme.headlineLarge),
              const SizedBox(height: 14),
              Text(
                '대화나 상황을 붙여넣고, 지금 필요한 답장과 전략을 확인하세요.',
                style: Theme.of(context).textTheme.bodyMedium,
              ),
              const Spacer(),
              FilledButton(
                onPressed: () => context.go('/auth'),
                child: const Text('플러팅지옥 시작'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

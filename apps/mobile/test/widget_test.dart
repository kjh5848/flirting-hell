import 'package:flirting_hell/app/app.dart';
import 'package:flirting_hell/data/models/bootstrap_snapshot.dart';
import 'package:flirting_hell/features/home/application/bootstrap_provider.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

void main() {
  testWidgets('protects app shell until local development sign in',
      (tester) async {
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          bootstrapProvider.overrideWith((ref) async {
            return const BootstrapSnapshot(
              user: BootstrapUser(
                userId: 'usr_test',
                onboardingCompleted: false,
                profile: BootstrapProfile(
                  nickname: '테스터',
                  speechStyle: '짧고 자연스럽게',
                  datingStyle: '천천히 확인하는 편',
                  guidanceLevel: 'BALANCED',
                  preferredPartnerStyle: '다정하게 표현하는 사람',
                  avoidAdvice: '단정하거나 압박하는 조언은 피하기',
                ),
              ),
              usage: BootstrapUsage(
                creditBalance: 0,
                freeRemaining: 3,
                rewardAdRemaining: 3,
              ),
              recentRooms: [],
            );
          }),
        ],
        child: const FlirtingHellApp(),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('플러팅지옥'), findsOneWidget);
    expect(find.text('플러팅지옥 시작'), findsOneWidget);

    await tester.tap(find.text('플러팅지옥 시작'));
    await tester.pumpAndSettle();
    expect(find.text('Firebase Auth'), findsOneWidget);

    await tester.tap(find.widgetWithText(OutlinedButton, '로컬 개발용 로그인'));
    await tester.pumpAndSettle();

    expect(find.text('오늘 이어갈 상담방'), findsOneWidget);
    expect(find.text('상담방 보기'), findsOneWidget);
  });
}

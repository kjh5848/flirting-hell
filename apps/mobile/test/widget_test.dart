import 'package:flirting_hell/app/app.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

void main() {
  testWidgets('protects app shell until development sign in', (tester) async {
    await tester.pumpWidget(
      const ProviderScope(
        child: FlirtingHellApp(),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('플러팅지옥'), findsOneWidget);
    expect(find.text('플러팅지옥 시작'), findsOneWidget);

    await tester.tap(find.text('플러팅지옥 시작'));
    await tester.pumpAndSettle();
    expect(find.text('개발용 로그인'), findsWidgets);

    await tester.tap(find.widgetWithText(FilledButton, '개발용 로그인'));
    await tester.pumpAndSettle();

    expect(find.text('오늘 이어갈 상담방'), findsOneWidget);
    expect(find.text('상담방 보기'), findsOneWidget);
  });
}

# 플러팅지옥 Flutter 앱

이 앱은 플러팅지옥의 iOS/Android 최종 사용자 제품이다.

## 현재 범위

Phase 1 scaffold:

- Flutter iOS/Android 프로젝트
- Riverpod ProviderScope
- go_router 기반 하단 탭 shell
- 홈, 상담방, 저장, 내 정보, 분석권 기본 화면
- 앱 디자인 토큰 기반 light theme

아직 포함하지 않는다:

- 실제 로그인
- 실제 API 호출
- Drift 로컬 DB
- RevenueCat 결제
- AdMob 리워드 광고
- ChannelTalk 고객상담

## 실행

```bash
flutter run
flutter analyze
flutter test
```

루트에서는 다음 명령을 사용한다:

```bash
npm run dev:mobile
npm run analyze:mobile
npm run test:mobile
```

상세 기준은 `../../docs/technical/flutter-app-tech-spec.md`를 따른다.

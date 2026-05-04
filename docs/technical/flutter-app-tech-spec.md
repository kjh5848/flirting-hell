# 플러팅지옥 Flutter 앱 기술 스펙

## 목적

이 문서는 플러팅지옥 iOS/Android 앱의 Flutter 구현 기준을 정의한다.

상위 결정:

- `docs/decisions/0005-native-app-spring-stack.md`
- `docs/technical/native-app-architecture.md`

Flutter 앱은 최종 사용자 제품 표면이다. React 웹은 랜딩, 정책, 고객지원, 운영 화면과 프로토타입 레퍼런스로 유지한다.

## 앱 책임

Flutter 앱이 담당한다:

- 소셜 로그인
- 상담방 목록/상세
- 카톡, DM, 문자, 상황 설명 붙여넣기
- iOS/Android 공유받기
- 대화/상황 원문 로컬 저장
- 분석 결과 표시
- 추천 답장 복사와 상담방별 저장
- 인앱결제 구매/복원
- 리워드 광고 시청
- ChannelTalk 고객상담 진입
- 계정 삭제 요청과 로컬 데이터 삭제

Flutter 앱이 직접 하지 않는다:

- AI API 직접 호출
- AI API 키 보관
- 결제 권한 최종 판단
- 리워드 광고 보상 최종 지급
- 서버 원문 대화 장기 저장
- 관리자 기능

## 기술 스택

| 영역 | 선택 | 이유 |
|---|---|---|
| 앱 프레임워크 | Flutter | iOS/Android 단일 코드베이스 |
| 언어 | Dart | Flutter 표준 |
| 상태관리 | Riverpod | 기능별 상태 분리와 테스트 용이성 |
| 라우팅 | go_router | 로그인/탭/딥링크/공유 진입 처리 |
| HTTP | Dio | Interceptor로 Firebase token, 오류 처리, retry 관리 |
| 모델 | freezed + json_serializable | 불변 모델과 API DTO 안정성 |
| 로컬 DB | Drift + SQLite | 원문 로컬 보관, 상담방 캐시, 오프라인 조회 |
| 보안 저장 | flutter_secure_storage | 토큰 보조 정보, 민감 설정 |
| 인증 | Firebase Auth | Apple, Google, Kakao 로그인 연결 |
| Kakao 로그인 | kakao_flutter_sdk_user | Kakao OAuth/OIDC 진입 |
| 결제 | purchases_flutter | RevenueCat 기반 인앱결제 |
| 광고 | google_mobile_ads | AdMob 리워드 광고 |
| 고객상담 | ChannelTalk Flutter SDK | 로그인 사용자 기준 상담 연결 |
| 공유받기 | receive_sharing_intent + native extension | Android 공유 인텐트, iOS Share Extension |
| 클립보드 | Flutter Clipboard API | 답장 복사 |

## 앱 폴더 구조

```text
apps/mobile/
  lib/
    main.dart
    app/
      app.dart
      router.dart
      bootstrap.dart
      env.dart
    core/
      network/
      storage/
      auth/
      analytics/
      errors/
      theme/
      widgets/
    features/
      auth/
      home/
      rooms/
      intake/
      analysis/
      saved_replies/
      billing/
      rewards/
      profile/
      support/
      settings/
    data/
      local/
      remote/
      models/
      repositories/
  test/
  integration_test/
```

규칙:

- `features/*`는 화면, 상태, feature repository facade를 가진다.
- `data/remote`는 Spring API DTO와 client만 둔다.
- `data/local`은 Drift database, DAO, local entity만 둔다.
- `core/network`는 Dio client와 auth interceptor를 둔다.
- `core/theme`는 디자인 토큰과 공통 타이포/색상/컴포넌트 기준을 둔다.
- feature 화면은 다른 feature 내부 파일을 직접 import하지 않는다. 공통 모델은 `data/models` 또는 `core`로 올린다.

## 라우팅 구조

라우팅은 `go_router`로 관리한다.

```text
/splash
/auth
/onboarding
/home
/rooms
/rooms/new
/rooms/:roomId
/rooms/:roomId/settings
/rooms/:roomId/intake
/rooms/:roomId/analysis
/rooms/:roomId/result/:turnId
/saved
/profile
/billing
/support
/settings
/settings/delete-account
/import/share
```

탭 구조:

| 탭 | route | 역할 |
|---|---|---|
| 홈 | `/home` | 오늘 이어갈 상담방, 사용량, 빠른 진입 |
| 상담방 | `/rooms` | 상대별 상담방 목록 |
| 저장 | `/saved` | 상담방별 저장 답장 |
| 내 정보 | `/profile` | 말투, 연애 스타일, 조언 수위 |
| 분석권 | `/billing` | 분석권, 인앱결제, 광고 보상 |

탭을 숨기는 집중 화면:

- `/rooms/:roomId/intake`
- `/rooms/:roomId/analysis`
- `/rooms/:roomId/result/:turnId`
- `/import/share`
- `/settings/delete-account`

이 화면들은 한 턴을 처리하는 중이므로 하단 탭 대신 상단 뒤로가기와 주 CTA만 둔다.

## 상태관리 구조

Riverpod provider는 기능별로 분리한다.

| Provider | 책임 |
|---|---|
| `authStateProvider` | Firebase 로그인 상태 |
| `bootstrapProvider` | 사용자, 분석권, 최근 상담방 초기 로딩 |
| `roomsProvider` | 상담방 목록 |
| `roomDetailProvider(roomId)` | 개별 상담방 상세, 저장 입력, 저장 답장 |
| `intakeDraftProvider(roomId)` | 붙여넣기/공유받기 임시 입력 |
| `analysisTurnProvider(roomId)` | 분석 요청, 로딩, 인사이트, 답장 결과 |
| `savedRepliesProvider` | 상담방별 저장 답장 그룹 |
| `billingProvider` | RevenueCat 상품, 구매, 복원 상태 |
| `rewardAdProvider` | 리워드 광고 로드/시청/보상 요청 |
| `profileProvider` | 내 말투, 연애 스타일, 조언 수위 |
| `supportProvider` | ChannelTalk boot/memberHash |

원칙:

- 화면은 provider 상태를 읽고 action을 호출한다.
- API 호출은 repository가 담당한다.
- repository는 remote data source와 local DAO를 조합한다.
- 서버 응답이 성공하면 로컬 캐시를 갱신한다.
- 실패 시 사용자에게 재시도 가능한 오류를 보여준다.

## 로컬 저장 정책

로컬 DB는 Drift + SQLite를 사용한다.

로컬 저장 대상:

- 사용자가 붙여넣은 원문
- 공유받은 원문
- 상담방 캐시
- 입력 요약 캐시
- 답장 결과 캐시
- 저장 답장 캐시
- 마지막 동기화 시각

서버로 보내지 않는 기본값:

- 카톡/DM/문자 원문 전문
- 전화번호, 주소, 실명처럼 상대를 특정할 수 있는 값

로컬 테이블 초안:

| 테이블 | 목적 |
|---|---|
| `local_rooms` | 상담방 캐시 |
| `local_intake_drafts` | 원문 임시/명시 저장 |
| `local_turns` | 분석 턴 캐시 |
| `local_saved_replies` | 저장 답장 캐시 |
| `local_sync_state` | 동기화 상태 |

중요 정책:

- 분석 요청 시 원문은 Spring API로 전송되지만 서버 장기 저장 대상이 아니다.
- 서버 저장 후 앱은 서버 `summary_id`, `turn_id`를 로컬 원문과 연결한다.
- 계정 삭제 시 로컬 DB도 삭제한다.
- 로그에는 원문을 남기지 않는다.

## 인증 플로우

앱은 Firebase Auth를 사용한다.

로그인 제공자:

- Apple
- Google
- Kakao

Kakao 처리 기본값:

```text
Flutter Kakao SDK
→ Kakao access token 획득
→ Spring Boot /api/auth/kakao/exchange
→ Spring Boot가 Kakao 토큰 검증
→ Firebase custom token 발급 또는 계정 연결 처리
→ Flutter signInWithCustomToken
→ Firebase ID token으로 앱 API 호출
```

API 호출 인증:

```http
Authorization: Bearer <Firebase ID token>
```

Dio interceptor 책임:

- 요청 전 Firebase ID token 첨부
- 401 응답 시 token refresh 후 1회 재시도
- 재시도 실패 시 로그인 화면으로 이동

## 공유받기 플로우

앱 전용 제품의 핵심 차별점이다.

Android:

- `ACTION_SEND` 텍스트 공유 인텐트 수신
- 카톡/DM/문자에서 공유된 텍스트를 앱으로 전달

iOS:

- Share Extension으로 텍스트 수신
- App Group 또는 URL scheme으로 메인 앱에 전달

공통 UX:

```text
외부 앱에서 공유
→ 플러팅지옥 열림
→ /import/share
→ 텍스트 미리보기
→ 상담방 선택 또는 새 상담방 만들기
→ 개인정보 삭제 안내
→ 대화/상황 붙여넣기 화면
→ 분석 시작
```

공유받은 텍스트는 자동으로 분석하지 않는다. 사용자가 상담방과 개인정보 삭제 안내를 확인한 뒤 분석한다.

## 화면 구조

기준 흐름:

```text
Splash
→ Auth
→ Onboarding
→ Home
→ Rooms
→ Room Detail
→ Intake
→ Analysis Insight
→ Reply Result
→ Save to Room
```

주요 화면 책임:

| 화면 | 책임 |
|---|---|
| Splash | 앱 초기화, 로그인 상태 확인, 부트스트랩 |
| Auth | Apple/Google/Kakao 로그인 |
| Onboarding | 내 말투, 연애 스타일, 조언 수위 최소 설정 |
| Home | 최근 상담방, 분석권, 빠른 CTA |
| Rooms | 상담방 목록 |
| Room Detail | 상대 상태, 저장 입력, 저장 답장, 붙여넣기 CTA |
| Intake | 대화/상황 입력, 자동 감지, 개인정보 안내 |
| Analysis Insight | 입력 분류/요약, 현재 상태, 추천 전략 |
| Reply Result | 1순위 답장, 다른 톤, 피해야 할 말, 다음 행동 |
| Saved | 상담방별 저장 답장 |
| Billing | 분석권 상품, 구매 복원, 리워드 광고 |
| Profile | 전역 말투/연애 스타일/조언 수위 |
| Support | ChannelTalk 상담 |
| Delete Account | 계정/서버 요약/로컬 데이터 삭제 |

## 디자인 시스템

Flutter 디자인은 기존 `DESIGN.md`의 방향을 따르되 앱 컴포넌트로 재정의한다.

기준:

- warm off-white 배경
- rose/coral accent
- 큰 카드와 넓은 여백
- 상담방 중심 구조
- AI 콘솔, 스캐너, SF 대시보드 금지
- 특정 메신저 복제 금지

Flutter 토큰 파일:

```text
lib/core/theme/app_colors.dart
lib/core/theme/app_typography.dart
lib/core/theme/app_spacing.dart
lib/core/theme/app_radius.dart
lib/core/theme/app_theme.dart
```

핵심 공통 위젯:

- `AppScaffold`
- `AppTopBar`
- `AppBottomNav`
- `PrimaryButton`
- `SecondaryButton`
- `AppCard`
- `RoomRow`
- `SavedReplyCard`
- `IntakeTextField`
- `InsightCard`
- `ReplyResultCard`
- `PrivacyNotice`
- `LoadingState`
- `ErrorState`

## Spring API 연동

Flutter 앱은 Spring API만 호출한다.

초기 API:

- `GET /api/me/bootstrap`
- `GET /api/rooms`
- `POST /api/rooms`
- `GET /api/rooms/{roomId}`
- `PATCH /api/rooms/{roomId}`
- `POST /api/rooms/{roomId}/analyses`
- `POST /api/rooms/{roomId}/reply-turns`
- `GET /api/saved-replies`
- `GET /api/billing/products`
- `POST /api/billing/revenuecat/sync`
- `POST /api/rewards/admob`
- `POST /api/channel/member-hash`
- `DELETE /api/me`

계약은 `docs/technical/app-api-spec-v2.md`에서 확정한다.

## 인앱결제

RevenueCat SDK를 사용한다.

상품:

- `analysis_10`
- `analysis_30`
- `analysis_100`

앱 책임:

- 상품 목록 표시
- 구매 시작
- 구매 복원
- 구매 결과 UX 표시
- 구매 후 서버 bootstrap 재조회

서버 책임:

- RevenueCat webhook 수신
- 중복 이벤트 방지
- PostgreSQL `credit_ledger` 적립
- 최종 분석권 잔액 계산

앱은 RevenueCat `CustomerInfo`만 믿고 분석권을 확정하지 않는다. 최종 잔액은 Spring API 응답 기준이다.

## 리워드 광고

AdMob 리워드 광고를 사용한다.

UX:

```text
분석권 부족
→ 리워드 광고 보기 CTA
→ 광고 시청 완료
→ Spring API에 reward claim
→ 서버에서 idempotency 확인
→ 분석권 +1 지급
```

정책:

- 광고 실패 시 분석권을 지급하지 않는다.
- 같은 reward event는 한 번만 지급한다.
- 하루 리워드 광고 횟수 제한은 Spring 서버 정책으로 관리한다.
- 연애 상담 UX를 해치지 않도록 배너/전면 광고는 MVP에서 제외한다.

## ChannelTalk 연동

흐름:

```text
앱 로그인
→ Spring API /api/channel/member-hash
→ memberId, memberHash 수신
→ ChannelTalk boot
→ 고객상담 열기
```

ChannelTalk에 넘기는 값:

- `memberId`: 앱 사용자 ID
- `memberHash`: Spring에서 발급
- `profile`: 가입일, 분석권 상태, 앱 버전 정도의 최소 정보

원문 대화나 민감한 연애 상담 내용은 ChannelTalk profile에 넣지 않는다.

## 오류 처리

공통 오류 화면/토스트:

| 상황 | 처리 |
|---|---|
| 네트워크 오류 | 재시도 CTA |
| 인증 만료 | 토큰 갱신 후 재시도, 실패 시 로그인 |
| 분석권 부족 | 구매/리워드 광고 CTA |
| AI 응답 지연 | 재시도 또는 나중에 확인 안내 |
| 안전 정책 차단 | 존중/동의 기준 안내 |
| 공유 텍스트 없음 | 붙여넣기 화면으로 이동 |
| 계정 삭제 실패 | 재시도와 고객지원 안내 |

## 테스트 전략

Widget test:

- 로그인 전/후 라우팅
- 하단 탭 노출/숨김
- 상담방 목록/상세
- 대화 붙여넣기
- 분석 결과 카드
- 상담방별 저장 답장
- 분석권 부족 CTA

Unit test:

- repository 상태 전환
- Drift DAO 저장/삭제
- API DTO parse
- Firebase token interceptor
- RevenueCat 상품 상태 mapping
- reward claim 중복 방지 client guard

Integration test:

- 로그인 후 bootstrap
- 상담방 생성
- 대화 붙여넣기 → 분석 → 답장 저장
- 저장 탭에서 상담방별 답장 확인
- 구매 복원 후 잔액 갱신
- 계정 삭제 후 로컬 DB 삭제

수동 기기 테스트:

- iOS Share Extension
- Android 공유 인텐트
- Apple 로그인
- Google 로그인
- Kakao 로그인
- RevenueCat sandbox 구매
- AdMob test reward ad
- ChannelTalk boot

## 개발 순서

1. Flutter 프로젝트 scaffold
2. theme, router, app shell 구성
3. Firebase Auth 연결
4. Spring bootstrap API mock 연동
5. 상담방 목록/상세 화면
6. 로컬 Drift DB
7. 대화/상황 붙여넣기
8. 공유받기 Android/iOS
9. 분석 인사이트/답장 결과 화면
10. 상담방별 저장 답장
11. RevenueCat 인앱결제
12. AdMob 리워드 광고
13. ChannelTalk
14. 계정 삭제
15. 스토어 제출 준비

## 다음 문서

이 문서 다음에는 `docs/technical/spring-backend-tech-spec.md`를 작성한다.

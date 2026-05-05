# Phase 2 인증과 Bootstrap 구현 메모

## 목적

Phase 2는 로그인한 앱 사용자가 서버 사용자, 전역 profile, 분석권 상태, 최근 상담방을 한 번에 받을 수 있게 만드는 단계다.

## 이번 구현 범위

외부 계정 설정 없이 검증 가능한 skeleton을 먼저 둔다.

- Spring 보호 API 인증 경로
- 개발용 Bearer token 검증 규칙
- Firebase Admin SDK 기반 ID token 검증 adapter
- FlutterFire Android/iOS 앱 등록과 Firebase client bootstrap
- `GET /api/me/bootstrap`
- `PATCH /api/me/profile`
- identity/profile/usage DB migration 초안
- Flutter 로그인 전/후 route guard
- Flutter bootstrap mock provider

## 개발용 인증 규칙

로컬 개발에서는 다음 형식의 token만 통과한다.

```http
Authorization: Bearer dev:<firebaseUid>
```

예시:

```http
Authorization: Bearer dev:local-user-1
```

이 규칙은 실제 Firebase 검증을 대체하기 위한 것이 아니라, Firebase Admin 설정 전에도 보호 API와 앱 route guard를 검증하기 위한 임시 adapter다.

## 아직 하지 않은 것

- Kakao access token 검증
- Firebase custom token 발급
- Firebase ID token refresh 재시도
- Apple/Google/Kakao 버튼 연결

## 다음 작업

1. Firebase Auth ID token mode에서 백엔드 `local,firebase` 프로필 연동 검증
2. Kakao token exchange 실제 adapter 구현
3. Apple/Google/Kakao 로그인 버튼 연결

## FlutterFire client 설정

Firebase CLI 로그인 후 FlutterFire CLI로 Firebase 프로젝트 `flirting-hell`에 Android/iOS 앱을 등록했다.

```bash
dart pub global activate flutterfire_cli
flutterfire configure \
  --project=flirting-hell \
  --platforms=android,ios \
  --android-package-name=com.flirtinghell.flirting_hell \
  --ios-bundle-id=com.flirtinghell.flirtingHell \
  --out=lib/firebase_options.dart
```

생성된 파일:

- `apps/mobile/lib/firebase_options.dart`
- `apps/mobile/android/app/google-services.json`
- `apps/mobile/ios/Runner/GoogleService-Info.plist`
- `apps/mobile/firebase.json`

앱 시작 시 `AppBootstrap.initialize()`에서 `Firebase.initializeApp()`을 호출한다.

Android `firebase_auth`가 SDK 23 이상을 요구하므로 앱의 `minSdk`는 `23`으로 고정한다.

로컬 Flutter SDK가 `3.22.2`이므로 Firebase Flutter 패키지는 최신 major가 아니라 현재 Android Gradle/Kotlin scaffold와 빌드 검증이 되는 버전으로 고정한다.

- `firebase_core`: `3.1.1`
- `firebase_auth`: `5.1.1`
- `dio`: `5.9.2`

## Flutter Auth와 API bootstrap 연결

앱 로그인 화면은 두 경로를 둔다.

| 버튼 | 용도 | 서버 토큰 |
|---|---|---|
| Firebase로 시작 | Firebase Auth 익명 로그인으로 실제 Firebase 사용자 uid 생성 | 기본 로컬에서는 `dev:<uid>` |
| 로컬 개발용 로그인 | Firebase 없이 앱 shell과 보호 라우트 빠른 검증 | `dev:local-user-1` |

Dio client는 `AppEnv.apiBaseUrl`로 API를 호출하고, 요청 전에 `AuthController.authorizationToken()` 값을 `Authorization: Bearer ...` 헤더로 붙인다.

기본값은 로컬 Spring 기본 프로필과 맞추기 위해 개발용 token을 사용한다.

```bash
flutter run \
  --dart-define=API_BASE_URL=http://localhost:8080/api \
  --dart-define=USE_DEVELOPMENT_AUTH_TOKEN=true
```

Firebase Admin SDK로 실제 ID token을 검증하려면 서버는 `local,firebase` 프로필로 실행하고 앱은 개발용 token을 끈다.

```bash
flutter run \
  --dart-define=API_BASE_URL=http://localhost:8080/api \
  --dart-define=USE_DEVELOPMENT_AUTH_TOKEN=false
```

Android emulator에서 로컬 Mac의 Spring 서버를 호출할 때는 `localhost` 대신 `10.0.2.2`를 사용한다.

주의:

- 위 파일들은 모바일 앱 식별용 Firebase client 설정이다.
- Firebase Admin 서비스 계정 JSON은 client 설정과 다르며, repo에 저장하지 않는다.

## PostgreSQL local 실행

기본 프로필은 DB 없이 in-memory adapter를 사용한다. 로컬 DB 검증이 필요할 때만 `local` 프로필을 켠다.

```bash
npm run dev:db
npm run dev:backend:local
```

프로젝트 전용 PostgreSQL은 다른 로컬 DB와 충돌하지 않도록 호스트 `55432` 포트를 사용한다.

local 프로필에서 활성화되는 것:

- PostgreSQL datasource
- Flyway migration
- JPA repository adapter

기본 프로필에서 유지되는 것:

- DB auto-configuration 제외
- in-memory repository adapter
- 빠른 단위/컨트롤러 테스트

## Firebase Admin profile 실행

Firebase 실제 ID token 검증은 `firebase` 프로필에서만 활성화한다.

```bash
export FLIRTING_HELL_FIREBASE_PROJECT_ID="firebase-project-id"

# 방법 A: 서비스 계정 JSON을 base64로 주입한다.
export FLIRTING_HELL_FIREBASE_SERVICE_ACCOUNT_BASE64="$(base64 -i /absolute/path/to/service-account.json)"

# 방법 B: 서비스 계정 JSON 파일 경로를 주입한다.
export FLIRTING_HELL_FIREBASE_SERVICE_ACCOUNT_PATH="/absolute/path/to/service-account.json"
npm run dev:backend:firebase
```

두 값을 모두 설정하면 `FLIRTING_HELL_FIREBASE_SERVICE_ACCOUNT_BASE64`가 우선한다.

프로필별 인증 adapter:

| 프로필 | 인증 adapter | 용도 |
|---|---|---|
| `default` | dev token verifier | DB 없이 빠른 로컬 개발 |
| `local` | dev token verifier | PostgreSQL/Flyway/JPA 검증 |
| `local,firebase` | Firebase Admin verifier | 실제 Firebase ID Token 검증 |

서비스 계정 JSON은 repo에 저장하지 않는다.

앱에서 실제 Firebase ID token을 보내는 Android emulator 실행 명령:

```bash
npm run dev:mobile:firebase
```

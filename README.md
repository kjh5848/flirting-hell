# 플러팅지옥

플러팅지옥은 사용자가 카톡, DM, 문자, 상황 설명을 붙여넣고 대화 흐름과 답장 전략을 확인하는 연애 커뮤니케이션 코칭 앱이다.

## 현재 구조

```text
apps/web       React 웹 프로토타입, 랜딩/정책/운영 화면 후보
apps/api       기존 Cloudflare Workers MVP API
apps/backend   Spring Boot 앱 전용 API
apps/mobile    Flutter iOS/Android 앱
contracts      앱과 서버가 공유할 API 계약
docs           제품/기술/결정 문서
packages       기존 TypeScript shared package
```

## 실행 명령

### 웹

```bash
npm run dev:web
npm run build:web
```

### 기존 Workers API

```bash
npm run dev:api
npm run build:api
```

### Spring 백엔드

```bash
npm run dev:db
npm run dev:backend
npm run dev:backend:local
npm run dev:backend:firebase
npm run test:backend
npm run build:backend
```

Spring 백엔드 health check:

```bash
curl http://localhost:8080/api/health
```

`dev:backend`는 DB 없이 in-memory adapter로 실행한다.
`dev:backend:local`은 `docker-compose.yml`의 PostgreSQL과 Flyway/JPA를 사용한다.
프로젝트 전용 PostgreSQL 호스트 포트는 `55432`다.
`dev:backend:firebase`는 PostgreSQL과 Firebase Admin SDK를 함께 사용한다.

Firebase Admin SDK 실행 전 필요한 환경 변수:

```bash
export FLIRTING_HELL_FIREBASE_PROJECT_ID="firebase-project-id"

# 방법 A: 서비스 계정 JSON을 base64로 주입한다.
export FLIRTING_HELL_FIREBASE_SERVICE_ACCOUNT_BASE64="$(base64 -i /absolute/path/to/service-account.json)"

# 방법 B: 서비스 계정 JSON 파일 경로를 주입한다.
export FLIRTING_HELL_FIREBASE_SERVICE_ACCOUNT_PATH="/absolute/path/to/service-account.json"
```

### Flutter 앱

```bash
npm run dev:mobile
npm run dev:mobile:firebase
npm run analyze:mobile
npm run test:mobile
```

로컬 Spring API를 함께 볼 때:

```bash
cd apps/mobile
flutter run --dart-define=API_BASE_URL=http://localhost:8080/api
```

Android emulator는 Mac의 로컬 서버를 `localhost`로 볼 수 없으므로 다음 주소를 사용한다.

```bash
cd apps/mobile
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080/api
```

Firebase client 설정은 FlutterFire CLI가 생성한 파일을 사용한다.

```text
apps/mobile/lib/firebase_options.dart
apps/mobile/android/app/google-services.json
apps/mobile/ios/Runner/GoogleService-Info.plist
apps/mobile/firebase.json
```

Firebase Admin 서비스 계정 JSON은 서버 검증용 비밀 파일이므로 repo에 저장하지 않는다.
`dev:mobile:firebase`는 Android emulator 기준으로 실제 Firebase ID token을 Spring 서버에 보낸다.

### AI 분석 provider

기본 분석 provider는 `mock`이다.

```bash
export FLIRTING_HELL_AI_PROVIDER=mock
```

실제 LLM API로 분석하려면 `gpt`, `gemini`, `claude` 중 하나를 고른다.

```bash
export FLIRTING_HELL_AI_PROVIDER=gpt
export FLIRTING_HELL_GPT_API_KEY="..."
export FLIRTING_HELL_GPT_MODEL="gpt-4o-mini"
```

Gemini:

```bash
export FLIRTING_HELL_AI_PROVIDER=gemini
export FLIRTING_HELL_GEMINI_API_KEY="..."
export FLIRTING_HELL_GEMINI_MODEL="gemini-2.5-flash-lite"
```

Claude:

```bash
export FLIRTING_HELL_AI_PROVIDER=claude
export FLIRTING_HELL_CLAUDE_API_KEY="..."
export FLIRTING_HELL_CLAUDE_MODEL="claude-haiku-4-5-20251001"
```

호환 alias:

- `openai` → `gpt`
- `anthropic` → `claude`

선택값:

```bash
export FLIRTING_HELL_GPT_BASE_URL="https://api.openai.com/v1"
export FLIRTING_HELL_GEMINI_BASE_URL="https://generativelanguage.googleapis.com"
export FLIRTING_HELL_CLAUDE_BASE_URL="https://api.anthropic.com"
```

AI 분석 계약은 `docs/product/ai-analysis-contract.md`를 따른다.

## 앱 전용 개발 기준

- 앱: Flutter
- 백엔드: Spring Boot, Java 21, DDD modular monolith
- DB: PostgreSQL, Flyway
- 인증: Firebase Auth
- 결제: RevenueCat 기반 인앱결제
- 광고: AdMob 리워드 광고

상세 순서는 `docs/product/native-app-development-phases.md`를 따른다.

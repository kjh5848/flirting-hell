# 플러팅지옥 네이티브 앱 아키텍처

## 목적

이 문서는 플러팅지옥을 앱 전용 제품으로 전환할 때의 전체 시스템 구조를 정의한다.

기준 결정은 `docs/decisions/0005-native-app-spring-stack.md`다. 기존 `Cloudflare Pages + Workers + D1 + Polar` 문서는 웹/PWA MVP 레거시 기준으로 유지하고, 앱 전용 구현은 이 문서를 기준으로 한다.

## 최종 구조

```text
Flutter 앱
React 웹
Spring Boot API 서버
PostgreSQL
Firebase Auth
RevenueCat
AdMob
ChannelTalk
Python FastAPI optional
Cloudflare DNS / WAF / CDN / 프록시
```

## 시스템 컨텍스트

```mermaid
flowchart TB
  User["👤 사용자"]
  Admin["👤 운영자"]

  subgraph Product["플러팅지옥 제품"]
    Mobile["📱 Flutter 앱<br/>iOS / Android"]
    Web["🌐 React 웹<br/>랜딩 / 정책 / 고객지원 / 운영"]
    API["☕ Spring Boot API<br/>도메인 / 권한 / 결제 / 분석"]
    DB[("🐘 PostgreSQL<br/>사용자 / 상담방 / 결제 ledger")]
  end

  Firebase["🔥 Firebase Auth<br/>Apple / Google / Kakao"]
  RevenueCat["💳 RevenueCat<br/>IAP / 복원 / Webhook"]
  Stores["🏪 App Store / Google Play<br/>인앱결제"]
  AdMob["📺 AdMob<br/>리워드 광고"]
  ChannelTalk["💬 ChannelTalk<br/>고객 상담"]
  LLM["🤖 LLM Provider<br/>대화 분석 / 답장 생성"]
  PythonAI["🐍 Python FastAPI<br/>AI 실험 / 벡터 검색 / 배치"]
  Cloudflare["☁️ Cloudflare<br/>DNS / SSL / WAF / CDN / 프록시"]

  User -->|앱 사용| Mobile
  User -->|웹 방문| Web
  Admin -->|운영 화면| Web

  Mobile -->|Firebase 로그인| Firebase
  Mobile -->|HTTPS API| Cloudflare
  Web -->|HTTPS API| Cloudflare
  Cloudflare --> API
  API --> DB

  Mobile -->|구매 요청| RevenueCat
  RevenueCat -->|StoreKit / Play Billing| Stores
  RevenueCat -->|구매 webhook| API

  Mobile -->|리워드 광고 시청| AdMob
  AdMob -->|보상 검증 요청| API

  Mobile -->|고객 상담| ChannelTalk
  API -->|memberHash 발급| ChannelTalk

  API -->|분석 요청| LLM
  API -. "필요 시 분석 위임" .-> PythonAI
  PythonAI -. "LLM / 벡터 / 배치" .-> LLM

  classDef client fill:#FFE66D,stroke:#F08C00,color:#111
  classDef backend fill:#4ECDC4,stroke:#0B7285,color:#fff
  classDef data fill:#A8DADC,stroke:#1864AB,color:#111
  classDef external fill:#F8D7DA,stroke:#C92A2A,color:#111
  classDef edge fill:#E3F8E8,stroke:#22A06B,color:#111

  class Mobile,Web client
  class API,PythonAI backend
  class DB data
  class Firebase,RevenueCat,Stores,AdMob,ChannelTalk,LLM external
  class Cloudflare edge
```

## 런타임 책임

| 런타임 | 책임 | 하지 않는 것 |
|---|---|---|
| Flutter 앱 | 상담방 UX, 공유받기, 붙여넣기, 로컬 원문 보관, 답장 복사, 인앱결제, 리워드 광고 | AI API 키 보관, 결제 권한 최종 판단, 서버 원문 장기 저장 |
| React 웹 | 랜딩, 약관, 개인정보처리방침, 고객지원, 운영/관리 화면 | 앱 핵심 상담 UX 대체 |
| Spring Boot | 인증 토큰 검증, 사용자/상담방/답장/분석권/광고 보상/이벤트 관리, AI 호출 중계 | 앱 UI 상태 관리, 원문 대화 장기 저장 |
| PostgreSQL | 정규화된 사용자 데이터, 상담방, 분석 턴, 저장 답장, 결제/광고 ledger | 카톡/DM 원문 전문 저장 |
| Firebase Auth | Apple/Google/Kakao 기반 사용자 식별 | 제품 데이터 저장, 결제 권한 관리 |
| RevenueCat | 스토어 인앱결제, 구매 복원, webhook 발송 | 분석권 ledger의 최종 원장 |
| AdMob | 리워드 광고 노출과 광고 SDK 이벤트 | 보상 중복 지급 판단 |
| Python FastAPI | 필요 시 AI 실험, 벡터 검색, 품질 평가, 배치 분석 | 계정/결제/상담방의 원천 서버 |

## 컨테이너 구조

```mermaid
flowchart TB
  subgraph Client["Client Layer"]
    Flutter["📱 apps/mobile<br/>Flutter"]
    React["🌐 apps/web<br/>React"]
  end

  subgraph Edge["Edge Layer"]
    CF["☁️ Cloudflare<br/>DNS / SSL / WAF / CDN / Proxy"]
  end

  subgraph Backend["Backend Layer"]
    Spring["☕ apps/backend<br/>Spring Boot"]
    Python["🐍 apps/ai optional<br/>FastAPI"]
  end

  subgraph Data["Data Layer"]
    Postgres[("🐘 PostgreSQL")]
    ObjectStorage[("🗄️ Object Storage optional<br/>정책 파일 / 백업")]
  end

  subgraph External["External Services"]
    Auth["🔥 Firebase Auth"]
    RC["💳 RevenueCat"]
    Ads["📺 AdMob"]
    Support["💬 ChannelTalk"]
    AI["🤖 LLM API"]
  end

  Flutter --> CF
  React --> CF
  CF --> Spring
  Spring --> Postgres
  Spring --> ObjectStorage
  Spring --> Auth
  Spring --> RC
  Spring --> Ads
  Spring --> Support
  Spring --> AI
  Spring -. "분석 고도화 시" .-> Python
  Python --> Postgres
  Python --> AI
```

## 핵심 사용자 흐름

### 1. 로그인과 부트스트랩

```mermaid
sequenceDiagram
  actor U as 사용자
  participant App as Flutter 앱
  participant Firebase as Firebase Auth
  participant API as Spring Boot
  participant DB as PostgreSQL

  U->>App: Apple / Google / Kakao 로그인
  App->>Firebase: 소셜 로그인
  Firebase-->>App: Firebase ID Token
  App->>API: GET /api/me/bootstrap<br/>Authorization: Bearer token
  API->>Firebase: 토큰 검증
  API->>DB: app_user upsert<br/>사용량/상담방 조회
  DB-->>API: 초기 데이터
  API-->>App: 사용자, 분석권, 최근 상담방
```

### 2. 대화/상황 분석

```mermaid
sequenceDiagram
  actor U as 사용자
  participant App as Flutter 앱
  participant Local as 로컬 SQLite
  participant API as Spring Boot
  participant DB as PostgreSQL
  participant AI as LLM API

  U->>App: 카톡/DM/상황 붙여넣기 또는 공유받기
  App->>Local: 원문 임시 저장
  App->>API: POST /api/rooms/:id/analyses
  API->>DB: 분석권/무료 사용량 확인
  API->>AI: 분류/요약/답장 생성 요청
  AI-->>API: 구조화 결과
  API->>DB: 요약, 전략, 답장 후보 저장<br/>원문 전문 제외
  API-->>App: 인사이트, 전략, 답장 후보
  App->>Local: 원문과 서버 summary id 연결
```

### 3. 답장 저장

```mermaid
sequenceDiagram
  actor U as 사용자
  participant App as Flutter 앱
  participant API as Spring Boot
  participant DB as PostgreSQL

  U->>App: 추천 답장 저장
  App->>API: POST /api/rooms/:id/reply-turns
  API->>DB: room_id 기준 저장
  DB-->>API: reply_turn
  API-->>App: 저장 완료
```

### 4. 인앱결제와 분석권 적립

```mermaid
sequenceDiagram
  actor U as 사용자
  participant App as Flutter 앱
  participant RC as RevenueCat
  participant Store as App Store / Google Play
  participant API as Spring Boot
  participant DB as PostgreSQL

  U->>App: 분석권 패키지 구매
  App->>RC: purchasePackage
  RC->>Store: StoreKit / Play Billing
  Store-->>RC: 구매 결과
  RC-->>App: CustomerInfo
  RC->>API: webhook purchase event
  API->>DB: purchase_events idempotency 확인
  API->>DB: credit_ledger 적립
  API-->>RC: 200 OK
  App->>API: GET /api/me/bootstrap
  API-->>App: 갱신된 분석권 잔액
```

### 5. 리워드 광고 보상

```mermaid
sequenceDiagram
  actor U as 사용자
  participant App as Flutter 앱
  participant Ads as AdMob
  participant API as Spring Boot
  participant DB as PostgreSQL

  U->>App: 리워드 광고 보기
  App->>Ads: 광고 로드/시청
  Ads-->>App: reward event
  App->>API: POST /api/rewards/admob
  API->>DB: reward idempotency 확인
  API->>DB: credit_ledger +1 적립
  API-->>App: 분석권 지급 결과
```

## 데이터 경계

| 데이터 | Flutter 로컬 | Spring/PostgreSQL | 외부 서비스 |
|---|---:|---:|---:|
| 사용자가 붙여넣은 원문 | O | X | AI 요청 처리 중 일시 전달 |
| 입력 제목/요약 | O | O | X |
| 추천 답장/이유/주의할 말 | O | O | X |
| 상담방 별칭/관계 상태 | O | O | X |
| Firebase UID | O | O | Firebase |
| 결제 거래 ID | X | O | RevenueCat / Store |
| 리워드 광고 이벤트 ID | X | O | AdMob |
| ChannelTalk memberHash | O | 발급만 | ChannelTalk |

원칙:

- 서버는 원문 전문을 장기 저장하지 않는다.
- 서버 저장 단위는 `상담방`, `입력 요약`, `분석 턴`, `저장 답장`, `ledger`다.
- 분석권은 클라이언트 상태가 아니라 서버 `credit_ledger`를 기준으로 판단한다.
- 같은 답장 문장도 다른 분석 턴에서 저장하면 별도 `reply_turn`으로 저장한다.

## 배포 구조

```mermaid
flowchart LR
  Dev["개발자"]
  Git["GitHub"]
  Store["App Store / Play Store"]
  WebHost["React 웹 호스팅"]
  Server["Spring Boot 컨테이너 호스팅"]
  DB["Managed PostgreSQL"]
  CF["Cloudflare DNS / WAF / SSL"]

  Dev -->|push| Git
  Git -->|mobile CI| Store
  Git -->|web CI| WebHost
  Git -->|backend CI| Server
  Server --> DB
  CF --> WebHost
  CF --> Server
```

운영 기본값:

- Spring Boot는 OCI 컨테이너로 빌드한다.
- 1차 배포 플랫폼은 별도 배포 문서에서 확정한다.
- Cloudflare Containers는 1차 메인 서버 배포 대상이 아니라 향후 후보로 둔다.
- Cloudflare는 우선 DNS, SSL, WAF, CDN, API 프록시 역할로 사용한다.

## 저장소 구조 목표

```text
flirting-hell/
  apps/mobile       Flutter 앱
  apps/web          React 웹
  apps/backend      Spring Boot API
  apps/ai           Python FastAPI optional
  docs              제품/기술/결정 문서
  contracts         OpenAPI 또는 공통 API 계약
```

초기에는 한 저장소 안에서 분리한다. 앱과 백엔드가 안정되면 필요할 때 별도 저장소로 나눌 수 있다.

## 아키텍처 원칙

- 앱 UX와 서버 도메인을 동시에 검증하기 위해 기능별 세로 슬라이스로 개발한다.
- 전체 디자인 시스템과 IA는 먼저 고정하되, 모든 화면을 끝까지 만든 뒤 백엔드를 붙이지 않는다.
- API 계약은 Flutter와 Spring 사이의 기준이다.
- 결제와 광고 보상은 무조건 서버 ledger를 원천으로 한다.
- AI 결과는 조언과 경고를 제공하되, 상대를 조종하거나 압박하는 기능을 만들지 않는다.

## 다음 문서

이 문서 다음에는 아래 순서로 세부 스펙을 작성한다.

1. `docs/technical/flutter-app-tech-spec.md`
2. `docs/technical/spring-backend-tech-spec.md`
3. `docs/technical/app-api-spec-v2.md`
4. `docs/technical/data-model-v2.md`
5. `docs/product/native-app-development-phases.md`

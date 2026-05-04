# 결정 0005: 앱 전용 제품은 Flutter, Spring Boot, PostgreSQL로 전환한다

## 상태

확정

## 날짜

2026-05-01

## 배경

플러팅지옥은 처음에 모바일 웹/PWA로 빠르게 검증하는 방향이었다. 이후 제품 방향이 앱 전용으로 바뀌면서 다음 요구가 커졌다.

- 카톡, DM, 문자 내용을 앱으로 공유받는 네이티브 기능
- 상담방별 답장 히스토리와 저장 구조
- 소셜 로그인과 계정 삭제
- 분석권 패키지 인앱결제
- 리워드 광고를 통한 무료 분석권 지급
- 장기적으로 관리자, 고객지원, 결제 ledger, 신고/안전 정책 확장

이 요구는 Cloudflare Workers와 D1만으로도 일부 구현할 수 있지만, 앱 전용 제품의 장기 운영 기준에서는 도메인 로직, 결제 정합성, 데이터 모델, 운영 도구가 더 중요하다.

## 결정

앱 전용 제품의 기준 스택은 다음으로 전환한다.

```text
Flutter 앱
React 웹
Spring Boot API 서버
PostgreSQL
Firebase Auth
RevenueCat + App Store / Google Play 인앱결제
AdMob 리워드 광고
ChannelTalk SDK
Python FastAPI optional
Cloudflare DNS / WAF / CDN / 프록시
```

역할은 다음처럼 분리한다.

| 영역 | 선택 | 책임 |
|---|---|---|
| 모바일 앱 | Flutter | iOS/Android 앱, 상담방 UX, 공유받기, 인앱결제, 리워드 광고 |
| 웹 | React | 랜딩, 약관, 개인정보처리방침, 고객지원, 필요 시 웹 결제/운영 화면 |
| 메인 서버 | Spring Boot | 계정, 상담방, 분석 요청, 저장, 결제 ledger, 권한, 이벤트, 계정 삭제 |
| DB | PostgreSQL | 사용자, 상담방, 분석 턴, 저장 답장, 결제/광고 ledger |
| 인증 | Firebase Auth | Apple, Google, Kakao 로그인과 사용자 식별 |
| 앱 결제 | RevenueCat | StoreKit, Google Play Billing, 구매 복원, webhook |
| 광고 | AdMob | 리워드 광고 시 분석권 지급 |
| 고객지원 | ChannelTalk | 로그인 사용자 기준 상담/CRM 연결 |
| AI 확장 | Python FastAPI | 필요 시 AI 실험, 벡터 검색, 배치 분석 분리 |
| 인프라 앞단 | Cloudflare | DNS, SSL, WAF, CDN, 트래픽 프록시 |

## 이유

### Spring Boot를 메인 서버로 선택한 이유

- 앱 전용 제품은 인증, 결제, 분석권 잔액, 상담방 저장, 계정 삭제 같은 도메인 정합성이 중요하다.
- 결제 ledger와 리워드 광고 보상은 중복 지급과 재처리를 막아야 하므로 트랜잭션과 데이터 모델이 중요하다.
- 관리자, 신고/차단, 고객지원, 통계 기능으로 확장할 가능성이 높다.
- PostgreSQL과 함께 쓰면 복잡한 관계형 데이터와 마이그레이션을 안정적으로 관리할 수 있다.

### Flutter를 앱 표면으로 선택한 이유

- iOS/Android를 한 코드베이스로 개발할 수 있다.
- 공유 확장, 클립보드, 푸시, 인앱결제, 리워드 광고 같은 앱 전용 기능을 붙이기 좋다.
- 현재 React 웹 프로토타입의 UX를 참고하되, 최종 사용자 제품은 앱 중심으로 재구성할 수 있다.

### React 웹을 유지하는 이유

- 앱스토어 외부에 필요한 랜딩, 약관, 개인정보처리방침, 고객지원 페이지가 필요하다.
- 앱 내부 디지털 결제는 인앱결제가 기본이지만, 웹 결제나 운영 페이지는 React 웹에서 별도로 다룰 수 있다.
- 현재 만들어진 React 프로토타입은 Flutter UI 설계의 레퍼런스로 활용한다.

### Python을 optional로 둔 이유

- 메인 서버를 Python으로 시작하지 않는다.
- 다만 AI 분석 실험, 벡터 검색, 대화 품질 평가, 배치 작업이 커지면 Python FastAPI 서버를 별도 서비스로 추가한다.
- 이때 Spring Boot는 권한과 저장의 원천으로 남고, Python은 분석 보조 서버가 된다.

## 대체안과 반려 이유

| 대체안 | 반려 이유 |
|---|---|
| Cloudflare Workers + D1 유지 | MVP에는 빠르지만 앱 전용 제품의 결제 ledger, 계정/삭제, 관계형 데이터 확장에는 한계가 커질 수 있다. |
| Cloudflare Containers에 Spring Boot 배포 | 가능하지만 Worker/Durable Object/Container 운영 모델이 추가되어 일반 Spring 운영보다 복잡하다. |
| Python FastAPI를 메인 서버로 사용 | AI 실험에는 좋지만 결제/권한/도메인 정합성 중심의 메인 서버로는 Spring이 더 안정적이다. |
| Flutter Web까지 사용 | 웹 랜딩/정책/운영 화면은 React 생태계가 더 적합하고, 기존 웹 자산을 살릴 수 있다. |

## 수익화 결정

앱 내부 디지털 기능 판매는 인앱결제를 기본으로 한다.

- 분석권 10회, 30회, 100회는 StoreKit/Google Play Billing 상품으로 등록한다.
- Flutter 앱은 RevenueCat SDK로 구매와 복원을 처리한다.
- Spring Boot는 RevenueCat webhook을 받아 PostgreSQL `credit_ledger`에 분석권을 적립한다.
- 리워드 광고는 AdMob을 사용하고, 광고 보상은 Spring Boot가 검증 후 분석권으로 적립한다.
- Polar는 앱 내부 디지털 결제에는 사용하지 않는다. 필요하면 React 웹 결제 후보로만 남긴다.

## 데이터 저장 원칙

- 원본 카톡/DM/문자 전문은 기본적으로 서버에 장기 저장하지 않는다.
- 앱 로컬 저장소에는 사용자가 붙여넣은 원문을 임시 또는 명시 저장 형태로 둘 수 있다.
- 서버에는 상담방, 입력 요약, 추천 답장, 선택 이유, 위험한 말, 다음 행동, 결제/광고 ledger만 저장한다.
- 계정 삭제 시 서버 요약 데이터와 로컬 저장 데이터를 함께 삭제할 수 있어야 한다.

## 영향

- `docs/decisions/0004-cloudflare-stack.md`의 Cloudflare Pages, Workers, D1, Polar 중심 결정은 앱 전용 제품 기준에서는 대체된다.
- 기존 React 웹은 폐기하지 않고 `랜딩/정책/운영/프로토타입` 역할로 축소한다.
- 기존 Workers API와 D1 문서는 Spring/PostgreSQL v2 문서가 작성될 때까지 레거시 MVP 문서로 유지한다.
- 새 구현 구조는 `apps/mobile`, `apps/backend`, `apps/web`을 기준으로 잡는다.

## 다음 문서

이 결정 다음에는 아래 문서를 순서대로 작성한다.

1. `docs/technical/native-app-architecture.md`
2. `docs/technical/flutter-app-tech-spec.md`
3. `docs/technical/spring-backend-tech-spec.md`
4. `docs/technical/app-api-spec-v2.md`
5. `docs/technical/data-model-v2.md`
6. `docs/product/native-app-development-phases.md`

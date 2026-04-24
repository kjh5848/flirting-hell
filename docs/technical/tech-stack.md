# 플러팅지옥 기술 스택

## 결론

플러팅지옥 MVP는 `Cloudflare Pages + Cloudflare Workers + D1 + Polar` 조합으로 만든다.

네이티브 iOS/Android 앱은 초기 검증 이후로 미룬다. V1의 핵심은 메시지 붙여넣기, AI 분석, 답장 복사, 분석권 결제이므로 모바일 웹/PWA로도 충분히 검증할 수 있다.

## 추천 스택

### 프론트엔드

- Cloudflare Pages
- Vite
- React
- TypeScript
- Tailwind CSS
- shadcn/ui 또는 직접 만든 최소 컴포넌트

선택 이유:

- 모바일 웹 MVP를 빠르게 만들 수 있다.
- Cloudflare Pages로 정적 프론트엔드를 빠르게 배포할 수 있다.
- 서버 로직은 Workers API로 분리해 런타임 차이를 명확히 관리한다.
- TypeScript로 AI 응답 스키마를 안전하게 다룰 수 있다.
- 나중에 PWA로 홈 화면 추가를 지원할 수 있다.

### 백엔드

- Cloudflare Workers
- Hono 또는 Workers 기본 라우팅
- 서버 전용 AI 호출
- Zod 기반 요청/응답 검증

선택 이유:

- 별도 서버 운영 없이 API를 만들 수 있다.
- AI API 키를 브라우저에 노출하지 않는다.
- D1, AI Gateway, Polar webhook을 Workers에서 직접 처리할 수 있다.

### AI 분석

- 서버에서 LLM API 호출
- JSON 응답 강제
- 안전 정책과 말투 분석 프롬프트 분리

구성:

- 대화 분위기 분석
- 이상형/연애 스타일 적합도 분석
- 사용자 말투 프로필 분석
- 답장 후보 생성
- 위험한 말 경고
- 다음 행동 추천

모델과 API 제공자는 구현 직전에 최신 공식 문서를 확인하고 확정한다.

### 데이터베이스

초기 추천:

- Cloudflare D1
- Drizzle ORM

저장 대상:

- 사용자 계정
- 무료 분석 사용량
- 분석권 잔액
- 말투 프로필 요약
- 이상형/연애 스타일 프로필
- 결제 내역
- 이벤트 로그

기본 정책:

- 원문 대화는 장기 보관하지 않는다.
- 사용자가 명시적으로 저장을 켠 경우에만 상대별 히스토리를 저장한다.
- 기본 저장 대상은 원문이 아니라 요약된 프로필과 이벤트다.

### 인증

초기 추천:

- 이메일 로그인
- 카카오 로그인은 후순위

이유:

- MVP에서는 인증보다 분석 경험 검증이 우선이다.
- 카카오 로그인은 한국 사용자에게 유리하지만 초기 구현 범위를 키운다.

### 결제

초기 유료 모델은 `분석권 패키지`다.

결제 제공자는 `Polar`를 사용한다.

구성:

- Workers에서 Polar Checkout 세션을 생성한다.
- 사용자는 Polar 결제 페이지로 이동한다.
- Polar webhook을 Workers가 수신한다.
- `order.paid` 이벤트를 확인한 뒤 D1의 분석권 잔액을 증가시킨다.
- 결제와 분석권 지급은 idempotency key로 중복 처리되지 않게 한다.

초기 패키지 후보:

- 분석권 30회: 3,900원
- 분석권 50회: 5,900원
- 분석권 100회: 9,900원

### 분석과 이벤트 로그

초기에는 자체 이벤트 테이블을 둔다.

이벤트 예시:

- `analysis_started`
- `analysis_completed`
- `reply_copied`
- `reply_regenerated`
- `tone_profile_saved`
- `free_limit_reached`
- `credit_purchase_completed`
- `repeat_purchase_completed`

외부 분석 도구는 제품 검증이 시작된 뒤 추가한다.

### 배포

초기 추천:

- Cloudflare Pages
- Cloudflare Workers
- Cloudflare D1

선택 이유:

- 프론트엔드, API, DB를 Cloudflare 안에서 운영할 수 있다.
- MVP 단계에서 서버 운영 부담이 낮다.
- 빠르게 URL을 공유하고 테스트할 수 있다.

## 대안 비교

| 선택지 | 장점 | 단점 | 판단 |
|---|---|---|---|
| Cloudflare Pages + Workers + D1 | Cloudflare 통합, 운영 단순, 서버리스 | Workers 런타임 호환성 확인 필요 | V1 확정 |
| Next.js on Workers | Next.js 기능 활용 가능 | OpenNext와 런타임 차이 관리 필요 | 필요 시 검토 |
| Vercel + Supabase | 일반적인 Next.js 흐름, 호환성 좋음 | Cloudflare 중심 운영과 다름 | 보류 |
| React Native | 앱 경험에 가까움 | 초기 설정과 배포 부담 큼 | V2 이후 |

## MVP 구현 범위

1. 모바일 웹 첫 화면
2. 빠른 성향 설정
3. 메시지 입력
4. 말투 자동 분석
5. AI 결과 화면
6. 답장 복사
7. 무료 분석 횟수 제한
8. 분석권 잔액 표시
9. 이벤트 로그 저장

결제 연동은 첫 프로토타입 이후에 붙인다. 먼저 사용자가 분석 결과와 답장 품질에 만족하는지 확인한다.

## 보류할 것

- 네이티브 앱 출시
- 앱스토어 심사
- 카카오 로그인
- 상대별 히스토리 장기 저장
- 구독 결제
- 관리자 대시보드

## 다음 작업

- `docs/technical/data-model.md` 작성
- `docs/technical/api-spec.md` 작성
- `docs/technical/polar-payment-flow.md` 작성
- `docs/product/wireframes.md` 작성
- Cloudflare Pages/Workers 프로젝트 생성

# Cloudflare 기반 MVP 스택 옵션

## 결론

플러팅지옥 MVP는 Cloudflare 기반으로 만든다.

최종 선택은 `Cloudflare Pages + Cloudflare Workers + D1 + Polar`다. 기존 `Next.js + Vercel + Supabase` 조합은 보류한다.

## Cloudflare-first 추천 스택

### 프론트엔드와 서버

- Cloudflare Pages
- Vite
- React
- TypeScript
- Tailwind CSS
- Cloudflare Workers

프론트엔드는 Cloudflare Pages에 배포하고, 서버 API는 Cloudflare Workers로 분리한다.

주의할 점:

- Workers 런타임은 일반 Node.js 서버와 다르다.
- Node.js 전용 라이브러리는 호환성을 확인해야 한다.
- 결제와 AI SDK는 Workers 호환성이 애매하면 REST API 직접 호출을 우선한다.

## 데이터베이스

### 기본 추천

- Cloudflare D1
- Drizzle ORM

D1은 Cloudflare Workers와 연결되는 serverless SQL 데이터베이스다. SQLite 문법을 기반으로 하며, 사용자 계정, 분석권 잔액, 말투 프로필, 이상형 프로필, 이벤트 로그 같은 MVP 데이터에 적합하다.

저장 대상:

- 사용자 계정
- 무료 분석 사용량
- 분석권 잔액
- 말투 프로필 요약
- 이상형/연애 스타일 프로필
- 결제 내역
- 이벤트 로그

원문 대화는 기본적으로 장기 저장하지 않는다.

## AI 구성

### 추천 구성

- Cloudflare Workers에서 LLM API 호출
- Cloudflare AI Gateway로 요청 관측, 비용 추적, 캐싱, 라우팅 관리

AI Gateway는 여러 AI 제공자 호출을 한 곳에서 관찰하고 관리하는 데 적합하다. 플러팅지옥은 답장 품질이 핵심이므로, 모델을 하나로 고정하기보다 Gateway를 통해 비용, 속도, 품질을 비교할 수 있게 두는 편이 좋다.

### Workers AI 사용 여부

Workers AI도 사용할 수 있지만, 한국어 연애 대화 품질이 핵심인 V1에서는 바로 주력 모델로 쓰기보다 후보로 둔다.

추천:

- V1 답장 품질 검증: 외부 고성능 LLM + AI Gateway
- 비용 최적화 실험: Workers AI 또는 더 저렴한 모델

## 저장소와 부가 기능

필요 시 추가할 수 있는 Cloudflare 제품:

- KV: 설정값, 기능 플래그, 짧은 캐시
- R2: 이미지, 리포트, 내보내기 파일 저장
- Queues: 분석 로그, 결제 후처리, 이벤트 비동기 처리
- Turnstile: 봇 방지
- Web Analytics: 기본 트래픽 분석

V1 초기에는 D1과 Workers만으로 시작하고, 필요할 때 추가한다.

## 결제

분석권 패키지 결제는 Cloudflare 자체 결제 기능으로 해결하는 것이 아니라, 외부 결제사를 붙인다.

결제 제공자:

- Polar

구성:

- 프론트엔드에서 결제 요청
- Workers가 Polar Checkout 세션 생성
- 사용자를 Polar 결제 페이지로 리다이렉트
- 결제 성공 후 Polar webhook을 Cloudflare Workers로 수신
- Workers가 D1의 분석권 잔액을 증가
- 이벤트 로그에 `credit_purchase_completed` 저장

주의할 점:

- Polar SDK가 Workers 런타임과 호환되는지 확인해야 한다.
- SDK 호환성이 애매하면 REST API를 직접 호출하는 방식이 안전하다.

## Cloudflare-first 장점

- 배포와 API를 Workers에 통합할 수 있다.
- D1, KV, Queues, AI Gateway를 한 플랫폼에서 연결할 수 있다.
- 글로벌 캐싱과 보안 기능을 기본으로 활용할 수 있다.
- Vercel + Supabase보다 인프라 구성이 단순해질 수 있다.

## Cloudflare-first 리스크

- 일반 Node.js 서버와 런타임 차이가 있다.
- 결제 SDK, AI SDK, ORM 호환성을 확인해야 한다.
- D1은 Postgres가 아니라 SQLite 계열이다.
- 팀이 Cloudflare Workers에 익숙하지 않으면 초기 디버깅 비용이 생길 수 있다.

## 추천 판단

확정 MVP 스택은 다음이다.

```text
Cloudflare Pages
Vite
React
TypeScript
Tailwind CSS
Cloudflare Workers
Cloudflare D1
Drizzle ORM
Cloudflare AI Gateway
External LLM API
Polar
```

이 결정은 `docs/decisions/0004-cloudflare-stack.md`에 남긴다.

# 결정 0004: Cloudflare Pages, Workers, D1, Polar를 사용한다

## 상태

확정

## 날짜

2026-04-24

## 배경

초기 기술 스택은 Next.js, Vercel, Supabase를 후보로 잡았다. 이후 Cloudflare 중심 운영을 선호하게 되었고, 프론트엔드, 서버, 데이터베이스, 결제를 Cloudflare 생태계와 Polar로 구성하기로 했다.

## 결정

플러팅지옥 MVP는 다음 스택으로 만든다.

- 프론트엔드: Cloudflare Pages
- 서버: Cloudflare Workers
- 데이터베이스: Cloudflare D1
- 결제: Polar
- 프론트엔드 프레임워크: Vite + React + TypeScript
- 스타일: Tailwind CSS
- 데이터 접근: Drizzle ORM
- AI 관측: Cloudflare AI Gateway

## 이유

- Cloudflare Pages와 Workers를 분리하면 프론트엔드와 API 책임이 명확하다.
- D1은 분석권 잔액, 말투 프로필, 이상형 프로필, 이벤트 로그에 충분하다.
- Workers는 Polar webhook과 AI API 호출을 처리하기에 적합하다.
- Polar는 분석권 패키지 같은 one-time purchase 모델에 맞고, webhook으로 결제 완료를 처리할 수 있다.
- Cloudflare 안에서 배포, API, DB, 보안 기능을 통합할 수 있다.

## 영향

기존 `0003-tech-stack.md`의 Next.js/Vercel/Supabase 중심 결정은 이 결정으로 대체한다.

MVP 구현은 Cloudflare Pages와 Workers를 별도 앱 또는 모노레포 패키지로 구성한다.

결제 SDK가 Workers 런타임과 맞지 않으면 Polar REST API를 직접 호출한다.

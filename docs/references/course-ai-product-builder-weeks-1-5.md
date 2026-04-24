# AI Product Builder 1~5주차 개발 참고 정리

## 목적

이 문서는 `바이브 코딩 1인 창업 부트캠프 (AI Product Builder)` 노션 페이지의 1~5주차 하위 문서에서 확인한 내용을 플러팅지옥 MVP 개발 참고용으로 정리한다.

확인 범위:

- 노션 메인 페이지의 `강의자료 / 출석 체크 / 과제 제출` 섹션
- 1주차부터 5주차까지의 하위 노션 페이지
- 각 주차의 라이브 영상, 강의자료 링크, 출석 체크, 과제 제출 또는 대체 항목

주의:

- Google Drive 강의자료 파일 내부 내용은 아직 열람하지 않았다.
- 이 문서는 노션 하위 페이지에 표시된 텍스트와 링크를 기준으로 한다.

## 원본 페이지

- 메인 노션: https://abrasive-detective-fdd.notion.site/1-AI-Product-Builder-2e2e55bd0f1b803285e6f90fc93bf4ff

## 1주차: 기획 → 첫 수익

### 핵심 주제

- 개요
- 웹 개발 기초
- 배포
- API
- 광고 수익화

### 링크

- 라이브 영상: https://www.youtube.com/watch?v=-iEIOXh9DCs
- 강의자료: https://drive.google.com/file/d/1e47--EsqnBml4oX3TKLbZH87ue_l3aKY/view?usp=sharing
- 출석 체크: https://airtable.com/appgVjc1ZTkVGRs66/shrY4O0ZqwNeAGQbg
- 과제 제출 이벤트: https://kko.to/Mk-zsZpb1H

### 노션에 표시된 참고 항목

- 확장자 변경 방법
- Mac용 메모장
- Google
- ChatGPT
- Cloudflare
- Cloudflare Pages
- `ERR_SSL_VERSION_OR_CIPHER_MISMATCH` 해결 메모
- Firebase Studio
- GitHub
- Gemini CLI
- OpenAI Codex CLI
- Claude Code
- opencode
- Formspree
- Disqus
- Teachable Machine
- Google AdSense

### 플러팅지옥 적용

1주차 내용은 MVP의 가장 기본 골격에 해당한다.

- Cloudflare Pages 배포를 기본으로 한다.
- AI 코딩 도구와 GitHub를 활용해 빠르게 프로토타입을 만든다.
- 광고 수익화는 V1 핵심이 아니므로 보류한다.
- 외부 서비스 연동은 필요한 것만 제한적으로 사용한다.

## 2주차: 유입 → 성장

### 핵심 주제

- SEO
- 데이터 분석
- 바이럴 구조
- 마케팅

### 링크

- 라이브 영상: https://youtube.com/live/sI6dDbxGuHA?feature=share
- 강의자료: https://drive.google.com/file/d/1TUqpTaWg9AGi_mbkh-kkL8icuT_8NKq4/view?usp=sharing
- 출석 체크: https://airtable.com/appgVjc1ZTkVGRs66/shrpRikvADbKVIwMO
- 과제 제출 이벤트: https://kko.to/HukF4i0K7D

### 노션에 표시된 참고 항목

- Google Analytics
- Microsoft Clarity
- Cloudflare
- Namecheap
- Gabia
- Cafe24
- Google SEO Starter Guide
- Naver Search Advisor
- Google Search Console
- SEO Site Checkup
- Kakao 공유 디버거
- GEO 관련 Google AI Search 자료
- Toss Payments GEO 관련 글
- AddToAny
- Userback

### 플러팅지옥 적용

2주차 내용은 출시 후 유입과 측정에 연결된다.

- 이벤트 로그는 자체 D1 테이블에 먼저 저장한다.
- 이후 Google Analytics 또는 Cloudflare Web Analytics를 붙인다.
- Microsoft Clarity는 모바일 UX 확인용으로 검토한다.
- 공유용 OG 태그와 카카오 공유 디버깅을 준비한다.
- SEO는 랜딩 페이지가 생길 때 적용한다.

## 3주차: AI → 결제

### 핵심 주제

- React
- Serverless
- AI API 활용
- 글로벌 결제 구현

### 링크

- 라이브 영상: https://youtube.com/live/X9cna-h9mC4?feature=share
- 강의자료: https://drive.google.com/file/d/1Kmeqlf68HSNLwTXmPWY8N4MriewCVArf/view?usp=sharing
- 출석 체크: https://airtable.com/appgVjc1ZTkVGRs66/shrYW1JjkOEiTk9dz
- 과제 제출 이벤트: https://kko.to/V3yn86vkXp

### 노션에 표시된 참고 항목

- Cloudflare Pages Functions
- Wrangler 설치 문서
- OpenAI API 문서
- OpenAI API 가격표
- OpenAI tokenizer
- Replicate
- Google Stitch
- Bootstrap
- Tailwind CSS
- shadcn/ui
- Motion for React
- Mobbin
- Polar acceptable use
- Polar
- Polar sandbox
- Userback

### 플러팅지옥 적용

3주차 내용은 플러팅지옥 MVP의 중심과 가장 직접적으로 연결된다.

- 프론트엔드는 Vite, React, TypeScript, Tailwind CSS로 만든다.
- 서버는 Cloudflare Workers로 만든다.
- AI 호출은 Workers에서 처리해 API 키를 숨긴다.
- AI 비용 관리를 위해 입력/출력 토큰을 측정한다.
- 결제는 Polar sandbox로 먼저 검증한다.
- 디자인 참고는 Mobbin, shadcn/ui, Tailwind, Motion을 활용한다.

## 4주차: SaaS → 반복 매출

### 핵심 주제

- 데이터베이스
- 회원가입/로그인
- 구독형 서비스 구현

### 링크

- 라이브 영상: https://youtube.com/live/cjmMNlOMBdg?feature=share
- 강의자료: https://drive.google.com/file/d/1aUcrQWg7ljrcqsFaclOlfFO8fAjPkRLN/view?usp=sharing
- 출석 체크: https://airtable.com/appgVjc1ZTkVGRs66/shrflYbUOg8dFNes3
- 과제 제출 이벤트: https://kko.to/kroYUB0_ph

### 노션에 표시된 참고 항목

- Supabase
- Supabase CLI
- Supabase API 키 설명
- Supabase account tokens
- Supabase Google social login
- Google Cloud
- Google Cloud Auth overview
- OpenWeather API
- GA4 gtag 이벤트 설정

### 플러팅지옥 적용

4주차 내용은 데이터 모델, 인증, 이벤트 추적 설계에 연결된다.

- 강의자료는 Supabase 중심이지만, 플러팅지옥은 D1로 대체한다.
- 핵심 개념은 동일하다: 사용자, 사용량, 결제, 이벤트 로그를 저장한다.
- 소셜 로그인은 V1에서는 보류하고, 익명 또는 이메일 기반 흐름을 우선한다.
- GA4 이벤트 설계는 `monetization-metrics.md`의 이벤트 이름과 연결한다.

## 5주차: 확장 → Exit

### 핵심 주제

- 실무 운영
- 퍼포먼스 마케팅
- 앱 개발
- 미국 법인
- Stripe
- Exit 전략

### 링크

- 라이브 영상: https://www.youtube.com/live/PPfLsWJ3wsI?si=ifIBRQ8L859VaLSp
- 강의자료: https://drive.google.com/file/d/1zf3Di4x9PLrRz5IjiMTgfjHNjoJP177Q/view?usp=sharing
- 출석 체크: https://airtable.com/appgVjc1ZTkVGRs66/shrdvjh0BUAqjpcu8
- 해커톤: https://hack.primer.kr/

### 노션에 표시된 참고 항목

- Capacitor
- Expo
- Expo login 명령
- Expo tunnel 실행 명령
- OpenAI API 문서
- Codex login 명령
- Stripe Atlas
- 해커톤

### 과제 제출 항목 확인

5주차 페이지에는 1~4주차처럼 `과제 제출 이벤트` 제목이 보이지 않았다.

대신 `해커톤` 항목과 `https://hack.primer.kr/` 링크가 표시되어 있다. 따라서 5주차는 과제 제출 이벤트가 해커톤으로 대체되었거나, 노션 페이지에 별도 과제 제출 블록이 없는 상태로 보인다.

### 플러팅지옥 적용

5주차 내용은 MVP 이후 확장 단계에 해당한다.

- V1은 Cloudflare Pages 기반 모바일 웹/PWA로 시작한다.
- 앱 전환이 필요하면 Capacitor 또는 Expo를 V2 이후 검토한다.
- 미국 법인과 Stripe Atlas는 글로벌 결제, 투자, Exit를 고려할 때 후순위로 본다.
- 해커톤 제출을 목표로 한다면 MVP 범위를 더 줄여야 한다.

## 플러팅지옥 개발 적용 순서

### 1단계: 1주차 기반

- Cloudflare Pages 프로젝트 생성
- Vite React 앱 생성
- 기본 화면 구성
- GitHub 저장소 연결

### 2단계: 3주차 기반

- Cloudflare Workers API 생성
- AI 분석 endpoint 구현
- OpenAI 또는 외부 LLM API 연결
- AI 응답 JSON 스키마 검증

### 3단계: 4주차 기반

- Cloudflare D1 데이터 모델 작성
- 분석권 잔액 저장
- 이벤트 로그 저장
- 말투 프로필과 이상형 프로필 저장

### 4단계: 3주차 + Polar 기반

- Polar sandbox 상품 생성
- Checkout API 연동
- `order.paid` webhook 처리
- 분석권 지급 중복 방지

### 5단계: 2주차 기반

- 핵심 이벤트 로깅
- 공유용 OG 설정
- Google Analytics 또는 Cloudflare Web Analytics 검토
- Microsoft Clarity 검토

### 6단계: 5주차 기반

- PWA 품질 개선
- 앱 전환 가능성 검토
- 해커톤 제출용 데모 정리

## 현재 플러팅지옥 스택과 강의 내용 매핑

| 플러팅지옥 결정 | 연결되는 주차 | 비고 |
|---|---|---|
| Cloudflare Pages | 1주차, 3주차 | 배포와 serverless 기반 |
| Cloudflare Workers | 3주차 | AI API와 Polar webhook 처리 |
| Cloudflare D1 | 4주차 | Supabase 개념을 D1로 대체 |
| Vite + React + Tailwind | 3주차 | React와 바이브 디자인 |
| Polar 결제 | 3주차 | 글로벌 결제, sandbox |
| 이벤트 지표 | 2주차, 4주차 | GA/gtag 개념과 자체 이벤트 로그 |
| PWA 우선, 앱은 후순위 | 5주차 | Capacitor/Expo는 V2 후보 |

## 다음 개발 문서

- `docs/technical/data-model.md`
- `docs/technical/api-spec.md`
- `docs/product/wireframes.md`
- `docs/technical/implementation-plan.md`

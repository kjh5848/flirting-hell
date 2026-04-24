# 플러팅지옥 문서

이 폴더는 플러팅지옥의 제품 방향, 브랜드 전략, MVP 범위, 주요 의사결정을 보관한다.

## 문서 구조

- `brand/`: 이름, 슬로건, 톤앤매너, 메시지 전략
- `product/`: 제품 콘셉트, MVP 범위, 사용자 흐름, 기능 명세
- `technical/`: 기술 스택, API, 데이터 모델, 배포 구조
- `decisions/`: 확정된 의사결정과 그 이유

## 현재 확정 사항

- 앱 이름은 `플러팅지옥`으로 한다.
- V1은 `메시지 분석 + 답장 추천 앱`으로 시작한다.
- 핵심 가치는 상대를 조종하는 기술이 아니라, 부담을 줄이고 호감을 자연스럽게 표현하는 AI 연애 코칭이다.
- 첫 타깃은 특정 성별이 아니라 `연애 초보 전체`로 잡고, 온보딩에서 관심 대상과 대화 맥락을 개인화한다.
- 첫 유료 모델은 월 구독이 아니라 `분석권 패키지`로 시작한다.
- MVP는 Cloudflare Pages, Workers, D1, Polar 기반 모바일 웹/PWA로 먼저 만든다.

## 다음에 작성할 문서

- `product/mvp-spec.md`: V1 화면 흐름, 입력값, AI 출력 포맷
- `product/ai-response-schema.md`: AI 응답 데이터 구조
- `product/screen-flow.md`: 앱 화면 흐름과 결과 화면 구조
- `product/tone-profile.md`: 사용자 말투 분석과 답장 반영 방식
- `product/monetization-metrics.md`: 무료/유료 구분과 제품 지표
- `technical/tech-stack.md`: MVP 기술 스택과 아키텍처 방향
- `technical/cloudflare-stack-option.md`: Cloudflare 기반 MVP 대안
- `technical/polar-payment-flow.md`: Polar 분석권 결제 흐름
- `technical/github-cloudflare-setup.md`: GitHub 저장소와 Cloudflare Pages 연결 절차
- `references/course-ai-product-builder-weeks-1-5.md`: AI Product Builder 1~5주차 개발 참고 정리
- `references/ai-product-builder-week5-summary.md`: 5주차 PDF 요약과 플러팅지옥 적용 포인트
- 화면별 와이어프레임
- 유료화 모델
- 앱스토어 소개 문구
- 개인정보 및 안전 정책

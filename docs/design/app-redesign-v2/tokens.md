# 플러팅지옥 앱 디자인 리뉴얼 v2 — 디자인 토큰

## Color

| Token | Value | Usage |
| --- | --- | --- |
| `background.app` | `#FBF3F0` | 앱 전체 배경 |
| `surface.default` | `#FFFFFF` | 주요 카드 |
| `surface.soft` | `#FFF8F4` | 입력/보조 영역 |
| `surface.raised` | `#FFFCFA` | 강조 카드 |
| `text.primary` | `#1D1719` | 제목/주요 본문 |
| `text.secondary` | `#76666A` | 설명/보조 본문 |
| `text.muted` | `#9B8A8E` | 비활성/힌트 |
| `accent.primary` | `#E43F5A` | CTA, 선택 상태, 핵심 강조 |
| `accent.soft` | `#FFF1F2` | 칩/부드러운 강조 배경 |
| `accent.warm` | `#FF7A59` | 제한적 보조 포인트 |
| `line.soft` | `rgba(29, 23, 25, 0.08)` | 카드/입력 경계 |

## Typography

| Token | Value | Usage |
| --- | --- | --- |
| `font.display` | Pretendard 900 | 앱명, 큰 섹션 제목 |
| `font.body` | Pretendard 600–700 | 본문, 설명 |
| `font.label` | Pretendard 800–900 | 칩, 버튼, 작은 라벨 |
| `display.size` | 28–30px | 화면 주제 |
| `section.size` | 22–26px | 카드 제목 |
| `body.size` | 14–16px | 설명/분석 문장 |
| `label.size` | 11–12px | 상태 라벨 |

## Radius

| Token | Value | Usage |
| --- | --- | --- |
| `radius.screenCard` | `32px` | 입력/결과 주요 카드 |
| `radius.contentCard` | `28px` | 보조 카드 |
| `radius.input` | `26px` | 대화 입력창 |
| `radius.bubble` | `18–22px` | 말풍선 |
| `radius.pill` | `999px` | 진행 단계/칩/버튼 |

## Shadow

| Token | Value | Usage |
| --- | --- | --- |
| `shadow.card` | `0 24px 60px rgba(42, 20, 27, 0.08)` | 주요 카드 |
| `shadow.pill` | `0 18px 45px rgba(42, 20, 27, 0.07)` | 단계 pill |
| `shadow.subtle` | `0 8px 18px rgba(42, 20, 27, 0.04)` | 작은 칩/말풍선 |

## Motion

- 화면 전환: `280–420ms`
- Easing: `cubic-bezier(0.16, 1, 0.3, 1)`
- 허용: 입력 화면 → 확인 화면 → 결과 화면의 부드러운 이동, 복사 완료 피드백
- 금지: AI 콘솔, 스캐너, SF 대시보드, 과한 그라데이션, 의미 없는 3D

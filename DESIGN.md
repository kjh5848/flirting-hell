# Design System — 플러팅지옥

---
tokens:
  color:
    background:
      canvas: "#FFF8F4"
      app: "#FBF3F0"
      surface: "#FFFFFF"
      surfaceWarm: "#FFF8F4"
      surfaceMuted: "#FFFCFA"
    text:
      primary: "#1D1719"
      secondary: "#76666A"
      muted: "#9B8A8E"
      inverse: "#FFF8F4"
    accent:
      primary: "#E43F5A"
      primaryHover: "#BE123C"
      soft: "#FFF1F2"
      warm: "#FF7A59"
      ink: "#2A141B"
    semantic:
      success: "#047857"
      successBg: "#ECFDF5"
      warning: "#B45309"
      warningBg: "#FFF7ED"
      danger: "#BE123C"
      dangerBg: "#FFF1F2"
      info: "#2563EB"
      infoBg: "#EFF6FF"
  typography:
    display:
      family: "Pretendard Variable"
      fallback: "Noto Sans KR, ui-sans-serif, system-ui, sans-serif"
      weight: 900
      tracking: "-0.055em"
    body:
      family: "Pretendard Variable"
      fallback: "Noto Sans KR, ui-sans-serif, system-ui, sans-serif"
      weight: 500
      tracking: "-0.015em"
    label:
      family: "Pretendard Variable"
      weight: 800
      tracking: "-0.01em"
    mono:
      family: "JetBrains Mono"
      fallback: "ui-monospace, SFMono-Regular, Menlo, monospace"
  spacing:
    unit: 4
    scale:
      2xs: 2
      xs: 4
      sm: 8
      md: 16
      lg: 24
      xl: 32
      2xl: 48
      3xl: 64
  radius:
    xs: 10
    sm: 14
    md: 18
    lg: 28
    xl: 32
    pill: 999
  shadow:
    card: "0 24px 60px rgba(42, 20, 27, 0.08)"
    raised: "0 28px 72px rgba(42, 20, 27, 0.12)"
    pill: "0 18px 45px rgba(42, 20, 27, 0.07)"
    focus: "0 0 0 4px rgba(228, 63, 90, 0.14)"
  component:
    appShell:
      maxWidth: 480
      mobilePriority: "input-first"
    messageInput:
      minHeight: 220
      radius: 26
    replyCard:
      radius: 32
      primaryAction: "copy"
    chip:
      radius: 999
      minHeight: 42
---

## Product Context

- **What this is:** 카톡, DM, 문자 대화를 붙여넣으면 지금 분위기와 말투를 읽고 보낼 답장을 추천하는 연애 커뮤니케이션 코치.
- **Who it's for:** 연애를 처음 시작했거나, 상대 반응을 해석하고 답장을 고르는 데 긴장하는 사용자.
- **Project type:** 모바일 우선 React/Vite 웹앱. Cloudflare Pages, Workers, D1로 운영한다.
- **Product boundary:** 상대를 조종하거나 압박하는 도구가 아니라, 부담을 줄이고 존중 있는 표현을 돕는 답장 코치다.

## Aesthetic Direction

- **Direction:** 정돈형 앱 구조 + 따뜻한 연애 포인트. Things 3의 정돈감과 Tinder의 rose/coral 포인트만 결합한다.
- **Decoration level:** Intentional and minimal. 감정은 포인트 컬러와 문장 밀도로 만들고, 장식용 카드/아이콘/그래디언트는 최소화한다.
- **Mood:** 카카오톡과 DM을 다루는 실사용 앱처럼 즉시 이해되어야 한다. 동시에 “연애 고민을 봐주는 개인 코치”의 따뜻함은 유지한다.
- **Design references:** Google Stitch/design.md 방식처럼 루트 `DESIGN.md`를 디자인 토큰과 의사결정의 단일 기준으로 둔다.
- **V2 references:** `docs/design/app-redesign-v2/`에 생성 시안, 외부 레퍼런스 링크, 토큰, 와이어프레임을 보관한다.

## Typography

- **Display/Hero:** Pretendard Variable 900 — 한국어 앱 제목과 짧은 문장을 강하게 잡는다.
- **Body:** Pretendard Variable 500 — 긴 분석 문장과 폼 설명을 읽기 쉽게 유지한다.
- **UI/Labels:** Pretendard Variable 800 — 칩, 버튼, 상태 라벨의 클릭 가능성을 분명하게 만든다.
- **Data/Tables:** Pretendard Variable, `font-variant-numeric: tabular-nums` — 사용량과 점수 표시가 흔들리지 않게 한다.
- **Code:** JetBrains Mono — 문서와 개발용 토큰 표기에만 사용한다.
- **Loading:** CDN 없이 시스템에 설치된 Pretendard를 우선 사용하고, 배포 전 필요하면 self-hosting을 검토한다.
- **Scale:** 12, 13, 14, 16, 18, 22, 28, 40, 56px를 기본 단계로 사용한다.

## Color

- **Approach:** Restrained. rose accent 1개, warm background, dark ink를 중심으로 한다.
- **Primary:** `#E43F5A` — 핵심 CTA, 선택된 진행 단계, 추천 답장 강조에만 사용한다.
- **Secondary:** `#FF7A59` — 따뜻한 보조 강조에 제한적으로 사용한다.
- **Neutrals:** `#FBF3F0`, `#FFF8F4`, `#FFFFFF`, `#76666A`, `#1D1719` — 따뜻한 앱 표면과 본문 대비.
- **Semantic:** success `#047857`, warning `#B45309`, error `#BE123C`, info `#2563EB`.
- **Dark mode:** V1에서는 만들지 않는다. 나중에 추가할 때는 단순 반전이 아니라 surface와 accent 채도를 별도로 낮춘다.

## Spacing

- **Base unit:** 4px.
- **Density:** Comfortable-compact. 모바일에서 한 화면 안에 입력, 조건, CTA가 보여야 한다.
- **Scale:** 2xs(2), xs(4), sm(8), md(16), lg(24), xl(32), 2xl(48), 3xl(64).

## Layout

- **Approach:** Mobile app frame. 웹이지만 모바일 앱처럼 중앙 프레임 안에서 `대화 입력 → 분위기 확인 → 답장 선택`이 전환된다.
- **Grid:** 모바일 1열. 데스크톱에서도 중앙 앱 프레임을 유지한다.
- **Max content width:** 480px.
- **Radius:** 입력 26px, 주요 카드 32px, 보조 카드 28px, 칩은 pill.
- **Primary hierarchy:** 답장 카드가 결과 화면의 주인공이다. 분위기/스타일/경고는 답장을 고르기 위한 보조 정보다.

## Components

- **App Shell:** 마케팅 랜딩이 아니라 분석 앱 화면으로 시작한다. 히어로 문구보다 입력 영역이 먼저 눈에 들어와야 한다.
- **Progress Pill:** `1 대화 입력 / 2 분위기 확인 / 3 답장 선택`을 상단에 고정해 현재 위치를 명확히 보여준다.
- **Message Input:** 대화 붙여넣기는 큰 텍스트 영역과 개인정보 경고를 함께 둔다.
- **Preference Chips:** 칩은 작고 명확하게. 한 줄 설명을 과하게 늘리지 않는다.
- **Reply Card Deck:** `순한맛`, `설렘맛`, `직진맛`은 카드 덱으로 보이고, 복사 버튼은 각 카드의 가장 강한 액션이다.
- **Style Fit:** 이상형/연애스타일 분석은 판정이 아니라 경고등으로 표현한다.
- **Safety Warning:** 조작, 압박, 성적 압박, 집착 신호는 rose/warning 톤으로 분명히 분리한다.

## Motion

- **Approach:** Minimal-functional. 모션은 앱의 핵심 흐름인 `대화 입력 → 분위기 확인 → 답장 선택`을 구분시키는 화면 전환에만 사용한다.
- **Easing:** enter `cubic-bezier(0.16, 1, 0.3, 1)`, exit `cubic-bezier(0.7, 0, 0.84, 0)`.
- **Duration:** micro 80ms, short 180ms, medium 280ms.
- **Rule:** AI 콘솔, 스캐너, SF 대시보드처럼 보이는 연출은 쓰지 않는다. 사용자가 이해하는 생활 언어와 화면 전환을 우선한다.
- **Opening:** 첫 방문에만 1.2~1.6초 말풍선 포털 오프닝을 보여준다. 말풍선이 회전/부상하다 추천 답장 카드로 수렴하는 느낌이다.
- **Post-opening UX:** 인트로 이후에는 `대화 입력 → 분위기 확인 → 답장 선택` 3단계 화면으로 전환되어 사용자가 현재 위치를 바로 이해하게 한다.
- **Analysis transition:** 분석 중에는 입력 화면에서 확인 화면으로 넘어가고, 결과가 준비되면 답장 선택 화면으로 전환된다.
- **Reduced motion:** `prefers-reduced-motion` 환경에서는 오프닝/3D/자동 스크롤을 최소화한다.

## Anti-Slop Rules

- 보라색 AI SaaS 그래디언트, 3열 아이콘 기능 카드, 의미 없는 유리모피즘, 장식용 blob을 쓰지 않는다.
- 카드가 너무 많아지면 정보 우선순위를 다시 정한다. 모든 정보를 같은 카드 무게로 보여주지 않는다.
- `system-ui`를 주요 디자인 결정처럼 사용하지 않는다. 폰트 선택은 명시한다.
- 대시보드처럼 보이면 실패다. 사용자는 “지금 답장 골라야 하는 앱”으로 느껴야 한다.
- 연애 조언 문구가 상대를 공략하거나 굴복시키는 방향이면 실패다.

## Decisions Log

| Date | Decision | Rationale |
| --- | --- | --- |
| 2026-04-24 | Root `DESIGN.md` created | Stitch/design.md 방식처럼 디자인 토큰과 UI 판단 기준을 코드보다 앞에 둔다. |
| 2026-04-24 | Chat-first romantic utility selected | 현재 UI가 카드형 대시보드에 가까워 MVP 핵심인 “답장 찾기”가 약하게 보였다. |
| 2026-04-24 | Restrained rose/warm palette selected | 플러팅 앱의 감정은 유지하되 흔한 AI SaaS 보라색과 과한 핑크 장식을 피한다. |
| 2026-04-25 | Post-intro cinematic flow added | 인트로가 끝난 뒤에도 대화 입력, 분위기 확인, 답장 선택이 이어지는 웹 UX로 제품 개성을 유지한다. |
| 2026-04-25 | AI-console metaphor removed | 사용자가 AI 도구를 조작하는 느낌보다 연애 대화를 정리하고 답장을 고르는 생활형 웹앱 UX가 더 적합하다. |
| 2026-04-25 | A direction selected for redesign v2 | Things 3의 정돈형 앱 구조와 Tinder의 따뜻한 포인트를 결합한 A안을 최종 UI 기준으로 삼는다. |

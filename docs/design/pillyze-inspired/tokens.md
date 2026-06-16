# Pillyze Inspired Tokens

## Color

| Token | Value | Usage |
| --- | --- | --- |
| `app.background` | `#FBF3F0` | 전체 앱 배경 |
| `surface.default` | `#FFFFFF` | 카드, 리스트, 바텀시트 |
| `surface.soft` | `#FFF8F4` | 입력 필드, 보조 면 |
| `text.primary` | `#1D1719` | 제목, CTA, 주요 답장 |
| `text.secondary` | `#76666A` | 설명, 보조값 |
| `text.muted` | `#9B8A8E` | 힌트, 비활성 |
| `accent.primary` | `#E43F5A` | CTA, 선택 상태, 중요 chip |
| `accent.soft` | `#FFF1F2` | rose chip 배경 |
| `accent.warm` | `#FF7A59` | 보조 강조 |
| `line.soft` | `#EFE3DF` | 카드/필드 경계 |

## Layout

| Token | Value | Usage |
| --- | --- | --- |
| `screen.width` | `390px` | 디자인 기준 모바일 폭 |
| `screen.height` | `844px` | 디자인 기준 모바일 높이 |
| `screen.paddingX` | `20px` | 기본 좌우 여백 |
| `header.top` | `44px` | 상태바 이후 헤더 시작 |
| `content.gap` | `12px` | 카드 그룹 사이 간격 |
| `section.gap` | `18px` | 주요 섹션 사이 간격 |
| `bottomNav.height` | `64px` | 하단 탭 바 |
| `bottomCTA.height` | `56px` | 하단 주 액션 |

## Radius

| Token | Value | Usage |
| --- | --- | --- |
| `radius.card` | `22–28px` | 카드 그룹 |
| `radius.sheet` | `34px` | 바텀시트 상단 |
| `radius.field` | `16px` | 입력/선택 필드 |
| `radius.button` | `20px` | CTA 버튼 |
| `radius.chip` | `999px` | 상태 chip |

## Component Density

| Component | Height | Padding |
| --- | --- | --- |
| `CompactListTile` | `76–88px` | `14–16px` |
| `FormSelectField` | `58–74px` | `14px` |
| `CardGroup` | content-driven | `18–22px` |
| `BottomCTA` | `56px` | horizontal `28px` |
| `StatusChip` | `26–30px` | horizontal `9–12px` |

## Flutter Mapping

- `CardGroup` → `SectionCard` 기본 padding을 화면별로 조정.
- `CompactListTile` → 상담방/저장/내 정보에서 공통 위젯으로 추출 후보.
- `BottomCTA` → 화면 하단 또는 스크롤 마지막의 `FilledButton`.
- `StatusChip` → `Container` + pill radius + label text.
- `FormSelectField` → `DropdownButtonFormField` + `TextFormField` 조합.

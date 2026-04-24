# 플러팅지옥 ERD

## 목적

이 문서는 플러팅지옥 MVP의 데이터 관계를 시각적으로 확정한다.

`docs/technical/data-model.md`가 테이블 컬럼과 migration 중심 문서라면, 이 문서는 엔티티 관계와 확장 방향을 보기 위한 ERD 문서다.

## MVP ERD

MVP는 로그인 없이 익명 사용자 기준으로 시작한다. 핵심 관계는 다음과 같다.

- 한 명의 익명 사용자는 날짜별 사용량을 여러 개 가진다.
- 한 명의 익명 사용자는 현재 프로필을 하나 가진다.
- 한 명의 익명 사용자는 여러 분석 결과를 만든다.
- 한 분석 결과에는 여러 이벤트가 연결될 수 있다.
- 이벤트는 분석과 무관한 행동도 기록할 수 있으므로 `analysis_id`는 선택값이다.

```mermaid
erDiagram
  anonymous_users ||--o{ usage_days : "has daily usage"
  anonymous_users ||--o| user_profiles : "has current profile"
  anonymous_users ||--o{ analyses : "creates analyses"
  anonymous_users ||--o{ events : "records events"
  analyses ||--o{ events : "can be referenced"

  anonymous_users {
    text id PK "anonymous user UUID"
    text created_at "ISO datetime"
    text last_seen_at "ISO datetime"
  }

  usage_days {
    text id PK "anonymous_user_id + usage_date"
    text anonymous_user_id FK
    text usage_date "YYYY-MM-DD Asia/Seoul"
    integer free_used_count "daily free analyses used"
    integer credit_used_count "paid credits used, MVP 0"
    text created_at "ISO datetime"
    text updated_at "ISO datetime"
  }

  user_profiles {
    text id PK "profile id"
    text anonymous_user_id FK
    text dating_styles_json "preferred dating styles"
    text preferred_partner_styles_json "ideal partner styles"
    text difficult_partner_styles_json "difficult partner styles"
    text attraction_reasons_json "current attraction reasons"
    text tone_profile_json "summarized user tone"
    text created_at "ISO datetime"
    text updated_at "ISO datetime"
  }

  analyses {
    text id PK "analysis id"
    text anonymous_user_id FK
    text relationship_stage "current relationship stage"
    text conversation_goal "user goal"
    text reply_intensity "soft, romantic, direct"
    text guidance_mode "support, balanced, reality check"
    text tone_mode "auto, manual, none"
    text mood_status "conversation mood"
    text style_fit_status "fit warning status"
    text ai_result_json "structured AI result, no raw chat"
    integer input_character_count "input size only"
    integer used_free_credit "0 or 1"
    integer used_paid_credit "0 or 1, MVP 0"
    text created_at "ISO datetime"
  }

  events {
    text id PK "event id"
    text anonymous_user_id FK
    text analysis_id FK "nullable"
    text event_name "event type"
    text metadata_json "small JSON payload"
    text created_at "ISO datetime"
  }
```

## MVP 테이블 역할

| 테이블 | 역할 | MVP에서 필요한 이유 |
|---|---|---|
| `anonymous_users` | 로그인 전 사용자를 구분 | 무료 3회 제한과 이벤트 추적 기준 |
| `usage_days` | 날짜별 분석 사용량 저장 | 하루 무료 3회 제한 구현 |
| `user_profiles` | 이상형/연애 스타일/말투 요약 저장 | 사용자 스타일 기반 분석과 답장 생성 |
| `analyses` | 분석 요청과 결과 요약 저장 | 결과 품질 검증, 피드백 연결, 재분석 추적 |
| `events` | 사용자 행동과 시스템 이벤트 저장 | 전환율, 복사율, 제한 도달률 측정 |

## 관계 상세

### `anonymous_users` → `usage_days`

관계: `1:N`

사용자 한 명은 날짜별 사용량 row를 여러 개 가진다.

예시:

```text
anonymous_user_id = user_1
2026-04-24 free_used_count = 3
2026-04-25 free_used_count = 1
```

무료 횟수 제한은 `anonymous_user_id + usage_date` unique index로 판단한다.

### `anonymous_users` → `user_profiles`

관계: `1:0..1`

MVP에서는 사용자당 현재 프로필 하나만 유지한다. 이상형과 원하는 연애 스타일은 바뀔 수 있으므로, history를 쌓지 않고 현재값을 덮어쓴다.

나중에 변화 이력을 분석할 필요가 생기면 `user_profile_versions`를 추가한다.

### `anonymous_users` → `analyses`

관계: `1:N`

사용자 한 명은 여러 분석을 만들 수 있다. 분석 row에는 원문 대화를 저장하지 않고, 다음 값만 저장한다.

- 입력 글자 수
- 사용자가 선택한 설정값
- AI 결과의 구조화 요약
- 분위기/적합도 상태값

### `analyses` → `events`

관계: `1:N`

분석 결과 이후의 행동을 연결한다.

예시:

- `reply_copied`
- `feedback_submitted`

단, `free_limit_reached`처럼 분석 ID가 없는 이벤트도 있으므로 `events.analysis_id`는 nullable이다.

## 이벤트 관점 데이터 흐름

```mermaid
flowchart TD
  A["사용자 방문"] --> B["anonymous_user_id 생성 또는 조회"]
  B --> C["GET /api/usage"]
  C --> D["usage_days 조회"]
  B --> E["POST /api/analyses"]
  E --> F{"무료 횟수 남음?"}
  F -- "아니오" --> G["events: free_limit_reached"]
  F -- "예" --> H["events: analysis_started"]
  H --> I["AI 분석"]
  I --> J["analyses 저장"]
  J --> K["usage_days free_used_count 증가"]
  K --> L["events: analysis_completed"]
  L --> M["결과 화면"]
  M --> N["events: reply_copied 또는 feedback_submitted"]
```

## 개인정보 저장 경계

ERD에서 의도적으로 제외한 엔티티가 있다.

| 제외 대상 | 이유 |
|---|---|
| `raw_conversations` | 카톡/DM 원문 장기 저장은 개인정보 리스크가 큼 |
| `partners` | 상대방을 식별하는 구조는 MVP에서 불필요하고 민감함 |
| `messages` | 대화 단위 저장은 서비스 가치 검증 후 재검토 |
| `contacts` | 연락처/전화번호 저장은 MVP 범위를 넘음 |

MVP 원칙은 `원문은 처리에만 사용하고 저장하지 않는다`이다.

## V2 결제 확장 ERD

분석권 패키지 결제를 붙이면 아래 엔티티를 추가한다.

```mermaid
erDiagram
  anonymous_users ||--o| credit_balances : "has balance"
  anonymous_users ||--o{ orders : "places orders"
  anonymous_users ||--o{ credit_transactions : "has credit ledger"
  orders ||--o{ credit_transactions : "grants credits"
  orders ||--o{ webhook_events : "receives provider events"

  credit_balances {
    text anonymous_user_id PK
    integer balance "current analysis credits"
    text updated_at "ISO datetime"
  }

  orders {
    text id PK "order id"
    text anonymous_user_id FK
    text provider "polar"
    text provider_order_id "external order id"
    text product_code "credit package"
    integer credit_amount "credits purchased"
    integer amount "minor unit"
    text currency "KRW or USD"
    text status "pending, paid, failed, refunded"
    text created_at "ISO datetime"
    text paid_at "ISO datetime nullable"
  }

  credit_transactions {
    text id PK "ledger id"
    text anonymous_user_id FK
    text order_id FK "nullable"
    text reason "purchase, analysis_use, refund, adjustment"
    integer delta "positive or negative"
    integer balance_after "computed balance"
    text created_at "ISO datetime"
  }

  webhook_events {
    text id PK "webhook event id"
    text order_id FK "nullable"
    text provider "polar"
    text provider_event_id "idempotency key"
    text event_type "provider event type"
    text payload_json "raw webhook payload"
    text processed_at "ISO datetime nullable"
    text created_at "ISO datetime"
  }
```

확장 원칙:

- `credit_balances`는 조회 속도를 위한 현재 잔액이다.
- `credit_transactions`가 실제 장부다.
- webhook은 중복 수신될 수 있으므로 `provider_event_id`를 unique 처리한다.
- 분석 1회 사용 시 `credit_transactions.delta = -1`로 기록한다.

## V2 로그인 확장 ERD

로그인을 붙이면 익명 사용자와 실제 계정을 연결한다.

```mermaid
erDiagram
  users ||--o{ user_identities : "has login identities"
  users ||--o{ sessions : "has sessions"
  users ||--o{ anonymous_user_links : "can link anonymous users"
  anonymous_users ||--o{ anonymous_user_links : "linked after login"

  users {
    text id PK "user id"
    text email "nullable"
    text created_at "ISO datetime"
    text updated_at "ISO datetime"
  }

  user_identities {
    text id PK "identity id"
    text user_id FK
    text provider "email, kakao, google"
    text provider_user_id "external user id"
    text created_at "ISO datetime"
  }

  sessions {
    text id PK "session id"
    text user_id FK
    text expires_at "ISO datetime"
    text created_at "ISO datetime"
  }

  anonymous_user_links {
    text id PK "link id"
    text user_id FK
    text anonymous_user_id FK
    text linked_at "ISO datetime"
  }
```

확장 원칙:

- 로그인 전 사용량과 구매 이력을 버리지 않기 위해 `anonymous_user_links`를 둔다.
- 이메일 로그인부터 시작하고 카카오 로그인은 후순위로 둔다.
- 로그인 도입 후에도 개인정보 최소 저장 원칙은 유지한다.

## 최종 결정

MVP ERD는 `anonymous_users`, `usage_days`, `user_profiles`, `analyses`, `events` 5개 테이블로 시작한다.

결제와 로그인은 ERD에 확장 방향만 남기고, MVP 1차 구현과 배포 범위에서는 제외한다.

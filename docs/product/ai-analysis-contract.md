# AI 분석 계약

## 목적

상담방의 `대화/상황 붙여넣기` 입력을 분석해 앱 UI가 바로 표시할 수 있는 구조화된 답장 카드로 변환한다.

이번 계약의 핵심은 두 가지다.

1. AI 호출은 `analysis` bounded context의 outbound port 뒤에 숨긴다.
2. 원본 대화 전문은 DB에 장기 저장하지 않고, 분석 요청 처리 중에만 사용한다.

## 공식 API 기준

- OpenAI Responses API를 사용한다.
- 답변은 Structured Outputs의 `text.format` / `json_schema` 방식으로 받는다.
- 이유: 답장 카드 UI는 고정 필드가 필요하므로 자유 텍스트보다 JSON Schema가 안전하다.

참고:

- [OpenAI Structured Outputs](https://developers.openai.com/api/docs/guides/structured-outputs)
- [OpenAI latest model guidance](https://developers.openai.com/api/docs/guides/latest-model)

## 입력 계약

백엔드 내부 port 입력:

```java
AnalysisRequest(
  roomAlias,
  relationshipStage,
  currentConcern,
  cautionNotes,
  requestedStrategyId,
  rawInput
)
```

필드 의미:

| 필드 | 의미 | 저장 여부 |
| --- | --- | --- |
| `roomAlias` | 상담방 상대 별칭 | 저장됨 |
| `relationshipStage` | 관계 단계 | 저장됨 |
| `currentConcern` | 상담방 고민 | 저장됨 |
| `cautionNotes` | 조심할 점 | 저장됨 |
| `requestedStrategyId` | 이번 분석 전략 | 저장됨 |
| `rawInput` | 붙여넣은 카톡/DM/문자/상황 설명 원문 | 장기 저장 안 함 |

## 출력 계약

AI는 반드시 아래 필드만 반환한다.

```json
{
  "sourceType": "KAKAO",
  "participantSummary": "나와 상대 발화가 구분되어 있어요.",
  "summary": "상대가 집에서 쉬는 흐름이라 가볍게 대화를 이어갈 수 있어요.",
  "currentState": "상대가 편하게 반응하는 중이라 부담 없이 한 번 더 이어갈 수 있어요.",
  "recommendedStrategyId": "MAKE_PLAN",
  "warnings": [
    "지금 바로 마음을 확인하려고 몰아붙이지 마세요."
  ],
  "primaryReply": "오 좋다 ㅋㅋ 그럼 오늘은 집에서 충전하는 날이네. 저녁은 뭐 먹을 생각이야?",
  "alternativeReplies": [
    "집에서 쉬는 날 좋지 ㅋㅋ 나는 이런 날 맛있는 거 먹으면 바로 회복돼",
    "그럼 오늘은 무리하지 말고 쉬어 ㅋㅋ 나중에 컨디션 좋을 때 맛있는 거 먹자"
  ],
  "replyReason": "상대가 편한 상태를 말했으니 일상 질문으로 이어가는 편이 안전합니다.",
  "nextAction": "상대가 답하면 가벼운 선택지로 약속 가능성을 봅니다."
}
```

## Enum

`sourceType`:

- `KAKAO`
- `DM`
- `TELEGRAM`
- `SMS`
- `SITUATION`
- `UNKNOWN`

`recommendedStrategyId`:

- `DEVELOP_ROMANCE`
- `CHECK_RELATIONSHIP_STATUS`
- `MAKE_PLAN`
- `MARRIAGE_VALUES`
- `SLOW_DOWN`

## 안전 정책

AI는 다음을 금지한다.

- 상대를 속이는 말
- 죄책감 유발
- 집착, 감시, 추적
- 성적 압박
- 고백이나 만남을 강요하는 말
- 상대 마음을 확정적으로 단정하는 표현

AI는 다음 방식으로 말한다.

- 가능성 중심으로 설명한다.
- 사용자의 말투를 최대한 유지한다.
- 부담을 낮추는 답장을 우선한다.
- 위험 신호가 있으면 경고하되 사용자의 선택을 금지하지 않는다.

## 저장 기준

저장한다.

- 입력 종류
- 발화자 구분 요약
- 대화/상황 요약
- 현재 상태
- 추천 전략
- 추천 답장
- 다른 톤 답장
- 피해야 할 말
- 다음 행동

저장하지 않는다.

- 원본 대화 전문
- 실명, 전화번호, 주소
- 카카오톡/DM 계정 식별자

## 환경 변수

기본값은 mock이다.

```bash
export FLIRTING_HELL_AI_PROVIDER=mock
```

실제 OpenAI 호출:

```bash
export FLIRTING_HELL_AI_PROVIDER=openai
export FLIRTING_HELL_OPENAI_API_KEY="..."
export FLIRTING_HELL_OPENAI_MODEL="gpt-4o-mini"
```

선택값:

```bash
export FLIRTING_HELL_OPENAI_BASE_URL="https://api.openai.com/v1"
```

비밀값은 문서, 로그, 커밋에 남기지 않는다.

## 실패 처리

1. OpenAI key가 없고 provider가 `openai`이면 분석 요청은 실패한다.
2. OpenAI가 refusal을 반환하면 분석 요청은 실패 처리한다.
3. 응답 본문에 output text가 없으면 분석 요청은 실패 처리한다.
4. JSON Schema에 맞지 않는 응답은 파싱 실패로 처리한다.

이후 단계에서 실패한 분석 시도는 `analysis_attempt`로 별도 저장할 수 있지만, 현재 Phase 3에서는 성공한 분석 결과만 저장한다.

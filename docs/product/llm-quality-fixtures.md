# LLM 품질 fixture 테스트

## 목적

실제 API 비용을 쓰기 전에 같은 입력 세트를 기준으로 분석 계약, 안전 문구, provider 공통 adapter 동작을 검증한다.

이 테스트는 기본적으로 외부 LLM API를 호출하지 않는다.

## 파일

| 구분 | 경로 | 역할 |
| --- | --- | --- |
| fixture 데이터 | `apps/backend/src/test/resources/analysis-fixtures/quality-fixtures.json` | 샘플 대화/상황과 기대 응답 |
| fixture 로더 | `apps/backend/src/test/java/com/flirtinghell/analysis/adapter/out/ai/AnalysisQualityFixtures.java` | JSON fixture 로딩 |
| fixture 테스트 | `apps/backend/src/test/java/com/flirtinghell/analysis/adapter/out/ai/AnalysisQualityFixtureTest.java` | mock/fake LLM 계약 검증 |

## 실행

```bash
npm run test:backend:analysis-quality
```

전체 백엔드 테스트에도 포함된다.

```bash
npm run test:backend
```

## 현재 fixture 범위

| ID | 입력 유형 | 전략 | 목적 |
| --- | --- | --- | --- |
| `kakao-rest-make-plan` | 카톡 | `MAKE_PLAN` | 쉬는 날 대화를 약속 가능성으로 이어가기 |
| `dm-story-develop-romance` | DM | `DEVELOP_ROMANCE` | 스토리 반응 이후 취향 대화로 이어가기 |
| `telegram-status-check` | 텔레그램 | `CHECK_RELATIONSHIP_STATUS` | 연애 여부를 직접 추궁하지 않고 맥락 확인 |
| `sms-slow-down` | 문자 | `SLOW_DOWN` | 답장 지연 상황에서 압박 줄이기 |
| `situation-marriage-values` | 상황 설명 | `MARRIAGE_VALUES` | 결혼 가치관을 무겁지 않게 확인 |
| `kakao-info-light-romance` | 카톡 | `DEVELOP_ROMANCE` | 첫 만남 후 다음 대화로 이어가기 |

## 검증 기준

- fixture ID 중복이 없어야 한다.
- MVP 입력 유형 5종을 포함해야 한다: `KAKAO`, `DM`, `TELEGRAM`, `SMS`, `SITUATION`.
- MVP 전략 5종을 포함해야 한다: `DEVELOP_ROMANCE`, `CHECK_RELATIONSHIP_STATUS`, `MAKE_PLAN`, `MARRIAGE_VALUES`, `SLOW_DOWN`.
- mock provider는 모든 fixture에서 구조화된 `AnalysisDraft`를 만들어야 한다.
- fake LLM provider는 같은 fixture 응답을 `LlmAnalysisAdapter`를 통해 파싱해야 한다.
- 추천 답장에는 압박, 죄책감 유발, 성적 압박, 몰래 행동을 유도하는 표현이 없어야 한다.
- 원본 입력 전문은 요약 필드에 그대로 복사되어서는 안 된다.

## 실제 provider 비교 방식

실제 GPT/Gemini/Claude 품질 비교는 별도 단계에서 진행한다.

1. 이 fixture의 `rawInput`을 같은 순서로 각 provider에 보낸다.
2. 응답의 `primaryReply`, `warnings`, `nextAction`을 사람이 리뷰한다.
3. 비용, 지연 시간, 한국어 자연스러움, 안전성 실패를 표로 기록한다.
4. 실제 API key가 필요한 테스트는 기본 CI에 넣지 않는다.

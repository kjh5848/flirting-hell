# 플러팅지옥 AI 응답 스키마

## 목적

이 문서는 플러팅지옥 V1에서 AI가 반환해야 하는 응답 구조를 고정한다.

프론트엔드와 백엔드는 이 스키마를 기준으로 결과 화면, 복사 버튼, 경고 표시, 재생성 기능을 구현한다.

## 응답 최상위 구조

```json
{
  "mood": {},
  "styleFit": {},
  "userToneProfile": {},
  "replyOptions": [],
  "riskyMessage": {},
  "nextAction": {},
  "safety": {},
  "feedbackPrompt": {}
}
```

## mood

현재 대화 분위기 분석이다.

```json
{
  "status": "호감 있음",
  "summary": "상대가 대화를 이어가려는 신호가 보입니다.",
  "confidence": "보통",
  "reasons": [
    "상대가 먼저 질문을 이어갔습니다.",
    "답장이 짧지 않고 감정 표현이 포함되어 있습니다."
  ]
}
```

허용값:

- `status`: `호감 있음`, `애매함`, `부담 가능성`, `잠시 쉬어야 함`
- `confidence`: `낮음`, `보통`, `높음`

## styleFit

사용자가 설정한 이상형과 연애 스타일을 기준으로, 상대의 대화 방식이 얼마나 잘 맞는지 분석한다.

이 값은 상대를 만나도 되는지 금지하거나 단정하기 위한 값이 아니다. 사용자가 끌림과 현실적 피로 가능성을 함께 볼 수 있도록 안내하는 경고등 역할을 한다.

```json
{
  "status": "매력은 있지만 피로할 수 있음",
  "summary": "상대는 담백하고 시크한 편이라, 표현이 많은 연애를 원하는 사용자에게는 다소 차갑게 느껴질 수 있습니다.",
  "matchingPoints": [
    "대화가 가볍고 편안하게 이어집니다.",
    "상대가 무례하거나 선을 넘는 표현은 보이지 않습니다."
  ],
  "possibleGaps": [
    "사용자가 원하는 다정한 표현은 아직 적게 보입니다.",
    "상대가 애정을 말로 자주 표현하는 타입인지는 더 확인이 필요합니다."
  ],
  "checkQuestions": [
    "너는 좋아하는 사람한테 표현을 자주 하는 편이야?",
    "연락은 자주 하는 게 편해, 아니면 각자 시간도 중요한 편이야?"
  ],
  "guidanceMode": "균형 조언",
  "guidance": "계속 보고 싶은 마음이 있다면 단정하지 말고, 상대의 애정 표현 방식부터 천천히 확인해보세요. 플러팅지옥은 당신의 연애를 응원합니다."
}
```

허용값:

- `status`: `잘 맞을 가능성 있음`, `매력은 있지만 피로할 수 있음`, `내 이상형과 차이가 큼`, `더 확인 필요`
- `guidanceMode`: `응원 위주`, `균형 조언`, `현실 체크`

## userToneProfile

사용자의 메시지에서 추정한 말투와 톤앤매너다.

```json
{
  "summary": "짧고 장난스러운 반말을 주로 쓰며, 호감 표현은 은근하게 하는 편입니다.",
  "speechLevel": "반말",
  "sentenceLength": "짧게",
  "warmth": "중간",
  "playfulness": "높음",
  "directness": "낮음",
  "emojiUsage": "가끔",
  "laughStyle": "ㅋㅋ",
  "signaturePatterns": [
    "문장을 짧게 끊어 말함",
    "가벼운 장난을 섞음",
    "감정 표현을 길게 설명하지 않음"
  ],
  "avoidPatterns": [
    "갑자기 너무 진지한 고백 문장",
    "평소보다 긴 감정 설명",
    "과한 애교 표현"
  ],
  "confidence": "보통"
}
```

허용값:

- `speechLevel`: `반말`, `존댓말`, `섞어서`, `판단 어려움`
- `sentenceLength`: `짧게`, `보통`, `길게`, `판단 어려움`
- `warmth`: `낮음`, `중간`, `높음`, `판단 어려움`
- `playfulness`: `낮음`, `중간`, `높음`, `판단 어려움`
- `directness`: `낮음`, `중간`, `높음`, `판단 어려움`
- `emojiUsage`: `안 씀`, `가끔`, `자주`, `판단 어려움`
- `laughStyle`: `안 씀`, `ㅋㅋ`, `ㅎㅎ`, `섞어서`, `판단 어려움`
- `confidence`: `낮음`, `보통`, `높음`

## replyOptions

지금 보낼 수 있는 답장 후보 3개다.

```json
[
  {
    "level": "순한맛",
    "text": "ㅋㅋ 맞아 그거 은근 재밌더라. 너는 그런 거 자주 해?",
    "reason": "부담 없이 대화를 이어갈 수 있습니다.",
    "bestFor": "상대 반응을 더 보고 싶을 때",
    "pressure": "낮음",
    "toneMatch": "평소처럼 짧고 장난스러운 말투를 유지했습니다."
  },
  {
    "level": "설렘맛",
    "text": "너랑 얘기하면 시간이 빨리 가는 것 같아.",
    "reason": "호감을 은근히 표현하면서도 과하지 않습니다.",
    "bestFor": "상대도 대화를 즐기는 분위기일 때",
    "pressure": "중간",
    "toneMatch": "호감 표현은 더했지만 문장은 길어지지 않게 조정했습니다."
  },
  {
    "level": "직진맛",
    "text": "나 너랑 한번 더 만나서 얘기해보고 싶어.",
    "reason": "약속으로 자연스럽게 이어갈 수 있습니다.",
    "bestFor": "상대가 적극적으로 반응하고 있을 때",
    "pressure": "높음",
    "toneMatch": "직진맛이지만 과한 애교나 긴 감정 설명은 피했습니다."
  }
]
```

허용값:

- `level`: `순한맛`, `설렘맛`, `직진맛`
- `pressure`: `낮음`, `중간`, `높음`

## riskyMessage

보내면 위험한 말과 대체 표현이다.

```json
{
  "avoidText": "왜 답장이 늦어?",
  "reason": "상대에게 추궁처럼 느껴질 수 있습니다.",
  "alternative": "바빴나 보다. 편할 때 답해줘."
}
```

## nextAction

다음 행동 추천이다.

```json
{
  "type": "대화 이어가기",
  "reason": "아직 약속을 잡기보다 공통 관심사를 조금 더 확인하는 편이 좋습니다.",
  "suggestedTiming": "지금 바로"
}
```

허용값:

- `type`: `대화 이어가기`, `약속 잡기`, `한 템포 쉬기`, `사과하기`, `관심사 질문하기`
- `suggestedTiming`: `지금 바로`, `조금 뒤`, `내일`, `상대 답장 후`

## safety

안전 판단이다.

```json
{
  "needsToneDown": false,
  "warning": null,
  "blocked": false,
  "blockedReason": null
}
```

차단이 필요한 경우:

```json
{
  "needsToneDown": true,
  "warning": "상대가 불편해하는 신호가 있어 직진맛 답장은 추천하지 않습니다.",
  "blocked": false,
  "blockedReason": null
}
```

## feedbackPrompt

결과 화면 하단의 피드백 수집 문구다.

```json
{
  "question": "이 답장이 도움이 됐나요?",
  "options": ["도움 됨", "별로임"]
}
```

## 실패 응답

대화가 너무 짧거나 발화자가 구분되지 않을 때는 분석을 단정하지 않는다.

```json
{
  "error": {
    "code": "NEED_MORE_CONTEXT",
    "message": "대화가 짧아서 호감 여부를 단정하기 어려워요. 최근 대화 10줄 정도를 더 붙여넣어 주세요."
  }
}
```

허용 오류 코드:

- `NEED_MORE_CONTEXT`
- `SPEAKER_NOT_CLEAR`
- `PRIVATE_INFO_DETECTED`
- `UNSAFE_REQUEST`

## 프론트엔드 표시 원칙

- `status`는 결과 화면 상단 배지로 표시한다.
- `replyOptions`는 카드 3개로 표시한다.
- `userToneProfile`은 접을 수 있는 요약 카드로 표시한다.
- 답장 카드에는 `toneMatch`를 짧게 표시한다.
- 각 답장 카드에는 복사 버튼을 제공한다.
- `riskyMessage`는 경고 영역으로 분리한다.
- `styleFit`은 분위기 분석 다음에 표시하되, 사용자의 선택을 대신하는 판정처럼 보이면 안 된다.
- `safety.warning`이 있으면 답장 후보보다 먼저 보여준다.
- `blocked`가 `true`이면 답장 생성 대신 안전 안내만 보여준다.

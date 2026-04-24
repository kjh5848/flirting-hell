import type { AnalysisRequest, AnalysisResult } from "@flirting-hell/shared";

export function createMockAnalysisResult(request: AnalysisRequest): AnalysisResult {
  const hasLaugh = request.conversationText.includes("ㅋㅋ") || request.conversationText.includes("ㅎㅎ");
  const isDirect = request.replyIntensity === "직진맛";

  return {
    mood: {
      status: hasLaugh ? "호감 있음" : "애매함",
      summary: hasLaugh
        ? "상대가 대화를 가볍게 이어갈 여지가 있습니다."
        : "대화가 이어지지만 아직 호감 신호는 강하지 않습니다.",
      confidence: "보통",
      reasons: [
        hasLaugh ? "웃음 표현이 있어 분위기가 완전히 닫혀 있지는 않습니다." : "상대 반응만으로는 호감 여부를 단정하기 어렵습니다.",
        "최근 대화 맥락을 기준으로 답장 강도는 한 단계 낮게 시작하는 편이 안전합니다."
      ]
    },
    styleFit: {
      status: "더 확인 필요",
      summary: "상대의 연애 스타일은 아직 충분히 드러나지 않았습니다.",
      matchingPoints: ["대화가 완전히 끊기지는 않았습니다."],
      possibleGaps: ["사용자가 원하는 다정함이나 표현 빈도는 더 확인이 필요합니다."],
      checkQuestions: ["너는 연락 자주 하는 편이야?", "좋아하는 사람한테 표현을 자주 하는 편이야?"],
      guidanceMode: request.guidanceMode,
      guidance: "지금 단정하지 말고, 계속 보고 싶은 마음이 있다면 작은 질문으로 상대의 애정 표현 방식을 확인해보세요."
    },
    userToneProfile: {
      summary: "짧고 자연스러운 말투를 유지하는 편으로 보입니다.",
      speechLevel: request.conversationText.includes("요") ? "존댓말" : "반말",
      sentenceLength: request.conversationText.length > 450 ? "보통" : "짧게",
      warmth: "중간",
      playfulness: hasLaugh ? "높음" : "중간",
      directness: isDirect ? "중간" : "낮음",
      emojiUsage: /[😀-🙏]/u.test(request.conversationText) ? "가끔" : "안 씀",
      laughStyle: request.conversationText.includes("ㅋㅋ") ? "ㅋㅋ" : request.conversationText.includes("ㅎㅎ") ? "ㅎㅎ" : "안 씀",
      signaturePatterns: ["부담을 크게 주지 않는 짧은 문장", "상대 반응을 보며 대화를 이어가는 방식"],
      avoidPatterns: ["갑작스러운 장문 고백", "답장을 재촉하는 표현", "상대 선택을 압박하는 말"],
      confidence: "보통"
    },
    replyOptions: [
      {
        level: "순한맛",
        text: "ㅋㅋ 그럼 오늘은 좀 쉬는 날이네. 뭐 하면서 쉬고 있어?",
        reason: "상대가 부담 없이 답할 수 있는 질문입니다.",
        bestFor: "상대 반응을 더 보고 싶을 때",
        pressure: "낮음",
        toneMatch: "짧고 가벼운 말투를 유지했습니다."
      },
      {
        level: "설렘맛",
        text: "너랑 얘기하면 은근 편해서 계속 물어보게 되네 ㅋㅋ",
        reason: "호감을 드러내지만 고백처럼 무겁지는 않습니다.",
        bestFor: "상대도 대화를 이어가고 있을 때",
        pressure: "중간",
        toneMatch: "장난스러운 표현 안에 호감 한 문장을 넣었습니다."
      },
      {
        level: "직진맛",
        text: "이번 주에 시간 맞으면 얼굴 보고 얘기해보고 싶어.",
        reason: "대화를 실제 만남으로 이어갈 수 있습니다.",
        bestFor: "상대가 먼저 질문하거나 반응이 적극적일 때",
        pressure: "높음",
        toneMatch: "과한 애교 없이 담백하게 직진했습니다."
      }
    ],
    riskyMessage: {
      avoidText: "왜 답장이 늦어?",
      reason: "상대에게 추궁이나 압박처럼 느껴질 수 있습니다.",
      alternative: "바빴나 보다. 편할 때 답해줘."
    },
    nextAction: {
      type: isDirect ? "약속 잡기" : "대화 이어가기",
      reason: isDirect ? "직진맛을 선택했다면 구체적인 만남 제안으로 연결할 수 있습니다." : "아직은 대화 온도를 조금 더 확인하는 편이 좋습니다.",
      suggestedTiming: "지금 바로"
    },
    safety: {
      needsToneDown: isDirect,
      warning: isDirect ? "직진맛은 상대 반응이 충분히 좋을 때만 사용하세요." : null,
      blocked: false,
      blockedReason: null
    },
    feedbackPrompt: {
      question: "이 답장이 도움이 됐나요?",
      options: ["도움 됨", "별로임"]
    }
  };
}

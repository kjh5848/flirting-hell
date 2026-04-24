import type { ConversationGoal, GuidanceMode, RelationshipStage, ReplyIntensity, ToneMode } from "./options";

export type ApiErrorCode =
  | "VALIDATION_ERROR"
  | "LIMIT_REACHED"
  | "AI_TIMEOUT"
  | "AI_INVALID_RESPONSE"
  | "UNSAFE_REQUEST"
  | "SERVER_ERROR";

export type ApiError = {
  code: ApiErrorCode;
  message: string;
};

export type ApiSuccess<T> = {
  ok: true;
  data: T;
};

export type ApiFailure = {
  ok: false;
  error: ApiError;
};

export type ApiResponse<T> = ApiSuccess<T> | ApiFailure;

export type UserPreferences = {
  datingStyles: string[];
  preferredPartnerStyles: string[];
  difficultPartnerStyles: string[];
  attractionReasons: string[];
};

export type AnalysisRequest = {
  conversationText: string;
  relationshipStage: RelationshipStage;
  conversationGoal: ConversationGoal;
  replyIntensity: ReplyIntensity;
  guidanceMode: GuidanceMode;
  toneMode: ToneMode;
  userPreferences?: UserPreferences;
};

export type UsageSummary = {
  date: string;
  freeLimit: number;
  usedToday: number;
  remainingToday: number;
  creditBalance: number;
};

export type MoodStatus = "호감 있음" | "애매함" | "부담 가능성" | "잠시 쉬어야 함";
export type Confidence = "낮음" | "보통" | "높음";
export type StyleFitStatus = "잘 맞을 가능성 있음" | "매력은 있지만 피로할 수 있음" | "내 이상형과 차이가 큼" | "더 확인 필요";
export type Pressure = "낮음" | "중간" | "높음";
export type NextActionType = "대화 이어가기" | "약속 잡기" | "한 템포 쉬기" | "사과하기" | "관심사 질문하기";
export type SuggestedTiming = "지금 바로" | "조금 뒤" | "내일" | "상대 답장 후";

export type AnalysisResult = {
  mood: {
    status: MoodStatus;
    summary: string;
    confidence: Confidence;
    reasons: string[];
  };
  styleFit: {
    status: StyleFitStatus;
    summary: string;
    matchingPoints: string[];
    possibleGaps: string[];
    checkQuestions: string[];
    guidanceMode: GuidanceMode;
    guidance: string;
  };
  userToneProfile: {
    summary: string;
    speechLevel: "반말" | "존댓말" | "섞어서" | "판단 어려움";
    sentenceLength: "짧게" | "보통" | "길게" | "판단 어려움";
    warmth: "낮음" | "중간" | "높음" | "판단 어려움";
    playfulness: "낮음" | "중간" | "높음" | "판단 어려움";
    directness: "낮음" | "중간" | "높음" | "판단 어려움";
    emojiUsage: "안 씀" | "가끔" | "자주" | "판단 어려움";
    laughStyle: "안 씀" | "ㅋㅋ" | "ㅎㅎ" | "섞어서" | "판단 어려움";
    signaturePatterns: string[];
    avoidPatterns: string[];
    confidence: Confidence;
  };
  replyOptions: Array<{
    level: ReplyIntensity;
    text: string;
    reason: string;
    bestFor: string;
    pressure: Pressure;
    toneMatch: string;
  }>;
  riskyMessage: {
    avoidText: string;
    reason: string;
    alternative: string;
  };
  nextAction: {
    type: NextActionType;
    reason: string;
    suggestedTiming: SuggestedTiming;
  };
  safety: {
    needsToneDown: boolean;
    warning: string | null;
    blocked: boolean;
    blockedReason: string | null;
  };
  feedbackPrompt: {
    question: string;
    options: ["도움 됨", "별로임"];
  };
};

export type CreateAnalysisData = {
  analysisId: string;
  usage: Omit<UsageSummary, "date">;
  result: AnalysisResult;
};

export type EventRequest = {
  eventName: "reply_copied" | "feedback_submitted";
  analysisId?: string;
  metadata?: Record<string, unknown>;
};

export type EventData = {
  eventId: string;
};

export type HealthData = {
  status: "ok";
  service: "flirting-hell-api";
  version: string;
};

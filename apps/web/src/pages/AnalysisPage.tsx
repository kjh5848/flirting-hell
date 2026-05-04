import { useEffect, useState } from "react";
import type { UsageSummary } from "@flirting-hell/shared";
import { CinematicIntroCanvas } from "../features/intro/components/CinematicIntroCanvas";
import { StylePreferenceForm } from "../features/profile/components/StylePreferenceForm";
import { useProfileSettings } from "../features/profile/hooks/useProfileSettings";
import { useUsageQuota } from "../features/usage/hooks/useUsageQuota";
import { Button } from "../shared/ui/Button";
import { Toast } from "../shared/ui/Toast";

type ToastState = {
  message: string;
  tone: "success" | "error" | "info";
};

type AppTabId = "home" | "rooms" | "saved" | "profile" | "billing";
type AppViewId =
  | AppTabId
  | "newRoom"
  | "roomDetail"
  | "roomSettings"
  | "turnInsight"
  | "turnInput"
  | "turnLoading"
  | "turnResult";
type StrategyId = "dating" | "relationshipCheck" | "datePlan" | "marriageValues" | "slowDown";
type ChatSpeaker = "me" | "them" | "coach";
type IntakeSourceType = "kakao" | "dm" | "telegram" | "sms" | "situation" | "unknown";
type ParsedParticipantRole = "me" | "them" | "unknown";

type StrategyOption = {
  id: StrategyId;
  title: string;
  description: string;
};

type StrategyRecommendation = {
  summary: string;
  recommendedReply: string;
  alternatives: string[];
  riskyPhrase: string;
  nextAction: string;
  why: string;
};

type ChatTurn = {
  id: string;
  strategyId: StrategyId;
  question: string;
} & StrategyRecommendation;

type ChatRoom = {
  id: string;
  name: string;
  relationLabel: string;
  status: string;
  lastMessage: string;
  lastUpdated: string;
  savedReplies: number;
  caution: string;
  history: Array<{
    id: string;
    speaker: ChatSpeaker;
    text: string;
    time?: string;
  }>;
  turns: ChatTurn[];
};

type ParsedIntake = {
  sourceType: IntakeSourceType;
  sourceLabel: string;
  confidenceLabel: string;
  participants: Array<{
    id: string;
    role: ParsedParticipantRole;
    label: string;
    text: string;
  }>;
  summary: string;
  currentState: string;
  recommendedStrategyId: StrategyId;
  warnings: string[];
};

type SavedIntakeDraft = {
  id: string;
  roomId: string;
  sourceLabel: string;
  title: string;
  summary: string;
  text: string;
  savedAt: string;
};

type SavedReplyCard = {
  id: string;
  roomId: string;
  turnId: string;
  strategyId: StrategyId;
  strategyTitle: string;
  reply: string;
  reason: string;
  sourceSummary: string;
  riskyPhrase: string;
  nextAction: string;
  savedAt: string;
};

type ViewCopy = {
  title: string;
  description: string;
};

const introStorageKey = "flirting-hell:intro-seen:v1";

const strategyOptions: StrategyOption[] = [
  {
    id: "dating",
    title: "연애로 발전",
    description: "호감 표현을 한 단계만 올립니다."
  },
  {
    id: "relationshipCheck",
    title: "여친/남친 여부 확인",
    description: "부담 없이 애인 여부를 확인합니다."
  },
  {
    id: "datePlan",
    title: "약속 잡기",
    description: "대화 흐름에서 자연스럽게 만남을 제안합니다."
  },
  {
    id: "marriageValues",
    title: "결혼 가치관",
    description: "무겁지 않게 장기 가치관을 탐색합니다."
  },
  {
    id: "slowDown",
    title: "속도 조절",
    description: "부담을 낮추고 대화를 유지합니다."
  }
];

const strategyRecommendations: Record<StrategyId, StrategyRecommendation> = {
  dating: {
    summary: "상대가 대화를 이어갈 여지를 줬으니, 호감 표현은 짧게만 올리는 편이 안전합니다.",
    recommendedReply: "나도 요즘 너랑 얘기하는 게 편하더라. 그래서 더 궁금해졌나 봐.",
    alternatives: ["너랑 얘기하면 이상하게 시간이 빨리 가.", "갑자기 이렇게 말하니까 나도 좀 설레네."],
    riskyPhrase: "그럼 우리 사귀는 거야?",
    nextAction: "답장 후 상대가 농담으로 넘기면 바로 고백하지 말고 한 번 더 대화를 이어갑니다.",
    why: "직진 고백이 아니라 감정의 온도만 한 단계 올려서 상대 반응을 볼 수 있습니다."
  },
  relationshipCheck: {
    summary: "애인 여부는 캐묻는 느낌보다 근황 질문 안에 넣는 편이 자연스럽습니다.",
    recommendedReply: "근데 너는 쉬는 날 보통 누구랑 보내? 약속 많은 편이야?",
    alternatives: ["주말엔 보통 친구들이랑 보내는 편이야?", "쉬는 날엔 데이트보다 집이 더 좋은 타입이야?"],
    riskyPhrase: "너 남친/여친 있어 없어?",
    nextAction: "상대가 사람 얘기를 꺼내면 관계를 단정하지 말고 편하게 이어갑니다.",
    why: "상대의 생활 패턴을 묻는 문장이라 애인 여부를 직접 압박하지 않습니다."
  },
  datePlan: {
    summary: "집에 있다는 흐름에서는 큰 약속보다 가벼운 카페/산책 제안이 맞습니다.",
    recommendedReply: "그럼 다음에 쉬는 날 맞으면 커피 한 잔 하자. 부담 없이 잠깐.",
    alternatives: ["다음에 시간 맞으면 그 얘기했던 카페 가보자.", "너무 거창한 거 말고 가볍게 산책 한 번 어때?"],
    riskyPhrase: "오늘 당장 나와.",
    nextAction: "상대가 망설이면 날짜를 압박하지 말고 선택지를 열어둡니다.",
    why: "만남 제안은 분명하지만, 시간과 부담을 낮춰 거절 압박을 줄입니다."
  },
  marriageValues: {
    summary: "결혼 이야기는 초반에 무겁게 던지면 부담이 커질 수 있어 가치관 대화로 우회합니다.",
    recommendedReply: "너는 연애할 때 편한 게 제일 중요해, 아니면 표현을 많이 하는 게 더 중요해?",
    alternatives: ["오래 만나는 관계에서 제일 중요하다고 생각하는 게 있어?", "너는 연애할 때 서로 생활 리듬 맞는 게 중요해?"],
    riskyPhrase: "너 결혼 생각 있어?",
    nextAction: "상대가 가볍게 답하면 깊게 파고들지 말고 기준 하나만 더 확인합니다.",
    why: "결혼 단어 없이도 장기 관계에서 중요한 기준을 확인할 수 있습니다."
  },
  slowDown: {
    summary: "상대 반응을 더 봐야 하는 상황이라면 호감 확인보다 편한 대화를 우선합니다.",
    recommendedReply: "ㅋㅋ 그럼 오늘은 푹 쉬어. 심심하면 나랑 조금 더 얘기하고.",
    alternatives: ["괜찮아. 답장 천천히 해도 돼.", "오늘은 쉬는 날이니까 편하게 있어. 나중에 얘기해도 돼."],
    riskyPhrase: "왜 답장이 이렇게 늦어?",
    nextAction: "상대가 다시 말을 걸면 그때 전략을 다시 선택합니다.",
    why: "상대에게 선택권을 주면서도 대화 문은 열어둡니다."
  }
};

const defaultConversationText = `나: 오늘 뭐해?
상대: 그냥 집에 있어 ㅋㅋ
나: 오 쉬는 날이네. 뭐하면서 쉬고 있어?
상대: 나도 집. 갑자기 네 생각나서`;

const sourceLabels: Record<IntakeSourceType, string> = {
  kakao: "카톡으로 보여요",
  dm: "DM으로 보여요",
  telegram: "텔레그램으로 보여요",
  sms: "문자로 보여요",
  situation: "상황 설명으로 보여요",
  unknown: "발화자 구분이 애매해요"
};

function detectSourceType(text: string): IntakeSourceType {
  const normalizedText = text.toLowerCase();

  if (/텔레그램|telegram/.test(normalizedText)) {
    return "telegram";
  }

  if (/dm|인스타|instagram|디엠/.test(normalizedText)) {
    return "dm";
  }

  if (/문자|sms|메시지/.test(normalizedText)) {
    return "sms";
  }

  if (/오전|오후|\d{1,2}:\d{2}|나[:：]|상대[:：]/.test(text)) {
    return "kakao";
  }

  if (text.trim().length >= 20) {
    return "situation";
  }

  return "unknown";
}

function inferStrategyId(text: string): StrategyId {
  if (/카페|커피|만나|약속|보자|산책/.test(text)) {
    return "datePlan";
  }

  if (/남친|여친|애인|소개팅|솔로/.test(text)) {
    return "relationshipCheck";
  }

  if (/결혼|가치관|오래|미래/.test(text)) {
    return "marriageValues";
  }

  if (/답장|늦|부담|천천히|피곤|정신없/.test(text)) {
    return "slowDown";
  }

  return "dating";
}

function parseConversationText(text: string): ParsedIntake {
  const sourceType = detectSourceType(text);
  const recommendedStrategyId = inferStrategyId(text);
  const lines = text
    .split(/\n+/)
    .map((line) => line.trim())
    .filter(Boolean);
  const participants = lines.slice(0, 8).map((line, index) => {
    const [rawLabel, ...rest] = line.split(/[:：]/);
    const hasExplicitLabel = rest.length > 0;
    const label = hasExplicitLabel ? rawLabel.trim() : "상황";
    const body = hasExplicitLabel ? rest.join(":").trim() : line;
    const role: ParsedParticipantRole = /^나|^me$/i.test(label)
      ? "me"
      : /^상대|^상대방|^친구|^그|^그녀|^them$/i.test(label)
        ? "them"
        : sourceType === "situation"
          ? "unknown"
          : "unknown";

    return {
      id: `parsed-${index}`,
      role,
      label,
      text: body
    };
  });
  const hasMe = participants.some((participant) => participant.role === "me");
  const hasThem = participants.some((participant) => participant.role === "them");
  const sourceLabel = hasMe && hasThem ? sourceLabels[sourceType] : sourceType === "situation" ? sourceLabels.situation : sourceLabels.unknown;
  const confidenceLabel = hasMe && hasThem ? "나와 상대를 분류했어요" : "발화자 수정이 필요할 수 있어요";
  const summary = /생각나|보고싶|궁금/.test(text)
    ? "상대가 먼저 감정 단서를 줬고, 사용자는 답장 강도를 고민하는 상황입니다."
    : /카페|커피|만나|약속/.test(text)
      ? "대화 안에 장소나 만남 단서가 있어 약속 제안 타이밍을 볼 수 있습니다."
      : /늦|정신없|부담/.test(text)
        ? "상대가 답장 속도나 상황을 설명했으므로 압박보다 여유가 필요한 상황입니다."
        : "대화 맥락을 요약했고, 지금 필요한 전략을 먼저 고르는 단계입니다.";
  const currentState = recommendedStrategyId === "dating"
    ? "호감 가능성 있음"
    : recommendedStrategyId === "datePlan"
      ? "약속 제안 타이밍"
      : recommendedStrategyId === "slowDown"
        ? "속도 조절 필요"
        : recommendedStrategyId === "marriageValues"
          ? "가치관 탐색 가능"
          : "관계 정보 확인 필요";
  const warnings = [
    "실명, 전화번호, 주소, 회사명은 지우고 분석하세요.",
    strategyRecommendations[recommendedStrategyId].riskyPhrase,
    hasMe && hasThem ? "분류가 맞는지 한 번만 확인하세요." : "발화자가 애매하면 나/상대를 수정해서 보는 편이 안전합니다."
  ];

  return {
    sourceType,
    sourceLabel,
    confidenceLabel,
    participants,
    summary,
    currentState,
    recommendedStrategyId,
    warnings
  };
}

function createSavedIntakeDraft(roomId: string, text: string, index: number): SavedIntakeDraft {
  const parsed = parseConversationText(text);
  const firstLine = text.split(/\n+/).map((line) => line.trim()).find(Boolean) ?? "상황 메모";

  return {
    id: `${roomId}-draft-${index}`,
    roomId,
    sourceLabel: parsed.sourceLabel,
    title: firstLine.replace(/^나[:：]\s*/, "").slice(0, 28),
    summary: parsed.summary,
    text,
    savedAt: index === 1 ? "방금" : "이전 분석"
  };
}

const initialSavedIntakes: SavedIntakeDraft[] = [
  createSavedIntakeDraft("jiu", defaultConversationText, 0)
];

const chatRooms: ChatRoom[] = [
  {
    id: "jiu",
    name: "지우",
    relationLabel: "썸 · 답장 대기",
    status: "상대 마음 확인 중",
    lastMessage: "나도 집. 갑자기 네 생각나서",
    lastUpdated: "오늘 8:55",
    savedReplies: 3,
    caution: "호감 표현은 가능하지만 고백처럼 들리면 부담이 커질 수 있습니다.",
    history: [
      { id: "jiu-1", speaker: "them", text: "오늘 뭐해?", time: "오전 11:09" },
      { id: "jiu-2", speaker: "me", text: "그냥 집에 있어 ㅋㅋ 너는?", time: "오전 11:10" },
      { id: "jiu-3", speaker: "them", text: "나도 집. 갑자기 네 생각나서", time: "오전 11:13" },
      { id: "jiu-4", speaker: "coach", text: "상대가 먼저 감정 단서를 줬습니다. 바로 고백보다 다음 질문으로 온도를 확인하세요.", time: "분석 기록" }
    ],
    turns: [
      {
        id: "TURN-003",
        strategyId: "dating",
        question: "호감 표현을 올려도 될까?",
        ...strategyRecommendations.dating
      },
      {
        id: "TURN-002",
        strategyId: "relationshipCheck",
        question: "애인이 있는지 자연스럽게 확인하기",
        ...strategyRecommendations.relationshipCheck
      }
    ]
  },
  {
    id: "haneul",
    name: "하늘",
    relationLabel: "소개팅 · 약속 조율",
    status: "다음 만남 제안 가능",
    lastMessage: "그 카페 얘기하니까 생각나네요",
    lastUpdated: "어제 22:10",
    savedReplies: 2,
    caution: "만남 제안은 좋지만 일정 압박보다 선택지를 열어두는 편이 안전합니다.",
    history: [
      { id: "haneul-1", speaker: "them", text: "오늘 얘기 편했어요.", time: "오후 9:58" },
      { id: "haneul-2", speaker: "me", text: "저도요. 시간이 빨리 갔네요.", time: "오후 10:01" },
      { id: "haneul-3", speaker: "them", text: "그 카페 얘기하니까 생각나네요.", time: "오후 10:10" },
      { id: "haneul-4", speaker: "coach", text: "상대가 장소 단서를 줬습니다. 짧은 다음 약속 제안이 자연스럽습니다.", time: "분석 기록" }
    ],
    turns: [
      {
        id: "TURN-001",
        strategyId: "datePlan",
        question: "다음 약속 잡기",
        ...strategyRecommendations.datePlan
      }
    ]
  },
  {
    id: "doyun",
    name: "도윤",
    relationLabel: "장기 대화 · 속도 조절",
    status: "답장 강도 낮추기",
    lastMessage: "요즘 좀 정신없어서 답이 느렸어",
    lastUpdated: "월요일",
    savedReplies: 1,
    caution: "추궁으로 느껴질 수 있어 답장 속도보다 상대 상황을 먼저 인정해야 합니다.",
    history: [
      { id: "doyun-1", speaker: "me", text: "요즘 답이 좀 늦네. 바빴어?", time: "오후 7:42" },
      { id: "doyun-2", speaker: "them", text: "응 미안. 요즘 좀 정신없어서 답이 느렸어.", time: "오후 8:15" },
      { id: "doyun-3", speaker: "coach", text: "상대가 상황 설명을 했습니다. 추궁보다 여유를 주는 답장이 맞습니다.", time: "분석 기록" }
    ],
    turns: [
      {
        id: "TURN-004",
        strategyId: "slowDown",
        question: "부담 주지 않고 이어가기",
        ...strategyRecommendations.slowDown
      }
    ]
  }
];

function createSavedReplyCard({
  roomId,
  turnId,
  strategy,
  recommendation,
  sourceSummary,
  savedAt
}: {
  roomId: string;
  turnId: string;
  strategy: StrategyOption;
  recommendation: StrategyRecommendation;
  sourceSummary: string;
  savedAt: string;
}): SavedReplyCard {
  return {
    id: `${roomId}-${turnId}-${strategy.id}`,
    roomId,
    turnId,
    strategyId: strategy.id,
    strategyTitle: strategy.title,
    reply: recommendation.recommendedReply,
    reason: recommendation.why,
    sourceSummary,
    riskyPhrase: recommendation.riskyPhrase,
    nextAction: recommendation.nextAction,
    savedAt
  };
}

const initialSavedReplyCards: SavedReplyCard[] = chatRooms.flatMap((room) =>
  room.turns.map((turn, index) => {
    const strategy = strategyOptions.find((item) => item.id === turn.strategyId) ?? strategyOptions[0];

    return createSavedReplyCard({
      roomId: room.id,
      turnId: turn.id,
      strategy,
      recommendation: turn,
      sourceSummary: turn.summary,
      savedAt: index === 0 ? "최근 저장" : "이전 저장"
    });
  })
);

const copyByView: Record<AppViewId, ViewCopy> = {
  home: {
    title: "플러팅지옥",
    description: "오늘 이어갈 상담방만 빠르게 확인합니다."
  },
  rooms: {
    title: "상담방",
    description: "상대별 대화 흐름을 고릅니다."
  },
  newRoom: {
    title: "새 상담방",
    description: "상대별 설정은 상담방을 만들 때만 묻습니다."
  },
  roomDetail: {
    title: "상담방",
    description: "대화 흐름과 답장 기록을 한눈에 봅니다."
  },
  roomSettings: {
    title: "상대 설정",
    description: "이 상담방에만 적용되는 기준입니다."
  },
  turnInsight: {
    title: "상황 요약",
    description: "붙여넣은 내용을 분류하고 지금 필요한 전략을 고릅니다."
  },
  turnInput: {
    title: "대화 붙여넣기",
    description: "카톡, DM, 문자, 상황 설명을 그대로 붙여넣습니다."
  },
  turnLoading: {
    title: "분석 중",
    description: "나와 상대, 상황 설명을 먼저 나누고 있습니다."
  },
  turnResult: {
    title: "답장 추천",
    description: "채팅방 흐름 안에서 답장과 다음 행동을 봅니다."
  },
  saved: {
    title: "저장된 답장",
    description: "대화별 추천 답장과 선택 이유를 다시 봅니다."
  },
  profile: {
    title: "내 정보",
    description: "전역 말투, 연애 스타일, 조언 수위를 관리합니다."
  },
  billing: {
    title: "분석권",
    description: "무료 사용량과 분석권 패키지를 확인합니다."
  }
};

function tabForView(view: AppViewId): AppTabId {
  if (["rooms", "newRoom", "roomDetail", "roomSettings", "turnInput", "turnLoading", "turnInsight", "turnResult"].includes(view)) {
    return "rooms";
  }

  return view as AppTabId;
}

function shouldShowBottomNav(view: AppViewId) {
  return ["home", "rooms", "newRoom", "roomDetail", "roomSettings", "saved", "profile", "billing"].includes(view);
}

function OpeningIntro({ onDismiss }: { onDismiss: () => void }) {
  return (
    <div className="opening-intro" role="status" aria-label="플러팅지옥 시작 화면">
      <div className="cinema-scene" aria-hidden="true">
        <CinematicIntroCanvas />
        <div className="cinema-vignette" />
      </div>
      <div className="opening-copy">
        <p>FLIRTING HELL</p>
        <h2>플러팅지옥</h2>
        <span>대화 루프에서 답장으로 탈출</span>
      </div>
      <button type="button" className="opening-skip" onClick={onDismiss}>바로 시작</button>
    </div>
  );
}

function BackButton({ onClick, label = "뒤로" }: { onClick: () => void; label?: string }) {
  return (
    <button type="button" className="mb-3 inline-flex items-center text-sm font-black text-ink-muted" onClick={onClick}>
      ← {label}
    </button>
  );
}

function HomeSection({
  usage,
  savedReplyCount,
  savedReplyCountsByRoom,
  onCreateRoom,
  onOpenRoom,
  onOpenRooms,
  onOpenSaved
}: {
  usage: UsageSummary | null;
  savedReplyCount: number;
  savedReplyCountsByRoom: Record<string, number>;
  onCreateRoom: () => void;
  onOpenRoom: (roomId: string) => void;
  onOpenRooms: () => void;
  onOpenSaved: () => void;
}) {
  const remaining = usage?.remainingToday ?? 3;

  return (
    <section className="section-screen space-y-3" aria-label="홈">
      <div className="rounded-[22px] bg-white p-4 shadow-sm ring-1 ring-ink/[0.06]">
        <div className="flex items-start justify-between gap-3">
          <div>
            <p className="text-xs font-black uppercase tracking-[0.16em] text-hell-600">Today</p>
            <h2 className="mt-1 text-2xl font-black leading-[1.08] tracking-[-0.055em] text-ink">이어갈 상담방</h2>
            <p className="mt-1 text-sm font-semibold leading-5 text-ink-muted">대화나 상황을 붙여넣으면 먼저 요약하고 전략을 추천합니다.</p>
          </div>
          <Button type="button" className="min-h-10 shrink-0 px-3 text-xs" onClick={onCreateRoom}>새 상담방</Button>
        </div>
      </div>

      <div className="grid grid-cols-3 gap-2">
        <button type="button" className="rounded-[18px] bg-white p-3 text-left ring-1 ring-ink/[0.06]" onClick={onOpenRooms}>
          <span className="text-[11px] font-black text-ink-faint">상담방</span>
          <strong className="mt-1 block text-xl font-black text-ink">{chatRooms.length}</strong>
          <p className="mt-1 text-xs font-bold text-ink-muted">진행 중</p>
        </button>
        <div className="rounded-[18px] bg-white p-3 ring-1 ring-ink/[0.06]">
          <span className="text-[11px] font-black text-ink-faint">무료</span>
          <strong className="mt-1 block text-xl font-black text-ink">{remaining}</strong>
          <p className="mt-1 text-xs font-bold text-ink-muted">남은 분석</p>
        </div>
        <button type="button" className="rounded-[18px] bg-white p-3 text-left ring-1 ring-ink/[0.06]" onClick={onOpenSaved}>
          <span className="text-[11px] font-black text-ink-faint">저장</span>
          <strong className="mt-1 block text-xl font-black text-ink">{savedReplyCount}</strong>
          <p className="mt-1 text-xs font-bold text-ink-muted">답장 카드</p>
        </button>
      </div>

      <div className="rounded-[22px] bg-white p-3 shadow-sm ring-1 ring-ink/[0.06]">
        <div className="mb-2 flex items-center justify-between gap-3">
          <h3 className="text-base font-black tracking-[-0.03em] text-ink">최근 상담방</h3>
          <button type="button" className="text-xs font-black text-hell-600" onClick={onOpenRooms}>전체보기</button>
        </div>
        <div className="grid gap-2">
          {chatRooms.slice(0, 3).map((room) => (
            <button key={room.id} type="button" className="chat-room-row" onClick={() => onOpenRoom(room.id)}>
              <span className="chat-room-avatar">{room.name.slice(0, 1)}</span>
              <span className="min-w-0 flex-1">
                <span className="flex items-center justify-between gap-2">
                  <strong className="truncate text-sm font-black text-ink">{room.name}</strong>
                  <small className="shrink-0 text-[11px] font-bold text-ink-faint">{room.lastUpdated}</small>
                </span>
                <span className="mt-1 block truncate text-xs font-bold text-ink-muted">{room.lastMessage}</span>
              </span>
              <span className="rounded-full bg-blush px-2 py-1 text-[10px] font-black text-hell-700">{savedReplyCountsByRoom[room.id] ?? 0}</span>
            </button>
          ))}
        </div>
      </div>
    </section>
  );
}

function RoomsListSection({
  selectedRoomId,
  savedReplyCountsByRoom,
  onSelectRoom,
  onCreateRoom
}: {
  selectedRoomId: string;
  savedReplyCountsByRoom: Record<string, number>;
  onSelectRoom: (roomId: string) => void;
  onCreateRoom: () => void;
}) {
  return (
    <section className="section-screen space-y-3" aria-label="상담방 목록">
      <div className="rounded-[22px] bg-white p-3 shadow-sm ring-1 ring-ink/[0.06]">
        <div className="mb-2 flex items-center justify-between gap-3">
          <div>
            <p className="text-xs font-black uppercase tracking-[0.16em] text-hell-600">Rooms</p>
            <h2 className="mt-1 text-xl font-black tracking-[-0.04em] text-ink">상담방 {chatRooms.length}개</h2>
          </div>
          <Button type="button" className="min-h-10 px-3 text-xs" onClick={onCreateRoom}>새 상담방</Button>
        </div>
        <div className="grid gap-2">
          {chatRooms.map((room) => (
            <button
              key={room.id}
              type="button"
              className={`chat-room-row ${room.id === selectedRoomId ? "is-active" : ""}`}
              onClick={() => onSelectRoom(room.id)}
            >
              <span className="chat-room-avatar">{room.name.slice(0, 1)}</span>
              <span className="min-w-0 flex-1">
                <span className="flex items-center justify-between gap-2">
                  <strong className="truncate text-sm font-black text-ink">{room.name}</strong>
                  <small className="shrink-0 text-[11px] font-bold text-ink-faint">{room.lastUpdated}</small>
                </span>
                <span className="mt-1 block truncate text-xs font-bold text-ink-muted">{room.relationLabel} · {room.lastMessage}</span>
              </span>
              <span className="rounded-full bg-blush px-2 py-1 text-[10px] font-black text-hell-700">{savedReplyCountsByRoom[room.id] ?? 0}</span>
            </button>
          ))}
        </div>
      </div>
    </section>
  );
}

function NewRoomSection({ onCancel, onCreate }: { onCancel: () => void; onCreate: () => void }) {
  return (
    <section className="section-screen space-y-3" aria-label="새 상담방 만들기">
      <BackButton onClick={onCancel} />
      <article className="rounded-[22px] bg-white p-4 shadow-sm ring-1 ring-ink/[0.06]">
        <p className="text-xs font-black uppercase tracking-[0.16em] text-hell-600">Room Setup</p>
        <h2 className="mt-1 text-xl font-black tracking-[-0.04em] text-ink">상대별 설정</h2>
        <p className="mt-2 text-sm font-semibold leading-6 text-ink-muted">실명 대신 별칭만 사용합니다. 이 설정은 이 상담방 안에서만 답장에 반영됩니다.</p>
        <div className="mt-4 grid gap-3">
          <label className="field-stack">
            <span>상대 별칭</span>
            <input defaultValue="지우" aria-label="상대 별칭" />
          </label>
          <label className="field-stack">
            <span>현재 관계</span>
            <select defaultValue="썸" aria-label="현재 관계">
              <option>짝사랑</option>
              <option>썸</option>
              <option>소개팅</option>
              <option>연인</option>
              <option>재회 고민</option>
              <option>기타</option>
            </select>
          </label>
          <label className="field-stack">
            <span>현재 고민</span>
            <select defaultValue="마음 확인" aria-label="현재 고민">
              <option>마음 확인</option>
              <option>대화 이어가기</option>
              <option>약속 잡기</option>
              <option>속도 조절</option>
            </select>
          </label>
          <label className="field-stack">
            <span>조심할 점</span>
            <textarea defaultValue="너무 급하게 고백처럼 들리지 않게" aria-label="조심할 점" />
          </label>
        </div>
      </article>
      <Button type="button" className="w-full" onClick={onCreate}>상담방 만들고 상세 보기</Button>
    </section>
  );
}

function RoomDetailSection({
  room,
  savedIntakes,
  savedReplies,
  onBack,
  onEditSettings,
  onStartTurn
}: {
  room: ChatRoom;
  savedIntakes: SavedIntakeDraft[];
  savedReplies: SavedReplyCard[];
  onBack: () => void;
  onEditSettings: () => void;
  onStartTurn: () => void;
}) {
  const recentIntakes = savedIntakes.filter((item) => item.roomId === room.id).slice(0, 3);
  const roomSavedReplies = savedReplies.filter((item) => item.roomId === room.id).slice(0, 3);

  return (
    <section className="section-screen room-workspace" aria-label="상담방 상세">
      <div className="room-topbar">
        <button type="button" onClick={onBack} aria-label="상담방 목록으로 돌아가기">←</button>
        <span>{room.relationLabel}</span>
        <button type="button" onClick={onEditSettings} aria-label="상대 설정 수정">설정</button>
      </div>

      <article className="room-hero-card">
        <div>
          <p>상담방</p>
          <h2>{room.name}</h2>
          <span>{room.status}</span>
        </div>
        <button type="button" onClick={onStartTurn}>대화나 상황 붙여넣기</button>
      </article>

      <article className="room-summary-card">
        <span>현재 조심할 점</span>
        <p>{room.caution}</p>
      </article>

      <div className="workspace-section-title">
        <h3>저장한 입력</h3>
        <span>{recentIntakes.length}개</span>
      </div>
      <div className="saved-intake-list">
        {recentIntakes.length > 0 ? recentIntakes.map((item) => (
          <button key={item.id} type="button" className="saved-intake-card" onClick={onStartTurn}>
            <span>{item.sourceLabel} · {item.savedAt}</span>
            <strong>{item.title}</strong>
            <p>{item.summary}</p>
          </button>
        )) : (
          <button type="button" className="saved-intake-empty" onClick={onStartTurn}>
            아직 저장된 입력이 없습니다. 카톡, DM, 문자, 상황 설명을 먼저 붙여넣어 보세요.
          </button>
        )}
      </div>

      {roomSavedReplies.length > 0 ? (
        <>
          <div className="workspace-section-title">
            <h3>저장된 답장</h3>
            <span>{roomSavedReplies.length}개</span>
          </div>
          <div className="saved-intake-list">
            {roomSavedReplies.map((reply) => (
              <button key={reply.id} type="button" className="saved-reply-preview" onClick={onStartTurn}>
                <span>{reply.strategyTitle} · {reply.savedAt}</span>
                <strong>{reply.reply}</strong>
                <p>{reply.reason}</p>
              </button>
            ))}
          </div>
        </>
      ) : null}
    </section>
  );
}

function RoomSettingsSection({ room, onBack }: { room: ChatRoom; onBack: () => void }) {
  return (
    <section className="section-screen space-y-3" aria-label="상대 설정">
      <BackButton onClick={onBack} label={`${room.name} 상담방`} />
      <article className="rounded-[22px] bg-white p-4 shadow-sm ring-1 ring-ink/[0.06]">
        <p className="text-xs font-black uppercase tracking-[0.16em] text-hell-600">Room Setting</p>
        <h2 className="mt-1 text-xl font-black tracking-[-0.04em] text-ink">{room.name}에게만 적용</h2>
        <div className="mt-4 grid gap-3">
          <label className="field-stack">
            <span>현재 관계</span>
            <select defaultValue={room.relationLabel.split(" · ")[0]} aria-label="현재 관계 수정">
              <option>짝사랑</option>
              <option>썸</option>
              <option>소개팅</option>
              <option>연인</option>
              <option>재회 고민</option>
            </select>
          </label>
          <label className="field-stack">
            <span>이 상대에게 조심할 점</span>
            <textarea defaultValue={room.caution} aria-label="이 상대에게 조심할 점" />
          </label>
        </div>
      </article>
      <Button type="button" className="w-full" onClick={onBack}>저장하고 돌아가기</Button>
    </section>
  );
}

function TurnInsightSection({
  room,
  parsedIntake,
  selectedStrategyId,
  onBack,
  onSelectStrategy,
  onNext
}: {
  room: ChatRoom;
  parsedIntake: ParsedIntake;
  selectedStrategyId: StrategyId;
  onBack: () => void;
  onSelectStrategy: (strategyId: StrategyId) => void;
  onNext: () => void;
}) {
  const selectedStrategy = strategyOptions.find((strategy) => strategy.id === selectedStrategyId) ?? strategyOptions[0];
  const recommendedStrategy = strategyOptions.find((strategy) => strategy.id === parsedIntake.recommendedStrategyId) ?? selectedStrategy;

  return (
    <section className="section-screen analysis-workspace" aria-label="상황 요약과 전략 선택">
      <BackButton onClick={onBack} label="입력 수정" />
      <article className="intake-insight-card">
        <div className="insight-card-header">
          <span>{parsedIntake.sourceLabel}</span>
          <strong>{parsedIntake.currentState}</strong>
        </div>
        <h2>{room.name}와의 상황 요약</h2>
        <p>{parsedIntake.summary}</p>
        <div className="parsed-line-list">
          {parsedIntake.participants.map((participant) => (
            <div key={participant.id} className={`parsed-line ${participant.role}`}>
              <span>{participant.label}</span>
              <strong>{participant.text}</strong>
            </div>
          ))}
        </div>
        <div className="warning-list">
          <strong>주의 신호</strong>
          {parsedIntake.warnings.map((warning) => (
            <span key={warning}>{warning}</span>
          ))}
        </div>
      </article>

      <article className="recommended-strategy-card">
        <span>추천 전략</span>
        <h2>{recommendedStrategy.title}</h2>
        <p>{recommendedStrategy.description}</p>
        <button type="button" onClick={() => onSelectStrategy(recommendedStrategy.id)}>
          이 전략으로 보기
        </button>
      </article>

      <div className="strategy-chip-grid compact">
        {strategyOptions.map((strategy) => (
          <button
            key={strategy.id}
            type="button"
            className={strategy.id === selectedStrategy.id ? "is-active" : ""}
            onClick={() => onSelectStrategy(strategy.id)}
          >
            <strong>{strategy.title}</strong>
            <span>{strategy.description}</span>
          </button>
        ))}
      </div>
      <Button type="button" className="w-full" onClick={onNext}>{selectedStrategy.title} 기준으로 답장 보기</Button>
    </section>
  );
}

function TurnInputSection({
  room,
  conversationText,
  savedIntakes,
  onBack,
  onLoadSavedIntake,
  onConversationTextChange,
  onAnalyze
}: {
  room: ChatRoom;
  conversationText: string;
  savedIntakes: SavedIntakeDraft[];
  onBack: () => void;
  onLoadSavedIntake: (text: string) => void;
  onConversationTextChange: (text: string) => void;
  onAnalyze: () => void;
}) {
  const isReady = conversationText.trim().length >= 20;
  const parsedPreview = parseConversationText(conversationText);
  const roomSavedIntakes = savedIntakes.filter((item) => item.roomId === room.id).slice(0, 4);

  return (
    <section className="section-screen input-workspace space-y-3" aria-label="대화 붙여넣기">
      <BackButton onClick={onBack} label={`${room.name} 상담방`} />
      <article className="paste-editor-card">
        <p className="text-xs font-black uppercase tracking-[0.16em] text-hell-600">Paste Anything</p>
        <h2 className="mt-1 text-xl font-black tracking-[-0.04em] text-ink">대화나 상황을 그대로 붙여넣기</h2>
        <p className="mt-2 text-sm font-semibold leading-6 text-ink-muted">카톡, DM, 텔레그램, 문자, 상황 설명을 먼저 넣으면 앱이 발화자와 상태를 나눠봅니다.</p>
        <div className="mt-3 rounded-[16px] bg-blush px-3 py-2 text-xs font-black text-hell-700">
          자동 감지: {parsedPreview.sourceLabel} · {parsedPreview.confidenceLabel}
        </div>
        <label className="field-stack">
          <span>붙여넣을 내용</span>
          <textarea
            value={conversationText}
            onChange={(event) => onConversationTextChange(event.target.value)}
            aria-label="대화나 상황 내용"
          />
        </label>
        <p className="mt-3 rounded-[16px] bg-cream p-3 text-xs font-bold leading-5 text-ink-muted">
          개인정보 주의: 이름, 전화번호, 주소, 회사명처럼 상대를 특정할 수 있는 정보는 지우고 분석하세요. 원본 전문은 기본 저장하지 않습니다.
        </p>
      </article>
      <div className="workspace-section-title">
        <h3>이전에 붙여넣은 내용</h3>
        <span>{roomSavedIntakes.length}개</span>
      </div>
      <div className="saved-intake-list">
        {roomSavedIntakes.length > 0 ? roomSavedIntakes.map((item) => (
          <button key={item.id} type="button" className="saved-intake-card" onClick={() => onLoadSavedIntake(item.text)}>
            <span>{item.sourceLabel} · {item.savedAt}</span>
            <strong>{item.title}</strong>
            <p>{item.summary}</p>
          </button>
        )) : (
          <div className="saved-intake-empty">분석하면 붙여넣은 내용의 요약이 이곳에 저장됩니다.</div>
        )}
      </div>
      <Button type="button" className="w-full" disabled={!isReady} onClick={onAnalyze}>분류하고 요약하기</Button>
      {!isReady ? <p className="text-center text-xs font-bold text-ink-faint">최근 대화를 조금 더 입력하면 분석할 수 있습니다.</p> : null}
    </section>
  );
}

function TurnLoadingSection({ room }: { room: ChatRoom }) {
  return (
    <section className="section-screen loading-transition" aria-label="분석 중">
      <div className="message-transition">
        <span className="message-bubble theirs">{room.lastMessage}</span>
        <span className="message-bubble mine">대화랑 상황을 먼저 나눠볼게</span>
        <span className="reply-preview">분류 중</span>
      </div>
      <div className="grid gap-2">
        <p className="human-progress-step is-live">입력 종류 감지</p>
        <p className="human-progress-step is-live">나와 상대 발화 분류</p>
        <p className="human-progress-step is-live">상황 요약</p>
        <p className="human-progress-step is-live">부담되는 말 제외</p>
      </div>
    </section>
  );
}

function TurnResultSection({
  room,
  strategy,
  parsedIntake,
  recommendation,
  onBack,
  onCopy,
  onSave
}: {
  room: ChatRoom;
  strategy: StrategyOption;
  parsedIntake: ParsedIntake;
  recommendation: StrategyRecommendation;
  onBack: () => void;
  onCopy: () => void;
  onSave: () => void;
}) {
  return (
    <section className="section-screen result-workspace" aria-label="답장 추천">
      <BackButton onClick={onBack} label="요약 다시 보기" />
      <article className="reply-result-card">
        <span>{strategy.title} · {parsedIntake.currentState}</span>
        <h2>{room.name}에게 지금 보낼 답장</h2>
        <strong>{recommendation.recommendedReply}</strong>
        <p>{recommendation.why}</p>
        <div className="reply-action-row">
          <button type="button" onClick={onCopy}>복사</button>
          <button type="button" onClick={onSave}>히스토리에 저장</button>
        </div>
      </article>

      <article className="reply-support-card">
        <h3>다른 톤</h3>
        {recommendation.alternatives.map((reply) => (
          <p key={reply}>{reply}</p>
        ))}
      </article>

      <article className="reply-support-card warning">
        <h3>피해야 할 말</h3>
        <strong>{recommendation.riskyPhrase}</strong>
        <span>{recommendation.nextAction}</span>
      </article>
    </section>
  );
}

function SavedRepliesSection({
  savedReplies,
  onOpenRoom
}: {
  savedReplies: SavedReplyCard[];
  onOpenRoom: (roomId: string) => void;
}) {
  const roomGroups = chatRooms
    .map((room) => ({
      room,
      replies: savedReplies.filter((reply) => reply.roomId === room.id)
    }))
    .filter((group) => group.replies.length > 0);

  return (
    <section className="section-screen space-y-3" aria-label="저장된 답장">
      <article className="rounded-[22px] bg-white p-4 shadow-sm ring-1 ring-ink/[0.06]">
        <p className="text-xs font-black uppercase tracking-[0.16em] text-hell-600">Saved by Room</p>
        <h2 className="mt-1 text-xl font-black tracking-[-0.04em] text-ink">상담방별 저장</h2>
        <p className="mt-2 text-sm font-semibold leading-6 text-ink-muted">답장은 전체 보관함에 섞지 않고 상대별 상담방에 묶어서 다시 봅니다.</p>
      </article>

      {roomGroups.map(({ room, replies }) => (
        <article key={room.id} className="saved-room-group">
          <button type="button" className="saved-room-group-header" onClick={() => onOpenRoom(room.id)}>
            <span>
              <strong>{room.name} 상담방</strong>
              <small>{room.relationLabel}</small>
            </span>
            <em>{replies.length}개</em>
          </button>
          <div className="saved-intake-list">
            {replies.map((item) => (
              <div key={item.id} className="saved-reply-preview">
                <span>{item.strategyTitle} · {item.savedAt}</span>
                <strong>{item.reply}</strong>
                <p>{item.reason}</p>
                <small>{item.sourceSummary}</small>
              </div>
            ))}
          </div>
        </article>
      ))}
      <p className="rounded-[18px] bg-white/70 p-4 text-sm font-bold leading-6 text-ink-muted ring-1 ring-ink/[0.05]">
        기본 정책은 원본 대화 전문 저장이 아니라 턴별 요약과 추천 답장 보관입니다.
      </p>
    </section>
  );
}

function ProfileSection({
  profile
}: {
  profile: ReturnType<typeof useProfileSettings>;
}) {
  return (
    <section className="section-screen space-y-4" aria-label="내 정보">
      <div className="rounded-[22px] bg-white p-5 shadow-soft ring-1 ring-ink/[0.06]">
        <p className="text-xs font-black uppercase tracking-[0.18em] text-hell-600">Global Setting</p>
        <h2 className="mt-2 text-2xl font-black tracking-[-0.05em] text-ink">모든 상담방의 기본값</h2>
        <p className="mt-2 text-sm font-semibold leading-6 text-ink-muted">내 말투, 연애 스타일, 조언 수위만 관리합니다. 상대별 설정은 각 상담방에서 수정합니다.</p>
      </div>
      <StylePreferenceForm value={profile.preferences} onChange={profile.updatePreferences} />
    </section>
  );
}

function BillingSection({ usage }: { usage: UsageSummary | null }) {
  const freeUsed = usage?.usedToday ?? 0;
  const freeLimit = usage?.freeLimit ?? 3;

  return (
    <section className="section-screen space-y-4" aria-label="분석권">
      <div className="rounded-[22px] bg-ink p-5 text-white shadow-raised">
        <p className="text-xs font-black uppercase tracking-[0.18em] text-hell-500">Analysis Pass</p>
        <h2 className="mt-2 text-3xl font-black tracking-[-0.06em]">무료 {freeUsed}/{freeLimit}회 사용</h2>
        <p className="mt-3 text-sm font-semibold leading-6 text-white/70">MVP는 분석권 패키지를 먼저 사용합니다. 구독보다 결제 구조가 단순하고 검증이 빠릅니다.</p>
      </div>
      {[
        ["입문 패키지", "10회", "가볍게 써보기"],
        ["실전 패키지", "30회", "여러 상담방 관리"],
        ["집중 패키지", "100회", "장기 대화 흐름 저장"]
      ].map(([name, amount, description], index) => (
        <article key={name} className={`rounded-[22px] bg-white p-4 ring-1 ${index === 1 ? "ring-hell-600/25 shadow-soft" : "ring-ink/[0.06]"}`}>
          <div className="flex items-center justify-between gap-3">
            <div>
              <h3 className="text-lg font-black text-ink">{name}</h3>
              <p className="mt-1 text-sm font-semibold text-ink-muted">{description}</p>
            </div>
            <strong className="text-2xl font-black text-hell-600">{amount}</strong>
          </div>
        </article>
      ))}
    </section>
  );
}

function AppBottomNav({
  activeView,
  onNavigate
}: {
  activeView: AppViewId;
  onNavigate: (tab: AppTabId) => void;
}) {
  const navItems: Array<{ id: AppTabId; label: string }> = [
    { id: "home", label: "홈" },
    { id: "rooms", label: "상담방" },
    { id: "saved", label: "저장" },
    { id: "profile", label: "내 정보" },
    { id: "billing", label: "분석권" }
  ];
  const activeTab = tabForView(activeView);

  return (
    <nav className="app-bottom-nav" aria-label="앱 메뉴">
      {navItems.map((item) => (
        <button
          key={item.id}
          type="button"
          className={item.id === activeTab ? "is-active" : ""}
          onClick={() => onNavigate(item.id)}
        >
          {item.label}
        </button>
      ))}
    </nav>
  );
}

export function AnalysisPage() {
  const usage = useUsageQuota();
  const profile = useProfileSettings();
  const [activeView, setActiveView] = useState<AppViewId>("home");
  const [selectedRoomId, setSelectedRoomId] = useState(chatRooms[0].id);
  const [selectedStrategyId, setSelectedStrategyId] = useState<StrategyId>("relationshipCheck");
  const [conversationText, setConversationText] = useState(defaultConversationText);
  const [parsedIntake, setParsedIntake] = useState<ParsedIntake>(() => parseConversationText(defaultConversationText));
  const [savedIntakes, setSavedIntakes] = useState<SavedIntakeDraft[]>(initialSavedIntakes);
  const [savedReplyCards, setSavedReplyCards] = useState<SavedReplyCard[]>(initialSavedReplyCards);
  const [toast, setToast] = useState<ToastState | null>(null);
  const [isIntroVisible, setIsIntroVisible] = useState(() => {
    if (typeof window === "undefined") {
      return false;
    }

    return !window.sessionStorage.getItem(introStorageKey) && !window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  });

  const selectedRoom = chatRooms.find((room) => room.id === selectedRoomId) ?? chatRooms[0];
  const selectedStrategy = strategyOptions.find((strategy) => strategy.id === selectedStrategyId) ?? strategyOptions[0];
  const recommendation = strategyRecommendations[selectedStrategy.id];
  const currentCopy = copyByView[activeView];
  const isBottomNavVisible = shouldShowBottomNav(activeView);
  const savedReplyCountsByRoom = savedReplyCards.reduce<Record<string, number>>((counts, reply) => {
    counts[reply.roomId] = (counts[reply.roomId] ?? 0) + 1;
    return counts;
  }, {});
  const navigate = (view: AppViewId) => {
    setActiveView(view);
    window.scrollTo({ top: 0, behavior: window.matchMedia("(prefers-reduced-motion: reduce)").matches ? "auto" : "smooth" });
  };
  const navigateTab = (tab: AppTabId) => navigate(tab);
  const openRoom = (roomId: string) => {
    setSelectedRoomId(roomId);
    navigate("roomDetail");
  };
  const startTurnAnalysis = () => {
    const nextParsedIntake = parseConversationText(conversationText);
    setParsedIntake(nextParsedIntake);
    setSelectedStrategyId(nextParsedIntake.recommendedStrategyId);
    setSavedIntakes((current) => [
      createSavedIntakeDraft(selectedRoom.id, conversationText, current.length + 1),
      ...current.filter((item) => !(item.roomId === selectedRoom.id && item.text.trim() === conversationText.trim()))
    ]);
    navigate("turnLoading");
    window.setTimeout(() => navigate("turnInsight"), 900);
  };
  const copyReply = async () => {
    try {
      await navigator.clipboard.writeText(recommendation.recommendedReply);
      setToast({ message: "추천 답장을 복사했습니다.", tone: "success" });
    } catch {
      setToast({ message: "복사에 실패했습니다. 답장을 직접 선택해 복사하세요.", tone: "error" });
    }
  };
  const saveTurnResult = () => {
    const turnId = `SAVED-${String(savedReplyCards.length + 1).padStart(3, "0")}`;
    const nextSavedReply = createSavedReplyCard({
      roomId: selectedRoom.id,
      turnId,
      strategy: selectedStrategy,
      recommendation,
      sourceSummary: parsedIntake.summary,
      savedAt: "방금"
    });

    setSavedReplyCards((current) => [nextSavedReply, ...current]);
    setToast({ message: "상담방 히스토리에 저장했습니다.", tone: "success" });
    navigate("roomDetail");
  };
  const dismissIntro = () => {
    window.sessionStorage.setItem(introStorageKey, "true");
    setIsIntroVisible(false);
  };
  useEffect(() => {
    document.documentElement.scrollLeft = 0;
    document.body.scrollLeft = 0;
  }, []);

  useEffect(() => {
    if (!isIntroVisible) {
      return;
    }

    const timerId = window.setTimeout(dismissIntro, 3600);
    return () => window.clearTimeout(timerId);
  }, [isIntroVisible]);

  return (
    <main className="relative min-h-screen w-full max-w-full overflow-hidden bg-[#fbf3f0] text-ink">
      {isIntroVisible ? <OpeningIntro onDismiss={dismissIntro} /> : null}
      <div className="pointer-events-none absolute inset-x-0 top-0 h-40 bg-[linear-gradient(180deg,#fff7f3,rgba(255,247,243,0))]" />

      <div className="motion-app-shell app-frame relative mx-auto flex min-h-screen w-full max-w-[480px] flex-col px-4 py-4">
        <header className="phone-app-header sticky top-0 z-10 -mx-4 mb-3 bg-[#fbf3f0]/92 px-4 py-3 backdrop-blur">
          <div className="phone-status-row" aria-hidden="true">
            <span>9:41</span>
            <span>●●●</span>
          </div>
          <div className="phone-title-row">
            <div>
              <p>FLIRTING HELL</p>
              <h1>{currentCopy.title}</h1>
            </div>
            <button type="button" aria-label="내 정보로 이동" onClick={() => navigate("profile")}>
              내 정보
            </button>
          </div>
          <p>{currentCopy.description}</p>
        </header>

        <div className={`flex flex-1 flex-col gap-4 ${isBottomNavVisible ? "pb-28" : "pb-6"}`}>
          <section className="section-viewport" aria-live="polite">
            <Toast message={toast?.message ?? null} tone={toast?.tone ?? "success"} />
            {activeView === "home" ? (
              <HomeSection
                usage={usage.usage}
                savedReplyCount={savedReplyCards.length}
                savedReplyCountsByRoom={savedReplyCountsByRoom}
                onCreateRoom={() => navigate("newRoom")}
                onOpenRoom={openRoom}
                onOpenRooms={() => navigate("rooms")}
                onOpenSaved={() => navigate("saved")}
              />
            ) : null}
            {activeView === "rooms" ? (
              <RoomsListSection
                selectedRoomId={selectedRoomId}
                savedReplyCountsByRoom={savedReplyCountsByRoom}
                onSelectRoom={openRoom}
                onCreateRoom={() => navigate("newRoom")}
              />
            ) : null}
            {activeView === "newRoom" ? <NewRoomSection onCancel={() => navigate("rooms")} onCreate={() => openRoom("jiu")} /> : null}
            {activeView === "roomDetail" ? (
              <RoomDetailSection
                room={selectedRoom}
                savedIntakes={savedIntakes}
                savedReplies={savedReplyCards}
                onBack={() => navigate("rooms")}
                onEditSettings={() => navigate("roomSettings")}
                onStartTurn={() => navigate("turnInput")}
              />
            ) : null}
            {activeView === "roomSettings" ? <RoomSettingsSection room={selectedRoom} onBack={() => navigate("roomDetail")} /> : null}
            {activeView === "turnInsight" ? (
              <TurnInsightSection
                room={selectedRoom}
                parsedIntake={parsedIntake}
                selectedStrategyId={selectedStrategyId}
                onBack={() => navigate("turnInput")}
                onSelectStrategy={setSelectedStrategyId}
                onNext={() => navigate("turnResult")}
              />
            ) : null}
            {activeView === "turnInput" ? (
              <TurnInputSection
                room={selectedRoom}
                conversationText={conversationText}
                savedIntakes={savedIntakes}
                onLoadSavedIntake={setConversationText}
                onConversationTextChange={setConversationText}
                onBack={() => navigate("roomDetail")}
                onAnalyze={startTurnAnalysis}
              />
            ) : null}
            {activeView === "turnLoading" ? <TurnLoadingSection room={selectedRoom} /> : null}
            {activeView === "turnResult" ? (
              <TurnResultSection
                room={selectedRoom}
                strategy={selectedStrategy}
                parsedIntake={parsedIntake}
                recommendation={recommendation}
                onBack={() => navigate("turnInsight")}
                onCopy={copyReply}
                onSave={saveTurnResult}
              />
            ) : null}
            {activeView === "saved" ? <SavedRepliesSection savedReplies={savedReplyCards} onOpenRoom={openRoom} /> : null}
            {activeView === "profile" ? <ProfileSection profile={profile} /> : null}
            {activeView === "billing" ? <BillingSection usage={usage.usage} /> : null}
          </section>
        </div>
      </div>
      {isBottomNavVisible ? <AppBottomNav activeView={activeView} onNavigate={navigateTab} /> : null}
    </main>
  );
}

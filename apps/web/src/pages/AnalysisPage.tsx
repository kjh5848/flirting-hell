import { useEffect, useState } from "react";
import type { UsageSummary } from "@flirting-hell/shared";
import { AnalysisForm } from "../features/analysis/components/AnalysisForm";
import { AnalysisLoading } from "../features/analysis/components/AnalysisLoading";
import { AnalysisResult } from "../features/analysis/components/AnalysisResult";
import { useAnalysis } from "../features/analysis/hooks/useAnalysis";
import { CinematicIntroCanvas } from "../features/intro/components/CinematicIntroCanvas";
import { StylePreferenceForm } from "../features/profile/components/StylePreferenceForm";
import { useProfileSettings } from "../features/profile/hooks/useProfileSettings";
import { FreeLimitNotice } from "../features/usage/components/FreeLimitNotice";
import { UsageBadge } from "../features/usage/components/UsageBadge";
import { useUsageQuota } from "../features/usage/hooks/useUsageQuota";
import { Toast } from "../shared/ui/Toast";
import { Button } from "../shared/ui/Button";

type ToastState = {
  message: string;
  tone: "success" | "error" | "info";
};

type AppSectionId = "home" | "analysis" | "saved" | "profile" | "billing";

const sectionCopy: Record<AppSectionId, { title: string; description: string }> = {
  home: {
    title: "플러팅지옥",
    description: "고민되는 대화를 상담방처럼 열고, 다음 답장만 빠르게 고릅니다."
  },
  analysis: {
    title: "새 상담",
    description: "대화 흐름을 보고, 지금 보낼 말만 고릅니다."
  },
  saved: {
    title: "저장된 답장",
    description: "대화별로 추천받은 답장과 선택 이유를 다시 확인합니다."
  },
  profile: {
    title: "내 정보",
    description: "내 말투, 관계 기준, 조언 수위를 답장 추천에 반영합니다."
  },
  billing: {
    title: "분석권",
    description: "무료 사용량과 분석권 패키지를 확인합니다."
  }
};

const introStorageKey = "flirting-hell:intro-seen:v1";
const flowSteps = [
  {
    id: "compose",
    label: "1",
    title: "대화 입력"
  },
  {
    id: "review",
    label: "2",
    title: "분위기 확인"
  },
  {
    id: "result",
    label: "3",
    title: "답장 선택"
  }
] as const;

type FlowStepId = (typeof flowSteps)[number]["id"];

function usageFromAnalysis(data: ReturnType<typeof useAnalysis>["data"], fallback: UsageSummary | null): UsageSummary | null {
  if (!data) {
    return fallback;
  }

  return {
    date: new Date().toISOString().slice(0, 10),
    ...data.usage
  };
}

function FlowProgress({ activeStep }: { activeStep: FlowStepId }) {
  const activeIndex = flowSteps.findIndex((step) => step.id === activeStep);

  return (
    <nav className="flow-progress" aria-label="답장 추천 단계">
      {flowSteps.map((step, index) => {
        const stateClassName = index < activeIndex ? "is-complete" : index === activeIndex ? "is-active" : "";

        return (
          <div key={step.id} className={`flow-step ${stateClassName}`}>
            <span>{step.label}</span>
            <p>{step.title}</p>
          </div>
        );
      })}
    </nav>
  );
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

function HomeSection({
  usage,
  onStartAnalysis,
  onOpenSaved,
  onOpenProfile
}: {
  usage: UsageSummary | null;
  onStartAnalysis: () => void;
  onOpenSaved: () => void;
  onOpenProfile: () => void;
}) {
  const remaining = usage?.remainingToday ?? 3;

  return (
    <section className="section-screen space-y-4" aria-label="홈">
      <div className="rounded-[28px] bg-white p-5 shadow-soft ring-1 ring-ink/[0.06]">
        <p className="text-xs font-black uppercase tracking-[0.18em] text-hell-600">Today</p>
        <h2 className="mt-2 text-[30px] font-black leading-[1.05] tracking-[-0.06em] text-ink">오늘 가장 궁금한 대화를 열어보세요</h2>
        <p className="mt-3 text-sm font-semibold leading-6 text-ink-muted">카톡, DM, 문자 흐름을 붙여넣고 상대 반응과 다음 답장을 한 상담방에 보관합니다.</p>
        <Button type="button" className="mt-5 w-full" onClick={onStartAnalysis}>새 상담 시작</Button>
      </div>

      <div className="grid grid-cols-3 gap-2">
        <div className="rounded-[22px] bg-white p-4 ring-1 ring-ink/[0.06]">
          <span className="text-[11px] font-black text-ink-faint">무료</span>
          <strong className="mt-2 block text-2xl font-black text-ink">{remaining}</strong>
          <p className="mt-1 text-xs font-bold text-ink-muted">남은 분석</p>
        </div>
        <button type="button" className="rounded-[22px] bg-white p-4 text-left ring-1 ring-ink/[0.06]" onClick={onOpenSaved}>
          <span className="text-[11px] font-black text-ink-faint">저장</span>
          <strong className="mt-2 block text-2xl font-black text-ink">2</strong>
          <p className="mt-1 text-xs font-bold text-ink-muted">답장 카드</p>
        </button>
        <button type="button" className="rounded-[22px] bg-white p-4 text-left ring-1 ring-ink/[0.06]" onClick={onOpenProfile}>
          <span className="text-[11px] font-black text-ink-faint">설정</span>
          <strong className="mt-2 block text-2xl font-black text-ink">완료</strong>
          <p className="mt-1 text-xs font-bold text-ink-muted">말투 기준</p>
        </button>
      </div>

      <div className="rounded-[28px] bg-white p-4 shadow-sm ring-1 ring-ink/[0.06]">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-xs font-black text-ink-faint">최근 상담방</p>
            <h3 className="mt-1 text-xl font-black tracking-[-0.04em] text-ink">지우와 대화</h3>
          </div>
          <span className="rounded-full bg-blush px-3 py-1 text-xs font-black text-hell-700">답장 대기</span>
        </div>
        <div className="mt-4 grid gap-2 rounded-[22px] bg-[#A9CFE0] p-3">
          <p className="max-w-[78%] rounded-[18px] bg-white px-3 py-2 text-sm font-black text-ink">오늘 뭐해?</p>
          <p className="ml-auto max-w-[78%] rounded-[18px] bg-[#FEE500] px-3 py-2 text-sm font-black text-ink">그냥 집에 있어 ㅋㅋ 너는?</p>
          <p className="max-w-[78%] rounded-[18px] bg-white px-3 py-2 text-sm font-black text-ink">나도 집. 갑자기 네 생각나서</p>
        </div>
        <button type="button" className="mt-4 w-full rounded-[18px] bg-ink px-4 py-3 text-sm font-black text-white" onClick={onStartAnalysis}>이어서 답장 추천받기</button>
      </div>
    </section>
  );
}

function SavedRepliesSection() {
  const savedReplies = [
    {
      room: "지우와 대화",
      turn: "TURN-002",
      reply: "갑자기 그렇게 말하니까 궁금해지네. 오늘은 집에서 뭐하면서 쉬고 있어?",
      reason: "바로 확인하지 않고 대화를 이어갈 수 있는 질문형 답장"
    },
    {
      room: "새 소개팅",
      turn: "TURN-001",
      reply: "오늘 얘기 편해서 좋았어요. 다음엔 말했던 카페도 같이 가봐요.",
      reason: "부담을 줄이고 자연스럽게 다음 약속으로 연결"
    }
  ];

  return (
    <section className="section-screen space-y-3" aria-label="저장된 답장">
      {savedReplies.map((item) => (
        <article key={item.turn} className="rounded-[28px] bg-white p-4 shadow-sm ring-1 ring-ink/[0.06]">
          <div className="flex items-center justify-between gap-3">
            <div>
              <p className="text-xs font-black text-ink-faint">{item.room}</p>
              <h2 className="mt-1 text-lg font-black text-ink">{item.turn}</h2>
            </div>
            <span className="rounded-full bg-cream px-3 py-1 text-xs font-black text-ink-muted">보관됨</span>
          </div>
          <p className="mt-4 rounded-[20px] bg-blush px-4 py-3 text-base font-black leading-7 text-ink">{item.reply}</p>
          <p className="mt-3 text-sm font-semibold leading-6 text-ink-muted">{item.reason}</p>
        </article>
      ))}
      <p className="rounded-[22px] bg-white/70 p-4 text-sm font-bold leading-6 text-ink-muted ring-1 ring-ink/[0.05]">
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
      <div className="rounded-[28px] bg-white p-5 shadow-soft ring-1 ring-ink/[0.06]">
        <p className="text-xs font-black uppercase tracking-[0.18em] text-hell-600">Profile</p>
        <h2 className="mt-2 text-2xl font-black tracking-[-0.05em] text-ink">답장을 내 말투에 맞추는 기준</h2>
        <p className="mt-2 text-sm font-semibold leading-6 text-ink-muted">이상형이 달라도 만남을 막지 않습니다. 대신 조심할 점과 확인 질문만 알려줍니다.</p>
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
      <div className="rounded-[28px] bg-ink p-5 text-white shadow-raised">
        <p className="text-xs font-black uppercase tracking-[0.18em] text-hell-500">Analysis Pass</p>
        <h2 className="mt-2 text-3xl font-black tracking-[-0.06em]">무료 {freeUsed}/{freeLimit}회 사용</h2>
        <p className="mt-3 text-sm font-semibold leading-6 text-white/70">MVP는 분석권 패키지를 먼저 사용합니다. 구독보다 결제 구조가 단순하고 검증이 빠릅니다.</p>
      </div>
      {[
        ["입문 패키지", "10회", "가볍게 써보기"],
        ["실전 패키지", "30회", "여러 상담방 관리"],
        ["집중 패키지", "100회", "장기 대화 흐름 저장"]
      ].map(([name, amount, description], index) => (
        <article key={name} className={`rounded-[24px] bg-white p-4 ring-1 ${index === 1 ? "ring-hell-600/25 shadow-soft" : "ring-ink/[0.06]"}`}>
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
  activeSection,
  onNavigate
}: {
  activeSection: AppSectionId;
  onNavigate: (section: AppSectionId) => void;
}) {
  const navItems: Array<{ id: AppSectionId; label: string }> = [
    { id: "home", label: "홈" },
    { id: "analysis", label: "상담" },
    { id: "saved", label: "저장" },
    { id: "profile", label: "내 정보" },
    { id: "billing", label: "분석권" }
  ];

  return (
    <nav className="app-bottom-nav" aria-label="앱 메뉴">
      {navItems.map((item) => (
        <button
          key={item.id}
          type="button"
          className={item.id === activeSection ? "is-active" : ""}
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
  const analysis = useAnalysis({ onCompleted: usage.reload });
  const [activeSection, setActiveSection] = useState<AppSectionId>("home");
  const [toast, setToast] = useState<ToastState | null>(null);
  const [isIntroVisible, setIsIntroVisible] = useState(() => {
    if (typeof window === "undefined") {
      return false;
    }

    return !window.sessionStorage.getItem(introStorageKey) && !window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  });

  const displayUsage = usageFromAnalysis(analysis.data, usage.usage);
  const isLimitReached = analysis.error?.includes("무료 분석 3회") ?? false;
  const activeStep: FlowStepId = analysis.isLoading ? "review" : analysis.data ? "result" : "compose";
  const currentCopy = sectionCopy[activeSection];
  const showToast = (message: string, tone: ToastState["tone"] = "success") => setToast({ message, tone });
  const navigate = (section: AppSectionId) => {
    setActiveSection(section);
    window.scrollTo({ top: 0, behavior: window.matchMedia("(prefers-reduced-motion: reduce)").matches ? "auto" : "smooth" });
  };
  const dismissIntro = () => {
    window.sessionStorage.setItem(introStorageKey, "true");
    setIsIntroVisible(false);
  };
  const replayIntro = () => {
    window.sessionStorage.removeItem(introStorageKey);
    setIsIntroVisible(true);
  };
  const restartAnalysis = () => {
    analysis.reset();
    window.scrollTo({ top: 0, behavior: window.matchMedia("(prefers-reduced-motion: reduce)").matches ? "auto" : "smooth" });
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

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: window.matchMedia("(prefers-reduced-motion: reduce)").matches ? "auto" : "smooth" });
  }, [activeStep, activeSection]);

  return (
    <main className="relative min-h-screen w-full max-w-full overflow-hidden bg-[#fbf3f0] text-ink">
      {isIntroVisible ? <OpeningIntro onDismiss={dismissIntro} /> : null}
      <div className="pointer-events-none absolute inset-x-0 top-0 h-40 bg-[linear-gradient(180deg,#fff7f3,rgba(255,247,243,0))]" />

      <div className="motion-app-shell app-frame relative mx-auto flex min-h-screen w-full max-w-[480px] flex-col px-4 py-4">
        <header className="app-header sticky top-0 z-10 -mx-4 mb-3 border-b border-black/[0.04] bg-[#fbf3f0]/88 px-4 py-3 backdrop-blur">
          <div className="flex items-start justify-between gap-3">
            <div>
              <p className="text-[11px] font-black tracking-[0.24em] text-hell-600">FLIRTING HELL</p>
              <h1 className="mt-1 text-2xl font-black leading-none tracking-[-0.055em] text-ink">{currentCopy.title}</h1>
            </div>
            <div className="flex shrink-0 flex-col items-end gap-2">
              <UsageBadge usage={displayUsage} isLoading={usage.isLoading && !analysis.data} />
              <button
                type="button"
                className="rounded-full bg-white px-3 py-1.5 text-[11px] font-black text-ink-muted shadow-sm ring-1 ring-black/[0.05] transition hover:-translate-y-0.5 hover:text-ink focus:outline-none focus-visible:ring-4 focus-visible:ring-hell-200"
                onClick={replayIntro}
              >
                인트로 보기
              </button>
            </div>
          </div>
          <p className="mt-2 text-sm font-semibold leading-6 text-ink-muted">{currentCopy.description}</p>
        </header>

        <div className="flex flex-1 flex-col gap-4 pb-6">
          {activeSection === "analysis" ? <FlowProgress activeStep={activeStep} /> : null}

          <section className={activeSection === "analysis" ? `stage-viewport is-${activeStep}` : "section-viewport"} aria-live="polite">
            <Toast message={toast?.message ?? null} tone={toast?.tone ?? "success"} />
            <Toast message={analysis.error} tone="error" />
            {activeSection === "home" ? (
              <HomeSection
                usage={displayUsage}
                onStartAnalysis={() => navigate("analysis")}
                onOpenSaved={() => navigate("saved")}
                onOpenProfile={() => navigate("profile")}
              />
            ) : null}
            {activeSection === "saved" ? <SavedRepliesSection /> : null}
            {activeSection === "profile" ? <ProfileSection profile={profile} /> : null}
            {activeSection === "billing" ? <BillingSection usage={displayUsage} /> : null}
            {activeSection === "analysis" && isLimitReached ? <FreeLimitNotice /> : null}
            {activeSection === "analysis" && isLimitReached && analysis.data ? <p className="rounded-2xl bg-white/70 px-4 py-3 text-sm font-bold text-ink-muted ring-1 ring-ink/10">아래 결과는 이전 분석 결과입니다.</p> : null}
            {activeSection === "analysis" && activeStep === "compose" ? (
              <div className="stage-screen">
                <AnalysisForm preferences={profile.preferences} isLoading={analysis.isLoading} onSubmit={analysis.analyze} />
                <details className="group mt-4 rounded-[24px] bg-white p-4 shadow-sm ring-1 ring-black/[0.04]">
                  <summary className="flex cursor-pointer list-none items-center justify-between gap-3">
                    <div>
                      <p className="text-xs font-black uppercase tracking-[0.18em] text-ink-faint">Dating Style</p>
                      <h2 className="mt-1 text-base font-black text-ink">내 연애 스타일</h2>
                    </div>
                    <span className="rounded-full bg-cream px-3 py-1 text-xs font-black text-ink-muted ring-1 ring-ink/5 group-open:hidden">열기</span>
                    <span className="hidden rounded-full bg-cream px-3 py-1 text-xs font-black text-ink-muted ring-1 ring-ink/5 group-open:inline">닫기</span>
                  </summary>
                  <div className="mt-4">
                    <p className="mb-4 text-sm leading-6 text-ink-muted">판정표가 아니라 경고등입니다. “만나라/말라”를 결정하지 않고, 확인할 점만 알려줍니다.</p>
                    <StylePreferenceForm value={profile.preferences} onChange={profile.updatePreferences} />
                  </div>
                </details>
              </div>
            ) : null}
            {activeSection === "analysis" && activeStep === "review" ? <AnalysisLoading /> : null}
            {activeSection === "analysis" && activeStep === "result" && analysis.data ? <AnalysisResult data={analysis.data} onCopied={showToast} onRestart={restartAnalysis} /> : null}
          </section>
          <AppBottomNav activeSection={activeSection} onNavigate={navigate} />
        </div>
      </div>
    </main>
  );
}

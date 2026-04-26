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

type ToastState = {
  message: string;
  tone: "success" | "error" | "info";
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

function AppBottomNav() {
  return (
    <nav className="app-bottom-nav" aria-label="앱 메뉴">
      <span className="is-active">홈</span>
      <span>기록</span>
      <span>내 정보</span>
      <span>가이드</span>
      <span>더보기</span>
    </nav>
  );
}

export function AnalysisPage() {
  const usage = useUsageQuota();
  const profile = useProfileSettings();
  const analysis = useAnalysis({ onCompleted: usage.reload });
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
  const showToast = (message: string, tone: ToastState["tone"] = "success") => setToast({ message, tone });
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
  }, [activeStep]);

  return (
    <main className="relative min-h-screen w-full max-w-full overflow-hidden bg-[#fbf3f0] text-ink">
      {isIntroVisible ? <OpeningIntro onDismiss={dismissIntro} /> : null}
      <div className="pointer-events-none absolute inset-x-0 top-0 h-40 bg-[linear-gradient(180deg,#fff7f3,rgba(255,247,243,0))]" />

      <div className="motion-app-shell app-frame relative mx-auto flex min-h-screen w-full max-w-[480px] flex-col px-4 py-4">
        <header className="app-header sticky top-0 z-10 -mx-4 mb-3 border-b border-black/[0.04] bg-[#fbf3f0]/88 px-4 py-3 backdrop-blur">
          <div className="flex items-start justify-between gap-3">
            <div>
              <p className="text-[11px] font-black tracking-[0.24em] text-hell-600">FLIRTING HELL</p>
              <h1 className="mt-1 text-2xl font-black leading-none tracking-[-0.055em] text-ink">플러팅지옥</h1>
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
          <p className="mt-2 text-sm font-semibold leading-6 text-ink-muted">대화 흐름을 보고, 지금 보낼 말만 고릅니다.</p>
        </header>

        <div className="flex flex-1 flex-col gap-4 pb-6">
          <FlowProgress activeStep={activeStep} />

          <section className={`stage-viewport is-${activeStep}`} aria-live="polite">
            <Toast message={toast?.message ?? null} tone={toast?.tone ?? "success"} />
            <Toast message={analysis.error} tone="error" />
            {isLimitReached ? <FreeLimitNotice /> : null}
            {isLimitReached && analysis.data ? <p className="rounded-2xl bg-white/70 px-4 py-3 text-sm font-bold text-ink-muted ring-1 ring-ink/10">아래 결과는 이전 분석 결과입니다.</p> : null}
            {activeStep === "compose" ? (
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
            {activeStep === "review" ? <AnalysisLoading /> : null}
            {activeStep === "result" && analysis.data ? <AnalysisResult data={analysis.data} onCopied={showToast} onRestart={restartAnalysis} /> : null}
          </section>
          <AppBottomNav />
        </div>
      </div>
    </main>
  );
}

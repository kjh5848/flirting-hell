import { useState } from "react";
import type { UsageSummary } from "@flirting-hell/shared";
import { AnalysisForm } from "../features/analysis/components/AnalysisForm";
import { AnalysisLoading } from "../features/analysis/components/AnalysisLoading";
import { AnalysisResult } from "../features/analysis/components/AnalysisResult";
import { useAnalysis } from "../features/analysis/hooks/useAnalysis";
import { StylePreferenceForm } from "../features/profile/components/StylePreferenceForm";
import { useProfileSettings } from "../features/profile/hooks/useProfileSettings";
import { FreeLimitNotice } from "../features/usage/components/FreeLimitNotice";
import { UsageBadge } from "../features/usage/components/UsageBadge";
import { useUsageQuota } from "../features/usage/hooks/useUsageQuota";
import { Card } from "../shared/ui/Card";
import { Toast } from "../shared/ui/Toast";

function usageFromAnalysis(data: ReturnType<typeof useAnalysis>["data"], fallback: UsageSummary | null): UsageSummary | null {
  if (!data) {
    return fallback;
  }

  return {
    date: new Date().toISOString().slice(0, 10),
    ...data.usage
  };
}

function EmptyResultState() {
  return (
    <Card className="overflow-hidden bg-white/80 p-0">
      <div className="border-b border-gray-100 bg-gray-950 px-5 py-4 text-white">
        <p className="text-xs font-bold uppercase tracking-[0.2em] text-rose-200">Preview</p>
        <h2 className="mt-2 text-2xl font-black">분석 결과가 여기에 표시됩니다.</h2>
      </div>
      <div className="space-y-4 p-5">
        <div className="rounded-3xl bg-gray-50 p-4">
          <div className="h-3 w-24 rounded-full bg-gray-200" />
          <div className="mt-4 h-5 w-3/4 rounded-full bg-gray-200" />
          <div className="mt-3 h-3 w-full rounded-full bg-gray-100" />
          <div className="mt-2 h-3 w-5/6 rounded-full bg-gray-100" />
        </div>
        <div className="grid gap-3 sm:grid-cols-3">
          {["순한맛", "설렘맛", "직진맛"].map((label) => (
            <div key={label} className="rounded-3xl border border-gray-100 bg-white p-4 shadow-sm">
              <span className="rounded-full bg-gray-950 px-3 py-1 text-xs font-bold text-white">{label}</span>
              <div className="mt-4 h-4 rounded-full bg-gray-100" />
              <div className="mt-2 h-4 w-2/3 rounded-full bg-gray-100" />
            </div>
          ))}
        </div>
        <p className="text-center text-sm leading-6 text-gray-500">왼쪽에 대화를 붙여넣고 `AI로 답장 찾기`를 누르면 mock 결과로 먼저 UI 흐름을 확인합니다.</p>
      </div>
    </Card>
  );
}

export function AnalysisPage() {
  const usage = useUsageQuota();
  const profile = useProfileSettings();
  const analysis = useAnalysis({ onCompleted: usage.reload });
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const displayUsage = usageFromAnalysis(analysis.data, usage.usage);
  const isLimitReached = analysis.error?.includes("무료 분석 3회") ?? false;

  return (
    <main className="min-h-screen bg-[radial-gradient(circle_at_top_left,#ffe4e6,transparent_30%),radial-gradient(circle_at_top_right,#fed7aa,transparent_24%),linear-gradient(180deg,#fff7ed_0%,#ffffff_48%,#f8fafc_100%)] px-4 py-5 text-gray-950 sm:px-6 lg:py-8">
      <div className="mx-auto grid max-w-6xl gap-6 lg:grid-cols-[minmax(0,0.92fr)_minmax(420px,1.08fr)]">
        <section className="space-y-5 lg:sticky lg:top-6 lg:self-start">
          <header className="rounded-[32px] bg-white/70 p-5 shadow-soft ring-1 ring-black/5 backdrop-blur">
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <p className="text-xs font-black tracking-[0.32em] text-hell-600">FLIRTING HELL</p>
                <h1 className="mt-3 text-4xl font-black leading-none tracking-[-0.05em] text-gray-950 sm:text-5xl">플러팅지옥</h1>
                <p className="mt-3 max-w-md text-base leading-7 text-gray-600">플러팅 무한루프 탈출. 지금 보낼 한마디를 찾다.</p>
              </div>
              <UsageBadge usage={displayUsage} isLoading={usage.isLoading && !analysis.data} />
            </div>
            <div className="mt-5 grid grid-cols-3 gap-2 text-center text-xs font-bold text-gray-500">
              <div className="rounded-2xl bg-white px-3 py-3 ring-1 ring-gray-100">1. 대화 입력</div>
              <div className="rounded-2xl bg-white px-3 py-3 ring-1 ring-gray-100">2. 스타일 선택</div>
              <div className="rounded-2xl bg-gray-950 px-3 py-3 text-white">3. 답장 복사</div>
            </div>
          </header>

          <AnalysisForm preferences={profile.preferences} isLoading={analysis.isLoading} onSubmit={analysis.analyze} />

          <Card>
            <div className="mb-4">
              <p className="text-xs font-bold uppercase tracking-[0.2em] text-gray-400">내 연애 스타일</p>
              <h2 className="mt-2 text-xl font-black text-gray-950">이상형은 고정값이 아니라 경고등입니다.</h2>
              <p className="mt-2 text-sm leading-6 text-gray-600">AI는 상대를 만나도 되는지 단정하지 않고, 내 스타일과 다른 지점만 현실적으로 알려줍니다.</p>
            </div>
            <StylePreferenceForm value={profile.preferences} onChange={profile.updatePreferences} />
          </Card>
        </section>

        <section className="space-y-4">
          <Toast message={toastMessage} tone="success" />
          <Toast message={analysis.error} tone="error" />
          {isLimitReached ? <FreeLimitNotice /> : null}
          {analysis.isLoading ? <AnalysisLoading /> : null}
          {analysis.data ? <AnalysisResult data={analysis.data} onCopied={setToastMessage} /> : null}
          {!analysis.data && !analysis.isLoading ? <EmptyResultState /> : null}
        </section>
      </div>
    </main>
  );
}

import { useState } from "react";
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

export function AnalysisPage() {
  const usage = useUsageQuota();
  const profile = useProfileSettings();
  const analysis = useAnalysis({ onCompleted: usage.reload });
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const isLimitReached = analysis.error?.includes("무료 분석 3회") ?? false;

  return (
    <main className="min-h-screen bg-[radial-gradient(circle_at_top_left,#ffe4e6,transparent_32%),linear-gradient(180deg,#fff7ed_0%,#ffffff_52%,#f8fafc_100%)] px-4 py-6 text-gray-950">
      <div className="mx-auto grid max-w-6xl gap-6 lg:grid-cols-[minmax(0,0.95fr)_minmax(360px,1.05fr)]">
        <section className="space-y-5 lg:sticky lg:top-6 lg:self-start">
          <div className="flex items-center justify-between gap-3">
            <div>
              <p className="text-sm font-black tracking-[0.3em] text-hell-600">FLIRTING HELL</p>
              <h1 className="mt-3 text-4xl font-black leading-tight tracking-[-0.04em] text-gray-950 sm:text-5xl">플러팅지옥</h1>
              <p className="mt-3 text-base leading-7 text-gray-600">플러팅 무한루프 탈출. 지금 보낼 한마디를 찾다.</p>
            </div>
            <UsageBadge usage={usage.usage} isLoading={usage.isLoading} />
          </div>

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
          {!analysis.data && !analysis.isLoading ? (
            <Card className="flex min-h-[420px] flex-col justify-center bg-white/75 text-center">
              <p className="text-sm font-bold text-gray-400">아직 분석 결과가 없습니다.</p>
              <h2 className="mt-3 text-2xl font-black text-gray-950">왼쪽에 대화를 붙여넣고 분석을 시작하세요.</h2>
              <p className="mx-auto mt-3 max-w-md text-sm leading-6 text-gray-500">MVP는 먼저 mock AI 결과로 화면과 무료 사용량 제한을 검증하고, 이후 실제 LLM API를 연결합니다.</p>
            </Card>
          ) : null}
        </section>
      </div>
    </main>
  );
}

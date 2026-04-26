import type { CreateAnalysisData } from "@flirting-hell/shared";
import { ToneProfileCard } from "../../profile/components/ToneProfileCard";
import { Button } from "../../../shared/ui/Button";
import { Card } from "../../../shared/ui/Card";
import { ReplyOptionCard } from "./ReplyOptionCard";
import { RiskyMessageCard } from "./RiskyMessageCard";

type AnalysisResultProps = {
  data: CreateAnalysisData;
  onCopied: (message: string, tone?: "success" | "error") => void;
  onRestart: () => void;
};

export function AnalysisResult({ data, onCopied, onRestart }: AnalysisResultProps) {
  const { result } = data;
  const recommendedReply = result.replyOptions.find((option) => option.level === "설렘맛") ?? result.replyOptions[0];
  const otherReplies = result.replyOptions.filter((option) => option.level !== recommendedReply.level);

  return (
    <div className="space-y-4">
      <div className="result-hero">
        <span>답장 준비 완료</span>
        <strong>지금은 이렇게 보내는 게 가장 자연스럽습니다.</strong>
        <p>아래 답장을 먼저 보고, 필요하면 더 순하거나 더 직진하는 톤을 선택하세요.</p>
      </div>

      <Card className="bg-white/90 p-4 shadow-sm">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <p className="text-xs font-black uppercase tracking-[0.18em] text-hell-600">현재 분위기</p>
          <span className="rounded-full bg-cream px-3 py-1 text-xs font-bold text-ink-muted ring-1 ring-ink/5">확신도 {result.mood.confidence}</span>
        </div>
        <div className="mt-3 flex flex-wrap items-center gap-2">
          <span className="rounded-full bg-hell-50 px-3 py-1 text-sm font-black text-hell-700 ring-1 ring-hell-100">{result.mood.status}</span>
          {result.safety.needsToneDown ? <span className="rounded-full bg-rose-50 px-3 py-1 text-sm font-black text-rose-700 ring-1 ring-rose-100">강도 낮추기 권장</span> : null}
        </div>
        <h2 className="mt-3 text-lg font-black leading-7 tracking-[-0.03em] text-ink">{result.mood.summary}</h2>
      </Card>

      {result.safety.warning ? (
        <Card className="border border-rose-100 bg-rose-50">
          <p className="text-xs font-bold uppercase tracking-[0.2em] text-rose-500">안전 경고</p>
          <p className="mt-2 text-sm leading-6 text-rose-700/80">{result.safety.warning}</p>
        </Card>
      ) : null}

      <div className="space-y-3">
        <div className="flex items-end justify-between gap-3">
          <div>
            <p className="text-xs font-black uppercase tracking-[0.18em] text-hell-600">추천 답장</p>
            <h3 className="mt-1 text-2xl font-black tracking-[-0.04em] text-ink">이걸 먼저 보내세요</h3>
          </div>
          <Button variant="ghost" type="button" onClick={onRestart}>다시 입력</Button>
        </div>
        <ReplyOptionCard option={recommendedReply} analysisId={data.analysisId} onCopied={onCopied} featured />
      </div>

      <details className="group rounded-[24px] bg-white/80 p-4 shadow-sm ring-1 ring-white/80">
        <summary className="flex cursor-pointer list-none items-center justify-between gap-3">
          <div>
            <p className="text-sm font-black text-ink">다른 톤도 보기</p>
            <p className="mt-1 text-xs font-semibold text-ink-muted">순한맛/직진맛 답장</p>
          </div>
          <span className="rounded-full bg-cream px-3 py-1 text-xs font-black text-ink-muted ring-1 ring-ink/5 group-open:hidden">열기</span>
          <span className="hidden rounded-full bg-cream px-3 py-1 text-xs font-black text-ink-muted ring-1 ring-ink/5 group-open:inline">닫기</span>
        </summary>
        <div className="mt-4 space-y-3">
          {otherReplies.map((option) => (
            <ReplyOptionCard key={option.level} option={option} analysisId={data.analysisId} onCopied={onCopied} />
          ))}
        </div>
      </details>

      <RiskyMessageCard riskyMessage={result.riskyMessage} />

      <details className="group rounded-[24px] bg-white/80 p-4 shadow-sm ring-1 ring-white/80">
        <summary className="flex cursor-pointer list-none items-center justify-between gap-3">
          <div>
            <p className="text-sm font-black text-ink">분석 자세히 보기</p>
            <p className="mt-1 text-xs font-semibold text-ink-muted">스타일, 말투, 다음 행동</p>
          </div>
          <span className="rounded-full bg-cream px-3 py-1 text-xs font-black text-ink-muted ring-1 ring-ink/5 group-open:hidden">열기</span>
          <span className="hidden rounded-full bg-cream px-3 py-1 text-xs font-black text-ink-muted ring-1 ring-ink/5 group-open:inline">닫기</span>
        </summary>
        <div className="mt-4 space-y-3">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.18em] text-ink-faint">내 스타일과의 적합도</p>
            <h3 className="mt-2 text-lg font-black tracking-[-0.03em] text-ink">{result.styleFit.status}</h3>
            <p className="mt-2 text-sm leading-6 text-ink-muted">{result.styleFit.summary}</p>
          </div>
          <div className="grid gap-3">
            <div className="rounded-3xl bg-emerald-50 p-4 ring-1 ring-emerald-100">
              <p className="text-sm font-black text-emerald-800">잘 맞는 부분</p>
              <ul className="mt-2 space-y-1 text-sm leading-6 text-emerald-700">
                {result.styleFit.matchingPoints.map((point) => <li key={point}>• {point}</li>)}
              </ul>
            </div>
            <div className="rounded-3xl bg-rose-50 p-4 ring-1 ring-rose-100">
              <p className="text-sm font-black text-rose-800">확인할 부분</p>
              <ul className="mt-2 space-y-1 text-sm leading-6 text-rose-700">
                {result.styleFit.possibleGaps.map((gap) => <li key={gap}>• {gap}</li>)}
              </ul>
            </div>
          </div>
          <div className="rounded-3xl bg-cream p-4 text-sm leading-6 text-ink-muted ring-1 ring-ink/5">
            <p className="font-black text-ink">가이드</p>
            <p className="mt-1">{result.styleFit.guidance}</p>
          </div>
          <ToneProfileCard profile={result.userToneProfile} />
          <Card className="shadow-none">
            <p className="text-xs font-bold uppercase tracking-[0.18em] text-ink-faint">다음 행동</p>
            <h3 className="mt-2 text-lg font-black text-ink">{result.nextAction.type}</h3>
            <p className="mt-2 text-sm leading-6 text-ink-muted">{result.nextAction.reason}</p>
            <p className="mt-3 rounded-2xl bg-cream px-4 py-3 text-sm font-black text-ink ring-1 ring-ink/5">추천 타이밍: {result.nextAction.suggestedTiming}</p>
          </Card>
        </div>
      </details>
    </div>
  );
}

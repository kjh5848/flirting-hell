import type { CreateAnalysisData } from "@flirting-hell/shared";
import { ToneProfileCard } from "../../profile/components/ToneProfileCard";
import { Button } from "../../../shared/ui/Button";
import { Card } from "../../../shared/ui/Card";
import { ReplyOptionCard } from "./ReplyOptionCard";
import { RiskyMessageCard } from "./RiskyMessageCard";

type AnalysisResultProps = {
  data: CreateAnalysisData;
  onCopied: (message: string) => void;
};

export function AnalysisResult({ data, onCopied }: AnalysisResultProps) {
  const { result } = data;

  return (
    <div className="space-y-4">
      <Card className="bg-gray-950 text-white">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <p className="text-xs font-bold uppercase tracking-[0.2em] text-rose-200">현재 분위기</p>
          <span className="rounded-full bg-white/10 px-3 py-1 text-xs font-bold text-white/80">확신도 {result.mood.confidence}</span>
        </div>
        <div className="mt-4 flex flex-wrap items-center gap-2">
          <span className="rounded-full bg-white px-3 py-1 text-sm font-black text-gray-950">{result.mood.status}</span>
          {result.safety.needsToneDown ? <span className="rounded-full bg-rose-500 px-3 py-1 text-sm font-black text-white">강도 낮추기 권장</span> : null}
        </div>
        <h2 className="mt-4 text-2xl font-black leading-tight tracking-[-0.03em]">{result.mood.summary}</h2>
        <ul className="mt-4 space-y-2 text-sm leading-6 text-white/75">
          {result.mood.reasons.map((reason) => <li key={reason}>• {reason}</li>)}
        </ul>
      </Card>

      <Card>
        <p className="text-xs font-bold uppercase tracking-[0.2em] text-gray-400">내 스타일과의 적합도</p>
        <h3 className="mt-2 text-xl font-black text-gray-950">{result.styleFit.status}</h3>
        <p className="mt-2 text-sm leading-6 text-gray-600">{result.styleFit.summary}</p>
        <div className="mt-4 grid gap-3 md:grid-cols-2">
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
        <div className="mt-4 rounded-3xl bg-gray-50 p-4 text-sm leading-6 text-gray-700">
          <p className="font-black text-gray-950">가이드</p>
          <p className="mt-1">{result.styleFit.guidance}</p>
        </div>
      </Card>

      <ToneProfileCard profile={result.userToneProfile} />

      {result.safety.warning ? (
        <Card className="border border-rose-100 bg-rose-50">
          <p className="text-xs font-bold uppercase tracking-[0.2em] text-rose-500">안전 경고</p>
          <p className="mt-2 text-sm leading-6 text-rose-700/80">{result.safety.warning}</p>
        </Card>
      ) : null}

      <div className="space-y-3">
        <div className="flex items-end justify-between gap-3">
          <div>
            <p className="text-xs font-bold uppercase tracking-[0.2em] text-gray-400">Reply Options</p>
            <h3 className="mt-1 text-2xl font-black tracking-[-0.03em] text-gray-950">지금 보내기 좋은 답장</h3>
          </div>
          <Button variant="ghost" type="button" onClick={() => window.scrollTo({ top: 0, behavior: "smooth" })}>다시 입력</Button>
        </div>
        {result.replyOptions.map((option) => (
          <ReplyOptionCard key={option.level} option={option} analysisId={data.analysisId} onCopied={onCopied} />
        ))}
      </div>

      <RiskyMessageCard riskyMessage={result.riskyMessage} />

      <Card>
        <p className="text-xs font-bold uppercase tracking-[0.2em] text-gray-400">다음 행동</p>
        <h3 className="mt-2 text-xl font-black text-gray-950">{result.nextAction.type}</h3>
        <p className="mt-2 text-sm leading-6 text-gray-600">{result.nextAction.reason}</p>
        <p className="mt-3 rounded-2xl bg-gray-50 px-4 py-3 text-sm font-black text-gray-950">추천 타이밍: {result.nextAction.suggestedTiming}</p>
      </Card>
    </div>
  );
}

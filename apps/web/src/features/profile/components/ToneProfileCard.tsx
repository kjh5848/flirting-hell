import type { AnalysisResult } from "@flirting-hell/shared";
import { Card } from "../../../shared/ui/Card";

type ToneProfileCardProps = {
  profile: AnalysisResult["userToneProfile"];
};

export function ToneProfileCard({ profile }: ToneProfileCardProps) {
  return (
    <Card>
      <p className="text-xs font-bold uppercase tracking-[0.2em] text-ink-faint">내 말투 분석</p>
      <h3 className="mt-2 text-lg font-black text-ink">{profile.summary}</h3>
      <dl className="mt-4 grid grid-cols-2 gap-3 text-sm">
        <div className="rounded-2xl bg-cream p-3 ring-1 ring-ink/5">
          <dt className="text-ink-muted">말투</dt>
          <dd className="font-bold text-ink">{profile.speechLevel}</dd>
        </div>
        <div className="rounded-2xl bg-cream p-3 ring-1 ring-ink/5">
          <dt className="text-ink-muted">문장 길이</dt>
          <dd className="font-bold text-ink">{profile.sentenceLength}</dd>
        </div>
        <div className="rounded-2xl bg-cream p-3 ring-1 ring-ink/5">
          <dt className="text-ink-muted">장난기</dt>
          <dd className="font-bold text-ink">{profile.playfulness}</dd>
        </div>
        <div className="rounded-2xl bg-cream p-3 ring-1 ring-ink/5">
          <dt className="text-ink-muted">직진도</dt>
          <dd className="font-bold text-ink">{profile.directness}</dd>
        </div>
      </dl>
    </Card>
  );
}

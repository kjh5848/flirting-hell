import type { AnalysisResult } from "@flirting-hell/shared";
import { Card } from "../../../shared/ui/Card";

type ToneProfileCardProps = {
  profile: AnalysisResult["userToneProfile"];
};

export function ToneProfileCard({ profile }: ToneProfileCardProps) {
  return (
    <Card>
      <p className="text-xs font-bold uppercase tracking-[0.2em] text-gray-400">내 말투 분석</p>
      <h3 className="mt-2 text-lg font-black text-gray-950">{profile.summary}</h3>
      <dl className="mt-4 grid grid-cols-2 gap-3 text-sm">
        <div className="rounded-2xl bg-gray-50 p-3">
          <dt className="text-gray-500">말투</dt>
          <dd className="font-bold text-gray-900">{profile.speechLevel}</dd>
        </div>
        <div className="rounded-2xl bg-gray-50 p-3">
          <dt className="text-gray-500">문장 길이</dt>
          <dd className="font-bold text-gray-900">{profile.sentenceLength}</dd>
        </div>
        <div className="rounded-2xl bg-gray-50 p-3">
          <dt className="text-gray-500">장난기</dt>
          <dd className="font-bold text-gray-900">{profile.playfulness}</dd>
        </div>
        <div className="rounded-2xl bg-gray-50 p-3">
          <dt className="text-gray-500">직진도</dt>
          <dd className="font-bold text-gray-900">{profile.directness}</dd>
        </div>
      </dl>
    </Card>
  );
}

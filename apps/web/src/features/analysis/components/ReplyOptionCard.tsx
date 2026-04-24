import type { AnalysisResult } from "@flirting-hell/shared";
import { Button } from "../../../shared/ui/Button";
import { Card } from "../../../shared/ui/Card";
import { cn } from "../../../shared/lib/cn";
import { recordClientEvent } from "../api/analysisApi";

type ReplyOptionCardProps = {
  option: AnalysisResult["replyOptions"][number];
  analysisId: string;
  onCopied: (message: string) => void;
};

const levelStyles = {
  순한맛: "bg-emerald-50 text-emerald-700 ring-emerald-100",
  설렘맛: "bg-rose-50 text-rose-700 ring-rose-100",
  직진맛: "bg-gray-950 text-white ring-gray-950"
} as const;

export function ReplyOptionCard({ option, analysisId, onCopied }: ReplyOptionCardProps) {
  async function copyReply() {
    await navigator.clipboard.writeText(option.text);
    onCopied(`${option.level} 답장을 복사했습니다.`);
    void recordClientEvent({ eventName: "reply_copied", analysisId, metadata: { replyLevel: option.level } });
  }

  return (
    <Card className="space-y-4 transition hover:-translate-y-0.5 hover:shadow-[0_24px_70px_rgba(15,23,42,0.16)]">
      <div className="flex items-center justify-between gap-3">
        <span className={cn("rounded-full px-3 py-1 text-xs font-black ring-1", levelStyles[option.level])}>{option.level}</span>
        <span className="rounded-full bg-gray-50 px-3 py-1 text-xs font-bold text-gray-500">부담도 {option.pressure}</span>
      </div>
      <p className="text-xl font-black leading-9 tracking-[-0.02em] text-gray-950">“{option.text}”</p>
      <div className="space-y-2 rounded-2xl bg-gray-50 p-4 text-sm leading-6 text-gray-600">
        <p><strong className="text-gray-900">이유:</strong> {option.reason}</p>
        <p><strong className="text-gray-900">상황:</strong> {option.bestFor}</p>
        <p><strong className="text-gray-900">말투:</strong> {option.toneMatch}</p>
      </div>
      <Button type="button" className="w-full" onClick={copyReply}>이 답장 복사하기</Button>
    </Card>
  );
}

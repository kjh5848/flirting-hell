import type { AnalysisResult } from "@flirting-hell/shared";
import { Button } from "../../../shared/ui/Button";
import { Card } from "../../../shared/ui/Card";
import { recordClientEvent } from "../api/analysisApi";

type ReplyOptionCardProps = {
  option: AnalysisResult["replyOptions"][number];
  analysisId: string;
  onCopied: (message: string) => void;
};

export function ReplyOptionCard({ option, analysisId, onCopied }: ReplyOptionCardProps) {
  async function copyReply() {
    await navigator.clipboard.writeText(option.text);
    onCopied(`${option.level} 답장을 복사했습니다.`);
    void recordClientEvent({ eventName: "reply_copied", analysisId, metadata: { replyLevel: option.level } });
  }

  return (
    <Card className="space-y-4">
      <div className="flex items-center justify-between gap-3">
        <span className="rounded-full bg-gray-950 px-3 py-1 text-xs font-bold text-white">{option.level}</span>
        <span className="text-xs font-semibold text-gray-400">부담도 {option.pressure}</span>
      </div>
      <p className="text-lg font-black leading-8 text-gray-950">“{option.text}”</p>
      <div className="space-y-2 text-sm leading-6 text-gray-600">
        <p><strong className="text-gray-900">이유:</strong> {option.reason}</p>
        <p><strong className="text-gray-900">상황:</strong> {option.bestFor}</p>
        <p><strong className="text-gray-900">말투:</strong> {option.toneMatch}</p>
      </div>
      <Button type="button" className="w-full" onClick={copyReply}>복사하기</Button>
    </Card>
  );
}

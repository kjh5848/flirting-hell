import type { AnalysisResult } from "@flirting-hell/shared";
import { Card } from "../../../shared/ui/Card";

type RiskyMessageCardProps = {
  riskyMessage: AnalysisResult["riskyMessage"];
};

export function RiskyMessageCard({ riskyMessage }: RiskyMessageCardProps) {
  return (
    <Card className="border border-amber-100 bg-amber-50">
      <p className="text-xs font-bold uppercase tracking-[0.2em] text-amber-600">보내면 위험한 말</p>
      <p className="mt-3 text-lg font-black text-amber-950">“{riskyMessage.avoidText}”</p>
      <p className="mt-2 text-sm leading-6 text-amber-800">{riskyMessage.reason}</p>
      <div className="mt-4 rounded-2xl bg-white/80 p-4 text-sm leading-6 text-gray-800">
        <strong>대체 표현:</strong> {riskyMessage.alternative}
      </div>
    </Card>
  );
}

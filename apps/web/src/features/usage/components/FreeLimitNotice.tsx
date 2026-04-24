import { Card } from "../../../shared/ui/Card";

export function FreeLimitNotice() {
  return (
    <Card className="border border-rose-100 bg-rose-50">
      <p className="text-sm font-bold text-rose-700">오늘 무료 분석을 모두 사용했어요.</p>
      <p className="mt-2 text-sm leading-6 text-rose-700/80">MVP에서는 하루 3회까지 무료 분석을 제공합니다. 다음 단계에서 분석권 패키지 결제를 연결할 예정입니다.</p>
    </Card>
  );
}

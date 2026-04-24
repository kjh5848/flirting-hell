import { Button } from "../../../shared/ui/Button";
import { Card } from "../../../shared/ui/Card";

export function FreeLimitNotice() {
  return (
    <Card className="border border-rose-100 bg-rose-50">
      <p className="text-xs font-bold uppercase tracking-[0.2em] text-rose-500">Limit Reached</p>
      <h3 className="mt-2 text-xl font-black text-rose-950">오늘 무료 분석을 모두 사용했어요.</h3>
      <p className="mt-2 text-sm leading-6 text-rose-700/80">MVP에서는 하루 3회까지 무료 분석을 제공합니다. 다음 단계에서 분석권 패키지 결제를 연결할 예정입니다.</p>
      <div className="mt-4 grid gap-2 sm:grid-cols-2">
        <Button type="button" variant="secondary">내일 다시 사용하기</Button>
        <Button type="button">분석권 출시 알림</Button>
      </div>
    </Card>
  );
}

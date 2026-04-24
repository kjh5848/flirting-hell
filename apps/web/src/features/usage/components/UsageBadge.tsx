import type { UsageSummary } from "@flirting-hell/shared";

type UsageBadgeProps = {
  usage: UsageSummary | null;
  isLoading?: boolean;
};

export function UsageBadge({ usage, isLoading = false }: UsageBadgeProps) {
  if (isLoading) {
    return <div className="rounded-full bg-white/80 px-4 py-2 text-sm font-semibold text-gray-500 ring-1 ring-black/5">사용량 확인 중</div>;
  }

  const remaining = usage?.remainingToday ?? 3;
  const limit = usage?.freeLimit ?? 3;

  return (
    <div className="rounded-full bg-gray-950 px-4 py-2 text-sm font-black text-white shadow-sm">
      무료 분석 {remaining}/{limit}회
    </div>
  );
}

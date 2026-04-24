import type { UsageSummary } from "@flirting-hell/shared";

type UsageBadgeProps = {
  usage: UsageSummary | null;
  isLoading?: boolean;
};

export function UsageBadge({ usage, isLoading = false }: UsageBadgeProps) {
  if (isLoading) {
    return <div className="rounded-full bg-white/80 px-4 py-2 text-sm font-semibold text-gray-500 ring-1 ring-black/5">사용량 확인 중</div>;
  }

  return (
    <div className="rounded-full bg-white/90 px-4 py-2 text-sm font-semibold text-gray-800 ring-1 ring-black/5">
      무료 분석 {usage?.remainingToday ?? 3}/{usage?.freeLimit ?? 3}회 남음
    </div>
  );
}

import type { UsageSummary } from "@flirting-hell/shared";

type UsageBadgeProps = {
  usage: UsageSummary | null;
  isLoading?: boolean;
};

export function UsageBadge({ usage, isLoading = false }: UsageBadgeProps) {
  if (isLoading) {
    return <div className="rounded-full bg-white/80 px-4 py-2 text-sm font-semibold text-ink-muted ring-1 ring-ink/5">사용량 확인 중</div>;
  }

  const remaining = usage?.remainingToday ?? 3;
  const limit = usage?.freeLimit ?? 3;

  return (
    <div className="rounded-full bg-white px-3 py-2 text-xs font-black text-hell-600 shadow-sm ring-1 ring-hell-100">
      무료 분석 {remaining}/{limit}회
    </div>
  );
}

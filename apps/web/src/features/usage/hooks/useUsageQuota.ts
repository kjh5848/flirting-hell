import { useCallback, useEffect, useState } from "react";
import type { UsageSummary } from "@flirting-hell/shared";
import { getUsage } from "../api/usageApi";

export function useUsageQuota() {
  const [usage, setUsage] = useState<UsageSummary | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const reload = useCallback(async () => {
    setIsLoading(true);
    try {
      setUsage(await getUsage());
    } catch {
      setUsage(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void reload();
  }, [reload]);

  return {
    usage,
    isLoading,
    reload
  };
}

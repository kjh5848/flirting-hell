import type { UsageSummary } from "@flirting-hell/shared";
import { apiRequest } from "../../../shared/lib/apiClient";

const useRemoteApi = import.meta.env.VITE_API_MODE === "remote";

export function getUsage(): Promise<UsageSummary> {
  if (!useRemoteApi) {
    return Promise.resolve({
      date: new Date().toISOString().slice(0, 10),
      freeLimit: 3,
      usedToday: 0,
      remainingToday: 3,
      creditBalance: 0
    });
  }

  return apiRequest<UsageSummary>("/api/usage");
}

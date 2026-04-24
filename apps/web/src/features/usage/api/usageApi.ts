import type { UsageSummary } from "@flirting-hell/shared";
import { apiRequest } from "../../../shared/lib/apiClient";

export function getUsage(): Promise<UsageSummary> {
  return apiRequest<UsageSummary>("/api/usage");
}

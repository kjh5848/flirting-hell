import type { AnalysisRequest, CreateAnalysisData, EventRequest } from "@flirting-hell/shared";
import { apiRequest } from "../../../shared/lib/apiClient";

export function createAnalysis(input: AnalysisRequest): Promise<CreateAnalysisData> {
  return apiRequest<CreateAnalysisData>("/api/analyses", {
    method: "POST",
    body: input
  });
}

export function recordClientEvent(input: EventRequest): Promise<{ eventId: string }> {
  return apiRequest<{ eventId: string }>("/api/events", {
    method: "POST",
    body: input
  });
}

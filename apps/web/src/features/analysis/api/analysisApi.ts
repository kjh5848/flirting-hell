import type { AnalysisRequest, CreateAnalysisData, EventRequest } from "@flirting-hell/shared";
import { apiRequest } from "../../../shared/lib/apiClient";
import { createMockAnalysis, recordMockEvent } from "./mockAnalysis";

const useRemoteApi = import.meta.env.VITE_API_MODE === "remote";

export function createAnalysis(input: AnalysisRequest): Promise<CreateAnalysisData> {
  if (!useRemoteApi) {
    return createMockAnalysis(input);
  }

  return apiRequest<CreateAnalysisData>("/api/analyses", {
    method: "POST",
    body: input
  });
}

export function recordClientEvent(input: EventRequest): Promise<{ eventId: string }> {
  if (!useRemoteApi) {
    return recordMockEvent();
  }

  return apiRequest<{ eventId: string }>("/api/events", {
    method: "POST",
    body: input
  });
}

import {
  conversationGoals,
  guidanceModes,
  relationshipStages,
  replyIntensities,
  toneModes,
  type AnalysisRequest,
  type ApiError,
  type ApiResponse,
  type CreateAnalysisData,
  type EventData,
  type EventRequest,
  type HealthData,
  type UsageSummary
} from "@flirting-hell/shared";
import {
  createAnalysisRecord,
  eventMetadataFromRequest,
  getUsageSummary,
  incrementFreeUsage,
  recordEvent,
  touchAnonymousUser,
  type Env
} from "./db";
import { createMockAnalysisResult } from "./mockAnalysis";

const jsonHeaders = {
  "content-type": "application/json; charset=utf-8",
  "access-control-allow-origin": "*",
  "access-control-allow-methods": "GET,POST,OPTIONS",
  "access-control-allow-headers": "content-type,x-anonymous-user-id"
};

function json<T>(response: ApiResponse<T>, init: ResponseInit = {}): Response {
  return new Response(JSON.stringify(response), {
    ...init,
    headers: {
      ...jsonHeaders,
      ...init.headers
    }
  });
}

function success<T>(data: T, init?: ResponseInit): Response {
  return json<T>({ ok: true, data }, init);
}

function failure(error: ApiError, status = 400): Response {
  return json<never>({ ok: false, error }, { status });
}

function getAnonymousUserId(request: Request): string | null {
  const value = request.headers.get("x-anonymous-user-id")?.trim();
  return value && value.length >= 16 ? value : null;
}

function includesValue<T extends readonly string[]>(values: T, value: unknown): value is T[number] {
  return typeof value === "string" && values.includes(value);
}

async function parseJson(request: Request): Promise<unknown> {
  try {
    return await request.json();
  } catch {
    return null;
  }
}

function validateAnalysisRequest(value: unknown): AnalysisRequest | ApiError {
  if (!value || typeof value !== "object") {
    return { code: "VALIDATION_ERROR", message: "요청 본문이 비어 있습니다." };
  }

  const body = value as Partial<AnalysisRequest>;
  const conversationText = typeof body.conversationText === "string" ? body.conversationText.trim() : "";

  if (conversationText.length < 20) {
    return { code: "VALIDATION_ERROR", message: "대화가 너무 짧아요. 최근 대화 10줄 정도를 붙여넣어 주세요." };
  }

  if (!includesValue(relationshipStages, body.relationshipStage)) {
    return { code: "VALIDATION_ERROR", message: "관계 단계를 선택해 주세요." };
  }

  if (!includesValue(conversationGoals, body.conversationGoal)) {
    return { code: "VALIDATION_ERROR", message: "대화 목적을 선택해 주세요." };
  }

  if (!includesValue(replyIntensities, body.replyIntensity)) {
    return { code: "VALIDATION_ERROR", message: "답장 강도를 선택해 주세요." };
  }

  if (!includesValue(guidanceModes, body.guidanceMode)) {
    return { code: "VALIDATION_ERROR", message: "조언 수위를 선택해 주세요." };
  }

  if (!includesValue(toneModes, body.toneMode)) {
    return { code: "VALIDATION_ERROR", message: "말투 반영 방식을 선택해 주세요." };
  }

  return {
    conversationText,
    relationshipStage: body.relationshipStage,
    conversationGoal: body.conversationGoal,
    replyIntensity: body.replyIntensity,
    guidanceMode: body.guidanceMode,
    toneMode: body.toneMode,
    userPreferences: body.userPreferences
  };
}

function validateEventRequest(value: unknown): EventRequest | ApiError {
  if (!value || typeof value !== "object") {
    return { code: "VALIDATION_ERROR", message: "이벤트 요청 본문이 비어 있습니다." };
  }

  const body = value as Partial<EventRequest>;
  if (body.eventName !== "reply_copied" && body.eventName !== "feedback_submitted") {
    return { code: "VALIDATION_ERROR", message: "허용되지 않는 이벤트입니다." };
  }

  return {
    eventName: body.eventName,
    analysisId: typeof body.analysisId === "string" ? body.analysisId : undefined,
    metadata: body.metadata && typeof body.metadata === "object" ? body.metadata : undefined
  };
}

async function handleUsage(request: Request, env: Env): Promise<Response> {
  const anonymousUserId = getAnonymousUserId(request);
  if (!anonymousUserId) {
    return failure({ code: "VALIDATION_ERROR", message: "익명 사용자 ID가 필요합니다." });
  }

  await touchAnonymousUser(env, anonymousUserId);
  const usage = await getUsageSummary(env, anonymousUserId);
  return success<UsageSummary>(usage);
}

async function handleCreateAnalysis(request: Request, env: Env): Promise<Response> {
  const anonymousUserId = getAnonymousUserId(request);
  if (!anonymousUserId) {
    return failure({ code: "VALIDATION_ERROR", message: "익명 사용자 ID가 필요합니다." });
  }

  const parsed = validateAnalysisRequest(await parseJson(request));
  if ("code" in parsed) {
    return failure(parsed);
  }

  await touchAnonymousUser(env, anonymousUserId);
  const usageBefore = await getUsageSummary(env, anonymousUserId);

  if (usageBefore.remainingToday <= 0) {
    await recordEvent(env, anonymousUserId, "free_limit_reached", { freeLimit: usageBefore.freeLimit });
    return failure({ code: "LIMIT_REACHED", message: "오늘 무료 분석 3회를 모두 사용했어요. 내일 다시 이용하거나 분석권 구매를 기다려 주세요." }, 429);
  }

  await recordEvent(env, anonymousUserId, "analysis_started", {
    relationshipStage: parsed.relationshipStage,
    conversationGoal: parsed.conversationGoal,
    replyIntensity: parsed.replyIntensity
  });

  const result = createMockAnalysisResult(parsed);
  const analysisId = await createAnalysisRecord(env, anonymousUserId, parsed, result);
  const usageAfter = await incrementFreeUsage(env, anonymousUserId);

  await recordEvent(env, anonymousUserId, "analysis_completed", {
    analysisId,
    moodStatus: result.mood.status,
    styleFitStatus: result.styleFit.status
  }, analysisId);

  const data: CreateAnalysisData = {
    analysisId,
    usage: {
      freeLimit: usageAfter.freeLimit,
      usedToday: usageAfter.usedToday,
      remainingToday: usageAfter.remainingToday,
      creditBalance: usageAfter.creditBalance
    },
    result
  };

  return success<CreateAnalysisData>(data);
}

async function handleCreateEvent(request: Request, env: Env): Promise<Response> {
  const anonymousUserId = getAnonymousUserId(request);
  if (!anonymousUserId) {
    return failure({ code: "VALIDATION_ERROR", message: "익명 사용자 ID가 필요합니다." });
  }

  const parsed = validateEventRequest(await parseJson(request));
  if ("code" in parsed) {
    return failure(parsed);
  }

  await touchAnonymousUser(env, anonymousUserId);
  const eventId = await recordEvent(env, anonymousUserId, parsed.eventName, eventMetadataFromRequest(parsed), parsed.analysisId);
  return success<EventData>({ eventId });
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    if (request.method === "OPTIONS") {
      return new Response(null, { status: 204, headers: jsonHeaders });
    }

    const url = new URL(request.url);

    try {
      if (request.method === "GET" && url.pathname === "/api/health") {
        return success<HealthData>({ status: "ok", service: "flirting-hell-api", version: "0.1.0" });
      }

      if (request.method === "GET" && url.pathname === "/api/usage") {
        return handleUsage(request, env);
      }

      if (request.method === "POST" && url.pathname === "/api/analyses") {
        return handleCreateAnalysis(request, env);
      }

      if (request.method === "POST" && url.pathname === "/api/events") {
        return handleCreateEvent(request, env);
      }

      return failure({ code: "VALIDATION_ERROR", message: "존재하지 않는 API 경로입니다." }, 404);
    } catch (error) {
      console.error(error);
      return failure({ code: "SERVER_ERROR", message: "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요." }, 500);
    }
  }
};

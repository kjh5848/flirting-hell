import type { AnalysisRequest, AnalysisResult, EventRequest, UsageSummary } from "@flirting-hell/shared";

export type Env = {
  DB: D1Database;
  FREE_DAILY_LIMIT?: string;
};

type UsageRow = {
  free_used_count: number;
  credit_used_count: number;
};

const seoulDateFormatter = new Intl.DateTimeFormat("en-CA", {
  timeZone: "Asia/Seoul",
  year: "numeric",
  month: "2-digit",
  day: "2-digit"
});

export function nowIso(): string {
  return new Date().toISOString();
}

export function todayInSeoul(): string {
  return seoulDateFormatter.format(new Date());
}

export function getFreeLimit(env: Env): number {
  const parsed = Number(env.FREE_DAILY_LIMIT ?? "3");
  return Number.isFinite(parsed) && parsed > 0 ? parsed : 3;
}

export async function touchAnonymousUser(env: Env, anonymousUserId: string): Promise<void> {
  const now = nowIso();
  await env.DB.prepare(
    `insert into anonymous_users (id, created_at, last_seen_at)
     values (?, ?, ?)
     on conflict(id) do update set last_seen_at = excluded.last_seen_at`
  ).bind(anonymousUserId, now, now).run();
}

export async function getUsageSummary(env: Env, anonymousUserId: string): Promise<UsageSummary> {
  const usageDate = todayInSeoul();
  const row = await env.DB.prepare(
    `select free_used_count, credit_used_count
     from usage_days
     where anonymous_user_id = ? and usage_date = ?`
  ).bind(anonymousUserId, usageDate).first<UsageRow>();

  const usedToday = row?.free_used_count ?? 0;
  const freeLimit = getFreeLimit(env);

  return {
    date: usageDate,
    freeLimit,
    usedToday,
    remainingToday: Math.max(freeLimit - usedToday, 0),
    creditBalance: 0
  };
}

export async function incrementFreeUsage(env: Env, anonymousUserId: string): Promise<UsageSummary> {
  const usageDate = todayInSeoul();
  const now = nowIso();
  const usageId = `${anonymousUserId}:${usageDate}`;

  await env.DB.prepare(
    `insert into usage_days (id, anonymous_user_id, usage_date, free_used_count, credit_used_count, created_at, updated_at)
     values (?, ?, ?, 1, 0, ?, ?)
     on conflict(anonymous_user_id, usage_date)
     do update set free_used_count = free_used_count + 1, updated_at = excluded.updated_at`
  ).bind(usageId, anonymousUserId, usageDate, now, now).run();

  return getUsageSummary(env, anonymousUserId);
}

export async function createAnalysisRecord(
  env: Env,
  anonymousUserId: string,
  request: AnalysisRequest,
  result: AnalysisResult
): Promise<string> {
  const analysisId = `ana_${crypto.randomUUID()}`;
  await env.DB.prepare(
    `insert into analyses (
      id,
      anonymous_user_id,
      relationship_stage,
      conversation_goal,
      reply_intensity,
      guidance_mode,
      tone_mode,
      mood_status,
      style_fit_status,
      ai_result_json,
      input_character_count,
      used_free_credit,
      used_paid_credit,
      created_at
    ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 0, ?)`
  ).bind(
    analysisId,
    anonymousUserId,
    request.relationshipStage,
    request.conversationGoal,
    request.replyIntensity,
    request.guidanceMode,
    request.toneMode,
    result.mood.status,
    result.styleFit.status,
    JSON.stringify(result),
    request.conversationText.length,
    nowIso()
  ).run();

  return analysisId;
}

export async function recordEvent(
  env: Env,
  anonymousUserId: string,
  eventName: string,
  metadata?: Record<string, unknown>,
  analysisId?: string
): Promise<string> {
  const eventId = `evt_${crypto.randomUUID()}`;
  await env.DB.prepare(
    `insert into events (id, anonymous_user_id, analysis_id, event_name, metadata_json, created_at)
     values (?, ?, ?, ?, ?, ?)`
  ).bind(eventId, anonymousUserId, analysisId ?? null, eventName, metadata ? JSON.stringify(metadata) : null, nowIso()).run();
  return eventId;
}

export function eventMetadataFromRequest(request: EventRequest): Record<string, unknown> {
  return request.metadata ?? {};
}

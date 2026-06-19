// 어드민은 내부 운영툴. 개발 중에는 dev 어드민 토큰을 사용한다.
// 실 운영은 Firebase custom claim(admin) 기반 로그인으로 교체한다(플랜 §4).
const DEV_ADMIN_TOKEN = "dev:admin-1";

async function apiGet<T>(path: string): Promise<T> {
  const res = await fetch(`/api${path}`, {
    headers: { Authorization: `Bearer ${DEV_ADMIN_TOKEN}` },
  });
  if (!res.ok) {
    throw new Error(`요청 실패 (HTTP ${res.status})`);
  }
  const body = await res.json();
  return body.data as T;
}

export interface OutcomeBreakdown {
  sentGood: number;
  sentSoso: number;
  notSent: number;
}

export interface CreditUsage {
  freeUsedToday: number;
  creditSpentToday: number;
  rewardWatched: number;
}

export interface AdminMetrics {
  dailyActiveUsers: number;
  analysesToday: number;
  savedReplies: number;
  outcome: OutcomeBreakdown;
  credit: CreditUsage;
}

export interface AdminUser {
  userId: string;
  onboardingCompleted: boolean;
  analysisCount: number;
  freeRemaining: number;
  joinedAt: string;
}

export interface ModerationFlag {
  source: string;
  generatedText: string;
  rules: string[];
}

export interface QualityRow {
  target: string;
  note: string;
}

export interface LlmStatus {
  provider: string;
  quality: QualityRow[];
  cost: { monthlyKrw: number; note: string };
}

export const fetchMetrics = () => apiGet<AdminMetrics>("/admin/metrics");
export const fetchUsers = () =>
  apiGet<{ users: AdminUser[] }>("/admin/users").then((d) => d.users);
export const fetchModeration = () =>
  apiGet<{ flags: ModerationFlag[] }>("/admin/moderation").then((d) => d.flags);
export const fetchLlm = () => apiGet<LlmStatus>("/admin/llm");

export const RULE_LABELS: Record<string, string> = {
  PRESSURE: "압박·재촉",
  GUILT: "죄책감 유발",
  OBSESSION: "집착·통제",
};

import { useEffect, useState } from "react";

import {
  AdminMetrics,
  AdminUser,
  Announcement,
  FeatureFlag,
  LlmStatus,
  ModerationFlag,
  RULE_LABELS,
  RevenueMetrics,
  fetchAnnouncements,
  fetchFlags,
  fetchLlm,
  fetchMetrics,
  fetchModeration,
  fetchRevenue,
  fetchUsers,
  setFlag,
} from "./api";

const COLORS = {
  bg: "#FBF4F1",
  surface: "#FFFFFF",
  ink: "#2A2024",
  sub: "#6E5C62",
  accent: "#C65F77",
  blush: "#F4E3E6",
  border: "#EFE0E3",
};

type View =
  | "dashboard"
  | "users"
  | "moderation"
  | "llm"
  | "flags"
  | "content"
  | "revenue";

export function App() {
  const [view, setView] = useState<View>("dashboard");

  return (
    <div
      style={{
        display: "flex",
        minHeight: "100vh",
        background: COLORS.bg,
        color: COLORS.ink,
        fontFamily:
          "Pretendard, 'Noto Sans KR', ui-sans-serif, system-ui, sans-serif",
      }}
    >
      <Sidebar view={view} onSelect={setView} />
      <main style={{ flex: 1, padding: "32px 40px", maxWidth: 980 }}>
        {view === "dashboard" && <Dashboard />}
        {view === "users" && <Users />}
        {view === "moderation" && <Moderation />}
        {view === "llm" && <Llm />}
        {view === "flags" && <Flags />}
        {view === "content" && <Content />}
        {view === "revenue" && <Revenue />}
      </main>
    </div>
  );
}

function Sidebar({
  view,
  onSelect,
}: {
  view: View;
  onSelect: (v: View) => void;
}) {
  const items: { key: View; label: string }[] = [
    { key: "dashboard", label: "대시보드" },
    { key: "users", label: "사용자" },
    { key: "moderation", label: "안전" },
    { key: "llm", label: "LLM 운영" },
    { key: "flags", label: "기능 플래그" },
    { key: "content", label: "콘텐츠" },
    { key: "revenue", label: "결제 지표" },
  ];
  return (
    <aside
      style={{
        width: 220,
        background: COLORS.surface,
        borderRight: `1px solid ${COLORS.border}`,
        padding: "28px 18px",
      }}
    >
      <div style={{ fontWeight: 900, fontSize: 18, letterSpacing: "-0.5px" }}>
        플러팅지옥
      </div>
      <div style={{ color: COLORS.sub, fontSize: 12, marginBottom: 28 }}>
        어드민 콘솔
      </div>
      {items.map((item) => (
        <button
          key={item.key}
          onClick={() => onSelect(item.key)}
          style={{
            display: "block",
            width: "100%",
            textAlign: "left",
            padding: "10px 12px",
            marginBottom: 6,
            borderRadius: 12,
            border: "none",
            cursor: "pointer",
            fontWeight: 700,
            fontSize: 14,
            background: view === item.key ? COLORS.blush : "transparent",
            color: view === item.key ? COLORS.accent : COLORS.ink,
          }}
        >
          {item.label}
        </button>
      ))}
    </aside>
  );
}

function useAsync<T>(loader: () => Promise<T>) {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<string | null>(null);
  useEffect(() => {
    let alive = true;
    loader()
      .then((d) => alive && setData(d))
      .catch((e) => alive && setError(String(e)));
    return () => {
      alive = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
  return { data, error };
}

function Dashboard() {
  const { data, error } = useAsync<AdminMetrics>(fetchMetrics);
  if (error) return <ErrorBox message={error} />;
  if (!data) return <Loading />;
  return (
    <div>
      <Header title="대시보드" subtitle="핵심 운영 지표 (집계·비식별)" />
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(3, 1fr)",
          gap: 14,
          marginBottom: 24,
        }}
      >
        <Metric label="오늘 활성 사용자" value={data.dailyActiveUsers} />
        <Metric label="오늘 분석 수" value={data.analysesToday} />
        <Metric label="저장된 답장" value={data.savedReplies} />
      </div>
      <Card title="결과 피드백 분포">
        <Bar label="보냈고 좋음" value={data.outcome.sentGood} />
        <Bar label="보냈는데 그냥" value={data.outcome.sentSoso} />
        <Bar label="아직 안 보냄" value={data.outcome.notSent} />
      </Card>
      <div style={{ height: 14 }} />
      <Card title="분석권 사용 (오늘)">
        <Row label="무료 사용" value={data.credit.freeUsedToday} />
        <Row label="분석권 차감" value={data.credit.creditSpentToday} />
        <Row label="리워드 시청" value={data.credit.rewardWatched} />
      </Card>
    </div>
  );
}

function Users() {
  const { data, error } = useAsync<AdminUser[]>(fetchUsers);
  if (error) return <ErrorBox message={error} />;
  if (!data) return <Loading />;
  return (
    <div>
      <Header title="사용자" subtitle="비식별 userId·사용량만 (원문·실명 없음)" />
      <Card>
        <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 14 }}>
          <thead>
            <tr style={{ color: COLORS.sub, textAlign: "left" }}>
              <Th>userId</Th>
              <Th>온보딩</Th>
              <Th>분석 수</Th>
              <Th>무료 잔여</Th>
              <Th>가입일</Th>
            </tr>
          </thead>
          <tbody>
            {data.map((u) => (
              <tr key={u.userId} style={{ borderTop: `1px solid ${COLORS.border}` }}>
                <Td mono>{u.userId}</Td>
                <Td>{u.onboardingCompleted ? "완료" : "전"}</Td>
                <Td>{u.analysisCount}</Td>
                <Td>{u.freeRemaining}</Td>
                <Td>{u.joinedAt}</Td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  );
}

function Moderation() {
  const { data, error } = useAsync<ModerationFlag[]>(fetchModeration);
  if (error) return <ErrorBox message={error} />;
  if (!data) return <Loading />;
  return (
    <div>
      <Header
        title="안전"
        subtitle="생성된 추천 답장만 검사 (원문 대화 미검사). 휴리스틱 신호 — 진짜 판정은 실 LLM 단계."
      />
      {data.length === 0 ? (
        <Card>
          <p style={{ color: COLORS.sub, fontSize: 14 }}>플래그된 출력이 없어요.</p>
        </Card>
      ) : (
        data.map((flag) => (
          <div key={flag.source} style={{ marginBottom: 10 }}>
            <Card>
              <div style={{ display: "flex", gap: 8, marginBottom: 8, flexWrap: "wrap" }}>
                {flag.rules.map((r) => (
                  <span
                    key={r}
                    style={{
                      background: COLORS.blush,
                      color: COLORS.accent,
                      fontSize: 12,
                      fontWeight: 700,
                      padding: "3px 10px",
                      borderRadius: 999,
                    }}
                  >
                    {RULE_LABELS[r] ?? r}
                  </span>
                ))}
                <span style={{ color: COLORS.sub, fontSize: 12, alignSelf: "center" }}>
                  {flag.source}
                </span>
              </div>
              <div style={{ fontSize: 14, fontWeight: 700 }}>"{flag.generatedText}"</div>
            </Card>
          </div>
        ))
      )}
    </div>
  );
}

function Llm() {
  const { data, error } = useAsync<LlmStatus>(fetchLlm);
  if (error) return <ErrorBox message={error} />;
  if (!data) return <Loading />;
  return (
    <div>
      <Header title="LLM 운영" subtitle="provider 현황 · 품질 · 비용" />
      <Card title="현재 provider">
        <div style={{ fontSize: 22, fontWeight: 900, color: COLORS.accent }}>
          {data.provider}
        </div>
      </Card>
      <div style={{ height: 14 }} />
      <Card title="품질 요약">
        {data.quality.map((q) => (
          <div key={q.target} style={{ padding: "8px 0" }}>
            <div style={{ fontSize: 14, fontWeight: 700 }}>{q.target}</div>
            <div style={{ color: COLORS.sub, fontSize: 13 }}>{q.note}</div>
          </div>
        ))}
      </Card>
      <div style={{ height: 14 }} />
      <Card title="비용(추정)">
        <Row label="월 추정(₩)" value={data.cost.monthlyKrw} />
        <p style={{ color: COLORS.sub, fontSize: 12, marginTop: 6 }}>{data.cost.note}</p>
      </Card>
    </div>
  );
}

function Flags() {
  const [flags, setFlags] = useState<FeatureFlag[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  useEffect(() => {
    fetchFlags().then(setFlags).catch((e) => setError(String(e)));
  }, []);
  const toggle = (key: string, enabled: boolean) => {
    setFlag(key, enabled).then(setFlags).catch((e) => setError(String(e)));
  };
  if (error) return <ErrorBox message={error} />;
  if (!flags) return <Loading />;
  return (
    <div>
      <Header title="기능 플래그" subtitle="기능 노출을 켜고 끕니다 (mock — 재시작 시 기본값)" />
      <Card>
        {flags.map((f) => (
          <div
            key={f.key}
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              padding: "12px 0",
              borderBottom: `1px solid ${COLORS.border}`,
            }}
          >
            <div>
              <div style={{ fontWeight: 700, fontSize: 14 }}>{f.label}</div>
              <div style={{ color: COLORS.sub, fontSize: 12, fontFamily: "ui-monospace, monospace" }}>
                {f.key}
              </div>
            </div>
            <Toggle on={f.enabled} onChange={(v) => toggle(f.key, v)} />
          </div>
        ))}
      </Card>
    </div>
  );
}

function Toggle({ on, onChange }: { on: boolean; onChange: (v: boolean) => void }) {
  return (
    <button
      onClick={() => onChange(!on)}
      style={{
        width: 46,
        height: 26,
        borderRadius: 999,
        border: "none",
        cursor: "pointer",
        background: on ? COLORS.accent : "#D8CCce",
        position: "relative",
        transition: "background 120ms",
      }}
      aria-pressed={on}
    >
      <span
        style={{
          position: "absolute",
          top: 3,
          left: on ? 23 : 3,
          width: 20,
          height: 20,
          borderRadius: "50%",
          background: "#fff",
          transition: "left 120ms",
        }}
      />
    </button>
  );
}

function Content() {
  const { data, error } = useAsync<Announcement[]>(fetchAnnouncements);
  if (error) return <ErrorBox message={error} />;
  if (!data) return <Loading />;
  return (
    <div>
      <Header title="콘텐츠" subtitle="공지·안내 (Phase 3 읽기 전용, 작성은 후속)" />
      {data.map((a) => (
        <div key={a.id} style={{ marginBottom: 10 }}>
          <Card>
            <div style={{ display: "flex", justifyContent: "space-between" }}>
              <span style={{ fontWeight: 900, fontSize: 16 }}>{a.title}</span>
              <span style={{ color: COLORS.sub, fontSize: 12 }}>{a.publishedAt}</span>
            </div>
            <p style={{ color: COLORS.sub, fontSize: 14, margin: "8px 0 0" }}>{a.body}</p>
          </Card>
        </div>
      ))}
    </div>
  );
}

function Revenue() {
  const { data, error } = useAsync<RevenueMetrics>(fetchRevenue);
  if (error) return <ErrorBox message={error} />;
  if (!data) return <Loading />;
  return (
    <div>
      <Header title="결제 지표" subtitle="결제 연동(RevenueCat) 전 — mock 지표" />
      <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 14, marginBottom: 24 }}>
        <Metric label="MRR (₩)" value={data.mrrKrw} />
        <Metric label="결제 사용자" value={data.payingUsers} />
        <Metric label="전환율 (%)" value={data.conversionRatePercent} />
      </div>
      <Card title="패키지별 매출">
        {data.byPackage.map((p) => (
          <Row key={p.name} label={`${p.name} · ${p.count}건`} value={p.revenueKrw} />
        ))}
        <div style={{ height: 6 }} />
        <Row label="이번 달 환불" value={data.refundsThisMonth} />
      </Card>
    </div>
  );
}

function Header({ title, subtitle }: { title: string; subtitle: string }) {
  return (
    <div style={{ marginBottom: 22 }}>
      <h1 style={{ margin: 0, fontSize: 26, fontWeight: 900, letterSpacing: "-1px" }}>
        {title}
      </h1>
      <p style={{ margin: "4px 0 0", color: COLORS.sub, fontSize: 13 }}>{subtitle}</p>
    </div>
  );
}

function Metric({ label, value }: { label: string; value: number }) {
  return (
    <div
      style={{
        background: COLORS.surface,
        border: `1px solid ${COLORS.border}`,
        borderRadius: 18,
        padding: "18px 20px",
      }}
    >
      <div style={{ color: COLORS.sub, fontSize: 12, fontWeight: 700 }}>{label}</div>
      <div style={{ fontSize: 30, fontWeight: 900, letterSpacing: "-1px", marginTop: 6 }}>
        {value.toLocaleString()}
      </div>
    </div>
  );
}

function Card({ title, children }: { title?: string; children: React.ReactNode }) {
  return (
    <div
      style={{
        background: COLORS.surface,
        border: `1px solid ${COLORS.border}`,
        borderRadius: 20,
        padding: "20px 22px",
      }}
    >
      {title && (
        <div style={{ fontWeight: 900, fontSize: 16, marginBottom: 14 }}>{title}</div>
      )}
      {children}
    </div>
  );
}

function Bar({ label, value }: { label: string; value: number }) {
  return (
    <div style={{ marginBottom: 10 }}>
      <div style={{ display: "flex", justifyContent: "space-between", fontSize: 13 }}>
        <span style={{ color: COLORS.sub }}>{label}</span>
        <span style={{ fontWeight: 700 }}>{value}%</span>
      </div>
      <div
        style={{
          height: 8,
          background: COLORS.blush,
          borderRadius: 999,
          marginTop: 4,
          overflow: "hidden",
        }}
      >
        <div
          style={{
            width: `${value}%`,
            height: "100%",
            background: COLORS.accent,
            borderRadius: 999,
          }}
        />
      </div>
    </div>
  );
}

function Row({ label, value }: { label: string; value: number }) {
  return (
    <div
      style={{
        display: "flex",
        justifyContent: "space-between",
        padding: "6px 0",
        fontSize: 14,
      }}
    >
      <span style={{ color: COLORS.sub }}>{label}</span>
      <span style={{ fontWeight: 700 }}>{value.toLocaleString()}</span>
    </div>
  );
}

function Th({ children }: { children: React.ReactNode }) {
  return (
    <th style={{ padding: "8px 6px", fontWeight: 700, fontSize: 12 }}>{children}</th>
  );
}

function Td({ children, mono }: { children: React.ReactNode; mono?: boolean }) {
  return (
    <td
      style={{
        padding: "10px 6px",
        fontFamily: mono ? "ui-monospace, Menlo, monospace" : "inherit",
      }}
    >
      {children}
    </td>
  );
}

function Loading() {
  return <p style={{ color: COLORS.sub }}>불러오는 중…</p>;
}

function ErrorBox({ message }: { message: string }) {
  return (
    <Card title="불러오지 못했어요">
      <p style={{ color: COLORS.sub, fontSize: 13 }}>
        백엔드(localhost:8080)가 실행 중인지, 어드민 권한 토큰인지 확인하세요. ({message})
      </p>
    </Card>
  );
}

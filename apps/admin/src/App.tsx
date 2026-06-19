import { useEffect, useState } from "react";

import {
  AdminMetrics,
  AdminUser,
  fetchMetrics,
  fetchUsers,
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

type View = "dashboard" | "users";

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
        {view === "dashboard" ? <Dashboard /> : <Users />}
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

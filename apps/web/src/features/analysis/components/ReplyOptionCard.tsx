import type { AnalysisResult } from "@flirting-hell/shared";
import { Button } from "../../../shared/ui/Button";
import { Card } from "../../../shared/ui/Card";
import { cn } from "../../../shared/lib/cn";
import { recordClientEvent } from "../api/analysisApi";

type ReplyOptionCardProps = {
  option: AnalysisResult["replyOptions"][number];
  analysisId: string;
  onCopied: (message: string, tone?: "success" | "error") => void;
  featured?: boolean;
};

const levelStyles = {
  순한맛: "bg-emerald-50 text-emerald-700 ring-emerald-100",
  설렘맛: "bg-hell-50 text-hell-700 ring-hell-100",
  직진맛: "bg-ink text-cream ring-ink"
} as const;

async function copyText(text: string): Promise<void> {
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(text);
    return;
  }

  const textarea = document.createElement("textarea");
  textarea.value = text;
  textarea.setAttribute("readonly", "true");
  textarea.style.position = "fixed";
  textarea.style.left = "-9999px";
  document.body.appendChild(textarea);
  textarea.select();

  const didCopy = document.execCommand("copy");
  document.body.removeChild(textarea);

  if (!didCopy) {
    throw new Error("Clipboard copy failed");
  }
}

export function ReplyOptionCard({ option, analysisId, onCopied, featured = false }: ReplyOptionCardProps) {
  const highlights = [
    { label: "톤", value: option.level },
    { label: "부담도", value: option.pressure },
    { label: "말투", value: "맞춤" }
  ];

  async function copyReply() {
    try {
      await copyText(option.text);
      onCopied(`${option.level} 답장을 복사했습니다.`);
      void recordClientEvent({ eventName: "reply_copied", analysisId, metadata: { replyLevel: option.level } });
    } catch {
      onCopied("복사하지 못했습니다. 답장 문장을 길게 눌러 직접 복사해 주세요.", "error");
    }
  }

  return (
    <Card className={cn("reply-card space-y-3 border border-white/80 transition hover:-translate-y-0.5", featured ? "bg-white p-5 shadow-soft ring-hell-100" : "bg-white/85 p-4 shadow-sm hover:shadow-soft")}>
      <div className="flex items-center justify-between gap-3">
        <span className={cn("rounded-full px-3 py-1 text-xs font-black ring-1", levelStyles[option.level])}>{option.level}</span>
        <span className="rounded-full bg-cream px-3 py-1 text-xs font-bold text-ink-muted ring-1 ring-ink/5">부담도 {option.pressure}</span>
      </div>
      <p className={cn("rounded-[24px] bg-blush px-4 py-4 font-black tracking-[-0.03em] text-ink ring-1 ring-hell-100/80", featured ? "text-2xl leading-10" : "text-lg leading-8")}>“{option.text}”</p>
      {featured ? (
        <div className="reply-highlights">
          {highlights.map((item) => (
            <span key={item.label}>
              <small>{item.label}</small>
              <strong>{item.value}</strong>
            </span>
          ))}
        </div>
      ) : null}
      <div className={cn("space-y-2 rounded-2xl bg-cream text-sm leading-6 text-ink-muted ring-1 ring-ink/5", featured ? "p-4" : "p-3")}>
        <p><strong className="text-ink">이유:</strong> {option.reason}</p>
        {featured ? <p><strong className="text-ink">상황:</strong> {option.bestFor}</p> : null}
        {featured ? <p><strong className="text-ink">말투:</strong> {option.toneMatch}</p> : null}
      </div>
      <Button type="button" className="w-full" onClick={copyReply}>{featured ? "추천 답장 복사하기" : "이 답장 복사하기"}</Button>
    </Card>
  );
}

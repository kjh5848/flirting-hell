import { cn } from "../lib/cn";

type ToastProps = {
  message: string | null;
  tone?: "info" | "error" | "success";
};

export function Toast({ message, tone = "info" }: ToastProps) {
  if (!message) {
    return null;
  }

  return (
    <div
      role={tone === "error" ? "alert" : "status"}
      className={cn(
        "rounded-3xl px-4 py-3 text-sm font-bold shadow-sm ring-1",
        tone === "error" && "bg-rose-50 text-rose-700 ring-rose-100",
        tone === "success" && "bg-emerald-50 text-emerald-700 ring-emerald-100",
        tone === "info" && "bg-white text-ink-muted ring-ink/10"
      )}
    >
      {message}
    </div>
  );
}

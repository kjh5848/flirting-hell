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
      className={cn(
        "rounded-2xl px-4 py-3 text-sm font-medium",
        tone === "error" && "bg-rose-50 text-rose-700 ring-1 ring-rose-100",
        tone === "success" && "bg-emerald-50 text-emerald-700 ring-1 ring-emerald-100",
        tone === "info" && "bg-gray-100 text-gray-700 ring-1 ring-gray-200"
      )}
    >
      {message}
    </div>
  );
}

import type { ButtonHTMLAttributes } from "react";
import { cn } from "../lib/cn";

type ChipProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  selected?: boolean;
};

export function Chip({ className, selected = false, ...props }: ChipProps) {
  return (
    <button
      type="button"
      className={cn(
        "min-h-10 cursor-pointer rounded-full px-3.5 py-2 text-sm font-bold transition ring-1 focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-hell-600/15",
        selected ? "bg-hell-600 text-white ring-hell-600" : "bg-white text-ink-muted ring-ink/10 hover:bg-cream",
        className
      )}
      {...props}
    />
  );
}

import type { ButtonHTMLAttributes } from "react";
import { cn } from "../lib/cn";

type ButtonVariant = "primary" | "secondary" | "ghost";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant;
};

const variantClassName: Record<ButtonVariant, string> = {
  primary: "bg-hell-600 text-white shadow-[0_18px_45px_rgba(228,63,90,0.24)] hover:bg-hell-700 disabled:bg-ink/20",
  secondary: "bg-white text-ink ring-1 ring-ink/10 hover:bg-cream disabled:text-ink/35",
  ghost: "bg-transparent text-ink-muted hover:bg-white/70 disabled:text-ink/30"
};

export function Button({ className, variant = "primary", ...props }: ButtonProps) {
  return (
    <button
      className={cn(
        "inline-flex min-h-11 cursor-pointer items-center justify-center rounded-full px-4 py-2 text-sm font-black transition focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-hell-600/15 disabled:cursor-not-allowed",
        variantClassName[variant],
        className
      )}
      {...props}
    />
  );
}

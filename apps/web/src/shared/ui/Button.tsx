import type { ButtonHTMLAttributes } from "react";
import { cn } from "../lib/cn";

type ButtonVariant = "primary" | "secondary" | "ghost";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant;
};

const variantClassName: Record<ButtonVariant, string> = {
  primary: "bg-gray-950 text-white shadow-soft hover:bg-gray-800 disabled:bg-gray-300",
  secondary: "bg-white text-gray-950 ring-1 ring-gray-200 hover:bg-gray-50 disabled:text-gray-400",
  ghost: "bg-transparent text-gray-600 hover:bg-white/70 disabled:text-gray-300"
};

export function Button({ className, variant = "primary", ...props }: ButtonProps) {
  return (
    <button
      className={cn(
        "inline-flex min-h-11 cursor-pointer items-center justify-center rounded-2xl px-4 py-2 text-sm font-semibold transition focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-gray-950/15 disabled:cursor-not-allowed",
        variantClassName[variant],
        className
      )}
      {...props}
    />
  );
}

import type { PropsWithChildren } from "react";
import { cn } from "../lib/cn";

type CardProps = PropsWithChildren<{
  className?: string;
}>;

export function Card({ children, className }: CardProps) {
  return <section className={cn("rounded-[32px] bg-white p-5 shadow-soft ring-1 ring-ink/[0.06]", className)}>{children}</section>;
}

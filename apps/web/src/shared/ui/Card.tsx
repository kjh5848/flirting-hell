import type { PropsWithChildren } from "react";
import { cn } from "../lib/cn";

type CardProps = PropsWithChildren<{
  className?: string;
}>;

export function Card({ children, className }: CardProps) {
  return <section className={cn("rounded-[28px] bg-white p-5 shadow-soft ring-1 ring-black/5", className)}>{children}</section>;
}

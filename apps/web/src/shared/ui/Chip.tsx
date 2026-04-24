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
        "min-h-11 cursor-pointer rounded-full px-4 py-2 text-sm font-bold transition ring-1 focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-gray-950/15",
        selected ? "bg-gray-950 text-white ring-gray-950" : "bg-white text-gray-700 ring-gray-200 hover:bg-gray-50",
        className
      )}
      {...props}
    />
  );
}

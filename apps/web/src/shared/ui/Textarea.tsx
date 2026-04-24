import type { TextareaHTMLAttributes } from "react";
import { cn } from "../lib/cn";

export function Textarea({ className, ...props }: TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return (
    <textarea
      className={cn(
        "min-h-52 w-full resize-y rounded-3xl border border-gray-200 bg-white px-4 py-4 text-base leading-7 outline-none transition placeholder:text-gray-400 focus:border-gray-950 focus:ring-4 focus:ring-gray-950/10",
        className
      )}
      {...props}
    />
  );
}

import type { TextareaHTMLAttributes } from "react";
import { cn } from "../lib/cn";

export function Textarea({ className, ...props }: TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return (
    <textarea
      className={cn(
        "min-h-44 w-full resize-y rounded-[26px] border border-ink/10 bg-white px-4 py-4 text-base leading-7 text-ink outline-none transition placeholder:text-ink-faint focus:border-hell-600 focus:ring-4 focus:ring-hell-600/15",
        className
      )}
      {...props}
    />
  );
}

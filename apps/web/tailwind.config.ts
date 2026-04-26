import type { Config } from "tailwindcss";

export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        cream: "#FFF8F4",
        blush: "#FFF1F2",
        ink: {
          DEFAULT: "#1D1719",
          soft: "#2A141B",
          muted: "#76666A",
          faint: "#9B8A8E"
        },
        hell: {
          50: "#fff1f2",
          100: "#ffe4e6",
          500: "#f43f5e",
          600: "#e43f5a",
          700: "#be123c",
          900: "#881337"
        },
        warm: {
          50: "#fff7ed",
          100: "#ffedd5",
          500: "#ff7a59"
        }
      },
      fontFamily: {
        sans: ['"Pretendard Variable"', '"Noto Sans KR"', "ui-sans-serif", "system-ui", "sans-serif"],
        mono: ['"JetBrains Mono"', "ui-monospace", "SFMono-Regular", "Menlo", "monospace"]
      },
      boxShadow: {
        soft: "0 24px 60px rgba(42, 20, 27, 0.08)",
        raised: "0 28px 72px rgba(42, 20, 27, 0.12)",
        pill: "0 18px 45px rgba(42, 20, 27, 0.07)"
      }
    }
  },
  plugins: []
} satisfies Config;

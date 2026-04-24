import type { Config } from "tailwindcss";

export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        hell: {
          50: "#fff1f2",
          500: "#f43f5e",
          600: "#e11d48",
          900: "#881337"
        }
      },
      boxShadow: {
        soft: "0 20px 60px rgba(15, 23, 42, 0.12)"
      }
    }
  },
  plugins: []
} satisfies Config;

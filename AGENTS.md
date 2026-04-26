# AGENTS.md

## Project Context

- Project name: `플러팅지옥` (`flirting-hell`).
- Product: AI dating/flirting coach for beginners.
- Core value: reduce anxiety and help users express interest respectfully; do not manipulate, pressure, or coerce the other person.
- MVP: mobile-first web/PWA where users paste KakaoTalk/DM/text messages, receive conversation analysis, tone-matched reply options, safety warnings, and next-action guidance.

## Communication

- Use Korean for user-facing explanations unless the user asks otherwise.
- Be direct and practical. Avoid vague encouragement or unnecessary verbosity.
- When dates matter, use exact dates.
- Before changing scope, explain the tradeoff and update the relevant doc.

## Repository Structure

```text
apps/web          React Vite frontend, Tailwind CSS
apps/api          Cloudflare Workers API, D1 binding, migrations
packages/shared   Shared TypeScript types/options/API contracts
docs/brand        Brand strategy and naming
docs/product      Product specs, screen flow, wireframes, phases
docs/technical    Architecture, API, DB, ERD, Cloudflare setup
docs/decisions    Architecture/product decision records
docs/references   Course/reference material summaries
```

## Planning Order

Follow this sequence before implementation when adding major product surface area:

```text
idea/problem
→ MVP scope
→ user flow/screen spec
→ AI response schema
→ monetization rule
→ web UI/wireframe
→ tech stack
→ system architecture
→ component architecture
→ API spec
→ ERD/DB model
→ phase/spec checklist
→ code
→ deploy
```

Relevant docs:

- `docs/product/pre-development-sequence.md`
- `docs/product/development-phases.md`
- `docs/product/phase-specs.md`
- `docs/product/wireframes.md`
- `docs/technical/mvp-architecture.md`
- `docs/technical/react-component-architecture.md`
- `docs/technical/api-spec.md`
- `docs/technical/erd.md`
- `docs/technical/data-model.md`

## Product Safety Rules

- Do not build features that encourage stalking, coercion, deception, sexual pressure, harassment, or bypassing consent.
- The app may suggest respectful flirting and clearer communication.
- The app must not tell users to force, trick, guilt-trip, or pressure someone.
- AI should not decide `meet this person` or `do not meet this person` as an absolute command. It should provide warnings, tradeoffs, and user-respecting guidance.
- The product position is `dating communication coach`, not `pickup/manipulation tool`.

## Privacy Rules

- Raw conversations are not stored long-term by default.
- Do not introduce `raw_conversations`, `messages`, `partners`, or `contacts` tables without explicit product/privacy review.
- Store only what the MVP needs: anonymous user ID, usage count, event logs, summarized profile, structured AI result.
- Show privacy guidance near message input: users should remove names, phone numbers, addresses, and other identifying information.

## Tech Stack

- Frontend: React, Vite, TypeScript, Tailwind CSS.
- API: Cloudflare Workers.
- DB: Cloudflare D1.
- Shared contracts: `packages/shared`.
- AI path: Workers → Cloudflare AI Gateway → external LLM API.
- Payment later: Polar checkout/webhook; not part of current MVP runtime.

## Commands

Run from repository root unless noted.

```bash
npm install
npm run typecheck
npm run build
npm run build:web
npm run build:api
npm run dev:web
npm run dev:api
```

Cloudflare API commands are run through the API workspace:

```bash
npm --workspace @flirting-hell/api exec -- wrangler login
npm --workspace @flirting-hell/api exec -- wrangler d1 create flirting-hell-db
npm --workspace @flirting-hell/api exec -- wrangler d1 migrations apply flirting-hell-db --remote
npm --workspace @flirting-hell/api exec -- wrangler deploy
```

## Cloudflare Pages Settings

For Pages Git integration:

```text
Root directory: /
Build command: npm run build
Build output directory: apps/web/dist
Framework preset: React (Vite) or Vite
```

Do not set the root directory to `apps/web` unless workspace/shared package behavior has been re-verified.

## Coding Rules

- Keep changes small and aligned with existing docs/specs.
- Before changing visual UI, read `DESIGN.md` and keep colors, typography, spacing, motion, and component hierarchy aligned with it.
- Use TypeScript types from `packages/shared` for frontend/API contracts.
- Do not duplicate enums across web/api if they belong in `packages/shared`.
- Components should follow the feature-slice structure documented in `docs/technical/react-component-architecture.md`.
- UI components in `shared/ui` must not know product-domain concepts.
- Feature code should not import private files from another feature; promote shared types/UI/utilities when needed.
- Do not add large state libraries until the need is proven. MVP uses React state/hooks first.

## Validation Rules

Before claiming a change is complete, run the most relevant checks:

```bash
npm run typecheck
npm run build
```

If API code changed, also run:

```bash
npm run build:api
```

If documentation only changed, at minimum inspect `git diff --stat` and verify links/paths are correct.

## Git Rules

- Do not commit or push unless the user explicitly asks.
- Commit messages should be short and action-oriented.
- Check `git status --short --branch` before and after committing.
- Do not rewrite history or reset without explicit user approval.

## Current Phase

Current state:

- Phase 0 documentation is mostly complete.
- Phase 1 web MVP scaffold exists with mock AI result.
- Next technical steps are Cloudflare Pages latest deployment check, D1 creation/migration, Workers API deploy, then actual AI integration.

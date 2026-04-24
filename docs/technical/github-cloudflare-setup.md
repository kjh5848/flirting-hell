# GitHub와 Cloudflare Pages 연결 절차

## 목표

플러팅지옥 프로젝트는 GitHub 저장소에 push한 뒤 Cloudflare Pages에서 GitHub repo를 연결해 자동 빌드한다.

## GitHub 저장소

추천 저장소:

- `kjh5848/flirting-hell`

현재 GitHub MCP에서는 새 저장소 생성 도구가 노출되어 있지 않다. 따라서 GitHub 웹에서 빈 저장소를 먼저 만든 뒤 로컬 repo를 연결한다.

## 로컬 연결 명령

GitHub에서 빈 저장소를 만든 뒤 아래 명령을 실행한다.

```bash
git remote add origin https://github.com/kjh5848/flirting-hell.git
git push -u origin main
```

이미 `origin`이 있으면 다음처럼 바꾼다.

```bash
git remote set-url origin https://github.com/kjh5848/flirting-hell.git
git push -u origin main
```

## Cloudflare Pages 연결

1. Cloudflare Dashboard에 접속한다.
2. Workers & Pages로 이동한다.
3. Create application을 선택한다.
4. Pages를 선택한다.
5. GitHub 저장소 `kjh5848/flirting-hell`을 연결한다.
6. 프론트엔드 앱 폴더와 빌드 명령은 실제 Vite 앱 생성 후 설정한다.

## 예상 빌드 설정

프론트엔드가 `apps/web`에 생성될 경우:

- Framework preset: `Vite`
- Build command: `npm run build`
- Build output directory: `dist`
- Root directory: `apps/web`

프로젝트 루트에 생성될 경우:

- Framework preset: `Vite`
- Build command: `npm run build`
- Build output directory: `dist`
- Root directory: `/`

## Workers 배포

API는 Cloudflare Workers로 별도 배포한다.

예상 폴더:

- `apps/api`

예상 명령:

```bash
npx wrangler deploy
```

## 다음 작업

- GitHub 빈 저장소 생성
- 로컬 repo에 `origin` 연결
- 첫 push
- Cloudflare Pages에서 repo 연결
- Vite React 앱 생성
- Workers API 생성

# Phase 3 상담방 Vertical Slice 구현 메모

## 목적

Phase 3의 첫 목표는 로그인한 사용자가 상대별 상담방을 만들고, 앱 재시작 후에도 서버에서 다시 조회할 수 있게 만드는 것이다.

상담방은 이후 `대화/상황 붙여넣기 → 분석 → 답장 저장` 흐름의 부모 리소스다.

## 이번 구현 범위

- `consultation_rooms` PostgreSQL migration
- Spring 상담방 domain/repository/service/controller
- `GET /api/rooms`
- `POST /api/rooms`
- `GET /api/rooms/{roomId}`
- `GET /api/me/bootstrap`의 `recentRooms` 실제 상담방 연결
- Flutter 상담방 목록 API 연결
- Flutter 새 상담방 생성 bottom sheet
- Flutter 상담방 상세 화면

## 저장 기준

상담방에는 원문 대화 전문을 저장하지 않는다.

저장하는 값:

- 상대 별칭
- 관계 단계
- 현재 고민
- 조심할 점
- 기본 전략
- 생성/수정 시각

## API 검증 기준

```bash
npm run test:backend
npm run build:backend
npm run analyze:mobile
```

실서버 검증은 Firebase Admin credential을 설정한 뒤 `local,firebase` 프로필로 실행한다.

```bash
export FLIRTING_HELL_FIREBASE_PROJECT_ID="flirting-hell"
export FLIRTING_HELL_FIREBASE_SERVICE_ACCOUNT_PATH="/absolute/path/to/service-account.json"
npm run dev:backend:firebase
```

앱은 실제 Firebase ID token 모드로 실행한다.

```bash
npm run dev:mobile:firebase
```

## 다음 작업

1. 상담방 수정 `PATCH /api/rooms/{roomId}`
2. 상담방 안에서 `대화/상황 붙여넣기` 초안 화면
3. `POST /api/rooms/{roomId}/analyses` mock 분석 저장
4. 분석 결과를 상담방 히스토리에 쌓기

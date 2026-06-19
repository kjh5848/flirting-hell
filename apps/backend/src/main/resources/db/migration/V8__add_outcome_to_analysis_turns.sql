-- 결과 피드백 루프: 추천 답장을 실제로 보냈고 어땠는지 기록(nullable).
-- 예: SENT_GOOD / SENT_SOSO / NOT_SENT. 백엔드는 의미를 해석하지 않는다.
alter table analysis_turns add column outcome text;

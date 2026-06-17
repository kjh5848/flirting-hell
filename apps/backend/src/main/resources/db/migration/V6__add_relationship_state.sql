-- 메모리 Phase A.5: 분석이 쌓여도 일정 크기로 유지되는 구조화 관계상태(JSON 문자열).
-- 분석마다 결정적 compaction으로 갱신한다. nullable(첫 분석 전에는 없음).
alter table consultation_rooms add column relationship_state text;

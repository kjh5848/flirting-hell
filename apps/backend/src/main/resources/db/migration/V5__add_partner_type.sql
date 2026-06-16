-- 분석 결과의 상대 5축 성향 추론을 JSON 문자열로 보관한다.
-- 백엔드는 의미를 해석하지 않고 클라이언트가 5축으로 파싱한다. nullable
-- (과거 분석/실제 LLM 미추론 시 null).
alter table analysis_turns add column partner_type text;

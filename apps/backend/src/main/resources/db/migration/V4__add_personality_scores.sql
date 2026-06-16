-- 연애 성향 5축 점수(self/ideal)를 보관한다.
-- 백엔드는 의미를 해석하지 않고 클라이언트가 보낸 JSON 문자열을 그대로 저장하므로
-- 별도 컬럼이 아니라 text 컬럼 2개로 둔다(프레임워크가 바뀌어도 스키마 불변). nullable.
alter table user_profiles add column personality_self text;
alter table user_profiles add column personality_ideal text;

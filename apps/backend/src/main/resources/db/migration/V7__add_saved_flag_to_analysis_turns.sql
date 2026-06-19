-- 사용자가 저장(북마크)한 답장 플래그. 저장 탭에서 상대별로 모아 본다.
alter table analysis_turns add column saved boolean not null default false;

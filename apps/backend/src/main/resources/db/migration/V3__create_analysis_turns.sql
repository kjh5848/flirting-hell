alter table consultation_rooms
  add column last_turn_summary text not null default '아직 분석 기록이 없어요',
  add column saved_reply_count integer not null default 0;

create table analysis_turns (
  id text primary key,
  room_id text not null references consultation_rooms (id),
  user_id text not null references app_users (id),
  source_type text not null,
  participant_summary text not null,
  summary text not null,
  current_state text not null,
  recommended_strategy_id text not null,
  warnings text not null,
  primary_reply text not null,
  alternative_replies text not null,
  reply_reason text not null,
  next_action text not null,
  created_at timestamptz not null
);

create index idx_analysis_turns_room_created on analysis_turns (room_id, created_at desc);
create index idx_analysis_turns_user_created on analysis_turns (user_id, created_at desc);

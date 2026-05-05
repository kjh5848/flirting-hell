create table consultation_rooms (
  id text primary key,
  user_id text not null references app_users (id),
  alias text not null,
  relationship_stage text not null,
  current_concern text,
  caution_notes text,
  preferred_strategy_id text,
  archived_at timestamptz,
  created_at timestamptz not null,
  updated_at timestamptz not null
);

create index idx_rooms_user_updated on consultation_rooms (user_id, updated_at desc);
create index idx_rooms_user_archived on consultation_rooms (user_id, archived_at);

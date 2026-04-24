create table if not exists anonymous_users (
  id text primary key,
  created_at text not null,
  last_seen_at text not null
);

create table if not exists usage_days (
  id text primary key,
  anonymous_user_id text not null,
  usage_date text not null,
  free_used_count integer not null default 0,
  credit_used_count integer not null default 0,
  created_at text not null,
  updated_at text not null,
  foreign key (anonymous_user_id) references anonymous_users(id)
);

create unique index if not exists idx_usage_days_user_date
on usage_days (anonymous_user_id, usage_date);

create table if not exists user_profiles (
  id text primary key,
  anonymous_user_id text not null,
  dating_styles_json text,
  preferred_partner_styles_json text,
  difficult_partner_styles_json text,
  attraction_reasons_json text,
  tone_profile_json text,
  created_at text not null,
  updated_at text not null,
  foreign key (anonymous_user_id) references anonymous_users(id)
);

create unique index if not exists idx_user_profiles_user
on user_profiles (anonymous_user_id);

create table if not exists analyses (
  id text primary key,
  anonymous_user_id text not null,
  relationship_stage text not null,
  conversation_goal text not null,
  reply_intensity text not null,
  guidance_mode text not null,
  tone_mode text not null,
  mood_status text,
  style_fit_status text,
  ai_result_json text,
  input_character_count integer not null,
  used_free_credit integer not null default 1,
  used_paid_credit integer not null default 0,
  created_at text not null,
  foreign key (anonymous_user_id) references anonymous_users(id)
);

create index if not exists idx_analyses_user_created
on analyses (anonymous_user_id, created_at);

create table if not exists events (
  id text primary key,
  anonymous_user_id text not null,
  analysis_id text,
  event_name text not null,
  metadata_json text,
  created_at text not null,
  foreign key (anonymous_user_id) references anonymous_users(id),
  foreign key (analysis_id) references analyses(id)
);

create index if not exists idx_events_user_created
on events (anonymous_user_id, created_at);

create index if not exists idx_events_name_created
on events (event_name, created_at);
